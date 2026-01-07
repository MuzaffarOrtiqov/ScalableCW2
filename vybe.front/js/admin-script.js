// ============================================
// API Configuration
// ============================================
const API_BASE_URL = window.location.hostname === 'localhost'
    ? 'http://localhost:8080/api'
    : '/api';

// ============================================
// State Management
// ============================================
let currentUser = null;
let videos = [];
let currentFilter = 'all';
let selectedFile = null;

// ============================================
// DOM Elements
// ============================================
const adminDashboard = document.getElementById('adminDashboard');
const uploadForm = document.getElementById('uploadForm');
const uploadArea = document.getElementById('uploadArea');
const videoFileInput = document.getElementById('videoFile');
const videoPreview = document.getElementById('videoPreview');
const previewVideo = document.getElementById('previewVideo');
const removeVideoBtn = document.getElementById('removeVideo');
const videoList = document.getElementById('videoList');
const emptyState = document.getElementById('emptyState');
const uploadBtn = document.getElementById('uploadBtn');
const totalVideosEl = document.getElementById('totalVideos');

// ============================================
// Initialize
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    // Auth is handled by auth-middleware.js
    currentUser = getCurrentUser(); // Get user from auth-middleware

    if (currentUser) {
        initEventListeners();
        loadVideos();
        loadStats();
    }
});

// ============================================
// Event Listeners
// ============================================
function initEventListeners() {
    // Upload area click
    uploadArea.addEventListener('click', () => {
        videoFileInput.click();
    });

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        if (file && (file.type.startsWith('video/') || file.type.startsWith('image/'))) {
            handleFileSelect(file);
        } else {
            showToast('Please upload a valid video or image file', 'error');
        }
    });

    // File input change
    videoFileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            handleFileSelect(file);
        }
    });

    // Remove video
    removeVideoBtn.addEventListener('click', () => {
        selectedFile = null;
        videoFileInput.value = '';
        videoPreview.style.display = 'none';
        uploadArea.style.display = 'block';
    });

    // Upload form submit
    uploadForm.addEventListener('submit', handleUpload);

    // Filter tabs
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentFilter = tab.dataset.filter;
            filterVideos();
        });
    });
}

// ============================================
// File Handling
// ============================================
function handleFileSelect(file) {
    // Check file size (500MB max)
    const maxSize = 500 * 1024 * 1024;
    if (file.size > maxSize) {
        showToast('File size exceeds 500MB limit', 'error');
        return;
    }

    // Check file type
    if (!file.type.startsWith('video/') && !file.type.startsWith('image/')) {
        showToast('Please select a valid video or image file', 'error');
        return;
    }

    selectedFile = file;

    // Show preview based on file type
    const fileURL = URL.createObjectURL(file);

    if (file.type.startsWith('image/')) {
        // Display image preview
        previewVideo.style.display = 'none';
        let imgPreview = document.getElementById('previewImage');
        if (!imgPreview) {
            imgPreview = document.createElement('img');
            imgPreview.id = 'previewImage';
            imgPreview.style.maxWidth = '100%';
            imgPreview.style.borderRadius = '8px';
            videoPreview.insertBefore(imgPreview, removeVideoBtn);
        }
        imgPreview.src = fileURL;
        imgPreview.style.display = 'block';
        imgPreview.onload = () => {
            URL.revokeObjectURL(fileURL);
        };
    } else {
        // Display video preview
        const imgPreview = document.getElementById('previewImage');
        if (imgPreview) {
            imgPreview.style.display = 'none';
        }
        previewVideo.style.display = 'block';
        previewVideo.src = fileURL;
        previewVideo.onloadeddata = () => {
            URL.revokeObjectURL(fileURL);
        };
    }

    uploadArea.style.display = 'none';
    videoPreview.style.display = 'block';
}

