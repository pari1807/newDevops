/**
 * Smart File Management System - Frontend JavaScript
 * Handles all UI interactions, API calls, and user interactions
 */

// ===========================
// GLOBAL STATE
// ===========================

const APP_STATE = {
    isLoggedIn: false,
    currentUser: null,
    currentSection: 'dashboard',
    files: [],
    isDarkMode: localStorage.getItem('darkMode') === 'true',
    deleteConfirmFile: null,
};

// Track in-flight uploads to prevent duplicate posts (key: name|size)
window.__PENDING_UPLOADS = window.__PENDING_UPLOADS || new Map();

// NOTE: temporary overlay-hiding removed to restore original behaviour

// ===========================
// INITIALIZATION
// ===========================

document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

function initializeApp() {
    // Check authentication
    checkAuthentication();
    
    // Apply theme
    applyTheme();
    
    // Load files and stats immediately after authentication
    // This ensures files are loaded before any rendering happens
    if (APP_STATE.isLoggedIn) {
        // Load files synchronously to avoid showing 0 files initially
        const filesReq = fetch('/api/files')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    APP_STATE.files = data.files || [];
                    renderFiles();
                    renderRecentFiles();
                }
            });
        
        const statsReq = fetch('/api/stats')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    updateStatistics(data);
                }
            });
        
        // Set up periodic refresh
        setInterval(loadFiles, 10000); // Refresh every 10 seconds
    }
    
    // Add event listeners
    addEventListeners();
}

function checkAuthentication() {
    const authToken = localStorage.getItem('authToken');
    const currentUser = localStorage.getItem('currentUser');
    
    if (authToken && currentUser) {
        APP_STATE.isLoggedIn = true;
        APP_STATE.currentUser = JSON.parse(currentUser);
        showDashboard();
    } else {
        showAuthPage();
    }
}

function addEventListeners() {
    const uploadArea = document.getElementById('uploadArea');
    if (uploadArea) {
        uploadArea.addEventListener('click', (event) => {
            if (event.target.closest('button, input, a, label')) {
                return;
            }

            document.getElementById('fileInput').click();
        });
    }

    const fileInput = document.getElementById('fileInput');
    if (fileInput) {
        fileInput.addEventListener('click', () => {
            fileInput.value = '';
        });
    }
}

// ===========================
// AUTHENTICATION
// ===========================

function handleLogin(event) {
    event.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;
    
    // Simple validation
    if (!email || !password) {
        showToast('Please fill in all fields', 'error');
        return;
    }
    
    if (!isValidEmail(email)) {
        showToast('Please enter a valid email address', 'error');
        return;
    }
    
    if (password.length < 8) {
        showToast('Password must be at least 8 characters', 'error');
        return;
    }
    
    // Simulate login (in production, this would call a backend API)
    APP_STATE.isLoggedIn = true;
    APP_STATE.currentUser = {
        id: 1,
        name: email.split('@')[0],
        email: email
    };
    
    localStorage.setItem('authToken', 'mock_token_' + Date.now());
    localStorage.setItem('currentUser', JSON.stringify(APP_STATE.currentUser));
    if (rememberMe) {
        localStorage.setItem('rememberEmail', email);
    }
    
    showToast('Login successful! Welcome ' + APP_STATE.currentUser.name, 'success');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1000);
}

function handleSignup(event) {
    event.preventDefault();
    
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const terms = document.getElementById('terms').checked;
    
    // Validation
    if (!name || !email || !password || !confirmPassword) {
        showToast('Please fill in all fields', 'error');
        return;
    }
    
    if (!isValidEmail(email)) {
        showToast('Please enter a valid email address', 'error');
        return;
    }
    
    if (password.length < 8) {
        showToast('Password must be at least 8 characters', 'error');
        return;
    }
    
    if (password !== confirmPassword) {
        showToast('Passwords do not match', 'error');
        return;
    }
    
    if (!terms) {
        showToast('Please agree to the Terms of Service', 'error');
        return;
    }
    
    // Simulate signup (in production, this would call a backend API)
    APP_STATE.isLoggedIn = true;
    APP_STATE.currentUser = {
        id: 1,
        name: name,
        email: email
    };
    
    localStorage.setItem('authToken', 'mock_token_' + Date.now());
    localStorage.setItem('currentUser', JSON.stringify(APP_STATE.currentUser));
    
    showToast('Account created successfully! Welcome ' + name, 'success');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1000);
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    APP_STATE.isLoggedIn = false;
    APP_STATE.currentUser = null;
    showToast('Logged out successfully', 'success');
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 1000);
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// ===========================
// UI NAVIGATION
// ===========================

