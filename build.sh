#!/bin/bash

# Smart File Management System - Build and Run Script
# This script builds the project and starts the server

set -e  # Exit on error

echo "======================================================"
echo "Smart File Management System - Build & Run"
echo "======================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check for Java
echo -e "${YELLOW}Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java is not installed. Please install Java 11 or higher.${NC}"
    exit 1
fi

java_version=$(java -version 2>&1 | grep -oP 'version "\K[0-9]+' | head -1)
if [ "$java_version" -lt 11 ]; then
    echo -e "${RED}Java version must be 11 or higher. Found version $java_version${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java version: $(java -version 2>&1 | head -1)${NC}"

# Check for Maven
echo ""
echo -e "${YELLOW}Checking Maven installation...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven is not installed. Please install Maven.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven version: $(mvn -v | head -1)${NC}"

# Create uploads directory
echo ""
echo -e "${YELLOW}Creating uploads directory...${NC}"
if [ ! -d "uploads" ]; then
    mkdir -p uploads
    echo -e "${GREEN}✓ Created uploads directory${NC}"
else
    echo -e "${GREEN}✓ uploads directory already exists${NC}"
fi

# Clean previous builds
echo ""
echo -e "${YELLOW}Cleaning previous builds...${NC}"
mvn clean -q
echo -e "${GREEN}✓ Clean complete${NC}"

# Build project
echo ""
echo -e "${YELLOW}Building project with Maven...${NC}"
mvn package -q
echo -e "${GREEN}✓ Build complete${NC}"

# Check if JAR was created
if [ ! -f "target/smart-file-management.jar" ]; then
    echo -e "${RED}Build failed! JAR not found.${NC}"
    exit 1
fi

# Start server
echo ""
echo -e "${YELLOW}Starting Smart File Management Server...${NC}"
echo ""

java -jar target/smart-file-management.jar