// ============================================
// Upload Video/Image to Backend
// ============================================
async function handleUpload(e) {
    e.preventDefault();

    if (!selectedFile) {
        showToast('Please select a video or image file', 'error');
        return;
    }

    const title = document.getElementById('videoTitle').value.trim();
    const location = document.getElementById('videoLocation').value.trim();
    const caption = document.getElementById('videoCaption').value.trim();
    const category = document.getElementById('videoCategory').value;
    const tags = document.getElementById('videoTags').value.trim();

    if (!title) {
        showToast('Please enter a title', 'error');
        return;
    }

    // Show loading state
    setUploadButtonLoading(true);

    try {
        // Create FormData
        const formData = new FormData();
        formData.append('video', selectedFile);

        // Create video data object
        const videoData = {
            title: title,
            caption: caption || '',
            location: location || '',
            category: category,
            tags: tags || '',
            status: 'published' // Auto-publish
        };

        // Append as JSON blob
        const blob = new Blob([JSON.stringify(videoData)], {
            type: 'application/json'
        });
        formData.append('data', blob);

        // Upload to backend
        const response = await fetch(`${API_BASE_URL}/videos`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `Upload failed with status ${response.status}`);
        }

        const result = await response.json();
        console.log('Upload successful:', result);

        showToast('Media uploaded successfully!', 'success');

        // Reset form
        resetUploadForm();

        // Reload videos and stats
        await Promise.all([loadVideos(), loadStats()]);

    } catch (error) {
        console.error('Upload error:', error);
        showToast('Failed to upload media: ' + error.message, 'error');
    } finally {
        setUploadButtonLoading(false);
    }
}