function showDashboard() {
    const authPages = document.querySelectorAll('.auth-page');
    const dashboardPage = document.querySelector('.dashboard-page');
    
    authPages.forEach(page => page.style.display = 'none');
    if (dashboardPage) dashboardPage.style.display = 'grid';
}

function showAuthPage() {
    const dashboardPage = document.querySelector('.dashboard-page');
    if (dashboardPage) dashboardPage.style.display = 'none';
}

function showSection(sectionName) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Show selected section
    const section = document.getElementById(sectionName + 'Section');
    if (section) {
        section.classList.add('active');
        APP_STATE.currentSection = sectionName;
    }
    
    // Update sidebar menu
    document.querySelectorAll('.sidebar .menu-item').forEach(item => {
        item.classList.remove('active');
    });
    event.target.closest('.menu-item')?.classList.add('active');
}

// ===========================
// FILE UPLOAD
// ===========================

function handleDragOver(event) {
    event.preventDefault();
    event.stopPropagation();
    const uploadArea = document.getElementById('uploadArea');
    uploadArea.classList.add('dragover');
}

function handleDragLeave(event) {
    event.preventDefault();
    event.stopPropagation();
    const uploadArea = document.getElementById('uploadArea');
    uploadArea.classList.remove('dragover');
}

function handleDrop(event) {
    event.preventDefault();
    event.stopPropagation();
    const uploadArea = document.getElementById('uploadArea');
    uploadArea.classList.remove('dragover');
    
    const files = event.dataTransfer.files;
    handleFiles(files);
}

function handleFileSelect(event) {
    const files = event.target.files;
    handleFiles(files);
    event.target.value = '';
}

function handleFiles(fileList) {
    if (fileList.length === 0) {
        showToast('No files selected', 'warning');
        return;
    }
    
    // Upload all files in parallel, but avoid duplicate uploads
    const uploadPromises = [];
    for (let file of fileList) {
        const key = file.name + '|' + file.size;
        if (window.__PENDING_UPLOADS.has(key)) {
            // reuse existing promise
            uploadPromises.push(window.__PENDING_UPLOADS.get(key));
        } else {
            const p = uploadFile(file);
            window.__PENDING_UPLOADS.set(key, p);
            // cleanup when done
            p.finally(() => window.__PENDING_UPLOADS.delete(key));
            uploadPromises.push(p);
        }
    }
    
    // Wait for all uploads to complete, then refresh
    Promise.all(uploadPromises)
        .then(() => {
            // All files uploaded successfully
            hideUploadProgress();
            loadFiles();
            loadStatistics();
        })
        .catch(error => {
            console.error('Some uploads failed:', error);
        });
}

function uploadFile(file) {
    return new Promise((resolve, reject) => {
        // Validate file size
        const maxSize = 100 * 1024 * 1024; // 100MB
        if (file.size > maxSize) {
            showToast('File too large: ' + file.name + ' (Max 100MB)', 'error');
            reject(new Error('File too large'));
            return;
        }
        
        // Create FormData
        const formData = new FormData();
        formData.append('file', file);
        
        // Show progress
        showUploadProgress(file.name);
        
        // Upload file
        fetch('/api/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Upload failed');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                showToast('File uploaded successfully: ' + file.name, 'success');
                resolve(data);
            } else {
                showToast('Upload failed: ' + (data.error || 'Unknown error'), 'error');
                reject(new Error(data.error || 'Upload failed'));
            }
        })
        .catch(error => {
            console.error('Upload error:', error);
            showToast('Upload failed: ' + error.message, 'error');
            reject(error);
        });
    });
}

