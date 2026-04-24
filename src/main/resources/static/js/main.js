/**
 * DoaTec - Main JavaScript
 * Funções utilitárias e componentes globais
 */

// ==================== UTILITIES ====================

/**
 * Escapa HTML para prevenir XSS
 */
function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * Formata uma data para o padrão brasileiro
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString + 'T00:00:00');
    return date.toLocaleDateString('pt-BR');
}

/**
 * Capitaliza primeira letra
 */
function capitalize(str) {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

/**
 * Converte status para classe CSS do pill
 */
function getStatusClass(status) {
    if (!status) return 'status-pending';

    const statusMap = {
        // Doações
        'triagem': 'status-triagem',
        'em triagem': 'status-triagem',
        'em_triagem': 'status-triagem',
        'aguardando coleta': 'status-triagem',
        'aguardando_coleta': 'status-triagem',
        'recebido': 'status-pendente',
        'em análise': 'status-pendente',
        'em_analise': 'status-pendente',
        'aprovado': 'status-aprovado',
        'aprovada': 'status-aprovado',
        'finalizado': 'status-concluido',
        'reprovado': 'status-reprovado',
        'recusado': 'status-reprovado',
        'rejeitado': 'status-reprovado',
        'rejeitada': 'status-reprovado',
        // Solicitações
        'pendente': 'status-pendente',
        'concluído': 'status-concluido',
        'concluido': 'status-concluido',
        'concluída': 'status-concluido',
        'concluida': 'status-concluido',
        // Equipamentos
        'disponivel': 'status-aprovado',
        'disponível': 'status-aprovado',
        'reservado': 'status-triagem',
        'entregue': 'status-entregue',
        // Suporte
        'aberto': 'status-reprovado',
        'em andamento': 'status-triagem',
        'em_andamento': 'status-triagem',
        'resolvido': 'status-concluido',
        'fechado': 'status-pending',
        // General
        'completed': 'status-concluido',
        'delivered': 'status-entregue'
    };

    return statusMap[status.toLowerCase()] || 'status-pending';
}

/**
 * Traduz status para português
 */
function translateStatus(status) {
    if (!status) return 'Desconhecido';

    const translations = {
        // Doações
        'em_triagem': 'Em Triagem',
        'triagem': 'Em Triagem',
        'triage': 'Em Triagem',
        'aguardando_coleta': 'Aguardando Coleta',
        'aguardando coleta': 'Aguardando Coleta',
        'recebido': 'Recebido',
        'em_analise': 'Em Análise',
        'em análise': 'Em Análise',
        'em analise': 'Em Análise',
        'finalizado': 'Finalizado',
        'rejeitada': 'Rejeitada',
        'aprovado': 'Aprovado',
        'aprovada': 'Aprovada',
        'approved': 'Aprovado',
        'rejected': 'Rejeitada',
        'reprovado': 'Reprovado',
        // Solicitações
        'pendente': 'Pendente',
        'pending': 'Pendente',
        'concluído': 'Concluído',
        'concluido': 'Concluído',
        'concluída': 'Concluída',
        'concluida': 'Concluída',
        'completed': 'Concluído',
        // Equipamentos
        'disponivel': 'Disponível',
        'disponível': 'Disponível',
        'reservado': 'Reservado',
        'entregue': 'Entregue',
        'delivered': 'Entregue',
        // Suporte
        'aberto': 'Aberto',
        'em_andamento': 'Em Andamento',
        'em andamento': 'Em Andamento',
        'resolvido': 'Resolvido',
        'fechado': 'Fechado',
        // Conservação
        'novo': 'Novo',
        'excelente': 'Excelente',
        'bom': 'Bom',
        'regular': 'Regular',
        'necessita_reparo': 'Necessita Reparo'
    };

    const lower = status.toLowerCase().replace(/-/g, '_');
    return translations[lower] || capitalize(status.replace(/_/g, ' '));
}

// ==================== SVG ICONS ====================
const Icons = {
    box: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
    </svg>`,

    tool: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
    </svg>`,

    user: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
    </svg>`,

    check: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12"/>
    </svg>`,

    gift: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 12 20 22 4 22 4 12"/>
        <rect x="2" y="7" width="20" height="5"/>
        <line x1="12" y1="22" x2="12" y2="7"/>
        <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/>
        <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
    </svg>`,

    heart: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
    </svg>`,

    calendar: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
    </svg>`,

    inbox: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/>
        <path d="M5.45 5.11L2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"/>
    </svg>`,

    package: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="16.5" y1="9.4" x2="7.5" y2="4.21"/>
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 2 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
        <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
        <line x1="12" y1="22.08" x2="12" y2="12"/>
    </svg>`,

    monitor: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
        <line x1="8" y1="21" x2="16" y2="21"/>
        <line x1="12" y1="17" x2="12" y2="21"/>
    </svg>`,

    empty: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 2 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
        <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
        <line x1="12" y1="22.08" x2="12" y2="12"/>
    </svg>`
};

/**
 * Cria um elemento de ícone SVG
 */
function createIcon(name, className = '', size = 48) {
    const wrapper = document.createElement('div');
    wrapper.className = `stat-icon step-icon ${className}`.trim();
    wrapper.style.width = size + 'px';
    wrapper.style.height = size + 'px';
    wrapper.innerHTML = Icons[name] || Icons.box;
    return wrapper;
}

// ==================== CARD CREATORS ====================

/**
 * Cria um card de estatística com ícone
 */
function createStatCard(value, label, iconName) {
    const card = document.createElement('div');
    card.className = 'stat-card';

    const icon = createIcon(iconName || 'box', 'stat-icon', 48);
    const valueEl = document.createElement('span');
    valueEl.className = 'stat-number';
    valueEl.textContent = value;
    const labelEl = document.createElement('span');
    labelEl.className = 'stat-label';
    labelEl.textContent = label;

    card.appendChild(icon);
    card.appendChild(valueEl);
    card.appendChild(labelEl);

    return card;
}

/**
 * Cria um estado vazio
 */
function createEmptyState(title, message, iconName) {
    const container = document.createElement('div');
    container.className = 'empty-state';

    const icon = createIcon(iconName || 'empty', 'empty-state-icon', 80);
    const titleEl = document.createElement('h3');
    titleEl.textContent = title;
    const messageEl = document.createElement('p');
    messageEl.textContent = message;

    container.appendChild(icon);
    container.appendChild(titleEl);
    container.appendChild(messageEl);

    return container;
}

/**
 * Cria um pill de status
 */
function createStatusPill(status) {
    const pill = document.createElement('span');
    pill.className = `status-pill ${getStatusClass(status)}`;
    pill.textContent = translateStatus(status);
    return pill;
}

/**
 * Cria um card de doação
 */
function createDonationCard(donation) {
    const card = document.createElement('div');
    card.className = 'donation-card';

    // Image/Thumbnail
    const imageDiv = document.createElement('div');
    imageDiv.className = 'donation-image';

    if (donation.foto) {
        const img = document.createElement('img');
        img.src = donation.foto;
        img.alt = 'Imagem da doação';
        img.style.width = '100%';
        img.style.height = '100%';
        img.style.objectFit = 'cover';
        img.style.borderRadius = '12px';
        imageDiv.appendChild(img);
    } else {
        const iconDiv = document.createElement('div');
        iconDiv.style.width = '48px';
        iconDiv.style.height = '48px';
        iconDiv.style.color = 'var(--neutral-400)';
        iconDiv.innerHTML = Icons.package;
        imageDiv.appendChild(iconDiv);
    }

    // Content
    const content = document.createElement('div');
    content.className = 'donation-content';

    // Header with title and status
    const header = document.createElement('div');
    header.className = 'donation-header';

    const title = document.createElement('h3');
    title.className = 'donation-title';
    title.textContent = `Doação #${donation.id}`;

    const statusPill = createStatusPill(donation.status);

    header.appendChild(title);
    header.appendChild(statusPill);

    // Date
    const dateEl = document.createElement('span');
    dateEl.className = 'donation-date';
    dateEl.textContent = formatDate(donation.dataDoacao);

    // Meta
    const meta = document.createElement('div');
    meta.className = 'donation-meta';
    meta.innerHTML = `
        <span class="donation-meta-item"><strong>Entrega:</strong> ${escapeHtml(donation.preferenciaEntrega) || 'Não especificada'}</span>
    `;

    // Items
    let itemsHtml = '';
    if (donation.itens && donation.itens.length > 0) {
        itemsHtml = '<div class="donation-items">';
        donation.itens.forEach(item => {
            itemsHtml += `
                <div class="donation-item">
                    <strong>${escapeHtml(item.tipoItem)}</strong>: ${escapeHtml(item.descricao)}
                </div>
            `;
        });
        itemsHtml += '</div>';
    }

    content.appendChild(header);
    content.appendChild(dateEl);
    content.appendChild(meta);

    if (itemsHtml) {
        const itemsDiv = document.createElement('div');
        itemsDiv.innerHTML = itemsHtml;
        content.appendChild(itemsDiv.firstElementChild);
    }

    card.appendChild(imageDiv);
    card.appendChild(content);

    return card;
}

// ==================== ANIMATIONS ====================

/**
 * Contador animado para números
 */
function animateCounter(element, target, duration = 1500) {
    const start = 0;
    const startTime = performance.now();

    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeProgress = 1 - Math.pow(1 - progress, 3); // easeOutCubic
        const current = Math.floor(start + (target - start) * easeProgress);

        element.textContent = current;

        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.textContent = target;
        }
    }

    requestAnimationFrame(update);
}

// ==================== FORM HELPERS ====================

/**
 * Adiciona loading state a um botão
 */
function setButtonLoading(button, loading, originalText = null) {
    if (loading) {
        button.dataset.originalText = button.textContent;
        button.classList.add('btn-loading');
        button.disabled = true;
        button.textContent = ' Carregando...';
    } else {
        button.classList.remove('btn-loading');
        button.disabled = false;
        button.textContent = originalText || button.dataset.originalText || 'Enviar';
    }
}

/**
 * Mostra notificação toast
 */
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 16px 24px;
        background: ${type === 'success' ? 'var(--color-success, #28a745)' : type === 'error' ? 'var(--color-danger, #dc3545)' : type === 'warning' ? 'var(--color-warning, #ffc107)' : 'var(--color-info, #007bff)'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    toast.textContent = message;

    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Export for global use
window.DoaTec = {
    formatDate,
    capitalize,
    escapeHtml,
    getStatusClass,
    translateStatus,
    createIcon,
    createStatCard,
    createEmptyState,
    createStatusPill,
    createDonationCard,
    animateCounter,
    setButtonLoading,
    showToast,
    Icons
};

// Add slide animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);