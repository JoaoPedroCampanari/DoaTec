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
        inventario: {
            status: '',
            conservacao: ''
        },
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
            { value: 'EM_ANALISE', label: 'Em análise' },
            { value: 'AGUARDANDO_ENTREGA', label: 'Aguardando entrega' },
            { value: 'ENTREGUE', label: 'Entregue' },
            { value: 'CANCELADA', label: 'Cancelada' },
            { value: 'COLETA_AGENDADA', label: 'Coleta agendada' },
            { value: 'RETIRADO', label: 'Retirado' },
            { value: 'RECUSADO', label: 'Recusado' }
        ], 'doacoes');

        // Solicitações
        this.createFilterButtons('solicitacoes-filters', [
            { value: '', label: 'Todos' },
            { value: 'EM_ANALISE', label: 'Em Análise' },
            { value: 'APROVADA', label: 'Aprovada' },
            { value: 'REJEITADA', label: 'Rejeitada' },
            { value: 'CANCELADA', label: 'Cancelada' },
            { value: 'CONCLUIDA', label: 'Concluída' }
        ], 'solicitacoes');

        // Inventário — filtros múltiplos (status + conservação)
        this.createInventarioFilters();

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

    /**
     * Renderiza os filtros de status e conservação no inventário.
     * Cada grupo é independente; combinados via AND no backend.
     */
    createInventarioFilters() {
        const container = document.getElementById('inventario-filters');
        if (!container) return;
        container.innerHTML = '';

        const groups = [
            {
                key: 'status',
                label: 'Status:',
                options: [
                    { value: '', label: 'Todos' },
                    { value: 'DISPONIVEL', label: 'Disponível' },
                    { value: 'RESERVADO', label: 'Reservado' },
                    { value: 'ENTREGUE', label: 'Entregue' }
                ]
            },
            {
                key: 'conservacao',
                label: 'Conservação:',
                options: [
                    { value: '', label: 'Todos' },
                    { value: 'NOVO', label: 'Novo' },
                    { value: 'EXCELENTE', label: 'Excelente' },
                    { value: 'BOM', label: 'Bom' },
                    { value: 'REGULAR', label: 'Regular' }
                ]
            }
        ];

        groups.forEach((group, idx) => {
            const groupEl = document.createElement('div');
            groupEl.style.display = 'flex';
            groupEl.style.gap = '8px';
            groupEl.style.alignItems = 'center';
            groupEl.style.flexWrap = 'wrap';
            if (idx > 0) groupEl.style.marginTop = '8px';

            const label = document.createElement('span');
            label.textContent = group.label;
            label.style.fontWeight = '600';
            label.style.marginRight = '4px';
            groupEl.appendChild(label);

            group.options.forEach(opt => {
                const btn = document.createElement('button');
                btn.className = 'admin-filter-btn' + (opt.value === '' ? ' active' : '');
                btn.textContent = opt.label;
                btn.dataset.value = opt.value;
                btn.addEventListener('click', () => {
                    groupEl.querySelectorAll('.admin-filter-btn').forEach(b => b.classList.remove('active'));
                    btn.classList.add('active');
                    this.filters.inventario[group.key] = opt.value;
                    this.loadInventario();
                });
                groupEl.appendChild(btn);
            });

            container.appendChild(groupEl);
        });
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
            const proximosStatus = this.getProximosStatusDoacao(d.status, d.preferenciaEntrega);
            return `
            <tr class="admin-row-clickable" onclick="AdminPanel.toggleDetail(this, 'doacao', ${d.id}, event)">
                <td>#${d.id}</td>
                <td>${escapeHtml(d.doadorNome || '-')}<br><small>${escapeHtml(d.doadorEmail || '')}</small></td>
                <td>${formatDate(d.dataDoacao)}</td>
                <td>${this.createPillHtml(d.status === 'APROVADO' ? 'Situação anterior' : d.status)}</td>
                <td>${escapeHtml(d.descricaoGeral || '-')}</td>
                <td class="admin-actions admin-doacao-acoes-cell">
                  <div class="admin-doacao-acoes">
                    ${proximosStatus.length ?
                        `<select class="admin-status-select" onclick="event.stopPropagation()" onmousedown="event.stopPropagation()" onchange="event.stopPropagation(); AdminPanel.alterarStatusDoacao(${d.id}, this.value); this.value = ''">
                            <option value="">Alterar status</option>
                            ${proximosStatus.map(st => `<option value="${st}">${st === 'CANCELADA' ? 'Cancelar doação' : this.formatStatusLabel(st)}</option>`).join('')}
                        </select>` : '<select class="admin-status-select" disabled aria-label="Alterar status indisponível" title="Não há alterações de status disponíveis"><option>Alterar status</option></select>'}
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('DOACAO', ${d.id}, 'Doação #${d.id}')">Conversar</button>
                  </div>
                </td>
            </tr>
            <tr class="admin-detail-row" id="detail-doacao-${d.id}" style="display:none">
                <td colspan="6">
                    <div class="admin-detail-content">
                        <p><strong>Entrega:</strong> ${escapeHtml(d.preferenciaEntrega === 'SOLICITAR_RETIRADA' ? 'Retirada no endereço do doador' : translateEntrega(d.preferenciaEntrega))}</p>
                        <p><strong>Endereço:</strong> ${d.preferenciaEntrega === 'PONTO_DE_COLETA'
                            ? 'Av. Presidente Vargas, 2331 - Garça, SP - 17400-000'
                            : (d.enderecoRetirada ? escapeHtml(d.enderecoRetirada) : 'Não informado')}</p>
                        <p><strong>Descrição:</strong> ${escapeHtml(d.descricaoGeral || 'Não informada')}</p>
                        ${d.urlFoto ? (() => {
                            const urls = d.urlFoto.split(',').map(u => u.trim()).filter(Boolean);
                            return `<p><strong>Foto${urls.length > 1 ? 's' : ''}:</strong></p>
                                <div class="admin-doacao-fotos">
                                    ${urls.map(u => `
                                        <a href="${escapeHtml(u)}" target="_blank" rel="noopener" class="admin-doacao-foto-link">
                                            <img src="${escapeHtml(u)}" alt="Foto da doação" class="admin-doacao-foto" loading="lazy" onerror="this.style.display='none'">
                                        </a>
                                    `).join('')}
                                </div>`;
                        })() : ''}
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

    getProximosStatusDoacao(status, modalidade) {
        const inicial = modalidade === 'PONTO_DE_COLETA' ? 'AGUARDANDO_ENTREGA' : modalidade === 'SOLICITAR_RETIRADA' ? 'COLETA_AGENDADA' : null;
        if (status === 'EM_ANALISE') return inicial ? [inicial, 'RECUSADO'] : ['RECUSADO'];
        if (status === 'APROVADO') return modalidade === 'PONTO_DE_COLETA' ? ['AGUARDANDO_ENTREGA', 'ENTREGUE'] : modalidade === 'SOLICITAR_RETIRADA' ? ['COLETA_AGENDADA', 'RETIRADO'] : [];
        if (status === 'AGUARDANDO_ENTREGA' && modalidade === 'PONTO_DE_COLETA') return ['ENTREGUE', 'CANCELADA'];
        if (status === 'COLETA_AGENDADA' && modalidade === 'SOLICITAR_RETIRADA') return ['RETIRADO', 'CANCELADA'];
        return [];
    },

    formatStatusLabel(status) {
        const labels = {
            'EM_ANALISE': 'Em análise',
            'APROVADO': 'Aprovar',
            'AGUARDANDO_ENTREGA': 'Aguardando entrega',
            'ENTREGUE': 'Entregue',
            'CANCELADA': 'Cancelada',
            'COLETA_AGENDADA': 'Coleta agendada',
            'RETIRADO': 'Retirado',
            'RECUSADO': 'Recusar',
            'EM_TRIAGEM': 'Em Triagem',
            'AGUARDANDO_COLETA': 'Aguardando Coleta',
            'RECEBIDO': 'Recebido',
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
        const isRejeicao = novoStatus === 'RECUSADO';
        this.openAvaliacaoModal(
            'Alterar Status — Doação #' + id,
            isRejeicao ? 'Informe o motivo da recusa. Esta ação é permanente e não poderá ser desfeita.' : novoStatus === 'CANCELADA' ? 'Confirme o cancelamento da doação. Esta ação é permanente e não poderá ser desfeita.' : 'Confirme a alteração para ' + this.formatStatusLabel(novoStatus) + '.',
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
            },
            isRejeicao || novoStatus === 'CANCELADA'
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
                <td>${escapeHtml(s.alunoNome || '-')}<br><small>RA: ${escapeHtml(s.alunoRa || '-')}</small></td>
                <td>${formatDate(s.dataSolicitacao)}</td>
                <td>${escapeHtml(s.preferenciaEquipamento || '-')}</td>
                <td>${this.createPillHtml(s.status)}</td>
                <td class="admin-actions admin-solicitacao-acoes-cell">
                    <div class="admin-solicitacao-acoes">
                    ${proximosStatus.length ?
                        `<select class="admin-status-select" onclick="event.stopPropagation()" onmousedown="event.stopPropagation()" onchange="event.stopPropagation(); AdminPanel.alterarStatusSolicitacao(${s.id}, this.value); this.value = ''">
                            <option value="">Alterar status</option>
                            ${proximosStatus.map(st => `<option value="${st}">${st === 'CANCELADA' ? 'Cancelar solicitação' : this.formatStatusLabel(st)}</option>`).join('')}
                        </select>` : '<select class="admin-status-select" disabled aria-label="Alterar status indisponível" title="Não há alterações de status disponíveis"><option>Alterar status</option></select>'}
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('SOLICITACAO', ${s.id}, 'Pedido #${s.id}')">Conversar</button>
                    </div>
                </td>
            </tr>
            <tr class="admin-detail-row" id="detail-solicitacao-${s.id}" style="display:none">
                <td colspan="6">
                    <div class="admin-detail-content">
                        <p><strong>Preferência:</strong> ${escapeHtml(s.preferenciaEquipamento || 'Não informada')}</p>
                        ${s.justificativa ? `<p><strong>Justificativa:</strong> ${escapeHtml(s.justificativa)}</p>` : ''}
                        ${s.urlComprovante ? (() => {
                            const url = s.urlComprovante.trim();
                            const isPdf = /\.pdf(\?.*)?$/i.test(url);
                            if (isPdf) {
                                return `<p><strong>Comprovante de renda:</strong></p>
                                    <div class="admin-doacao-fotos">
                                        <a href="${escapeHtml(url)}" target="_blank" rel="noopener" class="admin-comprovante-pdf">
                                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="32" height="32"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="9" y1="15" x2="15" y2="15"/><line x1="9" y1="11" x2="15" y2="11"/></svg>
                                            <span>Abrir PDF</span>
                                        </a>
                                    </div>`;
                            }
                            return `<p><strong>Comprovante de renda:</strong></p>
                                <div class="admin-doacao-fotos">
                                    <a href="${escapeHtml(url)}" target="_blank" rel="noopener" class="admin-doacao-foto-link">
                                        <img src="${escapeHtml(url)}" alt="Comprovante de renda" class="admin-doacao-foto" loading="lazy" onerror="this.style.display='none'">
                                    </a>
                                </div>`;
                        })() : ''}
                        ${s.observacaoAdmin ? `<p><strong>Obs. Admin:</strong> ${escapeHtml(s.observacaoAdmin)}</p>` : ''}
                        ${s.adminAvaliadorNome ? `<p><strong>Avaliador:</strong> ${escapeHtml(s.adminAvaliadorNome)}${s.dataAvaliacao ? ' — ' + formatDate(s.dataAvaliacao) : ''}</p>` : ''}
                        <div id="equipamentos-solicitacao-${s.id}" data-status="${escapeHtml(s.status)}" style="margin-top:12px"><strong>Equipamentos vinculados:</strong> Carregando...</div>
                        ${s.status === 'APROVADA' ? `
                            <div style="margin-top:12px">
                                <button class="btn-approve" onclick="AdminPanel.openAtribuirModal(${s.id}, false)">Atribuir equipamentos</button>
                            </div>` : ''}
                    </div>
                </td>
            </tr>
        `;}).join('');
    },

    async carregarEquipamentosSolicitacao(id) {
        const container = document.getElementById('equipamentos-solicitacao-' + id);
        if (!container) return;
        try {
            const response = await apiFetch('/api/admin/solicitacoes/' + id + '/equipamentos');
            const equipamentos = await response.json();
            if (!container.isConnected) return;
            container.innerHTML = '<strong>Equipamentos vinculados:</strong>';
            if (!equipamentos.length) { container.append(' Nenhum.'); return; }
            equipamentos.forEach(eq => {
                const linha = document.createElement('div');
                linha.style.cssText = 'display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-top:8px;overflow-wrap:anywhere';
                const texto = document.createElement('span');
                texto.textContent = '#' + eq.id + ' — ' + (eq.tipo || '-') + ' · ' + (eq.descricao || '-') + ' (' + translateStatus(eq.status) + ')';
                linha.append(texto);
                if (container.dataset.status === 'APROVADA' && eq.status === 'RESERVADO') {
                    const botao = document.createElement('button');
                    botao.type = 'button'; botao.className = 'btn-reject'; botao.textContent = 'Remover';
                    botao.onclick = () => this.removerEquipamentoSolicitacao(id, eq.id, equipamentos.length === 1);
                    linha.append(botao);
                }
                container.append(linha);
            });
        } catch (err) {
            if (container.isConnected) container.innerHTML = '<strong>Equipamentos vinculados:</strong> Não foi possível carregar.';
        }
    },

    async removerEquipamentoSolicitacao(id, equipamentoId, ultimo) {
        const mensagem = ultimo
            ? 'Ao remover o último equipamento, ele voltará ao inventário e a solicitação voltará para análise. Deseja continuar?'
            : 'Remover este equipamento da solicitação e devolvê-lo ao inventário?';
        if (!await DoaTec.confirmarAcao({ titulo: 'Remover equipamento?', mensagem, botao: 'Remover equipamento' })) return;
        try {
            await apiFetch('/api/admin/solicitacoes/' + id + '/equipamentos/' + equipamentoId + '?ultimoEsperado=' + ultimo, { method: 'DELETE' });
            showToast('Equipamento removido da solicitação.', 'success');
            if (ultimo) this.loadSolicitacoes();
            else this.carregarEquipamentosSolicitacao(id);
            this.loadInventario();
        } catch (err) { showToast(err.message || 'Não foi possível remover o equipamento.', 'error'); this.carregarEquipamentosSolicitacao(id); }
    },

    getProximosStatusSolicitacao(status) {
        const transicoes = {
            'EM_ANALISE': ['APROVADA', 'REJEITADA'],
            'APROVADA': ['CONCLUIDA', 'CANCELADA'],
            'REJEITADA': []
        };
        return transicoes[status] || [];
    },

    alterarStatusSolicitacao(id, novoStatus) {
        if (!novoStatus) return;
        if (novoStatus === 'APROVADA') {
            this.openAtribuirModal(id, true);
            return;
        }
        this.openAvaliacaoModal(
            'Alterar Status — Solicitação #' + id,
            novoStatus === 'REJEITADA' ? 'Informe o motivo da rejeição. Esta ação é permanente e não poderá ser desfeita.' : novoStatus === 'CANCELADA' ? 'Confirme o cancelamento. Equipamentos reservados serão liberados; se já houver entrega, o cancelamento não será permitido.' : novoStatus === 'CONCLUIDA' ? 'Ao concluir, todos os equipamentos reservados desta solicitação serão marcados como entregues no inventário.' : 'Confirme a alteração para ' + this.formatStatusLabel(novoStatus) + '.',
            novoStatus === 'REJEITADA',
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
                    if (novoStatus === 'CONCLUIDA' || novoStatus === 'CANCELADA') this.loadInventario();
                } catch (e) {
                    showToast(e.message || 'Erro ao alterar status', 'error');
                }
            },
            novoStatus === 'REJEITADA' || novoStatus === 'CANCELADA'
        );
    },

    // A aprovação e a atribuição de múltiplos equipamentos usam o mesmo seletor.
    openAtribuirModal(solicitacaoId, aprovar = false) {
        this._atribuirSolicitacaoId = solicitacaoId;
        this._aprovarAoVincular = aprovar;
        this._equipamentosSelecionados = new Set();
        this._atribuicaoEmAndamento = false;
        this._buscaAtribuicaoSeq = (this._buscaAtribuicaoSeq || 0) + 1;
        document.getElementById('modal-atribuir-solicitacao').textContent = '#' + solicitacaoId;
        document.getElementById('modal-atribuir-titulo').textContent = aprovar ? 'Aprovar solicitação' : 'Atribuir equipamentos à solicitação';
        const busca = document.getElementById('modal-atribuir-busca');
        busca.value = '';
        document.getElementById('modal-atribuir-observacao').value = '';
        document.getElementById('modal-atribuir-observacao-grupo').hidden = !aprovar;
        const confirmar = document.getElementById('modal-atribuir-confirmar');
        confirmar.textContent = aprovar ? 'Aprovar e vincular' : 'Atribuir selecionados';
        confirmar.onclick = () => this.confirmarAtribuicao();
        this.atualizarSelecaoEquipamentos();
        this.openModal('modal-atribuir');
        this.buscarEquipamentosParaAtribuir();
        clearTimeout(this._atribuirDebounce);
        busca.oninput = () => {
            clearTimeout(this._atribuirDebounce);
            this._atribuirDebounce = setTimeout(() => this.buscarEquipamentosParaAtribuir(), 250);
        };
    },

    atualizarSelecaoEquipamentos() {
        const quantidade = this._equipamentosSelecionados.size;
        document.getElementById('modal-atribuir-contagem').textContent = quantidade === 0 ? 'Nenhum equipamento selecionado.' : quantidade + (quantidade === 1 ? ' equipamento selecionado.' : ' equipamentos selecionados.');
        document.getElementById('modal-atribuir-confirmar').disabled = quantidade === 0 || this._atribuicaoEmAndamento;
    },

    async buscarEquipamentosParaAtribuir() {
        const sequencia = ++this._buscaAtribuicaoSeq;
        const q = document.getElementById('modal-atribuir-busca').value.trim();
        const params = new URLSearchParams();
        if (q) params.set('preferencia', q);
        const tbody = document.getElementById('modal-atribuir-tbody');
        tbody.innerHTML = '<tr><td colspan="5" class="admin-empty">Buscando...</td></tr>';
        try {
            const res = await apiFetch('/api/admin/inventario/disponiveis?' + params);
            const equipamentos = await res.json();
            if (sequencia !== this._buscaAtribuicaoSeq) return;
            if (!equipamentos.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="admin-empty">Nenhum equipamento disponível encontrado.</td></tr>';
                return;
            }
            tbody.innerHTML = equipamentos.map(eq => `
                <tr>
                    <td>#${eq.id}</td>
                    <td>${escapeHtml(eq.tipo)}</td>
                    <td>${escapeHtml(eq.descricao)}</td>
                    <td>${escapeHtml(eq.estadoConservacao || '-')}</td>
                    <td><input type="checkbox" value="${Number(eq.id)}" aria-label="Selecionar equipamento ${Number(eq.id)}" ${this._equipamentosSelecionados.has(Number(eq.id)) ? 'checked' : ''}></td>
                </tr>`).join('');
            tbody.querySelectorAll('input[type="checkbox"]').forEach(campo => {
                campo.addEventListener('change', () => {
                    const id = Number(campo.value);
                    if (campo.checked) this._equipamentosSelecionados.add(id);
                    else this._equipamentosSelecionados.delete(id);
                    this.atualizarSelecaoEquipamentos();
                });
            });
        } catch (e) {
            if (sequencia === this._buscaAtribuicaoSeq) tbody.innerHTML = '<tr><td colspan="5" class="admin-empty">Erro ao carregar equipamentos.</td></tr>';
        }
    },

    async confirmarAtribuicao() {
        const id = this._atribuirSolicitacaoId;
        const equipamentos = Array.from(this._equipamentosSelecionados);
        if (!id || !equipamentos.length || this._atribuicaoEmAndamento) return;
        this._atribuicaoEmAndamento = true;
        const aprovar = this._aprovarAoVincular;
        const botao = document.getElementById('modal-atribuir-confirmar');
        botao.disabled = true;
        try {
            const acao = aprovar ? 'aprovar-com-equipamentos' : 'atribuir-equipamentos';
            await apiFetch('/api/admin/solicitacoes/' + id + '/' + acao, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ equipamentos, observacao: document.getElementById('modal-atribuir-observacao').value.trim() })
            });
            showToast(aprovar ? 'Solicitação aprovada e equipamentos vinculados.' : 'Equipamentos vinculados.', 'success');
            this.closeModal('modal-atribuir');
            this.loadSolicitacoes();
            this.loadInventario();
        } catch (e) {
            showToast(e.message || 'Não foi possível vincular os equipamentos.', 'error');
            this._equipamentosSelecionados.clear();
            this.buscarEquipamentosParaAtribuir();
        } finally { this._atribuicaoEmAndamento = false; this.atualizarSelecaoEquipamentos(); }
    },

    // ==================== INVENTÁRIO ====================
    async loadInventario() {
        const f = this.filters.inventario;
        const params = new URLSearchParams();
        if (f.status) params.set('status', f.status);
        if (f.conservacao) params.set('conservacao', f.conservacao);

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
            tbody.innerHTML = '<tr><td colspan="7" class="admin-empty">Nenhum equipamento encontrado</td></tr>';
            return;
        }

        // Mantém referência aos objetos completos para o modal de edição
        this._inventarioCache = {};
        equipamentos.forEach(eq => { this._inventarioCache[eq.id] = eq; });

        tbody.innerHTML = equipamentos.map(eq => {
            const disponivel = eq.status === 'DISPONIVEL';
            const reservado = eq.status === 'RESERVADO';
            const acoes = [];
            if (reservado) acoes.push(`<button class="btn-approve" onclick="AdminPanel.marcarEntregue(${eq.id})">Marcar Entregue</button>`);
            acoes.push(eq.status === 'ENTREGUE' ? `<button class="btn-action" disabled title="Equipamentos entregues não podem ser editados">Editar</button>` : `<button class="btn-action" onclick="AdminPanel.openEquipamentoModal(${eq.id})">Editar</button>`);
            if (disponivel) acoes.push(`<button class="btn-reject" onclick="AdminPanel.deleteEquipamento(${eq.id})">Excluir</button>`);
            return `
            <tr>
                <td>#${eq.id}</td>
                <td>${this.createConservacaoPillHtml(eq.estadoConservacao)}</td>
                <td>${escapeHtml(eq.tipo || '-')}</td>
                <td>${escapeHtml(eq.descricao || '-')}</td>
                <td>${this.createPillHtml(eq.status)}</td>
                <td>${eq.alunoDestinatarioId ? `Solicitação #${escapeHtml(String(eq.solicitacaoDestinoId || '-'))}<br>${escapeHtml(eq.alunoDestinatarioNome || '-')}<br><small>RA: ${escapeHtml(eq.alunoDestinatarioDocumento || '-')}</small>` : '-'}</td>
                <td class="admin-inventario-acoes-cell"><div class="admin-inventario-acoes">${acoes.join(' ')}</div></td>
            </tr>`;
        }).join('');
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

    // ==================== MODAL CRIAR/EDITAR EQUIPAMENTO ====================

    /**
     * Abre o modal de equipamento. Sem id = modo criar; com id = modo editar.
     */
    async openEquipamentoModal(id) {
        const isEdit = id != null;
        const eq = isEdit ? (this._inventarioCache && this._inventarioCache[id]) : null;

        document.getElementById('modal-equipamento-title').textContent =
            isEdit ? `Editar equipamento #${id}` : 'Adicionar equipamento';
        document.getElementById('modal-equipamento-id').value = isEdit ? id : '';

        // Pré-preenche se editando
        const tipoEl = document.getElementById('modal-equipamento-tipo');
        const descEl = document.getElementById('modal-equipamento-descricao');
        const consEl = document.getElementById('modal-equipamento-conservacao');

        if (isEdit && eq) {
            tipoEl.value = eq.tipo || '';
            descEl.value = eq.descricao || '';
            // estadoConservacao no response vem como descrição ("Bom estado..."), não como enum.
            // Re-deriva do select pelo valor que o servidor exibirá.
            consEl.value = this._inferConservacaoEnum(eq.estadoConservacao) || 'BOM';
        } else {
            tipoEl.value = '';
            descEl.value = '';
            consEl.value = 'BOM';
        }

        this.openModal('modal-equipamento');

        // Configura o botão de confirmar (substitui handler para evitar acúmulo)
        const confirmBtn = document.getElementById('modal-equipamento-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);
        newBtn.addEventListener('click', () => this.submitEquipamento());
    },

    /**
     * Mapeia a descrição do estado de conservação (vinda do response) de volta
     * para o nome do enum. As descrições estão definidas em EstadoConservacao.
     */
    _inferConservacaoEnum(descricao) {
        if (!descricao) return null;
        const d = descricao.toLowerCase();
        if (d.includes('novo')) return 'NOVO';
        if (d.includes('excelente')) return 'EXCELENTE';
        if (d.includes('bom')) return 'BOM';
        if (d.includes('regular')) return 'REGULAR';
        return null;
    },

    async submitEquipamento() {
        const id = document.getElementById('modal-equipamento-id').value;
        const isEdit = !!id;

        const body = {
            tipo: document.getElementById('modal-equipamento-tipo').value.trim(),
            descricao: document.getElementById('modal-equipamento-descricao').value.trim(),
            estadoConservacao: document.getElementById('modal-equipamento-conservacao').value,
            preservarDoacao: isEdit
        };

        if (!body.tipo || !body.descricao) {
            showToast('Tipo e descrição são obrigatórios', 'warning');
            return;
        }

        const url = isEdit ? '/api/admin/inventario/' + id : '/api/admin/inventario';
        const method = isEdit ? 'PUT' : 'POST';

        try {
            const res = await apiFetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Erro ao salvar equipamento');
            }
            showToast(isEdit ? 'Equipamento atualizado' : 'Equipamento cadastrado', 'success');
            this.closeModal('modal-equipamento');
            this.loadInventario();
        } catch (err) {
            showToast(err.message || 'Erro ao salvar equipamento', 'error');
        }
    },

    async deleteEquipamento(id) {
        this.confirmAction('Excluir equipamento #' + id + '? Esta ação só é permitida para equipamentos disponíveis.', async () => {
            try {
                const res = await apiFetch('/api/admin/inventario/' + id, { method: 'DELETE' });
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || 'Erro ao excluir');
                }
                showToast('Equipamento excluído', 'success');
                this.loadInventario();
            } catch (err) {
                showToast(err.message || 'Erro ao excluir equipamento', 'error');
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
                <td>${escapeHtml(this.formatarAssuntoSuporte(t.assunto))}</td>
                <td>${this.createPillHtml(t.status)}</td>
                <td>${t.dataCriacao ? new Date(t.dataCriacao).toLocaleDateString('pt-BR') : '-'}</td>
                <td class="admin-suporte-acoes-cell"><div class="admin-suporte-acoes">
                    ${!['RESOLVIDO', 'FECHADO'].includes(t.status) && !(t.resposta && t.resposta.trim()) ? `<button class="btn-action" onclick="event.stopPropagation(); AdminPanel.responderSuporte(${t.id})">Responder</button>` : '<button class="btn-action" disabled title="Ticket já respondido ou finalizado. Utilize o chat para continuar o atendimento.">Responder</button>'}
                    ${t.status === 'EM_ANDAMENTO' ? `<button class="btn-action" onclick="event.stopPropagation(); AdminPanel.alterarStatusSuporte(${t.id})">Status</button>` : `<button class="btn-action" disabled title="${t.status === 'ABERTO' ? 'Responda ao ticket para iniciá-lo.' : 'Ticket finalizado.'}">Status</button>`}
                    <button class="btn-action" onclick="event.stopPropagation(); ChatWidget.open('SUPORTE', ${t.id}, 'Ticket #${t.id}')">Conversar</button>
                </div></td>
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

    formatarAssuntoSuporte(assunto) {
        const original = (assunto || '').trim();
        const nomes = { duvida: 'Dúvida geral', doacao: 'Problema com doação', solicitacao: 'Problema com solicitação', conta: 'Problema com minha conta', sugestao: 'Sugestão', outro: 'Outro' };
        return nomes[original.toLowerCase()] || (original ? original.charAt(0).toLocaleUpperCase('pt-BR') + original.slice(1) : 'Sem assunto');
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
            if (!await DoaTec.confirmarAcao({ titulo: 'Enviar resposta?', mensagem: 'Ao enviar a resposta, o ticket ficará em andamento.', botao: 'Enviar resposta' })) return;
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
        select.value = 'RESOLVIDO';
        const confirmBtn = document.getElementById('modal-status-suporte-confirm');
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        newBtn.addEventListener('click', async () => {
            try {
                const status = select.value;
                if (['RESOLVIDO', 'FECHADO'].includes(status) && !await DoaTec.confirmarAcao({ titulo: 'Alterar status do ticket?', mensagem: 'Este status é permanente e não poderá ser alterado.', botao: 'Confirmar' })) return;
                const res = await apiFetch('/api/admin/suporte/' + id + '/status?status=' + status, {
                    method: 'PUT'
                });
                if (!res.ok) throw new Error();
                showToast('Status alterado', 'success');
                this.closeModal('modal-status-suporte');
                this.loadSuporte();
            } catch (err) {
                showToast(err.message || 'Erro ao alterar status', 'error');
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
    openAvaliacaoModal(title, description, isReject, callback, acaoCritica = isReject) {
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
        newBtn.className = acaoCritica ? 'btn-reject' : 'btn-approve';
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
        // Máscara dinâmica no campo documento do modal criar admin
        const tipoSelect = document.getElementById('criar-admin-tipo');
        const docInput = document.getElementById('criar-admin-documento');
        if (tipoSelect && docInput && typeof IMask !== 'undefined') {
            let docMask = IMask(docInput, { mask: '000.000.000-00' }); // default CPF
            tipoSelect.addEventListener('change', () => {
                docMask.destroy();
                if (tipoSelect.value === 'DOADOR_PF') {
                    docMask = IMask(docInput, { mask: '000.000.000-00' });
                } else if (tipoSelect.value === 'DOADOR_PJ') {
                    docMask = IMask(docInput, { mask: '00.000.000/0000-00' });
                } else if (tipoSelect.value === 'ALUNO') {
                    docMask = IMask(docInput, { mask: '0000000000000' });
                }
                this._docMask = docMask;
            });
            this._docMask = docMask;
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
        const docInput = document.getElementById('criar-admin-documento');
        const documento = this._docMask ? this._docMask.unmaskedValue : docInput.value.trim();

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
            if (this._docMask) this._docMask.value = '';
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
            if (type === 'solicitacao') this.carregarEquipamentosSolicitacao(id);
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

    createConservacaoPillHtml(conservacao) {
        const classes = { NOVO: 'conservacao-novo', EXCELENTE: 'conservacao-excelente', BOM: 'conservacao-bom', REGULAR: 'conservacao-regular' };
        const classe = classes[conservacao] || 'status-pendente';
        return `<span class="status-pill ${classe}">${escapeHtml(translateStatus(conservacao))}</span>`;
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
