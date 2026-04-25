/**
 * Módulo de autenticação - gerencia login, logout e verificação de sessão
 * Usado em todas as páginas que precisam de autenticação
 */

/**
 * Lê o token CSRF do cookie XSRF-TOKEN (setado pelo CookieCsrfTokenRepository)
 */
function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Wrapper de fetch que injeta o header CSRF automaticamente em métodos de escrita
 * e adiciona timeout (10s por padrão)
 */
async function apiFetch(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
        const token = getCsrfToken();
        if (token) {
            options.headers = {
                ...options.headers,
                'X-XSRF-TOKEN': token
            };
        }
    }

    // Timeout com AbortController
    const timeoutMs = options.timeout || 10000;
    delete options.timeout;
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    options.signal = options.signal || controller.signal;

    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            let errorMsg = `Erro ${response.status}`;
            try {
                const errData = await response.json();
                errorMsg = errData.message || errData.error || errorMsg;
            } catch {}
            const error = new Error(errorMsg);
            error.status = response.status;
            throw error;
        }
        return response;
    } finally {
        clearTimeout(timeoutId);
    }
}

const Auth = {
    // Armazena dados do usuário logado
    currentUser: null,

    /**
     * Verifica se o usuário está autenticado
     * @returns {Promise<Object|null>} Dados do usuário ou null
     */
    async checkSession() {
        // Cache: retorna currentUser se ainda válido (TTL 30s)
        if (this.currentUser && this._sessionCacheTime && (Date.now() - this._sessionCacheTime < 30000)) {
            return this.currentUser;
        }

        try {
            const response = await fetch('/api/users/me', {
                method: 'GET',
                credentials: 'include', // Envia cookies de sessão
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                this.currentUser = await response.json();
                this._sessionCacheTime = Date.now();
                localStorage.setItem('loggedInUser', JSON.stringify(this.currentUser));
                return this.currentUser;
            } else {
                this.currentUser = null;
                this._sessionCacheTime = null;
                localStorage.removeItem('loggedInUser');
                return null;
            }
        } catch (error) {
            console.error('Erro ao verificar sessão:', error);
            return null;
        }
    },

    /**
     * Faz login do usuário
     * @param {string} email
     * @param {string} senha
     * @returns {Promise<{success: boolean, user?: Object, error?: string}>}
     */
    async login(email, senha) {
        try {
            const response = await apiFetch('/api/login', {
                method: 'POST',
                credentials: 'include', // Importante para sessão
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, senha })
            });

            if (response.ok) {
                const user = await response.json();
                this.currentUser = user;
                localStorage.setItem('loggedInUser', JSON.stringify(user));
                return { success: true, user };
            } else {
                const error = await response.text();
                return { success: false, error };
            }
        } catch (error) {
            console.error('Erro no login:', error);
            return { success: false, error: 'Erro de conexão com o servidor' };
        }
    },

    /**
     * Faz logout do usuário
     * @param {string} redirectUrl - URL para redirecionar após logout (padrão: index.html)
     * @returns {Promise<boolean>}
     */
    async logout(redirectUrl = 'index.html') {
        try {
            const response = await apiFetch('/api/logout', {
                method: 'POST',
                credentials: 'include'
            });

            this.currentUser = null;
            localStorage.removeItem('loggedInUser');

            // Redireciona após logout para atualizar o navbar
            if (redirectUrl) {
                window.location.href = redirectUrl;
            }

            return response.ok;
        } catch (error) {
            console.error('Erro no logout:', error);
            // Limpa local mesmo com erro
            this.currentUser = null;
            localStorage.removeItem('loggedInUser');

            // Redireciona mesmo com erro
            if (redirectUrl) {
                window.location.href = redirectUrl;
            }
            return false;
        }
    },

    /**
     * Retorna dados do usuário do localStorage (síncrono)
     * @returns {Object|null}
     */
    getUser() {
        if (this.currentUser) {
            return this.currentUser;
        }
        const stored = localStorage.getItem('loggedInUser');
        if (stored) {
            try {
                this.currentUser = JSON.parse(stored);
                return this.currentUser;
            } catch (e) {
                return null;
            }
        }
        return null;
    },

    /**
     * Verifica se o usuário tem role de admin
     * @returns {boolean}
     */
    isAdmin() {
        const user = this.getUser();
        return user && (user.role === 'ADMIN' || user.role === 'SUPER_ADMIN');
    },

    /**
     * Retorna a role do usuário logado
     * @returns {string|null}
     */
    getUserRole() {
        const user = this.getUser();
        return user ? user.role : null;
    },

    /**
     * Protege uma página - redireciona para login se não autenticado
     * @param {string} redirectUrl - URL para redirecionar após login (opcional)
     */
    async requireAuth(redirectUrl = 'login.html') {
        const user = await this.checkSession();
        if (!user) {
            window.location.href = redirectUrl;
            return false;
        }
        return true;
    }
};

// Exporta para uso global
window.Auth = Auth;