function showUploadProgress(filename) {
    const progressContainer = document.getElementById('uploadProgress');
    if (progressContainer) {
        document.getElementById('progressFilename').textContent = 'Uploading: ' + filename;
        progressContainer.style.display = 'block';
        animateProgress();
    }
}

function hideUploadProgress() {
    const progressContainer = document.getElementById('uploadProgress');
    if (progressContainer) {
        document.getElementById('progressFill').style.width = '0%';
        document.getElementById('progressPercent').textContent = '0';
        progressContainer.style.display = 'none';
    }
}

function animateProgress() {
    let progress = 0;
    const interval = setInterval(() => {
        progress += Math.random() * 30;
        if (progress > 90) progress = 90;
        updateProgressBar(progress);
        if (progress >= 90) clearInterval(interval);
    }, 500);
}

function updateProgressBar(percent) {
    const fill = document.getElementById('progressFill');
    const percentText = document.getElementById('progressPercent');
    if (fill && percentText) {
        fill.style.width = percent + '%';
        percentText.textContent = Math.round(percent);
    }
}

// ===========================
// FILE MANAGEMENT
// ===========================

function loadFiles() {
    fetch('/api/files')
        .then(response => {
            if (!response.ok) throw new Error('Failed to load files');
            return response.json();
        })
        .then(data => {
            if (data.success) {
                APP_STATE.files = data.files || [];
                renderFiles();
                renderRecentFiles();
            }
        })
        .catch(error => {
            console.error('Error loading files:', error);
        });
}

function renderFiles() {
    const filesGrid = document.getElementById('filesGrid');
    const recentTable = document.getElementById('recentTable');
    
    if (!filesGrid && !recentTable) return;
    
    if (APP_STATE.files.length === 0) {
        if (filesGrid) {
            filesGrid.innerHTML = '<div class="empty-state"><p>No files yet</p></div>';
        }
        if (recentTable) {
            recentTable.innerHTML = '<tr class="empty-row"><td colspan="5">No recent files</td></tr>';
        }
        return;
    }
    
    // Render grid
    if (filesGrid) {
        filesGrid.innerHTML = APP_STATE.files.map(file => `
            <div class="file-card">
                <div class="file-icon">${getFileIcon(file.filename)}</div>
                <div class="file-name" title="${file.filename}">${truncateFilename(file.filename)}</div>
                <div class="file-size">${formatBytes(file.size)}</div>
                <div class="file-actions">
                    <button class="file-action-btn" onclick="downloadFile('${file.filename}')" title="Download">⬇️</button>
                    <button class="file-action-btn" onclick="previewFile('${file.filename}')" title="Preview">👁️</button>
                    <button class="file-action-btn" onclick="showDeleteModal('${file.filename}')" title="Delete">🗑️</button>
                </div>
            </div>
        `).join('');
    }
    
    // Render table
    if (recentTable) {
        recentTable.innerHTML = APP_STATE.files.slice(0, 10).map(file => `
            <tr>
                <td>${getFileIcon(file.filename)}</td>
                <td>${file.filename}</td>
                <td>${formatBytes(file.size)}</td>
                <td>
                    <button class="file-action-btn" onclick="downloadFile('${file.filename}')" title="Download">⬇️</button>
                    <button class="file-action-btn" onclick="previewFile('${file.filename}')" title="Preview">👁️</button>
                    <button class="file-action-btn" onclick="showDeleteModal('${file.filename}')" title="Delete">🗑️</button>
                </td>
            </tr>
        `).join('');
    }
}

