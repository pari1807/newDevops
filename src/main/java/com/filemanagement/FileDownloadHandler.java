package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Handles file download requests
 */
public class FileDownloadHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadHandler.class);
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            // Extract filename from URL path
            String path = exchange.getRequestURI().getPath();
            String filename = path.substring("/api/download/".length());

            if (filename == null || filename.isEmpty()) {
                sendErrorResponse(exchange, 400, "Filename is required");
                return;
            }

            // Decode filename (URL encoded)
            filename = java.net.URLDecoder.decode(filename, "UTF-8");

            // Prevent directory traversal attacks
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                sendErrorResponse(exchange, 400, "Invalid filename");
                return;
            }

            String filePath = UPLOAD_DIR + "/" + filename;
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                sendErrorResponse(exchange, 404, "File not found");
                return;
            }

            // Set response headers
            exchange.getResponseHeaders().add("Content-Type", getMimeType(filename));

            // Use 'inline' for PDFs/images to view in browser, 'attachment' for others to
            // download
            String mimeType = getMimeType(filename);
            if (mimeType.startsWith("image/") || mimeType.equals("application/pdf")) {
                exchange.getResponseHeaders().add("Content-Disposition", "inline; filename=\"" + filename + "\"");
            } else {
                exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            }
            exchange.getResponseHeaders().add("Content-Length", String.valueOf(file.length()));

            // Send file
            exchange.sendResponseHeaders(200, file.length());
            OutputStream os = exchange.getResponseBody();

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            fis.close();
            os.close();

            logger.info("File downloaded: {}", filename);

        } catch (Exception e) {
            logger.error("Error during file download", e);
            try {
                sendErrorResponse(exchange, 500, "Download failed: " + e.getMessage());
            } catch (IOException ex) {
                logger.error("Failed to send error response", ex);
            }
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
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
