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
        usuarios: { page: 0, size: 20, total: 0 },
        gestaoAdmins: { page: 0, size: 20, total: 0 }
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

    // Callback do modal de avaliação
    _modalCallback: null,
    // Callback do modal de confirmação
    _confirmCallback: null,

    // ==================== INIT ====================
    async init() {
        const user = await Auth.requireAuth();
        if (!user) return;

        if (!Auth.isAdmin()) {
            showToast('Acesso restrito a administradores', 'error');
            setTimeout(() => window.location.href = 'index.html', 1500);
            return;
        }

        // Mostrar aba "Gestão de Admins" apenas para SUPER_ADMIN
        const isSuperAdmin = Auth.getUserRole() === 'SUPER_ADMIN';
        const tabBtn = document.getElementById('tab-btn-gestao-admins');
        if (tabBtn) tabBtn.style.display = isSuperAdmin ? '' : 'none';

        // Redirecionamento inteligente: ADMIN comum vai pra Suporte & Usuários
        if (!isSuperAdmin && window.location.hash === '#gestao-admins') {
            window.location.hash = '#suporte-usuarios';
        }

        this.setupTabs();
        this.setupSubTabs();
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
                    case 'suporte-usuarios': this.loadSuporte(); break;
                    case 'gestao-admins': this.loadGestaoAdmins(); break;
                }
            });
        });
    },

    setupSubTabs() {
        document.querySelectorAll('.admin-subtab').forEach(subtab => {
            subtab.addEventListener('click', () => {
                const parent = subtab.closest('.admin-tab-content');
                parent.querySelectorAll('.admin-subtab').forEach(s => s.classList.remove('active'));
                parent.querySelectorAll('.admin-subtab-content').forEach(c => c.classList.remove('active'));
                subtab.classList.add('active');
                const target = document.getElementById('subtab-' + subtab.dataset.subtab);
                if (target) target.classList.add('active');

                if (subtab.dataset.subtab === 'suporte') this.loadSuporte();
                if (subtab.dataset.subtab === 'usuarios') this.loadUsuarios();
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

        tbody.innerHTML = doacoes.map(d => `
            <tr>
                <td>#${d.id}</td>
                <td>${escapeHtml(d.doadorNome || '-')}<br><small>${escapeHtml(d.doadorEmail || '')}</small></td>
                <td>${formatDate(d.dataDoacao)}</td>
                <td>${this.createPillHtml(d.status)}</td>
                <td>${escapeHtml(d.descricaoGeral || '-')}</td>
                <td class="admin-actions">
                    ${d.status !== 'FINALIZADO' && d.status !== 'REJEITADA' ?
                        `<button class="btn-approve" onclick="AdminPanel.aprovarDoacao(${d.id})">Aprovar</button>
                         <button class="btn-reject" onclick="AdminPanel.rejeitarDoacao(${d.id})">Rejeitar</button>`
                        : '<span style="color:var(--neutral-400)">-</span>'}
                </td>
            </tr>
        `).join('');
    },

    aprovarDoacao(id) {
        this.openAvaliacaoModal('Aprovar Doação #' + id, 'Confirme a aprovação desta doação.', false, async (observacao) => {
            try {
                const res = await apiFetch('/api/admin/doacoes/' + id + '/aprovar', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ observacao })
                });
                if (!res.ok) throw new Error();
                showToast('Doação aprovada com sucesso', 'success');
                this.loadDoacoes();
            } catch {
                showToast('Erro ao aprovar doação', 'error');
            }
        });
    },

    rejeitarDoacao(id) {
        this.openAvaliacaoModal('Rejeitar Doação #' + id, 'Informe o motivo da rejeição.', true, async (observacao) => {
            try {
                const res = await apiFetch('/api/admin/doacoes/' + id + '/rejeitar', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ observacao })
                });
                if (!res.ok) throw new Error();
                showToast('Doação rejeitada', 'success');
                this.loadDoacoes();
            } catch {
                showToast('Erro ao rejeitar doação', 'error');
            }
        });
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

        tbody.innerHTML = solicitacoes.map(s => `
            <tr>
                <td>#${s.id}</td>
                <td>${escapeHtml(s.alunoNome || '-')}<br><small>${escapeHtml(s.alunoEmail || '')}</small></td>
                <td>${formatDate(s.dataSolicitacao)}</td>
                <td>${escapeHtml(s.preferenciaEquipamento || '-')}</td>
                <td>${this.createPillHtml(s.status)}</td>
                <td class="admin-actions">
                    ${s.status !== 'APROVADA' && s.status !== 'REJEITADA' && s.status !== 'CONCLUIDA' ?
                        `<button class="btn-approve" onclick="AdminPanel.aprovarSolicitacao(${s.id})">Aprovar</button>
                         <button class="btn-reject" onclick="AdminPanel.rejeitarSolicitacao(${s.id})">Rejeitar</button>` : ''}
                    <button class="btn-action" onclick="AdminPanel.verSugestoes(${s.id})">Sugestões</button>
                    ${s.status === 'APROVADA' ? `<button class="btn-approve" onclick="AdminPanel.closeMatchPanel()">Atribuir</button>` : ''}
                </td>
            </tr>
        `).join('');
    },

    aprovarSolicitacao(id) {
        this.openAvaliacaoModal('Aprovar Solicitação #' + id, 'Confirme a aprovação desta solicitação.', false, async (observacao) => {
            try {
                const res = await apiFetch('/api/admin/solicitacoes/' + id + '/aprovar', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ observacao })
                });
                if (!res.ok) throw new Error();
                showToast('Solicitação aprovada', 'success');
                this.loadSolicitacoes();
            } catch {
                showToast('Erro ao aprovar solicitação', 'error');
            }
        });
    },

    rejeitarSolicitacao(id) {
        this.openAvaliacaoModal('Rejeitar Solicitação #' + id, 'Informe o motivo da rejeição.', true, async (observacao) => {
            try {
                const res = await apiFetch('/api/admin/solicitacoes/' + id + '/rejeitar', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ observacao })
                });
                if (!res.ok) throw new Error();
                showToast('Solicitação rejeitada', 'success');
                this.loadSolicitacoes();
            } catch {
                showToast('Erro ao rejeitar solicitação', 'error');
            }
        });
    },

    async verSugestoes(solicitacaoId) {
        try {
            const res = await apiFetch('/api/admin/inventario/sugestoes/' + solicitacaoId);
            if (!res.ok) throw new Error();
            const data = await res.json();

            const panel = document.getElementById('match-panel');
            const content = document.getElementById('match-panel-content');
            panel.style.display = 'block';

            let html = `<p><strong>Aluno:</strong> ${escapeHtml(data.alunoNome || '-')} | <strong>Preferência:</strong> ${escapeHtml(data.preferenciaEquipamento || '-')}</p>`;

            if (!data.equipamentosCompativeis || !data.equipamentosCompativeis.length) {
                html += '<p style="color:var(--neutral-400)">Nenhum equipamento compatível encontrado.</p>';
            } else {
                html += '<table class="admin-table"><thead><tr><th>ID</th><th>Tipo</th><th>Descrição</th><th>Conservação</th><th>Score</th><th>Ação</th></tr></thead><tbody>';
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

            content.innerHTML = html;
        } catch (err) {
            console.error('Sugestões:', err);
            showToast('Erro ao carregar sugestões', 'error');
        }
    },

    closeMatchPanel() {
        const panel = document.getElementById('match-panel');
        if (panel) panel.style.display = 'none';
    },

    async atribuirEquipamento(equipamentoId, solicitacaoId) {
        try {
            const res = await apiFetch('/api/admin/inventario/' + equipamentoId + '/atribuir/' + solicitacaoId, {
                method: 'POST'
            });
            if (!res.ok) throw new Error();
            showToast('Equipamento atribuído com sucesso', 'success');
            this.closeMatchPanel();
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
            <tr>
                <td>#${t.id}</td>
                <td>${escapeHtml(t.autorNome || '-')}<br><small>${escapeHtml(t.autorEmail || '')}</small></td>
                <td>${escapeHtml(t.assunto || '-')}</td>
                <td>${this.createPillHtml(t.status)}</td>
                <td>${t.dataCriacao ? new Date(t.dataCriacao).toLocaleDateString('pt-BR') : '-'}</td>
                <td class="admin-actions">
                    <button class="btn-action" onclick="AdminPanel.responderSuporte(${t.id})">Responder</button>
                    <button class="btn-action" onclick="AdminPanel.alterarStatusSuporte(${t.id})">Status</button>
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

        tbody.innerHTML = usuarios.map(u => `
            <tr>
                <td>#${u.id}</td>
                <td>${escapeHtml(u.nome || '-')}</td>
                <td>${escapeHtml(u.email || '-')}</td>
                <td>${this.createTipoPessoaLabel(u.tipoPessoa)}</td>
                <td><span class="role-badge ${u.role === 'ADMIN' ? 'role-admin' : u.role === 'SUPER_ADMIN' ? 'role-super-admin' : ''}">${escapeHtml(u.role)}</span></td>
                <td>
                    <label class="toggle-active">
                        <input type="checkbox" ${u.ativo ? 'checked' : ''} data-toggle-user-id="${u.id}" data-toggle-user-ativo="${u.ativo}">
                        <span class="toggle-slider"></span>
                    </label>
                </td>
                <td class="admin-actions">
                    <button class="btn-action" data-user-id="${u.id}" data-user-role="${escapeHtml(u.role)}">Alterar Role</button>
                </td>
            </tr>
        `).join('');

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
            case 'gestaoAdmins': this.loadGestaoAdmins(); break;
        }
    },

    // ==================== SUPER ADMIN — GESTÃO DE ADMINS ====================
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

    async loadGestaoAdmins() {
        const { page, size } = this.pagination.gestaoAdmins;
        const tbody = document.getElementById('gestao-admins-tbody');
        if (!tbody) return;
        tbody.innerHTML = this.skeletonRows(7, 3);

        try {
            const res = await apiFetch(`/api/super-admin/admins?page=${page}&size=${size}`);
            const data = await res.json();
            this.pagination.gestaoAdmins.total = data.totalElements;

            if (data.content.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nenhum administrador encontrado.</td></tr>';
                return;
            }

            tbody.innerHTML = data.content.map(a => `
                <tr>
                    <td>${a.id}</td>
                    <td>${escapeHtml(a.nome)}</td>
                    <td>${escapeHtml(a.email)}</td>
                    <td>${this.createTipoPessoaLabel(a.tipoPessoa)}</td>
                    <td><span class="role-badge ${a.role === 'SUPER_ADMIN' ? 'role-super-admin' : 'role-admin'}">${escapeHtml(a.role)}</span></td>
                    <td>
                        <label class="toggle-active">
                            <input type="checkbox" ${a.ativo ? 'checked' : ''} ${a.role === 'SUPER_ADMIN' ? '' : ''}
                                data-toggle-status-id="${a.id}" data-toggle-status-ativo="${a.ativo}">
                            <span class="toggle-slider"></span>
                        </label>
                    </td>
                    <td class="admin-actions">
                        ${a.role !== 'SUPER_ADMIN' ? `
                            <button class="btn-action btn-sm" data-alterar-role-id="${a.id}" data-alterar-role-current="${escapeHtml(a.role)}">Alterar Role</button>
                            <button class="btn-danger btn-sm" data-excluir-id="${a.id}" data-excluir-nome="${escapeHtml(a.nome)}">Excluir</button>
                        ` : '<span class="text-muted">—</span>'}
                    </td>
                </tr>
            `).join('');

            // Event delegation para toggle de status
            tbody.querySelectorAll('[data-toggle-status-id]').forEach(input => {
                input.addEventListener('change', (e) => {
                    const id = e.target.dataset.toggleStatusId;
                    const novoAtivo = e.target.checked;
                    const msg = novoAtivo ? 'Ativar este administrador?' : 'Desativar este administrador?';
                    this.confirmAction(msg, () => this.alterarStatusAdmin(id, novoAtivo));
                });
            });
            // Event delegation para alterar role
            tbody.querySelectorAll('[data-alterar-role-id]').forEach(btn => {
                btn.addEventListener('click', () => {
                    const id = btn.dataset.alterarRoleId;
                    const currentRole = btn.dataset.alterarRoleCurrent;
                    this.alterarRoleAdmin(id, currentRole);
                });
            });
            tbody.querySelectorAll('[data-excluir-id]').forEach(btn => {
                btn.addEventListener('click', () => {
                    const id = btn.dataset.excluirId;
                    const nome = btn.dataset.excluirNome;
                    this.confirmAction(`Excluir administrador ${nome}? Esta ação não pode ser desfeita.`, () => this.excluirAdmin(id));
                });
            });

            this.renderPagination('gestao-admins-pagination', 'gestaoAdmins', data);
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">Erro ao carregar administradores.</td></tr>';
        }
    },

    async criarAdmin() {
        const nome = document.getElementById('criar-admin-nome').value.trim();
        const email = document.getElementById('criar-admin-email').value.trim();
        const senha = document.getElementById('criar-admin-senha').value;
        const tipoPessoa = document.getElementById('criar-admin-tipo').value;
        const documento = document.getElementById('criar-admin-documento').value.trim();

        if (!nome || !email) {
            showToast('Nome e email são obrigatórios', 'error');
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
                    documento: documento || null,
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
            this.loadGestaoAdmins();
        } catch (e) {
            showToast(e.message || 'Erro ao criar administrador', 'error');
        }
    },

    async rebaixarAdmin(id) {
        try {
            await apiFetch(`/api/super-admin/admins/${id}/rebaixar`, { method: 'PUT' });
            showToast('Administrador rebaixado para USER', 'success');
            this.loadGestaoAdmins();
        } catch (e) {
            showToast(e.message || 'Erro ao rebaixar administrador', 'error');
        }
    },

    async alterarStatusAdmin(id, ativo) {
        try {
            await apiFetch(`/api/super-admin/admins/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ativo })
            });
            showToast(`Admin ${ativo ? 'ativado' : 'desativado'} com sucesso`, 'success');
            this.loadGestaoAdmins();
        } catch (e) {
            showToast(e.message || 'Erro ao alterar status do admin', 'error');
            this.loadGestaoAdmins(); // reload to reset toggle
        }
    },

    async alterarRoleAdmin(id, currentRole) {
        const select = document.getElementById('modal-role-usuario-select');
        select.innerHTML = '<option value="USER">USER</option><option value="ADMIN">ADMIN</option>';
        select.value = currentRole;
        this._modalCallback = async () => {
            try {
                const novaRole = select.value;
                await apiFetch(`/api/super-admin/admins/${id}/role?novaRole=${novaRole}`, { method: 'PUT' });
                showToast(`Role alterada para ${novaRole}`, 'success');
                this.loadGestaoAdmins();
            } catch (e) {
                showToast(e.message || 'Erro ao alterar role', 'error');
            }
        };
        this.openModal('modal-role-usuario');
    },

    async excluirAdmin(id) {
        try {
            await apiFetch(`/api/super-admin/admins/${id}`, { method: 'DELETE' });
            showToast('Administrador excluído', 'success');
            this.loadGestaoAdmins();
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
