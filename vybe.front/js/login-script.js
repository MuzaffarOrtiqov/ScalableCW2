// ============================================
// API Configuration for Azure
// ============================================
const API_BASE_URL = 'http://4.233.136.94:8080/api';
// ============================================
// User Credentials Database (Replace with API)
// ============================================
const USERS = {
    admin: {
        username: 'admin',
        password: 'admin123',
        role: 'admin',
        displayName: 'Admin User'
    },
    user: {
        username: 'user',
        password: 'user123',
        role: 'user',
        displayName: 'Regular User'
    }
};

// ============================================
// DOM Elements
// ============================================
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const rememberMeCheckbox = document.getElementById('rememberMe');
const signupLink = document.getElementById('signupLink');
const loginButton = loginForm.querySelector('.btn-primary');

// ============================================
// Initialize
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    checkExistingSession();
    initEventListeners();
});

// ============================================
// Check for Existing Session
// ============================================
function checkExistingSession() {
    const sessionUser = sessionStorage.getItem('vybeUser');
    const localUser = localStorage.getItem('vybeUser');

    if (sessionUser || localUser) {
        const user = JSON.parse(sessionUser || localUser);
        redirectBasedOnRole(user.role);
    }
}

// ============================================
// Event Listeners
// ============================================
function initEventListeners() {
    // Login form submission
    loginForm.addEventListener('submit', handleLogin);

    // Sign up link (placeholder)
    signupLink.addEventListener('click', (e) => {
        e.preventDefault();
        showToast('Sign up feature coming soon!', 'info');
    });

    // Enter key shortcuts
    usernameInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            passwordInput.focus();
        }
    });
}

// ============================================
// Handle Login
// ============================================
async function handleLogin(e) {
    e.preventDefault();

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();
    const rememberMe = rememberMeCheckbox.checked;

    // Validate inputs
    if (!username || !password) {
        showToast('Please enter both username and password', 'error');
        return;
    }

    // Show loading state
    setButtonLoading(true);

    try {
        // Simulate API delay
        await new Promise(resolve => setTimeout(resolve, 800));

        // Authenticate user
        const user = authenticateUser(username, password);

        if (user) {
            // Create session object
            const sessionData = {
                username: user.username,
                role: user.role,
                displayName: user.displayName,
                loginTime: new Date().toISOString()
            };

            // Store session
            if (rememberMe) {
                localStorage.setItem('vybeUser', JSON.stringify(sessionData));
            } else {
                sessionStorage.setItem('vybeUser', JSON.stringify(sessionData));
            }

            showToast(`Welcome ${user.displayName}!`, 'success');

            // Redirect based on role after short delay
            setTimeout(() => {
                redirectBasedOnRole(user.role);
            }, 1000);

        } else {
            showToast('Invalid username or password', 'error');
            setButtonLoading(false);
        }

    } catch (error) {
        console.error('Login error:', error);
        showToast('An error occurred during login', 'error');
        setButtonLoading(false);
    }
}

// ============================================
// Authenticate User (Replace with API call)
// ============================================
function authenticateUser(username, password) {
    const user = USERS[username];

    if (user && user.password === password) {
        return user;
    }

    return null;
}

// ============================================
// API Authentication (Alternative method)
// ============================================
async function authenticateWithAPI(username, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('Authentication failed');
        }

        const data = await response.json();
        return data; // Should contain { user, token, role }

    } catch (error) {
        console.error('API authentication error:', error);
        throw error;
    }
}

// ============================================
// Redirect Based on Role
// ============================================
function redirectBasedOnRole(role) {
    if (role === 'admin') {
        window.location.href = 'admin.html';
    } else {
        window.location.href = 'index.html';
    }
}

// ============================================
// Set Button Loading State
// ============================================
function setButtonLoading(loading) {
    const btnText = loginButton.querySelector('.btn-text');
    const btnLoader = loginButton.querySelector('.btn-loader');

    if (loading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'flex';
        loginButton.disabled = true;
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        loginButton.disabled = false;
    }
}

// ============================================
// Show Toast Notification
// ============================================
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast toast-${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// ============================================
// Utility: Clear Session
// ============================================
function clearSession() {
    sessionStorage.removeItem('vybeUser');
    localStorage.removeItem('vybeUser');
}