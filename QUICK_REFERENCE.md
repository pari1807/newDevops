# Quick Reference Guide

## 🚀 Quick Start Commands

### Local Development

```bash
# Navigate to project
cd /Users/paritosh/Desktop/NewJavaProject

# Build the project
mvn clean package

# Run the application
java -jar target/smart-file-management.jar

# Access: http://localhost:8080
```

### Build Scripts

```bash
# Unix/Linux/macOS
chmod +x build.sh
./build.sh

# Windows
build.bat
```

### Docker

```bash
# Build image
docker build -t smart-file-manager:latest .

# Run container
docker run -d -p 8080:8080 -v $(pwd)/uploads:/app/uploads smart-file-manager:latest
```

---

## 📁 Project Files

### Backend (Java)

| File                      | Lines    | Purpose                                    |
| ------------------------- | -------- | ------------------------------------------ |
| FileManagementServer.java | 63       | Main server entry point, HTTP server setup |
| FileUploadHandler.java    | 241      | Handles multipart file uploads             |
| FileListHandler.java      | 210      | Lists files with sorting capabilities      |
| FileDeleteHandler.java    | 97       | Secure file deletion                       |
| FileDownloadHandler.java  | 145      | File download with proper headers          |
| FileStatsHandler.java     | 155      | Analytics and statistics                   |
| StaticFileHandler.java    | 149      | Serves frontend files                      |
| **Total Backend**         | **1060** | **7 classes, ~1000 LOC**                   |

### Frontend (Web)

| File               | Lines    | Purpose                            |
| ------------------ | -------- | ---------------------------------- |
| index.html         | 251      | Main dashboard interface           |
| login.html         | 77       | User login page                    |
| signup.html        | 93       | User registration page             |
| style.css          | 1608     | Complete styling (dark/light mode) |
| script.js          | 848      | All frontend functionality         |
| **Total Frontend** | **2877** | **~2880 LOC**                      |

### Configuration

| File                     | Purpose                   |
| ------------------------ | ------------------------- |
| pom.xml                  | Maven build configuration |
| Dockerfile               | Multi-stage Docker build  |
| .github/workflows/ci.yml | GitHub Actions CI/CD      |
| .gitignore               | Git ignore patterns       |

### Documentation

| File               | Content                     |
| ------------------ | --------------------------- |
| README.md          | Features, setup, API docs   |
| SETUP.md           | Detailed setup instructions |
| DEPLOYMENT.md      | Production deployment guide |
| CONTRIBUTING.md    | Developer guidelines        |
| PROJECT_SUMMARY.md | Project overview            |

### Build Scripts

| File      | Purpose                       |
| --------- | ----------------------------- |
| build.sh  | Unix/Linux/macOS build script |
| build.bat | Windows build script          |

---

## 🔌 API Endpoints

### File Management

| Method | Endpoint                 | Purpose            |
| ------ | ------------------------ | ------------------ |
| POST   | /api/upload              | Upload files       |
| GET    | /api/files               | List all files     |
| GET    | /api/files?sort=name     | Sort files by name |
| GET    | /api/files?sort=size     | Sort files by size |
| GET    | /api/files?sort=date     | Sort files by date |
| GET    | /api/download/{filename} | Download file      |
| DELETE | /api/delete/{filename}   | Delete file        |
| GET    | /api/stats               | Get statistics     |

### Static Files

| Path             | Purpose                |
| ---------------- | ---------------------- |
| GET /            | Dashboard (index.html) |
| GET /login.html  | Login page             |
| GET /signup.html | Signup page            |
| GET /style.css   | CSS stylesheet         |
| GET /script.js   | JavaScript bundle      |

---

## 🎨 UI Pages

### 1. Login Page

- Email validation
- Password show/hide toggle
- Remember me checkbox
- Social login buttons (UI only)
- Link to signup

### 2. Signup Page

- Name, email, password validation
- Password confirmation
- Terms acceptance
- Social signup buttons (UI only)
- Link to login

### 3. Dashboard (Main)

- **Top Bar:**
  - Search functionality
  - Dark/light mode toggle
  - User menu with logout
- **Sidebar:**
  - Navigation menu
  - Storage usage indicator
- **Main Content:**
  - Statistics cards (4 cards)
  - File upload area (drag & drop)
  - Recent files section
  - Files section (grid/table view)
  - Shared section

- **Modal:**
  - Delete confirmation dialog

---

## 🎯 Features List

### ✅ Implemented Features

- [x] User authentication (login/signup)
- [x] File upload with drag & drop
- [x] Multiple file upload
- [x] Real-time progress bar
- [x] File listing and browsing
- [x] File search
- [x] File sorting (name, date, size)
- [x] File download
- [x] File deletion with confirmation
- [x] Image preview
- [x] Statistics dashboard
- [x] Storage usage tracking
- [x] Dark/light mode
- [x] Responsive design
- [x] Mobile friendly
- [x] Toast notifications
- [x] Error handling
- [x] REST API
- [x] Docker support
- [x] GitHub Actions CI/CD

---

## 🔧 Configuration Points

### Server Port

**File:** FileManagementServer.java

```java
private static final int PORT = 8080;
```

### Max File Size

**File:** FileUploadHandler.java

```java
private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
```

### Thread Pool Size

**File:** FileManagementServer.java

```java
private static final int THREAD_POOL_SIZE = 10;
```

