// ============================================
// State Management
// ============================================
let allVideos = [];
let filteredVideos = [];
let videoCache = new Map();
let likedVideos = new Set();
let viewedVideos = new Set();

// ============================================
// Initialize Application
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

async function initializeApp() {
    try {
        Utils.showLoading(true);

        // Load videos from backend
        await loadVideos();

        // Initialize UI interactions
        initSearchFunctionality();
        initStoryInteractions();
        initSmoothScrollForStories();

        Utils.showLoading(false);

        if (allVideos.length > 0) {
            Utils.showToast('Media loaded successfully', 'success');
        }
    } catch (error) {
        console.error('Initialization error:', error);
        Utils.showLoading(false);
        showErrorState(error.message || 'Failed to load application');
    }
}

// ============================================
// Load Videos from Backend
// ============================================
async function loadVideos(retryCount = 0) {
    try {
        // Fetch all videos from backend
        const videos = await VideoAPI.getAll();

        allVideos = videos || [];
        filteredVideos = [...allVideos];

        if (allVideos.length === 0) {
            showEmptyState();
            return;
        }

        await renderVideoFeed();
        initializeVideoInteractions();

    } catch (error) {
        console.error('Error loading videos:', error);

        // Retry logic
        if (retryCount < CONFIG.MAX_RETRIES) {
            console.log(`Retrying... (${retryCount + 1}/${CONFIG.MAX_RETRIES})`);
            await new Promise(resolve => setTimeout(resolve, CONFIG.RETRY_DELAY));
            return loadVideos(retryCount + 1);
        }

        throw new Error('Failed to load videos. Please check if the backend is running.');
    }
}

