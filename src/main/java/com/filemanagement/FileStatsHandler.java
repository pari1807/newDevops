package com.filemanagement;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.File;
import java.util.*;

/**
 * Handles statistics/analytics requests
 */
public class FileStatsHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FileStatsHandler.class);
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
            JsonObject stats = getFileStatistics();
            sendResponse(exchange, 200, stats.toString());

        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Failed to get statistics: " + e.getMessage());
            sendResponse(exchange, 500, errorResponse.toString());
        }
    }

    /**
     * Calculate file statistics
     */
    private JsonObject getFileStatistics() {
        JsonObject stats = new JsonObject();
        File uploadDir = new File(UPLOAD_DIR);

        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            stats.addProperty("totalFiles", 0);
            stats.addProperty("totalSize", 0);
            stats.addProperty("totalSizeFormatted", "0 B");
            return stats;
        }

        File[] fileList = uploadDir.listFiles();
        if (fileList == null) {
            stats.addProperty("totalFiles", 0);
            stats.addProperty("totalSize", 0);
            stats.addProperty("totalSizeFormatted", "0 B");
            return stats;
        }

        long totalSize = 0;
        int totalFiles = 0;
        Map<String, Integer> typeCount = new HashMap<>();

        for (File file : fileList) {
            if (file.isFile() && !file.getName().startsWith(".")) {
                totalFiles++;
                totalSize += file.length();

                // Count by type
                String type = getFileType(file.getName());
                typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            }
        }

        stats.addProperty("totalFiles", totalFiles);
        stats.addProperty("totalSize", totalSize);
        stats.addProperty("totalSizeFormatted", formatBytes(totalSize));

        // Add file type distribution
        JsonObject typeDistribution = new JsonObject();
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            typeDistribution.addProperty(entry.getKey(), entry.getValue());
        }
        stats.add("typeDistribution", typeDistribution);

        stats.addProperty("success", true);

        return stats;
    }

    /**
     * Get file type category
     */
    private String getFileType(String filename) {
        String extension = filename.toLowerCase().substring(filename.lastIndexOf('.') + 1);

        if (isImageFile(extension))
            return "Image";
        if ("pdf".equals(extension))
            return "PDF";
        if ("zip".equals(extension) || "rar".equals(extension))
            return "Archive";
        if ("txt".equals(extension) || "csv".equals(extension))
            return "Document";
        if ("doc".equals(extension) || "docx".equals(extension))
            return "Word";
        if ("xls".equals(extension) || "xlsx".equals(extension))
            return "Spreadsheet";

        return "Other";
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
     * Format bytes to human readable format
     */
    private String formatBytes(long bytes) {
        if (bytes == 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
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
