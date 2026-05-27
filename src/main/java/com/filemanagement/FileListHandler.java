package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.util.*;

/**
 * Handles file list requests - returns JSON array of uploaded files
 */
public class FileListHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileListHandler.class);
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try {
            // Get query parameters
            String query = exchange.getRequestURI().getQuery();
            String sortBy = "date"; // Default sort by upload date
            String order = "desc"; // Default descending order

            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if ("sort".equals(keyValue[0])) {
                            sortBy = keyValue[1];
                        } else if ("order".equals(keyValue[0])) {
                            order = keyValue[1];
                        }
                    }
                }
            }

            List<Map<String, Object>> files = getUploadedFiles(sortBy, "asc".equals(order));

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("files", new Gson().toJsonTree(files));
            response.addProperty("count", files.size());

            sendResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            logger.error("Error listing files", e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Failed to list files: " + e.getMessage());
            sendResponse(exchange, 500, errorResponse.toString());
        }
    }

    /**
     * Get list of uploaded files with metadata
     */
    private List<Map<String, Object>> getUploadedFiles(String sortBy, boolean ascending) {
        List<Map<String, Object>> files = new ArrayList<>();
        File uploadDir = new File(UPLOAD_DIR);

        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            return files;
        }

        File[] fileList = uploadDir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile() && !file.getName().startsWith(".")) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("filename", file.getName());
                    fileInfo.put("size", file.length());
                    fileInfo.put("uploadDate", file.lastModified());
                    fileInfo.put("type", getMimeType(file.getName()));
                    fileInfo.put("icon", getIconForFile(file.getName()));
                    files.add(fileInfo);
                }
            }
        }

        // Sort files
        sortFiles(files, sortBy, ascending);

        return files;
    }

    /**
     * Sort files based on criteria
     */
    private void sortFiles(List<Map<String, Object>> files, String sortBy, boolean ascending) {
        Comparator<Map<String, Object>> comparator = null;

        if ("name".equals(sortBy)) {
            comparator = Comparator.comparing(f -> (String) f.get("filename"));
        } else if ("size".equals(sortBy)) {
            comparator = Comparator.comparing(f -> (Long) f.get("size"));
        } else { // date
            comparator = Comparator.comparing(f -> (Long) f.get("uploadDate"));
        }

        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            files.sort(comparator);
        }
    }

    /**
     * Get MIME type based on file extension
     */
    private String getMimeType(String filename) {
        String extension = filename.toLowerCase().substring(filename.lastIndexOf('.') + 1);

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Get icon name based on file type
     */
    private String getIconForFile(String filename) {
        String extension = filename.toLowerCase().substring(filename.lastIndexOf('.') + 1);

        if (isImageFile(extension))
            return "image";
        if ("pdf".equals(extension))
            return "pdf";
        if ("zip".equals(extension) || "rar".equals(extension))
            return "archive";
        if ("txt".equals(extension) || "csv".equals(extension))
            return "document";
        if ("doc".equals(extension) || "docx".equals(extension))
            return "word";
        if ("xls".equals(extension) || "xlsx".equals(extension))
            return "spreadsheet";

        return "file";
    }

    /**
     * Check if file is an image
     */
    private boolean isImageFile(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("gif") ||
                extension.equals("webp");
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
}
