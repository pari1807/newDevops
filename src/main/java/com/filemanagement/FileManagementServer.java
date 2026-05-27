package com.filemanagement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * Smart File Management System - Main Server
 * 
 * This is the main entry point for the file management server.
 * It sets up an HTTP server on port 8080 with the following endpoints:
 * - POST /api/upload - Upload files
 * - GET /api/files - List uploaded files
 * - DELETE /api/delete/{filename} - Delete files
 * - GET /api/download/{filename} - Download files
 * - GET / - Serve frontend files
 */
public class FileManagementServer {
    private static final Logger logger = LoggerFactory.getLogger(FileManagementServer.class);
    private static final int PORT = 9090;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        try {
            // Create HTTP server on port 9090
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Set up thread pool
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            server.setExecutor(threadPool);

            // Register API endpoints
            server.createContext("/api/upload", new FileUploadHandler());
            server.createContext("/api/files", new FileListHandler());
            server.createContext("/api/delete/", new FileDeleteHandler());
            server.createContext("/api/download/", new FileDownloadHandler());
            server.createContext("/api/stats", new FileStatsHandler());

            // Serve static files (frontend)
            server.createContext("/", new StaticFileHandler());

            // Start server
            server.start();
            logger.info("==================================================");
            logger.info("Smart File Management System Server Started");
            logger.info("==================================================");
            logger.info("Server running on: http://localhost:{}", PORT);
            logger.info("Access the application at: http://localhost:{}", PORT);
            logger.info("==================================================");

        } catch (IOException e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }
}
