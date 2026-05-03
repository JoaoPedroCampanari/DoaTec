/**
 * DoaTec - Admin Panel JavaScript
 * Lógica completa do painel administrativo
 */

const AdminPanel = {
    // Estado de paginação por aba
    pagination: {
        doacoes: { page: 0, size: 20, total: 0 },
        solicitacoes: { page: 0, size: 20, total: 0 },
        suporte: { page: 0, size: 20, total: 0 },
        usuarios: { page: 0, size: 20, total: 0 }
    },

    // Filtros atuais
    filters: {
        doacoes: '',
        solicitacoes: '',
        inventario: '',
        suporte: '',
        usuariosTipo: '',
        usuariosRole: ''
    },

    // Callback do modal de confirmação
    _confirmCallback: null,

    // ==================== INIT ====================
    async init() {
        const user = await Auth.requireAuth();
        if (!user) return;

        if (!Auth.isAdmin()) {
            showToast('Acesso restrito a administradores', 'error');
            setTimeout(() => window.location.href = '/index.html', 1500);
            return;
        }

        // Mostrar botão "Criar Admin" apenas para SUPER_ADMIN
        const isSuperAdmin = Auth.getUserRole() === 'SUPER_ADMIN';
        const btnCriarAdmin = document.getElementById('btn-criar-admin');
        if (btnCriarAdmin) btnCriarAdmin.style.display = isSuperAdmin ? '' : 'none';

        this.setupTabs();
        this.setupFilters();
        this.setupSuperAdminEvents();
        this.loadDashboard();
    },

    // ==================== TABS ====================
    setupTabs() {
        document.querySelectorAll('.admin-tab').forEach(tab => {
            tab.addEventListener('click', () => {
                document.querySelectorAll('.admin-tab').forEach(t => t.classList.remove('active'));
                document.querySelectorAll('.admin-tab-content').forEach(c => c.classList.remove('active'));
                tab.classList.add('active');
                const target = document.getElementById('tab-' + tab.dataset.tab);
                if (target) target.classList.add('active');

                // Carrega dados da aba ao clicar
                switch (tab.dataset.tab) {
                    case 'dashboard': this.loadDashboard(); break;
                    case 'doacoes': this.loadDoacoes(); break;
                    case 'solicitacoes': this.loadSolicitacoes(); break;
                    case 'inventario': this.loadInventario(); break;
                    case 'suporte': this.loadSuporte(); break;
                    case 'usuarios': this.loadUsuarios(); break;
                }
            });
        });
    },

    // ==================== FILTERS ====================
    setupFilters() {
        // Doações
        this.createFilterButtons('doacoes-filters', [
            { value: '', label: 'Todos' },
            { value: 'EM_TRIAGEM', label: 'Em Triagem' },
            { value: 'AGUARDANDO_COLETA', label: 'Aguardando Coleta' },
            { value: 'RECEBIDO', label: 'Recebido' },
            { value: 'EM_ANALISE', label: 'Em Análise' },
            { value: 'FINALIZADO', label: 'Finalizado' },
            { value: 'REJEITADA', label: 'Rejeitada' }
        ], 'doacoes');

        // Solicitações
        this.createFilterButtons('solicitacoes-filters', [
            { value: '', label: 'Todos' },
            { value: 'EM_ANALISE', label: 'Em Análise' },
            { value: 'APROVADA', label: 'Aprovada' },
            { value: 'REJEITADA', label: 'Rejeitada' },
            { value: 'CONCLUIDA', label: 'Concluída' }
        ], 'solicitacoes');

        // Inventário
        this.createFilterButtons('inventario-filters', [
            { value: '', label: 'Todos' },
            { value: 'DISPONIVEL', label: 'Disponível' },
            { value: 'RESERVADO', label: 'Reservado' },
            { value: 'ENTREGUE', label: 'Entregue' }
        ], 'inventario');

        // Suporte
        this.createFilterButtons('suporte-filters', [
            { value: '', label: 'Todos' },
            { value: 'ABERTO', label: 'Aberto' },
            { value: 'EM_ANDAMENTO', label: 'Em Andamento' },
            { value: 'RESOLVIDO', label: 'Resolvido' },
            { value: 'FECHADO', label: 'Fechado' }
        ], 'suporte');

        // Usuários — filtros especiais (tipo + role)
        this.createUserFilters();
    },

    createFilterButtons(containerId, options, filterKey) {
        const container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = '';

        options.forEach(opt => {
            const btn = document.createElement('button');
            btn.className = 'admin-filter-btn' + (opt.value === '' ? ' active' : '');
            btn.textContent = opt.label;
            btn.dataset.value = opt.value;
            btn.addEventListener('click', () => {
                container.querySelectorAll('.admin-filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.filters[filterKey] = opt.value;
                // Reset page
                if (this.pagination[filterKey]) this.pagination[filterKey].page = 0;
                switch (filterKey) {
                    case 'doacoes': this.loadDoacoes(); break;
                    case 'solicitacoes': this.loadSolicitacoes(); break;
                    case 'inventario': this.loadInventario(); break;
                    case 'suporte': this.loadSuporte(); break;
                }
            });
            container.appendChild(btn);
        });
    },

    createUserFilters() {
        const container = document.getElementById('usuarios-filters');
        if (!container) return;
        container.innerHTML = '';

        // Tipo filter
        const tipoGroup = document.createElement('div');
        tipoGroup.style.display = 'flex';
        tipoGroup.style.gap = '8px';
        tipoGroup.style.alignItems = 'center';
        tipoGroup.style.flexWrap = 'wrap';

        const tipoLabel = document.createElement('span');
        tipoLabel.textContent = 'Tipo:';
        tipoLabel.style.fontWeight = '600';
        tipoLabel.style.marginRight = '4px';
        tipoGroup.appendChild(tipoLabel);

        [
            { value: '', label: 'Todos' },
            { value: 'ALUNO', label: 'Aluno' },
            { value: 'DOADOR_PF', label: 'Doador PF' },
            { value: 'DOADOR_PJ', label: 'Doador PJ' }
        ].forEach(opt => {
            const btn = document.createElement('button');
            btn.className = 'admin-filter-btn' + (opt.value === '' ? ' active' : '');
            btn.textContent = opt.label;
            btn.dataset.value = opt.value;
            btn.addEventListener('click', () => {
                tipoGroup.querySelectorAll('.admin-filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.filters.usuariosTipo = opt.value;
                this.pagination.usuarios.page = 0;
                this.loadUsuarios();
            });
            tipoGroup.appendChild(btn);
        });
        container.appendChild(tipoGroup);

        // Role filter
        const roleGroup = document.createElement('div');
        roleGroup.style.display = 'flex';
        roleGroup.style.gap = '8px';
        roleGroup.style.alignItems = 'center';
        roleGroup.style.flexWrap = 'wrap';
        roleGroup.style.marginTop = '8px';

        const roleLabel = document.createElement('span');
        roleLabel.textContent = 'Role:';
        roleLabel.style.fontWeight = '600';
        roleLabel.style.marginRight = '4px';
        roleGroup.appendChild(roleLabel);

        [
            { value: '', label: 'Todos' },
            { value: 'USER', label: 'USER' },
            { value: 'ADMIN', label: 'ADMIN' }
        ].forEach(opt => {
            const btn = document.createElement('button');
            btn.className = 'admin-filter-btn' + (opt.value === '' ? ' active' : '');
            btn.textContent = opt.label;
            btn.dataset.value = opt.value;
            btn.addEventListener('click', () => {
                roleGroup.querySelectorAll('.admin-filter-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                this.filters.usuariosRole = opt.value;
                this.pagination.usuarios.page = 0;
                this.loadUsuarios();
            });
            roleGroup.appendChild(btn);
        });
        container.appendChild(roleGroup);
    },

    // ==================== DASHBOARD ====================
    async loadDashboard() {
        try {
            const res = await apiFetch('/api/admin/dashboard');
            if (!res.ok) throw new Error('Erro ao carregar dashboard');
            const data = await res.json();

            this.animateStat('stat-total-doacoes', data.totalDoacoes);
            this.animateStat('stat-doacoes-aprovadas', data.doacoesAprovadas);
            this.animateStat('stat-doacoes-rejeitadas', data.doacoesRejeitadas);
            this.animateStat('stat-solicitacoes-pendentes', data.totalSolicitacoesPendentes);
            this.animateStat('stat-solicitacoes-aprovadas', data.solicitacoesAprovadas);
            this.animateStat('stat-solicitacoes-rejeitadas', data.solicitacoesRejeitadas);
            this.animateStat('stat-tickets-abertos', data.totalTicketsAbertos);
            this.animateStat('stat-usuarios-ativos', data.totalUsuariosAtivos);
        } catch (err) {
            console.error('Dashboard:', err);
            showToast('Erro ao carregar dashboard', 'error');
        }
    },

    animateStat(elementId, targetValue) {
        const el = document.getElementById(elementId);
        if (el) animateCounter(el, targetValue);
    },

    // ==================== DOAÇÕES ====================
    async loadDoacoes() {
        const { page, size } = this.pagination.doacoes;
        const status = this.filters.doacoes;
        const params = new URLSearchParams({ page, size });
        if (status) params.set('status', status);

        try {
            const res = await apiFetch('/api/admin/doacoes?' + params);
            if (!res.ok) throw new Error('Erro ao carregar doações');
            const pageData = await res.json();
            this.pagination.doacoes.total = pageData.totalPages || 1;
            this.renderDoacoes(pageData.content || []);
            this.renderPagination('doacoes-pagination', 'doacoes', pageData);
        } catch (err) {
            console.error('Doações:', err);
            showToast('Erro ao carregar doações', 'error');
        }
    },

    renderDoacoes(doacoes) {
        const tbody = document.getElementById('doacoes-tbody');
        if (!doacoes.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="admin-empty">Nenhuma doação encontrada</td></tr>';
            return;
        }

        tbody.innerHTML = doacoes.map(d => {
            const proximosStatus = this.getProximosStatusDoacao(d.status);
            return `
            <tr class="admin-row-clickable" onclick="AdminPanel.toggleDetail(this, 'doacao', ${d.id}, event)">
                <td>#${d.id}</td>
                <td>${escapeHtml(d.doadorNome || '-')}<br><small>${escapeHtml(d.doadorEmail || '')}</small></td>
                <td>${formatDate(d.dataDoacao)}</td>
                <td>${this.createPillHtml(d.status)}</td>
                <td>${escapeHtml(d.descricaoGeral || '-')}</td>
                <td class="admin-actions">
                    ${proximosStatus.length ?
                        `<select class="admin-status-select" onclick="event.stopPropagation()" onmousedown="event.stopPropagation()" onchange="event.stopPropagation(); AdminPanel.alterarStatusDoacao(${d.id}, this.value)">
                            <option value="">Alterar status</option>
                            ${proximosStatus.map(st => `<option value="${st}">${this.formatStatusLabel(st)}</option>`).join('')}
                        </select>` : '<span style="color:var(--neutral-400)">-</span>'}
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('DOACAO', ${d.id}, 'Doação #${d.id}')">Conversar</button>
                </td>
            </tr>
            <tr class="admin-detail-row" id="detail-doacao-${d.id}" style="display:none">
                <td colspan="6">
                    <div class="admin-detail-content">
                        <p><strong>Logística:</strong> ${escapeHtml(d.preferenciaEntrega || 'Não informada')}</p>
                        <p><strong>Descrição:</strong> ${escapeHtml(d.descricaoGeral || 'Não informada')}</p>
                        ${d.urlFoto ? `<p><strong>Foto:</strong> <a href="${escapeHtml(d.urlFoto)}" target="_blank">Ver imagem</a></p>` : ''}
                        ${d.observacaoAdmin ? `<p><strong>Obs. Admin:</strong> ${escapeHtml(d.observacaoAdmin)}</p>` : ''}
                        ${d.adminAvaliadorNome ? `<p><strong>Avaliador:</strong> ${escapeHtml(d.adminAvaliadorNome)}${d.dataAvaliacao ? ' — ' + formatDate(d.dataAvaliacao) : ''}</p>` : ''}
                        ${d.itens && d.itens.length ? `
                            <p><strong>Itens doados:</strong></p>
                            <ul style="margin:4px 0 0 20px">${d.itens.map(i => `<li>${escapeHtml(i.tipo)}${i.descricao ? ' — ' + escapeHtml(i.descricao) : ''}</li>`).join('')}</ul>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;}).join('');
    },

    getProximosStatusDoacao(status) {
        const transicoes = {
            'EM_TRIAGEM': ['AGUARDANDO_COLETA', 'REJEITADA'],
            'AGUARDANDO_COLETA': ['RECEBIDO', 'REJEITADA'],
            'RECEBIDO': ['EM_ANALISE', 'REJEITADA'],
            'EM_ANALISE': ['FINALIZADO', 'REJEITADA'],
            'REJEITADA': ['EM_TRIAGEM']
        };
        return transicoes[status] || [];
    },

    formatStatusLabel(status) {
        const labels = {
            'EM_TRIAGEM': 'Em Triagem',
            'AGUARDANDO_COLETA': 'Aguardando Coleta',
            'RECEBIDO': 'Recebido',
            'EM_ANALISE': 'Em Análise',
            'FINALIZADO': 'Finalizado',
            'REJEITADA': 'Rejeitar',
            'EM_ANALISE_SOL': 'Em Análise',
            'APROVADA': 'Aprovar',
            'CONCLUIDA': 'Concluir'
        };
        return labels[status] || status;
    },

    alterarStatusDoacao(id, novoStatus) {
        if (!novoStatus) return;
        const isRejeicao = novoStatus === 'REJEITADA';
        this.openAvaliacaoModal(
            'Alterar Status — Doação #' + id,
            isRejeicao ? 'Informe o motivo da rejeição.' : 'Confirme a alteração de status para: ' + this.formatStatusLabel(novoStatus),
            isRejeicao,
            async (observacao) => {
                try {
                    const res = await apiFetch('/api/admin/doacoes/' + id + '/status?novoStatus=' + novoStatus, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ observacao })
                    });
                    if (!res.ok) {
                        const err = await res.json().catch(() => ({}));
                        throw new Error(err.message || 'Erro');
                    }
                    showToast('Status atualizado para ' + this.formatStatusLabel(novoStatus), 'success');
                    this.loadDoacoes();
                } catch (e) {
                    showToast(e.message || 'Erro ao alterar status', 'error');
                }
            }
        );
    },

    // ==================== SOLICITAÇÕES ====================
    async loadSolicitacoes() {
        const { page, size } = this.pagination.solicitacoes;
        const status = this.filters.solicitacoes;
        const params = new URLSearchParams({ page, size });
        if (status) params.set('status', status);

        try {
            const res = await apiFetch('/api/admin/solicitacoes?' + params);
            if (!res.ok) throw new Error('Erro ao carregar solicitações');
            const pageData = await res.json();
            this.pagination.solicitacoes.total = pageData.totalPages || 1;
            this.renderSolicitacoes(pageData.content || []);
            this.renderPagination('solicitacoes-pagination', 'solicitacoes', pageData);
        } catch (err) {
            console.error('Solicitações:', err);
            showToast('Erro ao carregar solicitações', 'error');
        }
    },

    renderSolicitacoes(solicitacoes) {
        const tbody = document.getElementById('solicitacoes-tbody');
        if (!solicitacoes.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="admin-empty">Nenhuma solicitação encontrada</td></tr>';
            return;
        }

        tbody.innerHTML = solicitacoes.map(s => {
            const proximosStatus = this.getProximosStatusSolicitacao(s.status);
            return `
            <tr class="admin-row-clickable" onclick="AdminPanel.toggleDetail(this, 'solicitacao', ${s.id}, event)">
                <td>#${s.id}</td>
                <td>${escapeHtml(s.alunoNome || '-')}<br><small>${escapeHtml(s.alunoEmail || '')}</small></td>
                <td>${formatDate(s.dataSolicitacao)}</td>
                <td>${escapeHtml(s.preferenciaEquipamento || '-')}</td>
                <td>${this.createPillHtml(s.status)}</td>
                <td class="admin-actions">
                    ${proximosStatus.length ?
                        `<select class="admin-status-select" onclick="event.stopPropagation()" onmousedown="event.stopPropagation()" onchange="event.stopPropagation(); AdminPanel.alterarStatusSolicitacao(${s.id}, this.value)">
                            <option value="">Alterar status</option>
                            ${proximosStatus.map(st => `<option value="${st}">${this.formatStatusLabel(st)}</option>`).join('')}
                        </select>` : ''}
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('SOLICITACAO', ${s.id}, 'Pedido #${s.id}')">Conversar</button>
                </td>
            </tr>
            <tr class="admin-detail-row" id="detail-solicitacao-${s.id}" style="display:none">
                <td colspan="6">
                    <div class="admin-detail-content">
                        <p><strong>Preferência:</strong> ${escapeHtml(s.preferenciaEquipamento || 'Não informada')}</p>
                        ${s.justificativa ? `<p><strong>Justificativa:</strong> ${escapeHtml(s.justificativa)}</p>` : ''}
                        ${s.observacaoAdmin ? `<p><strong>Obs. Admin:</strong> ${escapeHtml(s.observacaoAdmin)}</p>` : ''}
                        ${s.adminAvaliadorNome ? `<p><strong>Avaliador:</strong> ${escapeHtml(s.adminAvaliadorNome)}${s.dataAvaliacao ? ' — ' + formatDate(s.dataAvaliacao) : ''}</p>` : ''}
                        <div id="sugestoes-solicitacao-${s.id}"></div>
                    </div>
                </td>
            </tr>
        `;}).join('');

        // Carregar sugestões inline para solicitações APROVADAS quando detail row for aberto
        solicitacoes.forEach(s => {
            if (s.status === 'APROVADA') {
                this.carregarSugestoesInline(s.id);
            }
        });
    },

    getProximosStatusSolicitacao(status) {
        const transicoes = {
            'EM_ANALISE': ['APROVADA', 'REJEITADA'],
            'APROVADA': ['CONCLUIDA', 'REJEITADA'],
            'REJEITADA': ['EM_ANALISE']
        };
        return transicoes[status] || [];
    },

    alterarStatusSolicitacao(id, novoStatus) {
        if (!novoStatus) return;
        this.openAvaliacaoModal(
            'Alterar Status — Solicitação #' + id,
            'Confirme a alteração de status para: ' + this.formatStatusLabel(novoStatus),
            false,
            async (observacao) => {
                try {
                    const res = await apiFetch('/api/admin/solicitacoes/' + id + '/status?novoStatus=' + novoStatus, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ observacao })
                    });
                    if (!res.ok) {
                        const err = await res.json().catch(() => ({}));
                        throw new Error(err.message || 'Erro');
                    }
                    showToast('Status atualizado para ' + this.formatStatusLabel(novoStatus), 'success');
                    this.loadSolicitacoes();
                } catch (e) {
                    showToast(e.message || 'Erro ao alterar status', 'error');
                }
            }
        );
    },

    async carregarSugestoesInline(solicitacaoId) {
        try {
            const res = await apiFetch('/api/admin/inventario/sugestoes/' + solicitacaoId);
            if (!res.ok) return;
            const data = await res.json();
            const container = document.getElementById('sugestoes-solicitacao-' + solicitacaoId);
            if (!container) return;

            let html = '<p><strong>Equipamentos compatíveis:</strong></p>';
            if (!data.equipamentosCompativeis || !data.equipamentosCompativeis.length) {
                html += '<p style="color:var(--neutral-400)">Nenhum equipamento compatível encontrado.</p>';
            } else {
                html += '<table class="admin-table" style="margin-top:8px"><thead><tr><th>ID</th><th>Tipo</th><th>Descrição</th><th>Conservação</th><th>Score</th><th>Ação</th></tr></thead><tbody>';
                data.equipamentosCompativeis.forEach(eq => {
                    const scoreClass = eq.scoreCompatibilidade >= 80 ? 'high' : eq.scoreCompatibilidade >= 50 ? 'medium' : 'low';
                    html += `<tr>
                        <td>#${eq.equipamentoId}</td>
                        <td>${escapeHtml(eq.tipo)}</td>
                        <td>${escapeHtml(eq.descricao)}</td>
                        <td>${escapeHtml(eq.estadoConservacao || '-')}</td>
                        <td><span class="match-score ${scoreClass}">${eq.scoreCompatibilidade}%</span></td>
                        <td><button class="btn-approve" onclick="AdminPanel.atribuirEquipamento(${eq.equipamentoId}, ${solicitacaoId})">Atribuir</button></td>
                    </tr>`;
                });
                html += '</tbody></table>';
            }
            container.innerHTML = html;
        } catch (err) {
            console.error('Sugestões inline:', err);
        }
    },

    async atribuirEquipamento(equipamentoId, solicitacaoId) {
        try {
            const res = await apiFetch('/api/admin/inventario/' + equipamentoId + '/atribuir/' + solicitacaoId, {
                method: 'POST'
            });
            if (!res.ok) throw new Error();
            showToast('Equipamento atribuído com sucesso', 'success');
            this.loadSolicitacoes();
            this.loadInventario();
        } catch {
            showToast('Erro ao atribuir equipamento', 'error');
        }
    },

    // ==================== INVENTÁRIO ====================
    async loadInventario() {
        const status = this.filters.inventario;
        const params = new URLSearchParams();
        if (status) params.set('status', status);

        try {
            const res = await apiFetch('/api/admin/inventario?' + params);
            if (!res.ok) throw new Error('Erro ao carregar inventário');
            const equipamentos = await res.json();
            this.renderInventario(equipamentos);
        } catch (err) {
            console.error('Inventário:', err);
            showToast('Erro ao carregar inventário', 'error');
        }
    },

    renderInventario(equipamentos) {
        const tbody = document.getElementById('inventario-tbody');
        if (!equipamentos.length) {
            tbody.innerHTML = '<tr><td colspan="8" class="admin-empty">Nenhum equipamento encontrado</td></tr>';
            return;
        }

        tbody.innerHTML = equipamentos.map(eq => `
            <tr>
                <td>#${eq.id}</td>
                <td>${escapeHtml(eq.tipo || '-')}</td>
                <td>${escapeHtml(eq.descricao || '-')}</td>
                <td>${this.createPillHtml(eq.estadoConservacao)}</td>
                <td>${this.createPillHtml(eq.status)}</td>
                <td>${escapeHtml(eq.doadorOrigem || '-')}</td>
                <td>${eq.alunoDestinatarioId ? 'Aluno #' + eq.alunoDestinatarioId : '-'}</td>
                <td class="admin-actions">
                    ${eq.status === 'RESERVADO' ?
                        `<button class="btn-approve" onclick="AdminPanel.marcarEntregue(${eq.id})">Marcar Entregue</button>` :
                        '<span style="color:var(--neutral-400)">-</span>'}
                </td>
            </tr>
        `).join('');
    },

    async marcarEntregue(id) {
        this.confirmAction('Confirmar entrega do equipamento #' + id + '?', async () => {
            try {
                await apiFetch('/api/admin/inventario/' + id + '/entregar', { method: 'PUT' });
                showToast('Equipamento marcado como entregue', 'success');
                this.loadInventario();
            } catch {
                showToast('Erro ao marcar como entregue', 'error');
            }
        });
    },

    // ==================== SUPORTE ====================
    async loadSuporte() {
        const { page, size } = this.pagination.suporte;
        const status = this.filters.suporte;
        const params = new URLSearchParams({ page, size });
        if (status) params.set('status', status);

        try {
            const res = await apiFetch('/api/admin/suporte?' + params);
            if (!res.ok) throw new Error('Erro ao carregar suporte');
            const pageData = await res.json();
            this.pagination.suporte.total = pageData.totalPages || 1;
            this.renderSuporte(pageData.content || []);
            this.renderPagination('suporte-pagination', 'suporte', pageData);
        } catch (err) {
            console.error('Suporte:', err);
            showToast('Erro ao carregar tickets de suporte', 'error');
        }
    },

    renderSuporte(tickets) {
        const tbody = document.getElementById('suporte-tbody');
        if (!tickets.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="admin-empty">Nenhum ticket encontrado</td></tr>';
            return;
        }

        tbody.innerHTML = tickets.map(t => `
            <tr class="admin-row-clickable" onclick="AdminPanel.toggleDetail(this, 'suporte', ${t.id}, event)">
                <td>#${t.id}</td>
                <td>${escapeHtml(t.autorNome || '-')}<br><small>${escapeHtml(t.autorEmail || '')}</small></td>
                <td>${escapeHtml(t.assunto || '-')}</td>
                <td>${this.createPillHtml(t.status)}</td>
                <td>${t.dataCriacao ? new Date(t.dataCriacao).toLocaleDateString('pt-BR') : '-'}</td>
                <td class="admin-actions">
                    <button class="btn-action" onclick="event.stopPropagation(); AdminPanel.responderSuporte(${t.id})">Responder</button>
                    <button class="btn-action" onclick="event.stopPropagation(); AdminPanel.alterarStatusSuporte(${t.id})">Status</button>
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('SUPORTE', ${t.id}, 'Ticket #${t.id}')">Conversar</button>
                </td>
            </tr>
            <tr class="admin-detail-row" id="detail-suporte-${t.id}" style="display:none">
                <td colspan="6">
                    <div class="admin-detail-content">
                        <p><strong>Mensagem:</strong> ${escapeHtml(t.mensagem || 'Não informada')}</p>
                        ${t.resposta ? `<p><strong>Resposta:</strong> ${escapeHtml(t.resposta)}</p>` : ''}
                        ${t.adminResponsavelNome ? `<p><strong>Admin responsável:</strong> ${escapeHtml(t.adminResponsavelNome)}${t.dataResolucao ? ' — ' + new Date(t.dataResolucao).toLocaleDateString('pt-BR') : ''}</p>` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    responderSuporte(id) {
        const texto = document.getElementById('modal-resposta-texto');
        const warning = document.getElementById('modal-resposta-warning');
        texto.value = '';
        warning.style.display = 'none';

        const confirmBtn = document.getElementById('modal-resposta-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        newBtn.addEventListener('click', async () => {
            const resposta = texto.value.trim();
            if (!resposta) {
                warning.style.display = 'block';
                return;
            }
            try {
                const res = await apiFetch('/api/admin/suporte/' + id + '/responder', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ resposta })
                });
                if (!res.ok) throw new Error();
                showToast('Resposta enviada', 'success');
                this.closeModal('modal-resposta');
                this.loadSuporte();
            } catch {
                showToast('Erro ao responder ticket', 'error');
            }
        });

        this.openModal('modal-resposta');
    },

    alterarStatusSuporte(id) {
        const select = document.getElementById('modal-status-suporte-select');
        const confirmBtn = document.getElementById('modal-status-suporte-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        newBtn.addEventListener('click', async () => {
            try {
                const status = select.value;
                const res = await apiFetch('/api/admin/suporte/' + id + '/status?status=' + status, {
                    method: 'PUT'
                });
                if (!res.ok) throw new Error();
                showToast('Status alterado', 'success');
                this.closeModal('modal-status-suporte');
                this.loadSuporte();
            } catch {
                showToast('Erro ao alterar status', 'error');
            }
        });

        this.openModal('modal-status-suporte');
    },

    // ==================== USUÁRIOS ====================
    async loadUsuarios() {
        const tipo = this.filters.usuariosTipo;
        const role = this.filters.usuariosRole;

        try {
            let data;
            // API has separate endpoints for tipo and role, or the general paginated one
            if (tipo && !role) {
                const res = await apiFetch('/api/admin/usuarios/tipo/' + tipo);
                if (!res.ok) throw new Error();
                data = await res.json();
                this.renderUsuarios(data);
                document.getElementById('usuarios-pagination').innerHTML = '';
            } else if (role && !tipo) {
                const res = await apiFetch('/api/admin/usuarios/role/' + role);
                if (!res.ok) throw new Error();
                data = await res.json();
                this.renderUsuarios(data);
                document.getElementById('usuarios-pagination').innerHTML = '';
            } else if (tipo && role) {
                // Both filters — load all and filter client-side
                const res = await apiFetch('/api/admin/usuarios/tipo/' + tipo);
                if (!res.ok) throw new Error();
                data = await res.json();
                data = data.filter(u => u.role === role);
                this.renderUsuarios(data);
                document.getElementById('usuarios-pagination').innerHTML = '';
            } else {
                const { page, size } = this.pagination.usuarios;
                const params = new URLSearchParams({ page, size });
                const res = await apiFetch('/api/admin/usuarios?' + params);
                if (!res.ok) throw new Error();
                const pageData = await res.json();
                this.pagination.usuarios.total = pageData.totalPages || 1;
                this.renderUsuarios(pageData.content || []);
                this.renderPagination('usuarios-pagination', 'usuarios', pageData);
            }
        } catch (err) {
            console.error('Usuários:', err);
            showToast('Erro ao carregar usuários', 'error');
        }
    },

    renderUsuarios(usuarios) {
        const tbody = document.getElementById('usuarios-tbody');
        if (!usuarios.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="admin-empty">Nenhum usuário encontrado</td></tr>';
            return;
        }

        const currentUser = Auth.getUser();
        const currentUserId = currentUser ? currentUser.id : null;

        tbody.innerHTML = usuarios.map(u => {
            const isSelf = u.id === currentUserId;
            return `
            <tr>
                <td>#${u.id}</td>
                <td>${escapeHtml(u.nome || '-')}</td>
                <td>${escapeHtml(u.email || '-')}</td>
                <td>${this.createTipoPessoaLabel(u.tipoPessoa)}</td>
                <td><span class="role-badge ${u.role === 'ADMIN' ? 'role-admin' : u.role === 'SUPER_ADMIN' ? 'role-super-admin' : ''}">${escapeHtml(u.role)}</span></td>
                <td>
                    ${isSelf
                        ? '<span style="color:#9ca3af;font-size:12px;font-style:italic;">Você</span>'
                        : `<label class="toggle-active">
                            <input type="checkbox" ${u.ativo ? 'checked' : ''} data-toggle-user-id="${u.id}" data-toggle-user-ativo="${u.ativo}">
                            <span class="toggle-slider"></span>
                        </label>`}
                </td>
                <td class="admin-actions">
                    ${isSelf
                        ? '<span style="color:#9ca3af;font-size:12px;font-style:italic;">—</span>'
                        : `<button class="btn-action" data-user-id="${u.id}" data-user-role="${escapeHtml(u.role)}">Alterar Role</button>`}
                </td>
            </tr>
        `}).join('');

        // Event delegation for alterar role buttons (safe from XSS)
        tbody.querySelectorAll('button[data-user-role]').forEach(btn => {
            btn.addEventListener('click', () => {
                const userId = parseInt(btn.dataset.userId);
                const userRole = btn.dataset.userRole;
                this.alterarRoleUsuario(userId, userRole);
            });
        });

        // Event delegation for toggle ativo (with confirmation when desativando)
        tbody.querySelectorAll('[data-toggle-user-id]').forEach(input => {
            input.addEventListener('change', (e) => {
                const id = e.target.dataset.toggleUserId;
                const novoAtivo = e.target.checked;
                if (!novoAtivo) {
                    // Desativando — pede confirmação
                    e.target.checked = true; // reverte visualmente até confirmar
                    this.confirmAction('Desativar este usuário?', () => {
                        this.toggleUsuarioAtivo(id, novoAtivo);
                    });
                } else {
                    this.toggleUsuarioAtivo(id, novoAtivo);
                }
            });
        });
    },

    createTipoPessoaLabel(tipo) {
        const labels = {
            'ALUNO': 'Aluno',
            'DOADOR_PF': 'Doador PF',
            'DOADOR_PJ': 'Doador PJ'
        };
        return escapeHtml(labels[tipo] || tipo || '-');
    },

    async toggleUsuarioAtivo(id, ativo) {
        try {
            await apiFetch('/api/admin/usuarios/' + id + '/status', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ativo: ativo })
            });
            showToast(ativo ? 'Usuário ativado' : 'Usuário desativado', 'success');
            this.loadUsuarios();
        } catch (e) {
            showToast(e.message || 'Erro ao alterar status do usuário', 'error');
            this.loadUsuarios();
        }
    },

    alterarRoleUsuario(id, currentRole) {
        const select = document.getElementById('modal-role-usuario-select');
        // Popular options baseado no role do usuário logado
        const isSuperAdmin = Auth.getUserRole() === 'SUPER_ADMIN';
        select.innerHTML = '<option value="USER">USER</option>' +
            (isSuperAdmin ? '<option value="ADMIN">ADMIN</option>' : '');
        select.value = currentRole;
        const confirmBtn = document.getElementById('modal-role-usuario-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        newBtn.addEventListener('click', async () => {
            const novaRole = select.value;
            if (novaRole === currentRole) {
                this.closeModal('modal-role-usuario');
                return;
            }
            try {
                await apiFetch('/api/admin/usuarios/' + id + '/role?novaRole=' + novaRole, {
                    method: 'PUT'
                });
                showToast('Role alterada para ' + novaRole, 'success');
                this.closeModal('modal-role-usuario');
                this.loadUsuarios();
            } catch {
                showToast('Erro ao alterar role', 'error');
            }
        });

        this.openModal('modal-role-usuario');
    },

    // ==================== MODALS ====================
    openAvaliacaoModal(title, description, isReject, callback) {
        document.getElementById('modal-avaliacao-title').textContent = title;
        document.getElementById('modal-avaliacao-description').textContent = description;
        const obs = document.getElementById('modal-avaliacao-observacao');
        obs.value = '';
        const warning = document.getElementById('modal-avaliacao-warning');
        warning.style.display = isReject ? 'block' : 'none';
        obs.required = isReject;

        const confirmBtn = document.getElementById('modal-avaliacao-confirm');
        // Remove botão antigo e cria novo para limpar listeners
        const newBtn = document.createElement('button');
        newBtn.className = isReject ? 'btn-reject' : 'btn-approve';
        newBtn.id = 'modal-avaliacao-confirm';
        newBtn.textContent = isReject ? 'Rejeitar' : 'Confirmar';
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        newBtn.addEventListener('click', () => {
            const observacao = obs.value.trim();
            if (isReject && !observacao) {
                warning.style.display = 'block';
                return;
            }
            this.closeModal('modal-avaliacao');
            callback(observacao);
        });

        this.openModal('modal-avaliacao');
    },

    openModal(id) {
        const modal = document.getElementById(id);
        if (modal) {
            modal.style.display = 'flex';
            // Fechar ao clicar fora
            modal.onclick = (e) => {
                if (e.target === modal) this.closeModal(id);
            };
        }
    },

    closeModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.style.display = 'none';
    },

    // ==================== PAGINATION ====================
    renderPagination(containerId, key, pageData) {
        const container = document.getElementById(containerId);
        if (!container) return;

        const totalPages = pageData.totalPages || 1;
        const currentPage = pageData.number || 0;

        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        let html = '';

        // Botão anterior
        html += `<button ${currentPage === 0 ? 'disabled' : ''} onclick="AdminPanel.goToPage('${key}', ${currentPage - 1})">Anterior</button>`;

        // Números de página
        const startPage = Math.max(0, currentPage - 2);
        const endPage = Math.min(totalPages - 1, currentPage + 2);

        if (startPage > 0) {
            html += `<button onclick="AdminPanel.goToPage('${key}', 0)">1</button>`;
            if (startPage > 1) html += '<span style="padding:4px 8px">...</span>';
        }

        for (let i = startPage; i <= endPage; i++) {
            html += `<button class="${i === currentPage ? 'active' : ''}" onclick="AdminPanel.goToPage('${key}', ${i})">${i + 1}</button>`;
        }

        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) html += '<span style="padding:4px 8px">...</span>';
            html += `<button onclick="AdminPanel.goToPage('${key}', ${totalPages - 1})">${totalPages}</button>`;
        }

        // Botão próximo
        html += `<button ${currentPage >= totalPages - 1 ? 'disabled' : ''} onclick="AdminPanel.goToPage('${key}', ${currentPage + 1})">Próximo</button>`;

        container.innerHTML = html;
    },

    goToPage(key, page) {
        this.pagination[key].page = page;
        switch (key) {
            case 'doacoes': this.loadDoacoes(); break;
            case 'solicitacoes': this.loadSolicitacoes(); break;
            case 'suporte': this.loadSuporte(); break;
            case 'usuarios': this.loadUsuarios(); break;
        }
    },

    // ==================== SUPER ADMIN — CRIAR/GERENCIAR ADMINS ====================
    setupSuperAdminEvents() {
        // Botão criar admin
        const btnCriar = document.getElementById('btn-criar-admin');
        if (btnCriar) {
            btnCriar.addEventListener('click', () => this.openModal('modal-criar-admin'));
        }

        // Confirmar criação de admin
        const confirmCriar = document.getElementById('modal-criar-admin-confirm');
        if (confirmCriar) {
            confirmCriar.addEventListener('click', () => this.criarAdmin());
        }

        // Confirmar modal de confirmação genérico
        const confirmBtn = document.getElementById('modal-confirmacao-confirm');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => {
                if (typeof this._confirmCallback === 'function') {
                    this._confirmCallback();
                }
                this.closeModal('modal-confirmacao');
                this._confirmCallback = null;
            });
        }
    },

    async criarAdmin() {
        const nome = document.getElementById('criar-admin-nome').value.trim();
        const email = document.getElementById('criar-admin-email').value.trim();
        const senha = document.getElementById('criar-admin-senha').value;
        const tipoPessoa = document.getElementById('criar-admin-tipo').value;
        const documento = document.getElementById('criar-admin-documento').value.trim();

        if (!nome || !email || !documento) {
            showToast('Nome, email e documento são obrigatórios', 'error');
            return;
        }

        try {
            await apiFetch('/api/super-admin/admins', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    nome,
                    email,
                    senha: senha || null,
                    tipoPessoa,
                    documento,
                    role: 'ADMIN'
                })
            });
            showToast('Administrador criado com sucesso!', 'success');
            this.closeModal('modal-criar-admin');
            // Limpar formulário
            document.getElementById('criar-admin-nome').value = '';
            document.getElementById('criar-admin-email').value = '';
            document.getElementById('criar-admin-senha').value = '';
            document.getElementById('criar-admin-documento').value = '';
            this.loadUsuarios();
        } catch (e) {
            showToast(e.message || 'Erro ao criar administrador', 'error');
        }
    },

    async alterarRoleAdmin(id, currentRole) {
        const select = document.getElementById('modal-role-usuario-select');
        select.innerHTML = '<option value="USER">USER</option><option value="ADMIN">ADMIN</option>';
        select.value = currentRole;
        const confirmBtn = document.getElementById('modal-role-usuario-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
        newBtn.addEventListener('click', async () => {
            const novaRole = select.value;
            if (novaRole === currentRole) {
                showToast('Role já é ' + currentRole, 'error');
                return;
            }
            try {
                await apiFetch(`/api/super-admin/admins/${id}/role?novaRole=${novaRole}`, { method: 'PUT' });
                showToast(`Role alterada para ${novaRole}`, 'success');
                this.closeModal('modal-role-usuario');
                this.loadUsuarios();
            } catch (e) {
                showToast(e.message || 'Erro ao alterar role', 'error');
            }
        });
        this.openModal('modal-role-usuario');
    },

    async excluirAdmin(id) {
        try {
            await apiFetch(`/api/super-admin/admins/${id}`, { method: 'DELETE' });
            showToast('Administrador desativado', 'success');
            this.loadUsuarios();
        } catch (e) {
            showToast(e.message || 'Erro ao excluir administrador', 'error');
        }
    },

    // Modal de confirmação genérico
    confirmAction(message, callback) {
        document.getElementById('modal-confirmacao-message').textContent = message;
        this._confirmCallback = callback;
        this.openModal('modal-confirmacao');
    },

    // ==================== HELPERS ====================
    toggleDetail(row, type, id, event) {
        // Ignorar cliques em selects e botões dentro da row
        if (event && (event.target.tagName === 'SELECT' || event.target.tagName === 'BUTTON' || event.target.tagName === 'OPTION' || event.target.closest('.admin-status-select'))) {
            return;
        }
        const detailRow = document.getElementById(`detail-${type}-${id}`);
        if (!detailRow) return;
        const isVisible = detailRow.style.display !== 'none';
        // Fechar todos os outros detalhes da mesma tabela
        const tbody = row.closest('tbody');
        tbody.querySelectorAll('.admin-detail-row').forEach(r => {
            if (r !== detailRow) r.style.display = 'none';
        });
        tbody.querySelectorAll('.admin-row-clickable').forEach(r => {
            r.classList.remove('admin-row-active');
        });
        if (isVisible) {
            detailRow.style.display = 'none';
            row.classList.remove('admin-row-active');
        } else {
            detailRow.style.display = 'table-row';
            row.classList.add('admin-row-active');
        }
    },

    skeletonRows(cols, rows = 3) {
        let html = '';
        for (let r = 0; r < rows; r++) {
            html += '<tr>';
            for (let c = 0; c < cols; c++) {
                html += `<td><div class="skeleton-line"></div></td>`;
            }
            html += '</tr>';
        }
        return html;
    },

    createPillHtml(status) {
        const cls = getStatusClass(status);
        const label = translateStatus(status);
        return `<span class="status-pill ${cls}">${escapeHtml(label)}</span>`;
    }
};

// ==================== BOOT ====================
document.addEventListener('DOMContentLoaded', () => {
    AdminPanel.init();
});
