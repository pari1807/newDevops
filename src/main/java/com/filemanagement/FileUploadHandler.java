package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

/**
 * Handles file upload requests via multipart/form-data
 */
public class FileUploadHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadHandler.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final String HASH_MAP_FILE = ".smart-file-manager/hashmap.json";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "text/plain", "text/csv",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip", "application/x-rar-compressed"));

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try {
            // Create uploads directory if it doesn't exist
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid content type\"}");
                return;
            }

            // Extract boundary
            String boundary = contentType.split("boundary=")[1];

            // Read request body
            InputStream inputStream = exchange.getRequestBody();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] requestBody = buffer.toByteArray();

            // Parse multipart data
            List<FileUploadData> files = parseMultipartData(requestBody, boundary);

            if (files.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"No files provided\"}");
                return;
            }

            // Save files
            JsonObject response = new JsonObject();
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (FileUploadData fileData : files) {
                // Validate file size
                if (fileData.data.length > MAX_FILE_SIZE) {
                    logger.warn("File too large: {} ({}bytes)", fileData.filename, fileData.data.length);
                    continue;
                }

                // Validate file type
                if (!ALLOWED_TYPES.contains(fileData.contentType) && !fileData.contentType.startsWith("image/")) {
                    logger.warn("Invalid file type: {}", fileData.contentType);
                    continue;
                }

                // Compute hash of file to detect duplicates
                String sha256 = computeSHA256(fileData.data);

                // Load hashmap of known hashes
                Map<String, String> hashMap = loadHashMap();

                String existing = hashMap.get(sha256);
                String fileName;
                if (existing != null && new File(UPLOAD_DIR + "/" + existing).exists()) {
                    // File already uploaded previously
                    fileName = existing;
                    logger.info("Duplicate file upload detected, reusing: {}", fileName);
                } else {
                    // Generate unique filename and save
                    fileName = generateUniqueFilename(fileData.filename);
                    String filePath = UPLOAD_DIR + "/" + fileName;
                    Files.write(Paths.get(filePath), fileData.data);

                    // Update hashmap
                    hashMap.put(sha256, fileName);
                    saveHashMap(hashMap);
                }

                // Add to response
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("filename", fileName);
                fileInfo.put("originalName", fileData.filename);
                fileInfo.put("size", fileData.data.length);
                fileInfo.put("uploadDate", System.currentTimeMillis());
                fileInfo.put("type", fileData.contentType);
                uploadedFiles.add(fileInfo);

                logger.info("File uploaded successfully: {} ({} bytes)", fileName, fileData.data.length);
            }

            response.addProperty("success", true);
            response.addProperty("message", "Files uploaded successfully");
            response.add("files", new Gson().toJsonTree(uploadedFiles));

            sendResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            logger.error("Error during file upload", e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Upload failed: " + e.getMessage());
            sendResponse(exchange, 500, errorResponse.toString());
        }
    }

    /**
     * Generate unique filename to avoid conflicts
     */
    private String generateUniqueFilename(String originalFilename) {
        String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));

        String filename = originalFilename;
        int counter = 1;

        while (new File(UPLOAD_DIR + "/" + filename).exists()) {
            filename = baseName + "_" + counter + extension;
            counter++;
        }

        return filename;
    }

    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Parse multipart form data (binary-safe)
     */
    private List<FileUploadData> parseMultipartData(byte[] data, String boundary) throws IOException {
        List<FileUploadData> files = new ArrayList<>();
        String boundaryStr = "--" + boundary;
        byte[] boundaryBytes = boundaryStr.getBytes("ISO-8859-1");

        int pos = 0;
        while (pos < data.length) {
            int boundaryPos = indexOf(data, boundaryBytes, pos);
            if (boundaryPos == -1)
                break;

            pos = boundaryPos + boundaryBytes.length;

            // Skip optional CRLF
            if (pos < data.length && data[pos] == '\r')
                pos++;
            if (pos < data.length && data[pos] == '\n')
                pos++;

            // Check for end boundary
            if (pos + 1 < data.length && data[pos] == '-' && data[pos + 1] == '-') {
                break;
            }

            // Find header end
            int headerEnd = indexOf(data, "\r\n\r\n".getBytes("ISO-8859-1"), pos);
            if (headerEnd == -1) {
                headerEnd = indexOf(data, "\n\n".getBytes("ISO-8859-1"), pos);
                if (headerEnd == -1)
                    break;
                headerEnd += 2;
            } else {
                headerEnd += 4;
            }

            String headerStr = new String(data, pos, headerEnd - pos, "ISO-8859-1");
            String filename = extractValue(headerStr, "filename=\"", "\"");
            String contentType = extractValue(headerStr, "Content-Type: ", "\r\n");
            if (contentType == null)
                contentType = "application/octet-stream";

            if (filename == null || filename.isEmpty()) {
                pos = headerEnd;
                continue;
            }

            int contentStart = headerEnd;
            int contentEnd = indexOf(data, boundaryBytes, contentStart);
            if (contentEnd == -1)
                contentEnd = data.length;

            // Trim trailing CRLF
            while (contentEnd > contentStart && (data[contentEnd - 1] == '\n' || data[contentEnd - 1] == '\r'))
                contentEnd--;

            byte[] fileData = Arrays.copyOfRange(data, contentStart, contentEnd);
            files.add(new FileUploadData(filename, contentType.trim(), fileData));

            pos = contentEnd;
        }

        return files;
    }

    private int indexOf(byte[] data, byte[] pattern, int start) {
        outer: for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j])
                    continue outer;
            }
            return i;
        }
        return -1;
    }

    private String extractValue(String text, String startDelim, String endDelim) {
        int start = text.indexOf(startDelim);
        if (start == -1)
            return null;
        start += startDelim.length();
        int end = text.indexOf(endDelim, start);
        if (end == -1)
            end = text.length();
        return text.substring(start, end);
    }

    private String computeSHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            logger.warn("Failed to compute hash", e);
            return null;
        }
    }

    private synchronized Map<String, String> loadHashMap() {
        Map<String, String> map = new HashMap<>();
        try {
            File hm = new File(HASH_MAP_FILE);
            File legacyHm = new File(UPLOAD_DIR + "/.hashmap.json");

            if (!hm.exists() && legacyHm.exists()) {
                hm = legacyHm;
            }

            if (!hm.exists())
                return map;

            String content = new String(Files.readAllBytes(hm.toPath()), StandardCharsets.UTF_8);
            if (content.trim().isEmpty())
                return map;

            Map<?, ?> raw = new Gson().fromJson(content, Map.class);
            for (Map.Entry<?, ?> e : raw.entrySet()) {
                map.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
            }

            if (legacyHm.equals(hm)) {
                saveHashMap(map);
                Files.deleteIfExists(legacyHm.toPath());
            }
        } catch (Exception e) {
            logger.warn("Failed to load hash map", e);
        }
        return map;
    }

    private synchronized void saveHashMap(Map<String, String> map) {
        try {
            File hm = new File(HASH_MAP_FILE);
            File parent = hm.getParentFile();
            if (parent != null) {
                Files.createDirectories(parent.toPath());
            }

            String json = new Gson().toJson(map);
            Files.write(hm.toPath(), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            logger.warn("Failed to save hash map", e);
        }
    }

    /**
     * Inner class to hold file upload data
     */
    private static class FileUploadData {
        String filename;
        String contentType;
        byte[] data;

        FileUploadData(String filename, String contentType, byte[] data) {
            this.filename = filename;
            this.contentType = contentType;
            this.data = data;
        }
    }
}
