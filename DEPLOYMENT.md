# Deployment Guide

Complete guide for deploying Smart File Management System to production environments.

## Deployment Targets

- [Heroku](#heroku-deployment)
- [AWS](#aws-deployment)
- [Docker](#docker-deployment)
- [Linux Server](#linux-server-deployment)
- [Windows Server](#windows-server-deployment)

## Pre-Deployment Checklist

- [ ] All tests passing
- [ ] Code reviewed and merged
- [ ] Security scan completed
- [ ] Performance tested
- [ ] Documentation updated
- [ ] Environment variables configured
- [ ] Database backups ready
- [ ] SSL certificates ready (if using HTTPS)
- [ ] Firewall rules configured
- [ ] Monitoring setup

## Heroku Deployment

### 1. Prerequisites

```bash
# Install Heroku CLI
# macOS
brew tap heroku/brew && brew install heroku

# Linux
curl https://cli-assets.heroku.com/install.sh | sh

# Windows
# Download from https://devcenter.heroku.com/articles/heroku-cli
```

### 2. Setup Heroku

```bash
# Login to Heroku
heroku login

# Create Heroku app
heroku create smart-file-manager

# Check remote
git remote -v
```

### 3. Create Procfile

```bash
# Create file in project root
echo "web: java -Xmx512m -Xms256m -jar target/smart-file-management.jar" > Procfile
```

### 4. Deploy

```bash
# Commit changes
git add Procfile
git commit -m "Add Procfile for Heroku"

# Push to Heroku
git push heroku main

# View logs
heroku logs --tail

# Open app
heroku open
```

### 5. Scale Dynos

```bash
# Scale up if needed
heroku ps:scale web=2
```

## AWS Deployment

### Option 1: Elastic Beanstalk

```bash
# Install AWS CLI and EB CLI
pip install awsebcli

# Initialize application
eb init -p java-11-corretto smart-file-manager

# Create environment
eb create smart-file-manager-env

# Deploy
eb deploy

# View logs
eb logs

# SSH into instance
eb ssh
```

### Option 2: ECS (Elastic Container Service)

```bash
# Create ECR repository
aws ecr create-repository --repository-name smart-file-manager

# Build and push Docker image
docker build -t smart-file-manager:latest .
docker tag smart-file-manager:latest \
  <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/smart-file-manager:latest
docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/smart-file-manager:latest

# Create ECS cluster and task definition
# (Use AWS Console or CLI)
```

### Option 3: RDS + EC2

```bash
# Create RDS database (if needed)
aws rds create-db-instance \
  --db-instance-identifier fm-db \
  --db-instance-class db.t3.micro \
  --engine postgres

# Launch EC2 instance
aws ec2 run-instances --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.micro

# SSH and deploy
ssh -i keypair.pem ec2-user@<instance-ip>
```

## Docker Deployment

### 1. Build Image

```bash
docker build -t smart-file-manager:1.0.0 .
```

### 2. Push to Registry

**Docker Hub:**

```bash
# Login
docker login

# Tag image
docker tag smart-file-manager:1.0.0 username/smart-file-manager:1.0.0

# Push
docker push username/smart-file-manager:1.0.0
```

**AWS ECR:**

```bash
# Login
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin \
  <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag smart-file-manager:1.0.0 \
  <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/smart-file-manager:1.0.0

# Push
docker push <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/smart-file-manager:1.0.0
```

### 3. Run Container

```bash
docker run -d \
  --name smart-file-manager-prod \
  -p 80:8080 \
  -e JAVA_OPTS="-Xmx1024m -Xms512m" \
  -v /data/uploads:/app/uploads \
  -v /data/logs:/app/logs \
  --restart unless-stopped \
  smart-file-manager:1.0.0
```

### 4. Docker Compose Production

```yaml
version: "3.8"

services:
  app:
    image: smart-file-manager:1.0.0
    container_name: smart-file-manager-prod
    ports:
      - "80:8080"
    volumes:
      - /data/uploads:/app/uploads
      - /data/logs:/app/logs
    environment:
      - JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC
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
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## Linux Server Deployment

### 1. Server Setup (Ubuntu 20.04)

```bash
# Update system
sudo apt-get update
sudo apt-get upgrade -y

# Install Java
sudo apt-get install -y openjdk-11-jre-headless

# Create application user
sudo useradd -m -s /bin/false smartfm

# Create application directory
sudo mkdir -p /opt/smartfm
sudo chown smartfm:smartfm /opt/smartfm
```

### 2. Deploy Application

```bash
# Copy JAR file
sudo cp target/smart-file-management.jar /opt/smartfm/

# Create uploads directory
sudo mkdir -p /data/uploads
sudo chown smartfm:smartfm /data/uploads

# Create logs directory
sudo mkdir -p /var/log/smartfm
sudo chown smartfm:smartfm /var/log/smartfm
```

### 3. Create Systemd Service

```bash
# Create service file
sudo tee /etc/systemd/system/smartfm.service > /dev/null <<EOF
[Unit]
Description=Smart File Manager
After=network.target

[Service]
Type=simple
User=smartfm
WorkingDirectory=/opt/smartfm
Environment="JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64"
Environment="JAVA_OPTS=-Xmx1024m -Xms512m -XX:+UseG1GC"
ExecStart=/usr/lib/jvm/java-11-openjdk-amd64/bin/java -jar /opt/smartfm/smart-file-management.jar
Restart=on-failure
RestartSec=10

StandardOutput=file:/var/log/smartfm/output.log
StandardError=file:/var/log/smartfm/error.log

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable smartfm
sudo systemctl start smartfm

# Check status
sudo systemctl status smartfm
```

### 4. Nginx Reverse Proxy

```bash
# Install Nginx
sudo apt-get install -y nginx

# Create config
sudo tee /etc/nginx/sites-available/smartfm > /dev/null <<EOF
upstream smartfm_backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    client_max_body_size 100M;

    location / {
        proxy_pass http://smartfm_backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/smartfm /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# Test config
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

### 5. SSL/TLS with Let's Encrypt

```bash
# Install Certbot
sudo apt-get install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot certonly --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer
```

### 6. Monitoring

```bash
# View logs
sudo journalctl -u smartfm -f

# Monitor resources
top
free -h
df -h

# Check service
systemctl is-active smartfm
```

## Windows Server Deployment

### 1. Prerequisites

- Windows Server 2016+
- Java 11 installed
- Administrator access

### 2. Install as Windows Service

```cmd
# Create directory
mkdir C:\SmartFileManager
cd C:\SmartFileManager

# Copy JAR
copy path\to\smart-file-management.jar .

# Create service wrapper script
REM (Use NSSM - Non-Sucking Service Manager)
# Download from https://nssm.cc/download

# Install service
nssm install SmartFileManager "C:\Program Files\Java\jdk-11\bin\java.exe" "-jar C:\SmartFileManager\smart-file-management.jar"

# Set startup type
nssm set SmartFileManager Start SERVICE_AUTO_START

# Start service
nssm start SmartFileManager

# View status
nssm status SmartFileManager
```

### 3. IIS Configuration

```
1. Open IIS Manager
2. Create new Application Pool (No Managed Code)
3. Create new Website pointing to uploads directory
4. Configure reverse proxy rules
5. Add SSL certificate
```

## Monitoring & Logging

### 1. Application Logging

```bash
# Enable detailed logging
java -Djava.util.logging.config.file=logging.properties \
     -jar smart-file-management.jar
```

### 2. Monitor Disk Usage

```bash
# Check uploads directory
du -sh /data/uploads

# Alert if exceeds threshold
if [ $(du -s /data/uploads | cut -f1) -gt 50000000 ]; then
    echo "Disk usage warning" | mail -s "Alert" admin@example.com
fi
```

### 3. Performance Monitoring

```bash
# CPU and Memory
top -p $(pgrep -f smart-file-management)

# Network connections
netstat -an | grep 8080

# Open files
lsof -p $(pgrep -f smart-file-management)
```

## Backup & Recovery

### 1. Backup Script

```bash
#!/bin/bash
BACKUP_DIR="/backups/smartfm"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# Backup uploads
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /data/uploads/

# Backup database (if applicable)
# mysqldump -u user -p database > $BACKUP_DIR/database_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -mtime +7 -delete

echo "Backup completed: $DATE"
```

### 2. Recovery

```bash
# Restore uploads
tar -xzf /backups/smartfm/uploads_YYYYMMDD_HHMMSS.tar.gz -C /

# Verify
ls -la /data/uploads/
```

## Rollback Plan

### 1. Keep Previous Version

```bash
# Backup current
cp target/smart-file-management.jar \
   /opt/smartfm/smart-file-management.jar.backup

# Deploy new version
cp target/smart-file-management-new.jar \
   /opt/smartfm/smart-file-management.jar
```

### 2. Quick Rollback

```bash
# Restore previous
cp /opt/smartfm/smart-file-management.jar.backup \
   /opt/smartfm/smart-file-management.jar

# Restart service
sudo systemctl restart smartfm
```

## Health Checks

```bash
# Test endpoint
curl -f http://localhost:8080 || exit 1

# Test file upload
curl -F "file=@test.txt" http://localhost:8080/api/upload

# Test file listing
curl http://localhost:8080/api/files

# Test statistics
curl http://localhost:8080/api/stats
```

## Troubleshooting Production Issues

### High Memory Usage

```bash
# Check memory
free -h

# Increase heap size
export JAVA_OPTS="-Xmx2048m -Xms1024m"

# Restart application
```

### Slow File Uploads

- Check disk I/O: `iostat -x 1`
- Check network: `iftop`
- Check CPU: `top`

### Connection Errors

- Check firewall: `ufw status`
- Check ports: `netstat -tulpn`
- Check service: `systemctl status smartfm`

---

**Last Updated:** 2024
**Version:** 1.0.0
