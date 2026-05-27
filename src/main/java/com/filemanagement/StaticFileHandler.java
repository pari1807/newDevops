package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Serves static files (HTML, CSS, JavaScript) from the frontend directory
 */
public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private static final String FRONTEND_DIR = "frontend";

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
            String path = exchange.getRequestURI().getPath();

            // Default to index.html if root is requested
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // Prevent directory traversal
            if (path.contains("..")) {
                sendErrorResponse(exchange, 400, "Invalid path");
                return;
            }

            String filePath = FRONTEND_DIR + "/" + path;
            File file = new File(filePath);

            // Check if file exists
            if (!file.exists() || !file.isFile()) {
                // Try to serve index.html for SPA routing
                if (path.equals("index.html") || !path.contains(".")) {
                    filePath = FRONTEND_DIR + "/index.html";
                    file = new File(filePath);
                    if (!file.exists()) {
                        sendErrorResponse(exchange, 404, "Not found");
                        return;
                    }
                } else {
                    sendErrorResponse(exchange, 404, "Not found");
                    return;
                }
            }

            // Set content type
            String contentType = getContentType(filePath);
            exchange.getResponseHeaders().add("Content-Type", contentType);

            // Add caching headers for static files
            exchange.getResponseHeaders().add("Cache-Control", "public, max-age=3600");

            // Send file
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();

            logger.debug("Served static file: {}", filePath);

        } catch (Exception e) {
            logger.error("Error serving static file", e);
            try {
                sendErrorResponse(exchange, 500, "Internal server error");
            } catch (IOException ex) {
                logger.error("Failed to send error response", ex);
            }
        }
    }

    /**
     * Determine content type based on file extension
     */
    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else if (filePath.endsWith(".json")) {
            return "application/json";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filePath.endsWith(".webp")) {
            return "image/webp";
        } else if (filePath.endsWith(".ico")) {
            return "image/x-icon";
        } else if (filePath.endsWith(".woff")) {
            return "font/woff";
        } else if (filePath.endsWith(".woff2")) {
            return "font/woff2";
        } else if (filePath.endsWith(".ttf")) {
            return "font/ttf";
        } else if (filePath.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        }
        return "application/octet-stream";
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