function renderRecentFiles() {
    const recentList = document.getElementById('recentFilesList');
    if (!recentList) return;
    
    const recent = APP_STATE.files.slice(0, 6);
    
    if (recent.length === 0) {
        recentList.innerHTML = '<div class="empty-state"><p>No files uploaded yet</p><p>Start by uploading your first file above</p></div>';
        return;
    }
    
    recentList.innerHTML = recent.map(file => `
        <div class="file-card">
            <div class="file-icon">${getFileIcon(file.filename)}</div>
            <div class="file-name" title="${file.filename}">${truncateFilename(file.filename)}</div>
            <div class="file-size">${formatBytes(file.size)}</div>
            <div class="file-actions">
                <button class="file-action-btn" onclick="downloadFile('${file.filename}')" title="Download">⬇️</button>
                <button class="file-action-btn" onclick="previewFile('${file.filename}')" title="Preview">👁️</button>
                <button class="file-action-btn" onclick="showDeleteModal('${file.filename}')" title="Delete">🗑️</button>
            </div>
        </div>
    `).join('');
}

function downloadFile(filename) {
    try {
        const encodedFilename = encodeURIComponent(filename);
        const link = document.createElement('a');
        link.href = `/api/download/${encodedFilename}`;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('Downloading: ' + filename, 'success');
    } catch (error) {
        console.error('Download error:', error);
        showToast('Download failed', 'error');
    }
}

function previewFile(filename) {
    const extension = filename.split('.').pop().toLowerCase();
    
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) {
        openImagePreview(filename);
    } else if (extension === 'pdf') {
        openPDFPreview(filename);
    } else {
        showToast('Preview not available for this file type', 'warning');
    }
}

function openImagePreview(filename) {
    const encodedFilename = encodeURIComponent(filename);
    const modal = document.createElement('div');
    modal.className = 'modal open';
    modal.innerHTML = `
        <div class="modal-content" style="max-width: 80vw; width: 90%;">
            <div class="modal-header">
                <h2>${filename}</h2>
                <button type="button" class="modal-close" onclick="this.closest('.modal').remove()">×</button>
            </div>
            <div class="modal-body" style="text-align: center;">
                <img src="/api/download/${encodedFilename}" alt="${filename}" style="max-width: 100%; max-height: 70vh;">
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

function openPDFPreview(filename) {
    const encodedFilename = encodeURIComponent(filename);
    const pdfUrl = `/api/download/${encodedFilename}`;
    
    // Show modal with download/view options
    const modal = document.createElement('div');
    modal.className = 'modal open';
    modal.innerHTML = `
        <div class="modal-content" style="max-width: 500px;">
            <div class="modal-header">
                <h2>${filename}</h2>
                <button type="button" class="modal-close" onclick="this.closest('.modal').remove()">×</button>
            </div>
            <div class="modal-body" style="text-align: center; padding: 40px 20px;">
                <p style="font-size: 16px; color: var(--text-secondary); margin-bottom: 30px;">
                    📄 PDF File
                </p>
                <div style="display: flex; gap: 12px; justify-content: center;">
                    <button class="btn btn-primary" onclick="window.open('${pdfUrl}', '_blank'); this.closest('.modal').remove();">
                        Open in New Tab
                    </button>
                    <a href="${pdfUrl}" download class="btn btn-secondary" style="text-decoration: none;">
                        Download
                    </a>
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

function sortFiles(sortBy) {
    // Sort files locally
    if (sortBy === 'date') {
        APP_STATE.files.sort((a, b) => b.uploadDate - a.uploadDate);
    } else if (sortBy === 'name') {
        APP_STATE.files.sort((a, b) => a.filename.localeCompare(b.filename));
    } else if (sortBy === 'size') {
        APP_STATE.files.sort((a, b) => b.size - a.size);
    }
    renderFiles();
}

function performSearch() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    if (!searchTerm) {
        renderFiles();
        return;
    }
    
    const filtered = APP_STATE.files.filter(file => 
        file.filename.toLowerCase().includes(searchTerm)
    );
    
    const filesGrid = document.getElementById('filesGrid');
    if (!filesGrid) return;
    
    if (filtered.length === 0) {
        filesGrid.innerHTML = '<div class="empty-state"><p>No files found</p></div>';
        return;
    }
    
    filesGrid.innerHTML = filtered.map(file => `
        <div class="file-card">
            <div class="file-icon">${getFileIcon(file.filename)}</div>
            <div class="file-name" title="${file.filename}">${truncateFilename(file.filename)}</div>
            <div class="file-size">${formatBytes(file.size)}</div>
            <div class="file-actions">
                <button class="file-action-btn" onclick="downloadFile('${file.filename}')" title="Download">⬇️</button>
                <button class="file-action-btn" onclick="previewFile('${file.filename}')" title="Preview">👁️</button>
                <button class="file-action-btn" onclick="showDeleteModal('${file.filename}')" title="Delete">🗑️</button>
            </div>
        </div>
    `).join('');
}

