# Setup Guide - Smart File Management System

Complete step-by-step guide to set up and run the Smart File Management System.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Building the Project](#building-the-project)
- [Running the Application](#running-the-application)
- [Docker Setup](#docker-setup)
- [Database Setup](#database-setup)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **OS:** Linux, macOS, or Windows
- **RAM:** Minimum 2GB, Recommended 4GB+
- **Disk Space:** Minimum 1GB free

### Required Software

#### Java Development Kit (JDK)

**Windows, macOS, Linux:**

```bash
# Download and install Java 25 LTS
# https://adoptopenjdk.net/
# Verify installation
java -version
# Output: openjdk version "25.x.x" ...
```

#### Maven

**Windows:**

1. Download from https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH environment variable
4. Verify: `mvn -v`

**macOS (using Homebrew):**

```bash
brew install maven

# Verify
mvn -v
```

**Linux (Ubuntu/Debian):**

```bash
sudo apt-get update
sudo apt-get install maven

# Verify
mvn -v
```

#### Git

```bash
# Install Git
# https://git-scm.com/downloads

# Verify
git --version
```

#### Docker (Optional, for containerization)

```bash
# Download and install Docker Desktop
# https://www.docker.com/products/docker-desktop

# Verify
docker --version
docker run hello-world
```

## Local Development Setup

### Step 1: Clone Repository

```bash
# Navigate to desired directory
cd ~/projects

# Clone repository
git clone https://github.com/yourusername/smart-file-management.git

# Navigate to project
cd smart-file-management
```

### Step 2: Configure Java Path

**Windows (Command Prompt):**

```cmd
# Set JAVA_HOME
setx JAVA_HOME "C:\Program Files\Java\jdk-11"

# Verify
echo %JAVA_HOME%
java -version
```

**macOS/Linux:**

```bash
# Add to ~/.bash_profile or ~/.zshrc
export JAVA_HOME=$(/usr/libexec/java_home)
export PATH=$JAVA_HOME/bin:$PATH

# Apply changes
source ~/.bash_profile  # or ~/.zshrc
java -version
```

### Step 3: Create Uploads Directory

```bash
# Create uploads folder
mkdir -p uploads

# Set permissions (Linux/macOS)
chmod 755 uploads
```

### Step 4: Configure IDE (Optional)

**IntelliJ IDEA:**

1. File → Open → Select project root
2. Mark `src/main/java` as Sources Root
3. Mark `frontend` as Web Resources Root
4. Configure JDK 11 in Project Settings

**Eclipse:**

1. File → Import → Existing Maven Projects
2. Select project root
3. Eclipse automatically configures Maven

**VS Code:**

1. Install Extension Pack for Java
2. Install Maven for Java
3. Open project folder

## Building the Project

### Full Build

```bash
# Clean and build
mvn clean package

# Output: target/smart-file-management.jar
```

### Build with Skip Tests

```bash
mvn clean package -DskipTests
```

### Build Only (No Packaging)

```bash
mvn clean compile
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FileUploadHandlerTest

# Skip tests during build
mvn clean package -DskipTests
```

### Create Executable JAR

```bash
# Already handled in pom.xml
mvn clean package

# JAR location: target/smart-file-management.jar
```

## Running the Application

### Method 1: Run JAR Directly

```bash
# Navigate to project root
cd smart-file-management

# Run JAR
java -jar target/smart-file-management.jar

# Expected output:
# ==================================================
# Smart File Management System Server Started
# ==================================================
# Server running on: http://localhost:8080
```

### Method 2: Run with Maven

```bash
mvn exec:java -Dexec.mainClass="com.filemanagement.FileManagementServer"
```

### Method 3: Run with Custom JVM Options

```bash
java -Xmx512m -Xms256m -jar target/smart-file-management.jar
```

### Method 4: Background Execution (Linux/macOS)

```bash
nohup java -jar target/smart-file-management.jar > app.log 2>&1 &
```

### Method 5: Background Execution (Windows)

```cmd
javaw -jar target\smart-file-management.jar
```

### Access the Application

1. Open web browser
2. Navigate to: `http://localhost:8080`
3. You should see the login page

**Default Test Credentials (for development):**

- Email: `test@example.com`
- Password: `password123` (minimum 8 characters)

## Docker Setup

### Build Docker Image

```bash
# Navigate to project root
cd smart-file-management

# Build image
docker build -t smart-file-manager:latest .

# Verify image
docker images | grep smart-file-manager
```

### Run Docker Container

**Basic Run:**

```bash
docker run -d \
  --name smart-file-manager \
  -p 8080:8080 \
  smart-file-manager:latest
```

**With Volume Mounting (persistent uploads):**

```bash
docker run -d \
  --name smart-file-manager \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  smart-file-manager:latest
```

**With Memory Limits:**

```bash
docker run -d \
  --name smart-file-manager \
  -p 8080:8080 \
  -m 512m \
  --memory-swap 1g \
  -v $(pwd)/uploads:/app/uploads \
  smart-file-manager:latest
```

### Docker Compose Setup

**Create `docker-compose.yml`:**

```yaml
version: "3.8"

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: smart-file-manager
    ports:
      - "8080:8080"
    volumes:
      - ./uploads:/app/uploads
      - ./logs:/app/logs
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
    restart: unless-stopped
    healthcheck:
      test:
        [
          "CMD",
          "wget",
          "--no-verbose",
          "--tries=1",
          "--spider",
          "http://localhost:8080",
        ]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 40s
```

**Run with Docker Compose:**

```bash
# Start
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Docker Useful Commands

```bash
# View running containers
docker ps

# View all containers
docker ps -a

# View logs
docker logs smart-file-manager
docker logs -f smart-file-manager  # Follow logs

# Stop container
docker stop smart-file-manager

# Start container
docker start smart-file-manager

# Remove container
docker rm smart-file-manager

# Remove image
docker rmi smart-file-manager:latest

# Shell into container
docker exec -it smart-file-manager sh

# Inspect container
docker inspect smart-file-manager
```

## Database Setup

### Currently: File-based Storage

- Files stored in `uploads/` directory
- File metadata stored in memory (lost on restart)

### For Future: Database Integration

**PostgreSQL Setup (Future):**

```bash
# Install PostgreSQL
# macOS: brew install postgresql
# Ubuntu: sudo apt-get install postgresql

# Create database
createdb file_manager

# Create user
createuser -P fm_user

# Update connection in application
```

**MySQL Setup (Future):**

```bash
# Install MySQL
# macOS: brew install mysql
# Ubuntu: sudo apt-get install mysql-server

# Create database and user
mysql -u root -p
CREATE DATABASE file_manager;
CREATE USER 'fm_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON file_manager.* TO 'fm_user'@'localhost';
```

## Troubleshooting

### Issue: Port 8080 Already in Use

**Windows:**

```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**macOS/Linux:**

```bash
lsof -i :8080
kill -9 <PID>
```

**Solution: Use Different Port:**

```bash
# Modify FileManagementServer.java
private static final int PORT = 9090; // Change from 8080
```

### Issue: Maven Build Fails

```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install

# With verbose output
mvn clean install -X
```

### Issue: Java Version Mismatch

```bash
# Check Java version
java -version

# Set correct JAVA_HOME
# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 11)

# Linux
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# Verify
echo $JAVA_HOME
java -version
```

### Issue: Docker Build Fails

```bash
# Clear Docker cache
docker system prune -a

# Rebuild without cache
docker build --no-cache -t smart-file-manager:latest .

# Check build logs
docker build --progress=plain -t smart-file-manager:latest .
```

### Issue: File Upload Not Working

**Check Permissions:**

```bash
# Give write permission to uploads
chmod -R 777 uploads

# On Windows, check folder properties
# Security → Edit → Full Control
```

**Check Disk Space:**

```bash
# Linux/macOS
df -h

# Windows
wmic logicaldisk get name, size, freespace
```

### Issue: Application Crashes

```bash
# Increase heap memory
java -Xmx1024m -Xms512m -jar target/smart-file-management.jar

# Check logs
cat app.log

# Enable debug logging
java -Ddebug=true -jar target/smart-file-management.jar
```

### Issue: Cannot Connect to Server

1. Check if server is running:

   ```bash
   curl http://localhost:8080
   ```

2. Check firewall settings:
   - Windows: Add port 8080 to Windows Defender
   - macOS: System Preferences → Security & Privacy

3. Check port binding:
   ```bash
   netstat -tulpn | grep 8080  # Linux
   lsof -i :8080               # macOS
   netstat -ano | findstr 8080 # Windows
   ```

## Performance Tuning

### Optimize Java Heap

```bash
# For 4GB RAM server
java -Xmx2048m -Xms1024m -XX:+UseG1GC -jar app.jar

# For 8GB RAM server
java -Xmx4096m -Xms2048m -XX:+UseG1GC -jar app.jar
```

### Enable Compression

```bash
java -Djava.util.logging.config.file=logging.properties \
     -jar target/smart-file-management.jar
```

### Thread Pool Optimization

Modify `FileManagementServer.java`:

```java
private static final int THREAD_POOL_SIZE = 20; // Increase for more concurrent connections
```

## Security Configuration

### HTTPS Setup (Future)

```bash
# Generate self-signed certificate
keytool -genkey -alias tomcat -keyalg RSA -keystore keystore.jks

# Use in application
System.setProperty("javax.net.ssl.keyStore", "keystore.jks");
System.setProperty("javax.net.ssl.keyStorePassword", "password");
```

### CORS Configuration

Currently allows all origins. For production, modify `StaticFileHandler.java`:

```java
exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "https://yourdomain.com");
```

## Next Steps

1. **Read Documentation:** See README.md for full features
2. **Explore Code:** Check backend handlers and frontend code
3. **Run Examples:** Test upload, download, delete features
4. **Deploy:** Follow deployment guide for production
5. **Customize:** Modify for your specific needs

## Support & Resources

- **Java Documentation:** https://docs.oracle.com/en/java/
- **Maven Guide:** https://maven.apache.org/guides/
- **Docker Docs:** https://docs.docker.com/
- **GitHub Actions:** https://docs.github.com/en/actions/

---

**Last Updated:** 2024
**Version:** 1.0.0
