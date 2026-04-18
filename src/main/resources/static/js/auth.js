/**
 * Módulo de autenticação - gerencia login, logout e verificação de sessão
 * Usado em todas as páginas que precisam de autenticação
 */

const Auth = {
    // Armazena dados do usuário logado
    currentUser: null,

    /**
     * Verifica se o usuário está autenticado
     * @returns {Promise<Object|null>} Dados do usuário ou null
     */
    async checkSession() {
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
                localStorage.setItem('loggedInUser', JSON.stringify(this.currentUser));
                return this.currentUser;
            } else {
                this.currentUser = null;
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
            const response = await fetch('/api/login', {
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
     * @returns {Promise<boolean>}
     */
    async logout() {
        try {
            const response = await fetch('/api/logout', {
                method: 'POST',
                credentials: 'include'
            });

            this.currentUser = null;
            localStorage.removeItem('loggedInUser');

            return response.ok;
        } catch (error) {
            console.error('Erro no logout:', error);
            // Limpa local mesmo com erro
            this.currentUser = null;
            localStorage.removeItem('loggedInUser');
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
        return user && user.role === 'ADMIN';
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