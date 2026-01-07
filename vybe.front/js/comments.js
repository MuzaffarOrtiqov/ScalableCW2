// ============================================
// Comments API
// ============================================
const CommentsAPI = {
    /**
     * Add a new comment
     */
    async addComment(videoId, username, content) {
        return API.post('/comments', {
            videoId: videoId,
            username: username,
            content: content
        });
    },

    /**
     * Get comments for a video
     */
    async getComments(videoId) {
        return API.get(`/comments/video/${videoId}`);
    },

    /**
     * Get comment count
     */
    async getCommentCount(videoId) {
        return API.get(`/comments/video/${videoId}/count`);
    },

    /**
     * Delete a comment
     */
    async deleteComment(commentId) {
        return API.delete(`/comments/${commentId}`);
    },

    /**
     * Like a comment
     */
    async likeComment(commentId) {
        return API.post(`/comments/${commentId}/like`);
    },

    /**
     * Unlike a comment
     */
    async unlikeComment(commentId) {
        return API.post(`/comments/${commentId}/unlike`);
    }
};

// ============================================
// Comments State Management
// ============================================
let commentsCache = new Map();
let likedComments = new Set();

// ============================================
// Create Comments Section HTML
// ============================================
function createCommentsSection(videoId) {
    const currentUser = getCurrentUser();

    return `
        <div class="comments-section" data-video-id="${videoId}">
            <button class="show-comments-btn" onclick="toggleComments('${videoId}')">
                <span class="comments-count">View comments</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="6 9 12 15 18 9"></polyline>
                </svg>
            </button>
            
            <div class="comments-content comments-collapsed">
                ${currentUser && currentUser.role === 'user' ? `
                    <div class="add-comment">
                        <textarea 
                            class="comment-input" 
                            placeholder="Add a comment..."
                            rows="1"
                            maxlength="500"
                            data-video-id="${videoId}"
                        ></textarea>
                        <button class="btn-post-comment" onclick="postComment('${videoId}')" disabled>
                            Post
                        </button>
                    </div>
                ` : ''}
                
                <div class="comments-list" id="comments-list-${videoId}">
                    <div class="loading-comments">Loading comments...</div>
                </div>
            </div>
        </div>
    `;
}

// ============================================
// Toggle Comments Visibility
// ============================================
async function toggleComments(videoId) {
    const section = document.querySelector(`.comments-section[data-video-id="${videoId}"]`);
    const btn = section.querySelector('.show-comments-btn');
    const content = section.querySelector('.comments-content');

    btn.classList.toggle('expanded');
    content.classList.toggle('comments-collapsed');

    // Load comments if expanded and not already loaded
    if (!content.classList.contains('comments-collapsed') && !commentsCache.has(videoId)) {
        await loadComments(videoId);
    }
}

// ============================================
// Load Comments for a Video
// ============================================
async function loadComments(videoId) {
    const listContainer = document.getElementById(`comments-list-${videoId}`);

    try {
        // Check cache first
        let comments;
        if (commentsCache.has(videoId)) {
            comments = commentsCache.get(videoId);
        } else {
            comments = await CommentsAPI.getComments(videoId);
            commentsCache.set(videoId, comments);
        }

        renderComments(videoId, comments);
        updateCommentCount(videoId, comments.length);

    } catch (error) {
        console.error('Error loading comments:', error);
        listContainer.innerHTML = `
            <div class="comments-empty">
                <p style="color: #ed4956;">Failed to load comments</p>
            </div>
        `;
    }
}