// ===========================
// FILE DELETE
// ===========================

function showDeleteModal(filename) {
    APP_STATE.deleteConfirmFile = filename;
    const modal = document.getElementById('deleteModal');
    const fileNameElement = document.getElementById('deleteFileName');
    
    if (fileNameElement) {
        fileNameElement.textContent = filename;
    }
    
    if (modal) {
        modal.classList.add('open');
    }
}

function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (modal) {
        modal.classList.remove('open');
    }
    APP_STATE.deleteConfirmFile = null;
}

function confirmDelete() {
    const filename = APP_STATE.deleteConfirmFile;
    if (!filename) return;
    
    const encodedFilename = encodeURIComponent(filename);
    fetch(`/api/delete/${encodedFilename}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) throw new Error('Delete failed');
        return response.json();
    })
    .then(data => {
        if (data.success) {
            closeDeleteModal();
            showToast('File deleted successfully', 'success');
            loadFiles();
            loadStatistics();
        } else {
            showToast('Delete failed: ' + (data.error || 'Unknown error'), 'error');
        }
    })
    .catch(error => {
        console.error('Delete error:', error);
        showToast('Delete failed: ' + error.message, 'error');
    });
}

// ===========================
// STATISTICS & ANALYTICS
// ===========================

function loadStatistics() {
    fetch('/api/stats')
        .then(response => {
            if (!response.ok) throw new Error('Failed to load statistics');
            return response.json();
        })
        .then(data => {
            if (data.success) {
                updateStatistics(data);
            }
        })
        .catch(error => {
            console.error('Error loading statistics:', error);
        });
}

function updateStatistics(stats) {
    // Update stat cards
    const totalFilesEl = document.getElementById('totalFiles');
    const storageStatEl = document.getElementById('storageStat');
    const recentUploadsEl = document.getElementById('recentUploads');
    const fileCategoriesEl = document.getElementById('fileCategories');
    
    if (totalFilesEl) totalFilesEl.textContent = stats.totalFiles || 0;
    if (storageStatEl) storageStatEl.textContent = stats.totalSizeFormatted || '0 B';
    if (recentUploadsEl) recentUploadsEl.textContent = Math.min(stats.totalFiles || 0, 10);
    if (fileCategoriesEl) fileCategoriesEl.textContent = Object.keys(stats.typeDistribution || {}).length;
    
    // Update storage bar
    updateStorageBar(stats);
}

function updateStorageBar(stats) {
    const maxStorage = 100 * 1024 * 1024 * 1024; // 100GB
    const usedPercentage = (stats.totalSize / maxStorage) * 100;
    
    const storageUsedEl = document.getElementById('storageUsed');
    const usedStorageEl = document.getElementById('usedStorage');
    const totalStorageEl = document.getElementById('totalStorage');
    
    if (storageUsedEl) {
        storageUsedEl.style.width = Math.min(usedPercentage, 100) + '%';
    }
    
    if (usedStorageEl) {
        usedStorageEl.textContent = (stats.totalSize / (1024 * 1024 * 1024)).toFixed(2);
    }
    
    if (totalStorageEl) {
        totalStorageEl.textContent = '100';
    }
}

// ===========================
// THEME & UI
// ===========================

function applyTheme() {
    if (APP_STATE.isDarkMode) {
        document.body.classList.add('dark-mode');
        const themeIcon = document.getElementById('themeIcon');
        if (themeIcon) themeIcon.textContent = '☀️';
    } else {
        document.body.classList.remove('dark-mode');
        const themeIcon = document.getElementById('themeIcon');
        if (themeIcon) themeIcon.textContent = '🌙';
    }
}

function toggleTheme() {
    APP_STATE.isDarkMode = !APP_STATE.isDarkMode;
    localStorage.setItem('darkMode', APP_STATE.isDarkMode);
    applyTheme();
}

function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    
    if (input.type === 'password') {
        input.type = 'text';
    } else {
        input.type = 'password';
    }
}

function toggleUserMenu() {
    const userMenu = document.getElementById('userMenu');
    if (userMenu) {
        userMenu.classList.toggle('open');
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', (event) => {
    const userMenu = document.getElementById('userMenu');
    const userAvatar = document.querySelector('.user-avatar');
    
    if (userMenu && userAvatar && !userAvatar.contains(event.target) && !userMenu.contains(event.target)) {
        userMenu.classList.remove('open');
    }
});

// ===========================
// TOAST NOTIFICATIONS
// ===========================

function showToast(message, type = 'info', duration = 3000) {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    
    const toastId = 'toast-' + Date.now();
    const toast = document.createElement('div');
    toast.id = toastId;
    toast.className = `toast ${type}`;
    
    let icon = 'ℹ️';
    let title = 'Info';
    
    switch(type) {
        case 'success':
            icon = '✅';
            title = 'Success';
            break;
        case 'error':
            icon = '❌';
            title = 'Error';
            break;
        case 'warning':
            icon = '⚠️';
            title = 'Warning';
            break;
        case 'info':
            icon = 'ℹ️';
            title = 'Info';
            break;
    }
    
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
    `;
    
    container.appendChild(toast);
    
    // Auto remove after duration
    setTimeout(() => {
        const el = document.getElementById(toastId);
        if (el) {
            el.style.opacity = '0';
            el.style.transform = 'translateX(400px)';
            setTimeout(() => el.remove(), 300);
        }
    }, duration);
}

// ===========================
// UTILITY FUNCTIONS
// ===========================

function formatBytes(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

function formatDate(timestamp) {
    const date = new Date(timestamp);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    if (date.toDateString() === today.toDateString()) {
        return 'Today ' + date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    } else {
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }
}

function truncateFilename(filename, maxLength = 20) {
    if (filename.length <= maxLength) return filename;
    const extension = filename.split('.').pop();
    const name = filename.substring(0, filename.lastIndexOf('.'));
    const truncated = name.substring(0, maxLength - extension.length - 4);
    return truncated + '...' + extension;
}

function getFileIcon(filename) {
    const extension = filename.split('.').pop().toLowerCase();
    
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) return '🖼️';
    if (extension === 'pdf') return '📄';
    if (['zip', 'rar'].includes(extension)) return '📦';
    if (['txt', 'csv'].includes(extension)) return '📃';
    if (['doc', 'docx'].includes(extension)) return '📘';
    if (['xls', 'xlsx'].includes(extension)) return '📗';
    
    return '📁';
}

function getFileType(filename) {
    const extension = filename.split('.').pop().toLowerCase();
    
    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) return 'Image';
    if (extension === 'pdf') return 'PDF';
    if (['zip', 'rar'].includes(extension)) return 'Archive';
    if (['txt', 'csv'].includes(extension)) return 'Document';
    if (['doc', 'docx'].includes(extension)) return 'Word Document';
    if (['xls', 'xlsx'].includes(extension)) return 'Spreadsheet';
    
    return 'File';
}

// ===========================
// EVENT LISTENERS
// ===========================

// Keyboard shortcuts
document.addEventListener('keydown', (event) => {
    if (event.ctrlKey || event.metaKey) {
        if (event.key === 'k') {
            event.preventDefault();
            const searchInput = document.getElementById('searchInput');
            if (searchInput) searchInput.focus();
        }
    }
    
    if (event.key === 'Escape') {
        closeDeleteModal();
        const userMenu = document.getElementById('userMenu');
        if (userMenu) userMenu.classList.remove('open');
    }
});

// Handle Enter key in search
const searchInput = document.getElementById('searchInput');
if (searchInput) {
    searchInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            performSearch();
        }
    });
}

console.log('Smart File Management System initialized successfully!');