// ============================================
// Load Videos from Backend
// ============================================
async function loadVideos() {
    try {
        const response = await fetch(`${API_BASE_URL}/videos`, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to load videos: ${response.status}`);
        }

        videos = await response.json();
        console.log('Loaded videos:', videos.length);
        renderVideos();

    } catch (error) {
        console.error('Load videos error:', error);
        showToast('Failed to load videos: ' + error.message, 'error');
        videoList.style.display = 'none';
        emptyState.style.display = 'flex';
    }
}

// ============================================
// Render Videos
// ============================================
function renderVideos() {
    if (videos.length === 0) {
        videoList.style.display = 'none';
        emptyState.style.display = 'flex';
        return;
    }

    videoList.style.display = 'grid';
    emptyState.style.display = 'none';

    // Filter videos based on current filter
    const filteredVideos = currentFilter === 'all'
        ? videos
        : videos.filter(v => v.status === currentFilter);

    if (filteredVideos.length === 0) {
        videoList.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #8e8e8e; padding: 40px;">No media found for this filter</p>';
        return;
    }

    videoList.innerHTML = filteredVideos.map(video => createVideoCardHTML(video)).join('');

    // Fetch and attach signed URLs to videos
    attachVideoSources();
}

// ============================================
// Create Video Card HTML
// ============================================
function createVideoCardHTML(video) {
    // Check multiple possible field names from backend
    const filename = video.originalFilename || video.filename || '';

    // Better image detection
    const isImage = (
        filename.match(/\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i) ||
        (video.mimeType && video.mimeType.startsWith('image/'))
    );

    console.log(`Video ${video.id}: filename="${filename}", isImage=${isImage}`); // Debug log

    return `
        <div class="video-card" data-id="${video.id}">
            <div class="video-thumbnail">
                ${isImage ? `
                    <img
                        class="media-player image-player"
                        data-id="${video.id}"
                        alt="${escapeHtml(video.title)}"
                        style="width: 100%; height: 100%; object-fit: cover;"
                    />
                ` : `
                    <video
                        class="media-player video-player"
                        data-id="${video.id}"
                        preload="metadata"
                        muted
                    ></video>
                    <div class="video-duration">--:--</div>
                    <div class="play-overlay">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="white">
                            <path d="M8 5v14l11-7z"/>
                        </svg>
                    </div>
                `}
            </div>

            <div class="video-info">
                <h3 class="video-title">${escapeHtml(video.title)}</h3>
                <p class="video-caption" style="font-size: 14px; color: #8e8e8e; margin: 8px 0;">
                    ${escapeHtml(video.caption || 'No caption')}
                </p>

                <div class="video-meta">
                    <span>📍 ${escapeHtml(video.location || 'No location')}</span>
                    <span>📂 ${escapeHtml(video.category)}</span>
                </div>

                <div class="video-meta" style="margin-top: 8px;">
                    <span>👁️ ${formatNumber(video.views || 0)}</span>
                    <span>❤️ ${formatNumber(video.likes || 0)}</span>
                </div>

                <div class="video-status" style="margin-top: 8px;">
                    <span class="status-badge status-${(video.status || 'draft').toLowerCase()}">${video.status || 'draft'}</span>
                    ${isImage ? '<span class="status-badge" style="background: #4CAF50;">Image</span>' : '<span class="status-badge" style="background: #2196F3;">Video</span>'}
                </div>

                <div class="video-actions" style="margin-top: 12px;">
                    <button class="btn-small" onclick="editVideo('${video.id}')">Edit</button>
                    <button class="btn-small btn-danger" onclick="deleteVideo('${video.id}')">Delete</button>
                </div>
            </div>
        </div>
    `;
}

// ============================================
// Attach Video Sources
// ============================================
async function attachVideoSources() {
    const mediaElements = document.querySelectorAll('.media-player');

    for (const mediaEl of mediaElements) {
        const videoId = mediaEl.dataset.id;

        try {
            // FIX: Correct endpoint path - should be /videos/{id}/stream
            const response = await fetch(`${API_BASE_URL}/videos/stream/${videoId}`);

            if (!response.ok) {
                throw new Error(`Failed to load media stream: ${response.status}`);
            }

            const data = await response.json();
            console.log(`Fetched stream URL for ${videoId}:`, data.url);

            // Add event listeners BEFORE setting src
            if (mediaEl.tagName === 'IMG') {
                mediaEl.addEventListener('load', () => {
                    console.log(`✅ Image loaded successfully: ${videoId}`);
                });

                mediaEl.addEventListener('error', (e) => {
                    console.error('❌ Image load error:', videoId, e);
                    const thumbnail = mediaEl.closest('.video-thumbnail');
                    thumbnail.innerHTML = `
                        <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #000; color: #fff;">
                            <p>Image unavailable</p>
                        </div>
                    `;
                });
            }

            if (mediaEl.tagName === 'VIDEO') {
                mediaEl.addEventListener('loadedmetadata', () => {
                    const duration = formatDuration(mediaEl.duration);
                    const durationEl = mediaEl.closest('.video-thumbnail').querySelector('.video-duration');

                    if (durationEl) {
                        durationEl.textContent = duration;
                    }
                    console.log(`✅ Video loaded successfully: ${videoId}`);
                });

                mediaEl.addEventListener('error', (e) => {
                    console.error('❌ Video load error:', videoId, e);
                    const thumbnail = mediaEl.closest('.video-thumbnail');
                    thumbnail.innerHTML = `
                        <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #000; color: #fff;">
                            <p>Video unavailable</p>
                        </div>
                    `;
                });
            }

            // NOW set the src (after listeners are attached)
            mediaEl.src = data.url;

        } catch (error) {
            console.error(`❌ Failed to fetch stream URL for ${videoId}:`, error);
            const thumbnail = mediaEl.closest('.video-thumbnail');
            thumbnail.innerHTML = `
                <div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #000; color: #fff;">
                    <p>Error loading media</p>
                </div>
            `;
        }
    }
}

// ============================================
// Filter Videos
// ============================================
function filterVideos() {
    renderVideos();
}

// ============================================
// Edit Video
// ============================================
async function editVideo(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/videos/${id}`);

        if (!response.ok) {
            throw new Error('Failed to load media details');
        }

        const video = await response.json();

        // Populate form with video data
        document.getElementById('videoTitle').value = video.title || '';
        document.getElementById('videoCaption').value = video.caption || '';
        document.getElementById('videoLocation').value = video.location || '';
        document.getElementById('videoCategory').value = video.category || 'nature';
        document.getElementById('videoTags').value = video.tags || '';

        // Scroll to form
        window.scrollTo({ top: 0, behavior: 'smooth' });

        showToast('Loaded media for editing. Update fields and submit.', 'info');

        // Store edit ID for update
        uploadForm.dataset.editId = id;
        uploadBtn.querySelector('.btn-text').textContent = 'Update Media';

        // Modify form submit to update instead of create
        uploadForm.removeEventListener('submit', handleUpload);
        uploadForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await handleUpdate(id);
        }, { once: true });

    } catch (error) {
        console.error('Edit error:', error);
        showToast('Failed to load media details: ' + error.message, 'error');
    }
}

