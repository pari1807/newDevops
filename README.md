# Smart File Management System

A modern, full-stack file management system built with **HTML5, CSS3, JavaScript, Core Java, Maven, Docker, and GitHub Actions**. Perfect for secure file uploads, management, and analytics with a professional SaaS-like interface.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/java-25+-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## 🌟 Features

### Authentication System

- ✅ User login and signup pages
- ✅ Form validation with real-time feedback
- ✅ Password show/hide toggle
- ✅ Remember me functionality
- ✅ Session management

### File Upload System

- ✅ Drag and drop file upload
- ✅ Browse file selection
- ✅ Real-time upload progress bar
- ✅ Multiple file upload support
- ✅ File size validation (Max 100MB)
- ✅ File type validation
- ✅ Toast notifications

### File Management Dashboard

- ✅ Display files in grid/table view
- ✅ File icons based on type
- ✅ Upload date and file size display
- ✅ Search functionality
- ✅ Sort by date, name, or size
- ✅ Download files
- ✅ Delete files with confirmation
- ✅ Image and PDF preview

### Dashboard Analytics

- ✅ Total uploaded files count
- ✅ Total storage used
- ✅ Recent uploads display
- ✅ File category statistics
- ✅ Storage usage progress bar

### UI/UX Features

- ✅ Fully responsive design (mobile, tablet, desktop)
- ✅ Modern glassmorphism UI design
- ✅ Smooth animations and transitions
- ✅ Sidebar navigation
- ✅ Dark/light mode toggle
- ✅ Beautiful gradients
- ✅ Professional typography
- ✅ Toast notifications
- ✅ Loading animations
- ✅ Empty states

## 🏗️ Project Structure

```
smart-file-management/
├── src/main/java/com/filemanagement/
│   ├── FileManagementServer.java          # Main server entry point
│   ├── FileUploadHandler.java             # Handles file uploads
│   ├── FileListHandler.java               # Lists uploaded files
│   ├── FileDeleteHandler.java             # Deletes files
│   ├── FileDownloadHandler.java           # Downloads files
│   ├── FileStatsHandler.java              # Returns analytics
│   └── StaticFileHandler.java             # Serves frontend files
├── frontend/
│   ├── index.html                         # Main dashboard
│   ├── login.html                         # Login page
│   ├── signup.html                        # Signup page
│   ├── style.css                          # Complete styling
│   └── script.js                          # All functionality
├── uploads/                               # User uploaded files directory
├── pom.xml                                # Maven configuration
├── Dockerfile                             # Docker configuration
├── .github/workflows/ci.yml               # GitHub Actions CI/CD
├── README.md                              # This file
├── SETUP.md                               # Setup instructions
└── .gitignore                             # Git ignore file
```

## 🚀 Quick Start

### Prerequisites