### Allowed File Types

**File:** FileUploadHandler.java

```java
private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
    "image/jpeg", "image/png", "application/pdf", ...
));
```

### Upload Directory

**File:** All handlers

```bash
private static final String UPLOAD_DIR = "uploads";
```

---

## 📊 Project Statistics

| Metric              | Value      |
| ------------------- | ---------- |
| Total Files         | 30+        |
| Java Classes        | 7          |
| HTML Pages          | 3          |
| Total Lines of Code | 6,600+     |
| CSS Lines           | 1,600+     |
| JavaScript Lines    | 800+       |
| Java Lines          | 1,000+     |
| Documentation Lines | 1,500+     |
| JAR Size            | 394 KB     |
| Build Time          | ~2 seconds |
| Startup Time        | <1 second  |

---

## 🧪 Testing

### Manual Testing Checklist

**Authentication:**

- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Signup with new account
- [ ] Password show/hide toggle
- [ ] Remember me functionality

**File Upload:**

- [ ] Single file upload
- [ ] Multiple file upload
- [ ] Drag and drop upload
- [ ] File size validation
- [ ] File type validation
- [ ] Progress bar animation

**File Management:**

- [ ] List files
- [ ] Sort by name
- [ ] Sort by size
- [ ] Sort by date
- [ ] Search files
- [ ] Download file
- [ ] Delete file with confirmation
- [ ] Preview image

**UI/UX:**

- [ ] Dark mode toggle
- [ ] Responsive on mobile
- [ ] Responsive on tablet
- [ ] Responsive on desktop
- [ ] Toast notifications
- [ ] Keyboard navigation

---

## 🚀 Performance Tips

### Optimize for Better Performance

1. **Increase Heap Memory:**

   ```bash
   java -Xmx1024m -Xms512m -XX:+UseG1GC -jar target/smart-file-management.jar
   ```

2. **Increase Thread Pool:**
   Modify `FileManagementServer.java` line 29

   ```java
   private static final int THREAD_POOL_SIZE = 20;
   ```

3. **Enable Caching:**
   Files are cached by browser via HTTP headers

4. **Monitor Performance:**

   ```bash
   # Check memory
   jps -lv

   # Check connections
   netstat -an | grep 8080
   ```

---

## 🐛 Common Issues & Solutions

| Issue                    | Solution                                                 |
| ------------------------ | -------------------------------------------------------- |
| Port 8080 in use         | Change PORT in FileManagementServer.java or kill process |
| Upload folder permission | `chmod 777 uploads` (Linux/macOS)                        |
| Java not found           | Install Java 11+ and set JAVA_HOME                       |
| Maven not found          | Install Maven and add to PATH                            |
| Build fails              | Run `mvn clean install`                                  |
| Docker build fails       | Run `docker system prune -a` then rebuild                |

---

## 📱 Browser DevTools Tips

### Open DevTools

- Chrome: F12 or Cmd+Option+I (macOS)
- Firefox: F12 or Cmd+Option+I (macOS)
- Safari: Cmd+Option+I (macOS)

### Network Tab

- Monitor API requests
- Check response times
- Verify file uploads

### Console

- View logs
- Run JavaScript
- Debug issues

### Storage

- View localStorage
- Check session data

---

## 🔐 Security Checklist

- [x] Input validation
- [x] File type checking
- [x] File size limits
- [x] Directory traversal prevention
- [x] SQL injection prevention (no SQL)
- [x] XSS prevention
- [x] CSRF protection ready
- [x] CORS properly configured

---

## 📞 Support Resources

### Documentation

- **README.md** - Overview and features
- **SETUP.md** - Setup and installation
- **DEPLOYMENT.md** - Production deployment
- **CONTRIBUTING.md** - Developer guide
- **PROJECT_SUMMARY.md** - Complete project info

### Getting Help

1. Read relevant documentation
2. Check GitHub Issues
3. Review code comments
4. Check browser console

---

## 🎓 Learning Resources

### For Backend Development

- [Java HttpServer Documentation](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/com/sun/net/httpserver/HttpServer.html)
- [Gson Library](https://github.com/google/gson)
- [SLF4J Logging](http://www.slf4j.org/)

### For Frontend Development

- [MDN Web Docs](https://developer.mozilla.org/)
- [CSS Grid Guide](https://css-tricks.com/snippets/css/complete-guide-grid/)
- [Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API)

### For DevOps

- [Docker Documentation](https://docs.docker.com/)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Maven Documentation](https://maven.apache.org/)

---

## 📋 Deployment Checklist

- [ ] All tests passing
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Environment variables configured
- [ ] SSL certificate ready (if HTTPS)
- [ ] Database backups created
- [ ] Monitoring setup
- [ ] Logging configured
- [ ] Firewall rules updated
- [ ] DNS configured (if domain)

---

## 🎉 You're All Set!

Your Smart File Management System is complete and ready to use!

**Quick Links:**

- 📖 [README](README.md) - Project overview
- 🚀 [SETUP](SETUP.md) - Get started
- 🌐 [DEPLOYMENT](DEPLOYMENT.md) - Deploy to production
- 🤝 [CONTRIBUTING](CONTRIBUTING.md) - Contribute to project
- 📊 [PROJECT SUMMARY](PROJECT_SUMMARY.md) - Complete details

---

**Version:** 1.0.0
**Last Updated:** 2024
