/**
 * Módulo de Notificações - gerencia exibição, contador e ações de notificações
 */
const Notifications = {
    // Container do dropdown de notificações
    container: null,
    badge: null,
    dropdown: null,
    notificationsList: null,

    /**
     * Inicializa o sistema de notificações
     * Deve ser chamado após o DOM estar pronto
     */
    init() {
        this.createDropdown();
        this.startPolling();
    },

    /**
     * Cria o dropdown de notificações no DOM
     */
    createDropdown() {
        // Encontra o container de notificações no navbar
        this.container = document.getElementById('notificationsContainer');
        if (!this.container) {
            console.warn('Container de notificações não encontrado');
            return;
        }

        this.badge = this.container.querySelector('.notification-badge');
        this.dropdown = this.container.querySelector('.notification-dropdown');
        this.notificationsList = this.container.querySelector('.notifications-list');

        // Fecha dropdown ao clicar fora
        document.addEventListener('click', (e) => {
            if (!this.container.contains(e.target)) {
                this.hideDropdown();
            }
        });

        // Toggle dropdown ao clicar no ícone
        const bellIcon = this.container.querySelector('.notification-bell');
        if (bellIcon) {
            bellIcon.addEventListener('click', (e) => {
                e.preventDefault();
                this.toggleDropdown();
            });
        }
    },

    /**
     * Busca contador de notificações não lidas
     */
    async fetchCount() {
        const user = this.getUser();
        if (!user || !user.id) {
            this.updateBadge(0);
            return 0;
        }

        try {
            const response = await fetch(`/api/notificacoes/usuario/${user.id}/count`, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const count = await response.json();
                this.updateBadge(count);
                return count;
            }
        } catch (error) {
            console.error('Erro ao buscar contador de notificações:', error);
        }
        return 0;
    },

    /**
     * Busca lista de notificações
     */
    async fetchNotifications() {
        const user = this.getUser();
        if (!user || !user.id) return [];

        try {
            const response = await fetch(`/api/notificacoes/usuario/${user.id}`, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Erro ao buscar notificações:', error);
        }
        return [];
    },

    /**
     * Atualiza o badge de contador
     */
    updateBadge(count) {
        if (!this.badge) return;

        if (count > 0) {
            this.badge.textContent = count > 99 ? '99+' : count;
            this.badge.style.display = 'block';
        } else {
            this.badge.style.display = 'none';
        }
    },

    /**
     * Exibe o dropdown com lista de notificações
     */
    async showDropdown() {
        if (!this.dropdown || !this.notificationsList) return;

        // Mostra loading
        this.notificationsList.innerHTML = '<div class="notification-loading">Carregando...</div>';
        this.dropdown.style.display = 'block';

        // Busca notificações
        const notifications = await this.fetchNotifications();

        // Renderiza lista
        this.renderNotifications(notifications);
    },

    /**
     * Esconde o dropdown
     */
    hideDropdown() {
        if (this.dropdown) {
            this.dropdown.style.display = 'none';
        }
    },

    /**
     * Toggle do dropdown
     */
    toggleDropdown() {
        if (this.dropdown && this.dropdown.style.display === 'block') {
            this.hideDropdown();
        } else {
            this.showDropdown();
        }
    },

    /**
     * Renderiza lista de notificações
     */
    renderNotifications(notifications) {
        if (!this.notificationsList) return;

        if (notifications.length === 0) {
            this.notificationsList.innerHTML = `
                <div class="notification-empty">
                    <span>🔔</span>
                    <p>Nenhuma notificação</p>
                </div>
            `;
            return;
        }

        this.notificationsList.innerHTML = notifications.map(n => `
            <div class="notification-item ${n.lida ? 'read' : 'unread'}" data-id="${n.id}">
                <div class="notification-title">${this.escapeHtml(n.titulo)}</div>
                <div class="notification-message">${this.escapeHtml(n.mensagem)}</div>
                <div class="notification-time">${this.formatDate(n.dataCriacao)}</div>
            </div>
        `).join('');

        // Adiciona click handlers para marcar como lida
        this.notificationsList.querySelectorAll('.notification-item.unread').forEach(item => {
            item.addEventListener('click', () => this.markAsRead(item.dataset.id));
        });
    },

    /**
     * Marca notificação como lida
     */
    async markAsRead(notificationId) {
        try {
            const response = await fetch(`/api/notificacoes/${notificationId}/ler`, {
                method: 'PUT',
                credentials: 'include'
            });

            if (response.ok) {
                // Atualiza UI
                const item = this.notificationsList.querySelector(`[data-id="${notificationId}"]`);
                if (item) {
                    item.classList.remove('unread');
                    item.classList.add('read');
                }
                // Atualiza contador
                this.fetchCount();
            }
        } catch (error) {
            console.error('Erro ao marcar notificação como lida:', error);
        }
    },

    /**
     * Marca todas notificações como lidas
     */
    async markAllAsRead() {
        const user = this.getUser();
        if (!user || !user.id) return;

        try {
            const response = await fetch(`/api/notificacoes/usuario/${user.id}/ler-todas`, {
                method: 'PUT',
                credentials: 'include'
            });

            if (response.ok) {
                this.fetchCount();
                this.hideDropdown();
            }
        } catch (error) {
            console.error('Erro ao marcar todas como lidas:', error);
        }
    },

    /**
     * Inicia polling para atualizar contador periodicamente
     */
    startPolling() {
        // Busca contador imediatamente
        this.fetchCount();

        // Atualiza a cada 60 segundos
        setInterval(() => {
            this.fetchCount();
        }, 60000);
    },

    /**
     * Obtém usuário logado do localStorage
     */
    getUser() {
        if (window.Auth && window.Auth.getUser) {
            return window.Auth.getUser();
        }
        const stored = localStorage.getItem('loggedInUser');
        if (stored) {
            try {
                return JSON.parse(stored);
            } catch (e) {
                return null;
            }
        }
        return null;
    },

    /**
     * Escapa HTML para prevenir XSS
     */
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    /**
     * Formata data para exibição
     */
    formatDate(dateStr) {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return 'Agora mesmo';
        if (minutes < 60) return `${minutes} min atrás`;
        if (hours < 24) return `${hours}h atrás`;
        if (days < 7) return `${days}d atrás`;
        return date.toLocaleDateString('pt-BR');
    }
};

// Exporta para uso global
window.Notifications = Notifications;