// ============================================
// Update Video
// ============================================
async function handleUpdate(id) {
    const title = document.getElementById('videoTitle').value.trim();
    const location = document.getElementById('videoLocation').value.trim();
    const caption = document.getElementById('videoCaption').value.trim();
    const category = document.getElementById('videoCategory').value;
    const tags = document.getElementById('videoTags').value.trim();

    if (!title) {
        showToast('Please enter a title', 'error');
        return;
    }

    setUploadButtonLoading(true);

    try {
        const response = await fetch(`${API_BASE_URL}/videos/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title,
                caption,
                location,
                category,
                tags,
                status: 'PUBLISHED'
            })
        });

        if (!response.ok) {
            throw new Error('Update failed');
        }

        showToast('Media updated successfully!', 'success');

        // Reset form
        resetUploadForm();

        // Re-attach original upload handler
        uploadForm.removeEventListener('submit', handleUpdate);
        uploadForm.addEventListener('submit', handleUpload);

        // Reload videos
        await Promise.all([loadVideos(), loadStats()]);

    } catch (error) {
        console.error('Update error:', error);
        showToast('Failed to update media: ' + error.message, 'error');
    } finally {
        setUploadButtonLoading(false);
    }
}

// ============================================
// Delete Video
// ============================================
async function deleteVideo(id) {
    if (!confirm('Are you sure you want to delete this media? This action cannot be undone.')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/videos/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Delete failed');
        }

        showToast('Media deleted successfully', 'success');

        // Reload videos and stats
        await Promise.all([loadVideos(), loadStats()]);

    } catch (error) {
        console.error('Delete error:', error);
        showToast('Failed to delete media: ' + error.message, 'error');
    }
}

// ============================================
// Load Stats from Backend
// ============================================
async function loadStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/videos/stats`);

        if (!response.ok) {
            throw new Error('Failed to load stats');
        }

        const stats = await response.json();

        // Update stats in UI
        totalVideosEl.textContent = stats.totalVideos || 0;
        const statCards = document.querySelectorAll('.stat-card h3');
        if (statCards[1]) statCards[1].textContent = formatNumber(stats.totalViews || 0);
        if (statCards[2]) statCards[2].textContent = formatNumber(stats.totalLikes || 0);

    } catch (error) {
        console.error('Load stats error:', error);
        totalVideosEl.textContent = '0';
    }
}

// ============================================
// Utility Functions
// ============================================
function setUploadButtonLoading(loading) {
    const btnText = uploadBtn.querySelector('.btn-text');
    const btnLoader = uploadBtn.querySelector('.btn-loader');

    if (loading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline-flex';
        uploadBtn.disabled = true;
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        uploadBtn.disabled = false;
    }
}

function resetUploadForm() {
    uploadForm.reset();
    selectedFile = null;
    videoPreview.style.display = 'none';
    uploadArea.style.display = 'flex';
    delete uploadForm.dataset.editId;
    uploadBtn.querySelector('.btn-text').textContent = 'Upload Media';

    // Hide both preview elements
    const previewVideo = document.getElementById('previewVideo');
    const previewImage = document.getElementById('previewImage');
    if (previewVideo) previewVideo.style.display = 'none';
    if (previewImage) previewImage.style.display = 'none';
}

function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

function formatDuration(seconds) {
    if (isNaN(seconds)) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast toast-${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============================================
// Make functions global for onclick handlers
// ============================================
window.editVideo = editVideo;
window.deleteVideo = deleteVideo;