# Contributing Guide

Thank you for your interest in contributing to Smart File Management System! We welcome contributions from everyone.

## Code of Conduct

- Be respectful and inclusive
- Maintain professional communication
- Report issues constructively
- Focus on code quality

## Getting Started

### 1. Fork the Repository

```bash
# Click "Fork" button on GitHub
# Clone your fork
git clone https://github.com/your-username/smart-file-management.git
cd smart-file-management
```

### 2. Create a Feature Branch

```bash
# Create and switch to new branch
git checkout -b feature/your-feature-name

# Naming conventions:
# - feature/add-user-profile
# - bugfix/fix-upload-issue
# - docs/update-readme
```

### 3. Set Up Development Environment

```bash
# Install dependencies
mvn install

# Build project
mvn clean package

# Run tests
mvn test
```

## Development Workflow

### Making Changes

1. **Backend Changes:**
   - Modify files in `src/main/java/com/filemanagement/`
   - Follow Java naming conventions
   - Add Javadoc comments
   - Write unit tests

2. **Frontend Changes:**
   - Modify files in `frontend/` directory
   - Update CSS in `style.css`
   - Update JavaScript in `script.js`
   - Test responsiveness

3. **Code Style:**
   - Use 4-space indentation
   - Max line length: 120 characters
   - Use meaningful variable names
   - Add comments for complex logic

### Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ClassName

# Run with coverage
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Committing Changes

```bash
# Stage changes
git add .

# Commit with meaningful message
git commit -m "Add feature: Brief description"

# Commit message format:
# - Use imperative mood ("Add" not "Added")
# - First line max 50 characters
# - Detailed explanation after blank line if needed
# - Reference issues: "Fixes #123"

# Examples:
# "Add file preview functionality"
# "Fix upload progress bar display"
# "Update documentation for API"
```

### Push Changes

```bash
# Push to your fork
git push origin feature/your-feature-name
```

## Pull Request Process

### Before Submitting

1. **Update from main:**

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests:**

   ```bash
   mvn clean test
   ```

3. **Build locally:**

   ```bash
   mvn clean package
   ```

4. **Check code quality:**
   - No console errors
   - All tests passing
   - No compiler warnings

### Creating PR

1. Go to GitHub repository
2. Click "New Pull Request"
3. Select your branch
4. Fill in PR template:

   ```markdown
   ## Description

   Brief description of changes

   ## Type of Change

   - [ ] Bug fix
   - [ ] New feature
   - [ ] Documentation
   - [ ] Breaking change

   ## Related Issues

   Closes #123

   ## Testing

   How to test the changes

   ## Screenshots (if applicable)

   ## Checklist

   - [ ] Tests pass
   - [ ] Code follows style guide
   - [ ] Documentation updated
   - [ ] No new warnings
   ```

## Development Guidelines

### Java Backend

```java
// Good - Clear, documented
/**
 * Uploads a file to the server
 * @param file The file to upload
 * @return true if upload successful
 */
public boolean uploadFile(File file) {
    // Implementation
}

// Bad - No documentation
public boolean upload(File f) {
    // Implementation
}
```

### Frontend JavaScript

```javascript
// Good - Clear function with comments
function handleFileUpload(files) {
  // Validate files
  if (!files || files.length === 0) {
    showToast("No files selected", "error");
    return;
  }

  // Process each file
  for (let file of files) {
    uploadFile(file);
  }
}

// Bad - Unclear variable names
function handle(f) {
  if (f) f.forEach((x) => upload(x));
}
```

### CSS

```css
/* Good - Clear class names */
.file-upload-area {
  border: 2px dashed var(--border-color);
  padding: 48px;
  transition: all 0.3s ease;
}

/* Bad - Non-descriptive */
.area {
  border: 2px dashed #ccc;
  padding: 48px;
}
```

## Adding Features

### Backend Feature Checklist

- [ ] Create new handler class
- [ ] Implement `HttpHandler` interface
- [ ] Add CORS headers
- [ ] Handle all HTTP methods
- [ ] Return JSON responses
- [ ] Add error handling
- [ ] Log important actions
- [ ] Write unit tests
- [ ] Update API documentation

### Frontend Feature Checklist

- [ ] Add HTML structure
- [ ] Add CSS styling
- [ ] Add JavaScript functionality
- [ ] Handle errors with toast
- [ ] Test on mobile
- [ ] Add loading states
- [ ] Update documentation

## Documentation

### Code Comments

```java
// Use for explaining "why", not "what"

// Good
// Use Gson for serialization to maintain consistency across codebase
JsonObject response = new JsonObject();

// Bad
// Create a JsonObject
JsonObject response = new JsonObject();
```

### README/Documentation

- Use clear, concise language
- Include examples
- Add diagrams if helpful
- Keep it up-to-date

## Issue Reporting

### Bug Reports

```markdown
## Description

Brief description of the bug

## Steps to Reproduce

1. Step one
2. Step two
3. Step three

## Expected Behavior

What should happen

## Actual Behavior

What actually happens

## Screenshots/Logs

If applicable

## Environment

- Java version
- Browser/OS
- Maven version
```

### Feature Requests

```markdown
## Feature Description

What feature would you like?

## Use Case

Why would this be useful?

## Implementation Details (Optional)

How might this be implemented?

## Alternatives

Other approaches considered?
```

## Testing Requirements

### New Features Must Include

- Unit tests (minimum 80% coverage)
- Integration tests if applicable
- Edge case testing
- Error handling tests

### Test File Naming

```
src/test/java/com/filemanagement/FileUploadHandlerTest.java
```

### Example Test

```java
@Test
public void testFileUploadSuccess() {
    // Arrange
    File testFile = new File("test.pdf");

    // Act
    boolean result = fileHandler.upload(testFile);

    // Assert
    assertTrue(result);
    assertTrue(new File(UPLOAD_DIR + "/test.pdf").exists());
}
```

## Performance Considerations

- Minimize dependencies
- Use efficient algorithms
- Cache when appropriate
- Profile before optimizing
- Consider memory usage

## Security Guidelines

- Validate all inputs
- Sanitize file names
- Check file types/sizes
- Prevent directory traversal
- Use secure file operations
- Never log sensitive data
- Validate CORS origins

## Review Process

1. **Automated Checks:**
   - GitHub Actions CI/CD
   - Code quality scans
   - Security checks

2. **Code Review:**
   - At least 1 maintainer approval
   - All tests passing
   - No conflicts with main

3. **Merge:**
   - Squash commits for clean history
   - Use meaningful merge commit message

## Useful Commands

```bash
# Update main branch
git fetch origin
git rebase origin/main

# View commits
git log --oneline -10

# View changes
git diff

# Undo uncommitted changes
git checkout -- file.java

# Revert last commit
git revert HEAD

# Force update branch
git push origin feature-branch -f
```

## Getting Help

- **Issues:** GitHub Issues page
- **Discussions:** GitHub Discussions
- **Email:** maintainer@example.com
- **Chat:** [Discord/Slack invite if applicable]

## Contributor Recognition

Contributors will be:

- Added to CONTRIBUTORS.md
- Mentioned in releases
- Acknowledged in documentation

## License

By contributing, you agree your code will be licensed under the MIT License.

---

**Thank you for contributing! 🎉**