// ============================================
// Render Comments
// ============================================
function renderComments(videoId, comments) {
    const listContainer = document.getElementById(`comments-list-${videoId}`);
    const currentUser = getCurrentUser();

    if (comments.length === 0) {
        listContainer.innerHTML = `
            <div class="comments-empty">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"></path>
                </svg>
                <p>No comments yet. Be the first to comment!</p>
            </div>
        `;
        return;
    }

    listContainer.innerHTML = comments.map(comment => {
        const isLiked = likedComments.has(comment.id);
        const canDelete = currentUser && (currentUser.username === comment.username || currentUser.role === 'admin');

        return `
            <div class="comment-item" data-comment-id="${comment.id}">
                <div class="comment-avatar"></div>
                <div class="comment-content">
                    <div class="comment-header">
                        <span class="comment-username">${escapeHtml(comment.username)}</span>
                        <span class="comment-time">${Utils.getTimeAgo(comment.createdAt)}</span>
                    </div>
                    <p class="comment-text">${escapeHtml(comment.content)}</p>
                    <div class="comment-actions">
                        <button class="comment-action-btn ${isLiked ? 'liked' : ''}" onclick="toggleCommentLike(${comment.id}, '${videoId}')">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="${isLiked ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="2">
                                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                            </svg>
                            ${comment.likes > 0 ? `<span class="comment-like-count">${comment.likes}</span>` : ''}
                        </button>
                        ${canDelete ? `
                            <button class="comment-action-btn delete-comment-btn" onclick="deleteComment(${comment.id}, '${videoId}')">
                                Delete
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// ============================================
// Post a Comment
// ============================================
async function postComment(videoId) {
    const currentUser = getCurrentUser();
    if (!currentUser || currentUser.role !== 'user') {
        Utils.showToast('Only users can post comments', 'error');
        return;
    }

    const textarea = document.querySelector(`.comment-input[data-video-id="${videoId}"]`);
    const content = textarea.value.trim();

    if (!content) {
        Utils.showToast('Please enter a comment', 'error');
        return;
    }

    const btn = textarea.parentElement.querySelector('.btn-post-comment');
    btn.disabled = true;
    btn.textContent = 'Posting...';

    try {
        const comment = await CommentsAPI.addComment(
            videoId,
            currentUser.username,
            content
        );

        // Clear input
        textarea.value = '';

        // Update cache
        const cachedComments = commentsCache.get(videoId) || [];
        cachedComments.unshift(comment);
        commentsCache.set(videoId, cachedComments);

        // Re-render comments
        await loadComments(videoId);

        Utils.showToast('Comment posted successfully!', 'success');

    } catch (error) {
        console.error('Error posting comment:', error);
        Utils.showToast('Failed to post comment', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Post';
    }
}

// ============================================
// Delete a Comment
// ============================================
async function deleteComment(commentId, videoId) {
    if (!confirm('Are you sure you want to delete this comment?')) {
        return;
    }

    try {
        await CommentsAPI.deleteComment(commentId);

        // Update cache
        const cachedComments = commentsCache.get(videoId) || [];
        const updatedComments = cachedComments.filter(c => c.id !== commentId);
        commentsCache.set(videoId, updatedComments);

        // Re-render comments
        await loadComments(videoId);

        Utils.showToast('Comment deleted successfully', 'success');

    } catch (error) {
        console.error('Error deleting comment:', error);
        Utils.showToast('Failed to delete comment', 'error');
    }
}

// ============================================
// Toggle Comment Like
// ============================================
async function toggleCommentLike(commentId, videoId) {
    const isLiked = likedComments.has(commentId);

    try {
        let updatedComment;

        if (isLiked) {
            updatedComment = await CommentsAPI.unlikeComment(commentId);
            likedComments.delete(commentId);
        } else {
            updatedComment = await CommentsAPI.likeComment(commentId);
            likedComments.add(commentId);
        }

        // Update cache
        const cachedComments = commentsCache.get(videoId) || [];
        const index = cachedComments.findIndex(c => c.id === commentId);
        if (index !== -1) {
            cachedComments[index] = updatedComment;
            commentsCache.set(videoId, cachedComments);
        }

        // Re-render comments
        renderComments(videoId, cachedComments);

    } catch (error) {
        console.error('Error toggling comment like:', error);
        Utils.showToast('Failed to update like', 'error');
    }
}

// ============================================
// Update Comment Count Display
// ============================================
function updateCommentCount(videoId, count) {
    const section = document.querySelector(`.comments-section[data-video-id="${videoId}"]`);
    const countSpan = section.querySelector('.comments-count');

    if (count === 0) {
        countSpan.textContent = 'No comments yet';
    } else if (count === 1) {
        countSpan.textContent = 'View 1 comment';
    } else {
        countSpan.textContent = `View all ${count} comments`;
    }
}

// ============================================
// Enable Comment Input
// ============================================
function enableCommentInput() {
    document.addEventListener('input', (e) => {
        if (e.target.classList.contains('comment-input')) {
            const btn = e.target.parentElement.querySelector('.btn-post-comment');
            btn.disabled = e.target.value.trim().length === 0;
        }
    });

    // Auto-resize textarea
    document.addEventListener('input', (e) => {
        if (e.target.classList.contains('comment-input')) {
            e.target.style.height = 'auto';
            e.target.style.height = e.target.scrollHeight + 'px';
        }
    });
}

// ============================================
// Initialize Comments on Page Load
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    enableCommentInput();
});

// Make functions global
window.toggleComments = toggleComments;
window.postComment = postComment;
window.deleteComment = deleteComment;
window.toggleCommentLike = toggleCommentLike;