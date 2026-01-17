// ============================================
// API Configuration for Azure Deployment
// ============================================
const CONFIG = {
    // API Base URL - Azure Container Apps Backend
    API_BASE_URL: 'http://4.233.136.94:8080/api',

    // Request timeout (milliseconds)
    REQUEST_TIMEOUT: 30000,

    // Retry configuration
    MAX_RETRIES: 3,
    RETRY_DELAY: 1000,

    // Video player settings
    VIDEO_AUTOPLAY_THRESHOLD: 0.5, // 50% of video visible

    // Pagination
    VIDEOS_PER_PAGE: 10,

    // Cache duration (milliseconds)
    CACHE_DURATION: 5 * 60 * 1000, // 5 minutes
};

// ============================================
// API Helper Functions
// ============================================
const API = {
    /**
     * Make a fetch request with timeout and retry logic
     */
    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), CONFIG.REQUEST_TIMEOUT);

        const defaultOptions = {
            signal: controller.signal,
            headers: {
                'Accept': 'application/json',
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, { ...defaultOptions, ...options });
            clearTimeout(timeoutId);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            clearTimeout(timeoutId);

            if (error.name === 'AbortError') {
                throw new Error('Request timeout - please check your connection');
            }
            throw error;
        }
    },

    /**
     * GET request
     */
    async get(endpoint, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'GET' });
    },

    /**
     * POST request
     */
    async post(endpoint, data = null, isFormData = false) {
        const options = {
            method: 'POST',
            body: isFormData ? data : JSON.stringify(data)
        };

        if (!isFormData) {
            options.headers = {
                'Content-Type': 'application/json'
            };
        }

        return this.request(endpoint, options);
    },

    /**
     * PUT request
     */
    async put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
    },

    /**
     * DELETE request
     */
    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// ============================================
// Video API Endpoints
// ============================================
const VideoAPI = {
    /**
     * Get all videos with optional status filter
     */
    async getAll(status = null) {
        const params = status ? { status } : {};
        return API.get('/videos', params);
    },

    /**
     * Get video by ID
     */
    async getById(id) {
        return API.get(`/videos/${id}`);
    },

    /**
     * Get video stream URL
     */
    async getStreamUrl(id) {
        return API.get(`/videos/${id}/stream`);
    },

    /**
     * Increment video views
     */
    async incrementViews(id) {
        return API.post(`/videos/${id}/view`);
    },

    /**
     * Increment video likes
     */
    async incrementLikes(id) {
        return API.post(`/videos/${id}/like`);
    },

    /**
     * Decrement video likes (unlike)
     */
    async decrementLikes(id) {
        return API.post(`/videos/${id}/unlike`);
    },

    /**
     * Get video statistics
     */
    async getStats() {
        return API.get('/videos/stats');
    },

    /**
     * Upload video (if implementing upload feature)
     */
    async upload(formData) {
        return API.post('/videos', formData, true);
    },

    /**
     * Update video
     */
    async update(id, data) {
        return API.put(`/videos/${id}`, data);
    },

    /**
     * Delete video
     */
    async delete(id) {
        return API.delete(`/videos/${id}`);
    }
};

// ============================================
// Utility Functions
// ============================================
const Utils = {
    /**
     * Show toast notification
     */
    showToast(message, type = 'info') {
        const toast = document.getElementById('toast');
        if (!toast) return;

        toast.textContent = message;
        toast.className = `toast toast-${type} show`;

        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    },

    /**
     * Format number (1234 -> 1.2K)
     */
    formatNumber(num) {
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        }
        if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    },

    /**
     * Format duration (seconds -> MM:SS)
     */
    formatDuration(seconds) {
        if (isNaN(seconds)) return '--:--';
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins}:${secs.toString().padStart(2, '0')}`;
    },

    /**
     * Format tags
     */
    formatTags(tagsString) {
        if (!tagsString) return '';
        return tagsString.split(',')
            .map(tag => `#${tag.trim()}`)
            .join(' ');
    },

    /**
     * Get time ago string
     */
    getTimeAgo(dateString) {
        if (!dateString) return 'just now';

        const date = new Date(dateString);
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);

        if (diffInSeconds < 60) return 'just now';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)} minutes ago`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)} hours ago`;
        if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)} days ago`;

        return date.toLocaleDateString();
    },

    /**
     * Debounce function
     */
    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    /**
     * Show loading overlay
     */
    showLoading(show = true) {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.style.display = show ? 'flex' : 'none';
        }
    }
};