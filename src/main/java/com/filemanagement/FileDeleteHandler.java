package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Handles file deletion requests
 */
public class FileDeleteHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileDeleteHandler.class);
    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"DELETE".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try {
            // Extract filename from URL path
            String path = exchange.getRequestURI().getPath();
            String filename = path.substring("/api/delete/".length());

            if (filename == null || filename.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Filename is required\"}");
                return;
            }

            // Decode filename (URL encoded)
            filename = java.net.URLDecoder.decode(filename, "UTF-8");

            // Prevent directory traversal attacks
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid filename\"}");
                return;
            }

            String filePath = UPLOAD_DIR + "/" + filename;
            File file = new File(filePath);

            if (!file.exists()) {
                sendResponse(exchange, 404, "{\"error\": \"File not found\"}");
                return;
            }

            // Delete the file
            boolean deleted = file.delete();

            if (deleted) {
                JsonObject response = new JsonObject();
                response.addProperty("success", true);
                response.addProperty("message", "File deleted successfully");
                response.addProperty("filename", filename);
                sendResponse(exchange, 200, response.toString());
                logger.info("File deleted: {}", filename);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to delete file\"}");
                logger.error("Failed to delete file: {}", filename);
            }

        } catch (Exception e) {
            logger.error("Error during file deletion", e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Deletion failed: " + e.getMessage());
            sendResponse(exchange, 500, errorResponse.toString());
        }
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
