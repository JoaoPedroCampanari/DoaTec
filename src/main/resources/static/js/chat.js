/**
 * ChatWidget — componente de chat reutilizável para tickets, doações e solicitações.
 * Usa polling (5s) para atualizar mensagens enquanto aberto.
 */
const ChatWidget = {
    overlay: null,
    pollingId: null,
    contexto: null,
    referenciaId: null,
    currentUserId: null,
    isOpen: false,

    async open(contexto, referenciaId, titulo) {
        if (this.isOpen) this.close();

        this.contexto = contexto;
        this.referenciaId = referenciaId;
        this.isOpen = true;

        // Pega ID do usuário logado
        const user = await Auth.checkSession();
        this.currentUserId = user ? user.id : null;

        // Cria overlay
        this.overlay = document.createElement('div');
        this.overlay.className = 'chat-overlay';
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) this.close();
        });

        this.overlay.innerHTML = `
            <div class="chat-panel">
                <div class="chat-header">
                    <span class="chat-title">${DoaTec.escapeHtml(titulo || 'Chat')}</span>
                    <button class="chat-close">&times;</button>
                </div>
                <div class="chat-messages"></div>
                <div class="chat-input-area">
                    <input type="text" placeholder="Digite sua mensagem..." maxlength="500">
                    <button>Enviar</button>
                </div>
            </div>
        `;

        document.body.appendChild(this.overlay);

        // Eventos
        const closeBtn = this.overlay.querySelector('.chat-close');
        closeBtn.addEventListener('click', () => this.close());

        const input = this.overlay.querySelector('.chat-input-area input');
        const sendBtn = this.overlay.querySelector('.chat-input-area button');

        sendBtn.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Carrega mensagens
        await this.loadMessages();

        // Inicia polling
        this.pollingId = setInterval(() => this.loadMessages(), 5000);

        // Foca no input
        input.focus();
    },

    close() {
        if (this.pollingId) {
            clearInterval(this.pollingId);
            this.pollingId = null;
        }
        if (this.overlay) {
            this.overlay.remove();
            this.overlay = null;
        }
        this.isOpen = false;
        this.contexto = null;
        this.referenciaId = null;
    },

    async loadMessages() {
        if (!this.isOpen || !this.overlay) return;

        try {
            const response = await apiFetch(`/api/chat/historico/${this.contexto}/${this.referenciaId}`);
            if (!response.ok) {
                container.innerHTML = `
                    <div style="text-align:center; color:var(--color-danger, #dc3545); font-size:0.85rem; padding:40px 0;">
                        Não foi possível carregar as mensagens.
                    </div>
                `;
                return;
            }

            const mensagens = await response.json();
            const container = this.overlay.querySelector('.chat-messages');
            if (!container) return;

            const wasAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 30;

            container.innerHTML = '';

            if (!mensagens || mensagens.length === 0) {
                container.innerHTML = `
                    <div style="text-align:center; color:var(--neutral-400); font-size:0.85rem; padding:40px 0;">
                        Nenhuma mensagem ainda.<br>Envie a primeira!
                    </div>
                `;
                return;
            }

            mensagens.forEach(msg => {
                const isOwn = msg.remetenteId === this.currentUserId;
                const bubble = document.createElement('div');
                bubble.className = `chat-bubble ${isOwn ? 'chat-bubble-user' : 'chat-bubble-other'}`;

                const senderLabel = !isOwn ? `<div class="chat-bubble-sender">${DoaTec.escapeHtml(msg.remetenteNome || 'Admin')}</div>` : '';
                const timeStr = msg.dataEnvio ? new Date(msg.dataEnvio).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '';

                bubble.innerHTML = `
                    ${senderLabel}
                    <div>${DoaTec.escapeHtml(msg.conteudo)}</div>
                    <div class="chat-bubble-time">${timeStr}</div>
                `;
                container.appendChild(bubble);
            });

            container.scrollTop = container.scrollHeight;
        } catch (e) {
            console.error('Chat: erro ao carregar mensagens', e);
        }
    },

    async sendMessage() {
        const input = this.overlay?.querySelector('.chat-input-area input');
        if (!input) return;

        const conteudo = input.value.trim();
        if (!conteudo) return;

        // Renderização otimista — mostra a bolha imediatamente
        const container = this.overlay.querySelector('.chat-messages');
        const optimisticBubble = document.createElement('div');
        optimisticBubble.className = 'chat-bubble chat-bubble-user';
        optimisticBubble.innerHTML = `
            <div>${DoaTec.escapeHtml(conteudo)}</div>
            <div class="chat-bubble-time">Enviando...</div>
        `;
        // Remove estado vazio se existir
        const emptyMsg = container.querySelector('div[style*="text-align:center"]');
        if (emptyMsg) emptyMsg.remove();
        container.appendChild(optimisticBubble);
        container.scrollTop = container.scrollHeight;

        input.value = '';

        try {
            const response = await apiFetch('/api/chat/enviar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    conteudo: conteudo,
                    referenciaId: this.referenciaId,
                    contexto: this.contexto
                })
            });

            if (!response.ok) {
                optimisticBubble.innerHTML = `<div>${DoaTec.escapeHtml(conteudo)}</div><div class="chat-bubble-time" style="color:#ef4444;">Falha ao enviar</div>`;
                const err = await response.json().catch(() => ({}));
                DoaTec.showToast(err.message || err.erro || 'Erro ao enviar mensagem', 'error');
                return;
            }

            await this.loadMessages();
        } catch (e) {
            console.error('Chat: erro ao enviar', e);
            optimisticBubble.innerHTML = `<div>${DoaTec.escapeHtml(conteudo)}</div><div class="chat-bubble-time" style="color:#ef4444;">Falha ao enviar</div>`;
            DoaTec.showToast('Erro ao enviar mensagem', 'error');
        }
    }
};