// ============================================
// Render Video Feed
// ============================================
async function renderVideoFeed() {
    const feedContainer = document.getElementById('video-feed');

    if (!feedContainer) {
        console.error('Feed container not found');
        return;
    }

    feedContainer.innerHTML = '';

    if (filteredVideos.length === 0) {
        feedContainer.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #8e8e8e;">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 16px; opacity: 0.5;">
                    <circle cx="11" cy="11" r="8"></circle>
                    <path d="m21 21-4.35-4.35"></path>
                </svg>
                <p style="font-size: 16px;">No media found matching your search.</p>
            </div>
        `;
        return;
    }

    for (const video of filteredVideos) {
        try {
            const videoCard = await createVideoCard(video);
            feedContainer.appendChild(videoCard);
        } catch (error) {
            console.error(`Error rendering media ${video.id}:`, error);
        }
    }
}

// ============================================
// Detect Media Type
// ============================================
function getMediaType(filename) {
    if (!filename) return 'video';

    const extension = filename.toLowerCase().split('.').pop();
    const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'];
    const videoExtensions = ['mp4', 'webm', 'ogg', 'mov', 'avi'];

    if (imageExtensions.includes(extension)) {
        return 'image';
    } else if (videoExtensions.includes(extension)) {
        return 'video';
    }

    return 'video'; // default
}

// ============================================
// Create Video Card (Now supports images too)
// ============================================
// Updated createVideoCard function with comments section
async function createVideoCard(video) {
    const article = document.createElement('article');
    article.className = 'video-post';
    article.dataset.videoId = video.id;

    const mediaType = getMediaType(video.originalFilename || video.path);

    let mediaUrl = '';
    try {
        if (videoCache.has(video.id)) {
            mediaUrl = videoCache.get(video.id);
        } else {
            const response = await VideoAPI.getStreamUrl(video.id);
            mediaUrl = response.url;
            videoCache.set(video.id, mediaUrl);

            setTimeout(() => {
                videoCache.delete(video.id);
            }, CONFIG.CACHE_DURATION);
        }
    } catch (error) {
        console.error(`Failed to load media URL for ${video.id}:`, error);
        mediaUrl = '';
    }

    const isLiked = likedVideos.has(video.id);

    let mediaElement = '';
    if (mediaType === 'image') {
        mediaElement = mediaUrl ? `
            <img class="media-image" 
                 src="${mediaUrl}" 
                 alt="${escapeHtml(video.title || 'Image')}" 
                 data-video-id="${video.id}">
        ` : `
            <div class="video-error">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>Image unavailable</p>
            </div>
        `;
    } else {
        mediaElement = mediaUrl ? `
            <video class="video-player" loop muted playsinline data-video-id="${video.id}">
                <source src="${mediaUrl}" type="video/mp4">
                Your browser does not support the video tag.
            </video>
            <button class="play-btn" aria-label="Play video">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="white">
                    <path d="M8 5v14l11-7z"/>
                </svg>
            </button>
            <div class="video-duration">--:--</div>
        ` : `
            <div class="video-error">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>Video unavailable</p>
            </div>
        `;
    }

    article.innerHTML = `
        <div class="post-header">
            <div class="user-info">
                <div class="user-avatar"></div>
                <div>
                    <h3 class="username">vybe_admin</h3>
                    <p class="location">${escapeHtml(video.location || 'Unknown Location')}</p>
                </div>
            </div>
            <button class="more-btn" aria-label="More options">•••</button>
        </div>
        <div class="video-container" data-media-type="${mediaType}">
            ${mediaElement}
        </div>
        <div class="post-actions">
            <div class="left-actions">
                <button class="action-btn like-btn ${isLiked ? 'liked' : ''}" data-video-id="${video.id}" aria-label="Like">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                </button>
                <button class="action-btn comment-btn" aria-label="Comment" onclick="scrollToComments('${video.id}')">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
                    </svg>
                </button>
                <button class="action-btn share-btn" aria-label="Share">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="22" y1="2" x2="11" y2="13"></line>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                    </svg>
                </button>
            </div>
            <button class="action-btn bookmark-btn" aria-label="Bookmark">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
                </svg>
            </button>
        </div>
        <div class="post-info">
            <p class="likes">${Utils.formatNumber(video.likes || 0)} likes</p>
            <p class="caption"><strong>vybe_admin</strong> ${escapeHtml(video.caption || video.title || '')}</p>
            ${video.tags ? `<p class="tags">${Utils.formatTags(video.tags)}</p>` : ''}
            <p class="post-time">${Utils.getTimeAgo(video.createdAt)}</p>
        </div>
        ${createCommentsSection(video.id)}
    `;

    return article;
}

// ============================================
// Initialize Video Interactions
// ============================================
function initializeVideoInteractions() {
    const videoPosts = document.querySelectorAll('.video-post');

    videoPosts.forEach(post => {
        const videoContainer = post.querySelector('.video-container');
        const mediaType = videoContainer?.dataset.mediaType;
        const video = post.querySelector('.video-player');
        const image = post.querySelector('.media-image');
        const playBtn = post.querySelector('.play-btn');
        const likeBtn = post.querySelector('.like-btn');
        const shareBtn = post.querySelector('.share-btn');
        const bookmarkBtn = post.querySelector('.bookmark-btn');
        const moreBtn = post.querySelector('.more-btn');
        const videoId = post.dataset.videoId;

        // Handle video-specific interactions
        if (mediaType === 'video' && video) {
            // Load video metadata
            video.addEventListener('loadedmetadata', () => {
                const durationEl = post.querySelector('.video-duration');
                if (durationEl) {
                    durationEl.textContent = Utils.formatDuration(video.duration);
                }
            });

            // Handle video load errors
            video.addEventListener('error', (e) => {
                console.error(`Video load error for ${videoId}:`, e);
                videoContainer.innerHTML = `
                    <div class="video-error">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        <p>Unable to load video</p>
                    </div>
                `;
            });

            // Play/Pause functionality
            if (playBtn) {
                playBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    togglePlayPause(video, playBtn);
                });
            }

            videoContainer.addEventListener('click', (e) => {
                if (!e.target.closest('.play-btn')) {
                    togglePlayPause(video, playBtn);
                }
            });

            // Track video view when played
            let viewTracked = false;
            video.addEventListener('play', async () => {
                if (!viewTracked && !viewedVideos.has(videoId)) {
                    await trackVideoView(videoId);
                    viewTracked = true;
                    viewedVideos.add(videoId);
                }

                // Pause other videos
                videoPosts.forEach(otherPost => {
                    const otherVideo = otherPost.querySelector('.video-player');
                    const otherPlayBtn = otherPost.querySelector('.play-btn');
                    if (otherVideo && otherVideo !== video && !otherVideo.paused) {
                        otherVideo.pause();
                        if (otherPlayBtn) otherPlayBtn.classList.remove('playing');
                    }
                });
            });

            // Update play button visibility
            video.addEventListener('play', () => {
                if (playBtn) playBtn.classList.add('playing');
            });

            video.addEventListener('pause', () => {
                if (playBtn) playBtn.classList.remove('playing');
            });
        }

        // Handle image-specific interactions
        if (mediaType === 'image' && image) {
            // Handle image load errors
            image.addEventListener('error', (e) => {
                console.error(`Image load error for ${videoId}:`, e);
                videoContainer.innerHTML = `
                    <div class="video-error">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="12" y1="8" x2="12" y2="12"></line>
                            <line x1="12" y1="16" x2="12.01" y2="16"></line>
                        </svg>
                        <p>Unable to load image</p>
                    </div>
                `;
            });

            // Track image view when loaded
            image.addEventListener('load', async () => {
                if (!viewedVideos.has(videoId)) {
                    await trackVideoView(videoId);
                    viewedVideos.add(videoId);
                }
            });
        }

        // Common interactions for both media types
        // Like functionality
        likeBtn.addEventListener('click', async () => {
            await handleLike(videoId, likeBtn, post);
        });

        // Share functionality
        shareBtn.addEventListener('click', () => {
            handleShare(videoId, mediaType === 'video' ? video : image);
        });

        // Bookmark functionality
        bookmarkBtn.addEventListener('click', () => {
            handleBookmark(videoId, bookmarkBtn);
        });

        // More button functionality
        moreBtn.addEventListener('click', () => {
            showVideoOptions(videoId, mediaType === 'video' ? video : image);
        });
    });

    // Initialize Intersection Observer
    initializeIntersectionObserver();
}

// Rest of the functions remain the same...
// (handleLike, trackVideoView, handleShare, handleBookmark, togglePlayPause,
//  animateLike, initializeIntersectionObserver, initSearchFunctionality, etc.)

// ============================================
// Handle Like with Backend Sync
// ============================================
async function handleLike(videoId, likeBtn, post) {
    const likesElement = post.querySelector('.likes');
    const isLiked = likeBtn.classList.contains('liked');
    const currentLikes = parseInt(likesElement.textContent.replace(/[^0-9]/g, '')) || 0;

    try {
        // Optimistic UI update
        likeBtn.classList.toggle('liked');

        if (!isLiked) {
            likesElement.textContent = Utils.formatNumber(currentLikes + 1) + ' likes';
            animateLike(likeBtn);
            likedVideos.add(videoId);

            // Send like to backend
            await VideoAPI.incrementLikes(videoId);
        } else {
            likesElement.textContent = Utils.formatNumber(Math.max(0, currentLikes - 1)) + ' likes';
            likedVideos.delete(videoId);

            // Send unlike to backend
            await VideoAPI.decrementLikes(videoId);
        }

    } catch (error) {
        console.error('Error updating like:', error);
        Utils.showToast('Failed to update like', 'error');

        // Revert UI on error
        likeBtn.classList.toggle('liked');
        likesElement.textContent = Utils.formatNumber(isLiked ? currentLikes : currentLikes - 1) + ' likes';

        if (isLiked) {
            likedVideos.add(videoId);
        } else {
            likedVideos.delete(videoId);
        }
    }
}

// ============================================
// Track Video View
// ============================================
async function trackVideoView(videoId) {
    try {
        await VideoAPI.incrementViews(videoId);
        console.log(`View tracked for video: ${videoId}`);
    } catch (error) {
        console.error('Error tracking view:', error);
    }
}

// ============================================
// Handle Share
// ============================================
function handleShare(videoId, video) {
    const shareData = {
        title: 'Check out this video on Vybe!',
        text: 'Watch this amazing video',
        url: window.location.href
    };

    if (navigator.share) {
        navigator.share(shareData)
            .then(() => Utils.showToast('Shared successfully!', 'success'))
            .catch((error) => {
                if (error.name !== 'AbortError') {
                    copyToClipboard(window.location.href);
                }
            });
    } else {
        copyToClipboard(window.location.href);
    }
}

// ============================================
// Handle Bookmark
// ============================================
function handleBookmark(videoId, bookmarkBtn) {
    bookmarkBtn.classList.toggle('bookmarked');
    const isBookmarked = bookmarkBtn.classList.contains('bookmarked');

    if (isBookmarked) {
        Utils.showToast('Video saved!', 'success');
        bookmarkBtn.querySelector('svg').style.fill = 'currentColor';
    } else {
        Utils.showToast('Video removed from saved', 'info');
        bookmarkBtn.querySelector('svg').style.fill = 'none';
    }
}

// ============================================
// Toggle Play/Pause
// ============================================
function togglePlayPause(video, playBtn) {
    if (video.paused) {
        video.play().catch(error => {
            console.error('Error playing video:', error);
            Utils.showToast('Unable to play video', 'error');
        });
    } else {
        video.pause();
    }
}

// ============================================
// Animate Like Button
// ============================================
function animateLike(btn) {
    btn.style.transform = 'scale(1.3)';
    setTimeout(() => {
        btn.style.transform = 'scale(1)';
    }, 200);
}

// ============================================
// Intersection Observer for Auto-pause
// ============================================
function initializeIntersectionObserver() {
    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: CONFIG.VIDEO_AUTOPLAY_THRESHOLD
    };

    const videoObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            const video = entry.target.querySelector('.video-player');
            const playBtn = entry.target.querySelector('.play-btn');

            if (video && !entry.isIntersecting && !video.paused) {
                video.pause();
                if (playBtn) playBtn.classList.remove('playing');
            }
        });
    }, observerOptions);

    document.querySelectorAll('.video-post').forEach(post => {
        videoObserver.observe(post);
    });
}

// ============================================
// Search Functionality
// ============================================
function initSearchFunctionality() {
    const searchInput = document.getElementById('search-input');

    if (searchInput) {
        const debouncedSearch = Utils.debounce((searchTerm) => {
            filterVideos(searchTerm);
        }, 300);

        searchInput.addEventListener('input', (e) => {
            const searchTerm = e.target.value.toLowerCase().trim();
            debouncedSearch(searchTerm);
        });
    }
}

function filterVideos(searchTerm) {
    if (!searchTerm) {
        filteredVideos = [...allVideos];
    } else {
        filteredVideos = allVideos.filter(video => {
            const caption = (video.caption || '').toLowerCase();
            const title = (video.title || '').toLowerCase();
            const location = (video.location || '').toLowerCase();
            const tags = (video.tags || '').toLowerCase();

            return caption.includes(searchTerm) ||
                title.includes(searchTerm) ||
                location.includes(searchTerm) ||
                tags.includes(searchTerm);
        });
    }

    renderVideoFeed();

    if (filteredVideos.length > 0) {
        initializeVideoInteractions();
    }
}

// ============================================
// Show Video Options
// ============================================
function showVideoOptions(videoId, video) {
    const options = [
        { label: 'Copy Link', action: () => copyToClipboard(window.location.href) },
        { label: 'Report', action: () => Utils.showToast('Report feature coming soon', 'info') },
        { label: 'Not Interested', action: () => Utils.showToast('Feedback recorded', 'success') }
    ];

    // Simple alert for now - can be replaced with a modal
    const choice = prompt('Options:\n1. Copy Link\n2. Report\n3. Not Interested\n\nEnter number:');

    if (choice && options[parseInt(choice) - 1]) {
        options[parseInt(choice) - 1].action();
    }
}

// ============================================
// Story Interactions
// ============================================
function initStoryInteractions() {
    const storyItems = document.querySelectorAll('.story-item');
    storyItems.forEach(story => {
        story.addEventListener('click', () => {
            const storyName = story.querySelector('.story-name')?.textContent;
            console.log('Story clicked:', storyName);
            Utils.showToast('Story feature coming soon!', 'info');
        });
    });
}

// ============================================
// Smooth Scroll for Stories
// ============================================
function initSmoothScrollForStories() {
    const storiesContainer = document.getElementById('stories-section');
    if (!storiesContainer) return;

    let isDown = false;
    let startX;
    let scrollLeft;

    storiesContainer.addEventListener('mousedown', (e) => {
        isDown = true;
        storiesContainer.style.cursor = 'grabbing';
        startX = e.pageX - storiesContainer.offsetLeft;
        scrollLeft = storiesContainer.scrollLeft;
    });

    storiesContainer.addEventListener('mouseleave', () => {
        isDown = false;
        storiesContainer.style.cursor = 'grab';
    });

    storiesContainer.addEventListener('mouseup', () => {
        isDown = false;
        storiesContainer.style.cursor = 'grab';
    });

    storiesContainer.addEventListener('mousemove', (e) => {
        if (!isDown) return;
        e.preventDefault();
        const x = e.pageX - storiesContainer.offsetLeft;
        const walk = (x - startX) * 2;
        storiesContainer.scrollLeft = scrollLeft - walk;
    });
}

// Scroll to comments section
function scrollToComments(videoId) {
    const section = document.querySelector(`.comments-section[data-video-id="${videoId}"]`);
    const btn = section.querySelector('.show-comments-btn');
    const content = section.querySelector('.comments-content');

    // Open comments if collapsed
    if (content.classList.contains('comments-collapsed')) {
        btn.click();
    }

    // Scroll to comments
    section.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

    // Focus on comment input if user role
    setTimeout(() => {
        const input = section.querySelector('.comment-input');
        if (input) {
            input.focus();
        }
    }, 500);
}

// Make function global
window.scrollToComments = scrollToComments;

// ============================================
// Utility Functions
// ============================================
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text)
            .then(() => Utils.showToast('Link copied to clipboard!', 'success'))
            .catch(() => fallbackCopyToClipboard(text));
    } else {
        fallbackCopyToClipboard(text);
    }
}

function fallbackCopyToClipboard(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    try {
        document.execCommand('copy');
        Utils.showToast('Link copied to clipboard!', 'success');
    } catch (err) {
        Utils.showToast('Failed to copy link', 'error');
    }
    document.body.removeChild(textarea);
}

function showEmptyState() {
    const feedContainer = document.getElementById('video-feed');
    if (feedContainer) {
        feedContainer.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #8e8e8e;">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 16px; opacity: 0.5;">
                    <rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18"></rect>
                    <line x1="7" y1="2" x2="7" y2="22"></line>
                    <line x1="17" y1="2" x2="17" y2="22"></line>
                </svg>
                <h3 style="font-size: 18px; margin-bottom: 8px; color: #262626;">No videos yet</h3>
                <p style="font-size: 14px;">Check back later for new content!</p>
            </div>
        `;
    }
}

function showErrorState(message) {
    const feedContainer = document.getElementById('video-feed');
    if (feedContainer) {
        feedContainer.innerHTML = `
            <div style="text-align: center; padding: 60px 20px; color: #ed4956;">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="margin-bottom: 16px;">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <h3 style="font-size: 18px; margin-bottom: 8px; color: #262626;">Error Loading Videos</h3>
                <p style="font-size: 14px; margin-bottom: 16px;">${escapeHtml(message)}</p>
                <button onclick="location.reload()" style="padding: 10px 20px; background: #0095f6; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 600;">
                    Reload Page
                </button>
            </div>
        `;
    }
}