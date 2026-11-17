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
    podeEnviar: false,

    async open(contexto, referenciaId, titulo, mostrarId = false) {
        if (this.isOpen) this.close();

        this.contexto = contexto;
        this.referenciaId = referenciaId;
        this.isOpen = true;
        this.podeEnviar = false;

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
                    <div class="chat-heading">
                        <span class="chat-title">${DoaTec.escapeHtml(titulo || 'Chat')}</span>
                        ${mostrarId ? `<span class="chat-reference">ID: ${DoaTec.escapeHtml(String(referenciaId))}</span>` : ''}
                    </div>
                    <button class="chat-close">&times;</button>
                </div>
                <div class="chat-messages"></div>
                <div class="chat-closed-notice" style="display:none;padding:14px;text-align:center;color:var(--neutral-600);border-top:1px solid var(--neutral-200);font-size:0.85rem;">Conversa encerrada.</div>
                <div class="chat-input-area">
                    <input type="text" placeholder="Digite sua mensagem..." maxlength="500" disabled>
                    <button disabled>Enviar</button>
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
        if (this.podeEnviar && this.isOpen) input.focus();
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
        this.podeEnviar = false;
        this.contexto = null;
        this.referenciaId = null;
    },

    async loadMessages() {
        if (!this.isOpen || !this.overlay) return;

        try {
            const overlay = this.overlay;
            const response = await apiFetch(`/api/chat/conversa/${this.contexto}/${this.referenciaId}`);
            const conversa = await response.json();
            if (!this.isOpen || this.overlay !== overlay) return;
            const mensagens = conversa.mensagens;
            this.podeEnviar = conversa.podeEnviar === true;
            const area = overlay.querySelector('.chat-input-area');
            const aviso = overlay.querySelector('.chat-closed-notice');
            area.style.display = this.podeEnviar ? '' : 'none';
            aviso.style.display = this.podeEnviar ? 'none' : 'block';
            area.querySelector('input').disabled = !this.podeEnviar;
            area.querySelector('button').disabled = !this.podeEnviar;
            const container = this.overlay.querySelector('.chat-messages');
            if (!container) return;

            const wasAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 30;

            container.innerHTML = '';

            if (!mensagens || mensagens.length === 0) {
                container.innerHTML = `
                    <div style="text-align:center; color:var(--neutral-400); font-size:0.85rem; padding:40px 0;">
                        Nenhuma mensagem ainda.${this.podeEnviar ? '<br>Envie a primeira!' : ''}
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
            if (Auth.isAdmin()) {
                const ultimaMensagemId = Math.max(...mensagens.map(m => m.id));
                const leitura = await apiFetch(`/api/chat/lidas-equipe/${this.contexto}/${this.referenciaId}?ultimaMensagemId=${ultimaMensagemId}`, { method: 'POST' });
                if (leitura.ok) document.dispatchEvent(new Event('chat-equipe-lido'));
            }
        } catch (e) {
            console.error('Chat: erro ao carregar mensagens', e);
            if (this.isOpen && this.overlay) {
                this.podeEnviar = false;
                const input = this.overlay.querySelector('.chat-input-area input');
                const botao = this.overlay.querySelector('.chat-input-area button');
                if (input) input.disabled = true;
                if (botao) botao.disabled = true;
                const container = this.overlay.querySelector('.chat-messages');
                if (container && !container.children.length) container.textContent = 'Não foi possível carregar a conversa. Tentaremos novamente.';
            }
        }
    },

    async sendMessage() {
        const input = this.overlay?.querySelector('.chat-input-area input');
        if (!input || !this.podeEnviar) return;

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
            DoaTec.showToast(e.message || 'Erro ao enviar mensagem', 'error');
            await this.loadMessages();
        }
    }
};