- **Java 11+** - [Download](https://adoptopenjdk.net/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
- **Docker** (optional) - [Download](https://www.docker.com/products/docker-desktop)
- **Git** - [Download](https://git-scm.com/)

### Local Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/yourusername/smart-file-management.git
   cd smart-file-management
   ```

2. **Build the project**

   ```bash
   mvn clean package
   ```

3. **Run the server**

   ```bash
   java -jar target/smart-file-management.jar
   ```

4. **Access the application**
   - Open your browser: `http://localhost:8080`
   - Login page appears by default
   - Create account or login with demo credentials

## 🐳 Docker Setup

### Build Docker Image

```bash
docker build -t smart-file-manager:latest .
```

### Run Docker Container

```bash
docker run -d \
  --name smart-file-manager \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  smart-file-manager:latest
```

Access the application at `http://localhost:8080`

### Docker Compose (Optional)

```yaml
version: "3.8"
services:
  app:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./uploads:/app/uploads
    environment:
      - JAVA_OPTS=-Xmx512m
```

Run with: `docker-compose up -d`

## 🔌 API Endpoints

### Authentication

- `POST /login` - User login
- `POST /signup` - User registration
- `POST /logout` - User logout

### File Management

- `POST /api/upload` - Upload files
- `GET /api/files` - List all files
- `GET /api/files?sort=date&order=asc` - Sort files
- `GET /api/download/{filename}` - Download file
- `DELETE /api/delete/{filename}` - Delete file

### Analytics

- `GET /api/stats` - Get file statistics

### Static Files

- `GET /` - Main dashboard
- `GET /login.html` - Login page
- `GET /signup.html` - Signup page

## 🛠️ Development

### Project Structure Details

**Backend (Core Java)**

- Uses Java `HttpServer` for lightweight HTTP handling
- No external web framework (Spring Boot not used)
- Multipart form data parsing for file uploads
- JSON responses using Gson library
- Thread pool for concurrent request handling

**Frontend**

- Vanilla JavaScript (no frameworks)
- Responsive CSS Grid and Flexbox
- Modern CSS features (Glassmorphism, gradients)
- Local storage for authentication
- Fetch API for backend communication

### Building & Compiling

```bash
# Full build
mvn clean install

# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test

# Run with specific profile
mvn clean package -P production
```

### Running in Development

```bash
# Terminal 1: Start the server
java -jar target/smart-file-management.jar

# Terminal 2: Optional - Live server for frontend changes
python -m http.server 8080  # From frontend directory
```

## 📚 Configuration

### Server Settings

- **Port:** 8080
- **Max File Size:** 100MB
- **Thread Pool:** 10 threads
- **Upload Directory:** `uploads/`

### Allowed File Types

- Images: `jpg, jpeg, png, gif, webp`
- Documents: `pdf, txt, csv`
- Office: `doc, docx, xls, xlsx`
- Archives: `zip, rar`

Modify these in `FileUploadHandler.java` if needed.

## 🔒 Security Features

- File type validation
- File size validation
- Directory traversal attack prevention
- CORS enabled for cross-origin requests
- Input sanitization
- Secure file storage

## 📊 GitHub Actions CI/CD

The project includes a comprehensive GitHub Actions workflow that:

- ✅ Runs on push and pull requests
- ✅ Builds with Maven
- ✅ Runs test suite
- ✅ Builds Docker image
- ✅ Pushes to GitHub Container Registry
- ✅ Code quality analysis (SonarCloud)
- ✅ Security scanning (Trivy)
- ✅ Automated deployments

### Setup GitHub Actions

1. Add secrets to your repository:
   - `SONAR_TOKEN` - For SonarCloud analysis
   - `DOCKER_USERNAME` - Docker Hub username
   - `DOCKER_PASSWORD` - Docker Hub password

2. Workflows will automatically run on push/PR

## 🎨 Design Highlights

- **Modern Glassmorphism** - Contemporary UI design
- **Dark/Light Mode** - User preference toggle
- **Responsive** - Works on all devices
- **Accessible** - WCAG compliant
- **Performance** - Optimized assets and caching
- **Animations** - Smooth transitions throughout

## 📱 Browser Support

- Chrome/Chromium 90+
- Firefox 88+
- Safari 14+
- Edge 90+
- Mobile browsers (iOS Safari, Chrome Mobile)

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Permission Denied (Linux/Mac)

```bash
chmod +x ./build.sh
```

### File Upload Issues

- Check `uploads/` directory permissions
- Verify file size is under 100MB
- Check file type is allowed
- Ensure disk space is available

### Docker Issues

```bash
# Clean Docker
docker system prune -a

# Rebuild image
docker build --no-cache -t smart-file-manager:latest .
```

## 📝 API Usage Examples

### Upload File

```bash
curl -X POST \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/file.pdf" \
  http://localhost:8080/api/upload
```

### List Files

```bash
curl http://localhost:8080/api/files

# With sorting
curl http://localhost:8080/api/files?sort=size&order=desc
```

### Download File

```bash
curl -O http://localhost:8080/api/download/myfile.pdf
```

### Delete File

```bash
curl -X DELETE http://localhost:8080/api/delete/myfile.pdf
```

### Get Statistics

```bash
curl http://localhost:8080/api/stats
```

## 🚢 Deployment

### Heroku

```bash
git push heroku main
```

### AWS

1. Build Docker image
2. Push to ECR
3. Deploy to ECS or AppRunner

### Azure

1. Build Docker image
2. Push to ACR
3. Deploy to App Service

### Google Cloud

1. Build Docker image
2. Push to GCR
3. Deploy to Cloud Run or GKE

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📧 Support & Contact

- **Issues:** [GitHub Issues](https://github.com/yourusername/smart-file-management/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/smart-file-management/discussions)
- **Email:** support@example.com

## 🙌 Acknowledgments

- Maven for build automation
- Java HttpServer for lightweight HTTP handling
- Gson for JSON processing
- GitHub Actions for CI/CD
- Docker for containerization

## 📈 Roadmap

- [ ] User authentication database
- [ ] File sharing with expiring links
- [ ] File versioning and history
- [ ] Advanced search and filtering
- [ ] API key management
- [ ] Webhooks support
- [ ] S3 integration
- [ ] Multi-language support
- [ ] Database migration tools
- [ ] Mobile applications

---

**Built with ❤️ using Java, JavaScript, and modern web technologies**

**Last Updated:** 2024
**Version:** 1.0.0
