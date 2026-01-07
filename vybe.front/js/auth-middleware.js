// ============================================
// Authentication Middleware
// Add this script to both admin.html and index.html
// to protect pages and verify user sessions
// ============================================

/**
 * Check if user is authenticated and has the required role
 * @param {string} requiredRole - 'admin' or 'user'
 * @returns {Object|null} - User object if authenticated, null otherwise
 */
function checkAuth(requiredRole = null) {
    const sessionUser = sessionStorage.getItem('vybeUser');
    const localUser = localStorage.getItem('vybeUser');

    if (!sessionUser && !localUser) {
        redirectToLogin();
        return null;
    }

    const user = JSON.parse(sessionUser || localUser);

    // Check if session is valid (optional - add expiration check)
    if (isSessionExpired(user)) {
        clearSession();
        redirectToLogin();
        return null;
    }

    // Check role if required
    if (requiredRole && user.role !== requiredRole) {
        showToast('Unauthorized access', 'error');
        redirectBasedOnRole(user.role);
        return null;
    }

    return user;
}

/**
 * Check if session has expired (24 hours)
 */
function isSessionExpired(user) {
    if (!user.loginTime) return false;

    const loginTime = new Date(user.loginTime);
    const now = new Date();
    const hoursSinceLogin = (now - loginTime) / (1000 * 60 * 60);

    return hoursSinceLogin > 24; // Session expires after 24 hours
}

/**
 * Redirect to login page
 */
function redirectToLogin() {
    if (!window.location.pathname.includes('login.html')) {
        window.location.href = 'login.html';
    }
}

/**
 * Redirect based on user role
 */
function redirectBasedOnRole(role) {
    if (role === 'admin') {
        window.location.href = 'admin.html';
    } else {
        window.location.href = 'index.html';
    }
}

/**
 * Clear session data
 */
function clearSession() {
    sessionStorage.removeItem('vybeUser');
    localStorage.removeItem('vybeUser');
    sessionStorage.removeItem('vybeAdmin'); // Legacy support
    localStorage.removeItem('vybeAdmin'); // Legacy support
}

/**
 * Logout user
 */
function logout() {
    clearSession();
    showToast('Logged out successfully', 'success');
    setTimeout(() => {
        redirectToLogin();
    }, 1000);
}

/**
 * Get current user
 */
function getCurrentUser() {
    const sessionUser = sessionStorage.getItem('vybeUser');
    const localUser = localStorage.getItem('vybeUser');

    if (sessionUser || localUser) {
        return JSON.parse(sessionUser || localUser);
    }

    return null;
}

/**
 * Update user display name in UI
 */
function updateUserDisplay() {
    const user = getCurrentUser();

    if (user) {
        const userNameElements = document.querySelectorAll('.admin-name, .user-name');
        userNameElements.forEach(el => {
            el.textContent = user.displayName || user.username;
        });
    }
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.textContent = message;
    toast.className = `toast toast-${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============================================
// Auto-initialize on page load
// ============================================
(function() {
    // Only run auth check if not on login page
    if (!window.location.pathname.includes('login.html')) {
        document.addEventListener('DOMContentLoaded', () => {
            const isAdminPage = window.location.pathname.includes('admin.html');
            const requiredRole = isAdminPage ? 'admin' : null;

            const user = checkAuth(requiredRole);

            if (user) {
                updateUserDisplay();
                console.log('User authenticated:', user.username, 'Role:', user.role);
            }
        });
    }
})();