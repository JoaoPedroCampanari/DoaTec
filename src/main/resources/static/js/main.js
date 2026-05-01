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
    if (!status) return 'status-pendente';

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
        'rejeitada': 'status-reprovado',
        'rejeitado': 'status-reprovado',
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
        'fechado': 'status-pendente',
        // General
        'completed': 'status-concluido',
        'delivered': 'status-entregue'
    };

    return statusMap[status.toLowerCase()] || 'status-pendente';
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

// ==================== PROFILE HELPERS ====================

function formatUserRole(tipo) {
    if (!tipo) return 'Usuário';
    const roles = {
        'ALUNO': 'Aluno',
        'DOADOR_PF': 'Doador (Pessoa Física)',
        'DOADOR_PJ': 'Doador (Pessoa Jurídica)',
        'ADMIN': 'Administrador'
    };
    return roles[tipo] || tipo;
}

function formatDocument(userData) {
    if (!userData.documento) return '-';
    return userData.documento;
}

function getAvatarColor(name) {
    if (!name) return 'var(--primary-100)';
    const colors = [
        '#E3F2FD', '#E8F5E9', '#FFF3E0', '#F3E5F5',
        '#E0F2F1', '#FBE9E7', '#F1F8E9', '#E1F5FE'
    ];
    const charCode = name.toUpperCase().charCodeAt(0);
    return colors[charCode % colors.length];
}

/**
 * Formata data em formato relativo (ex: "5 min atrás", "2h atrás")
 */
function formatRelativeDate(dateStr) {
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

// Export for global use
window.DoaTec = {
    formatDate,
    capitalize,
    escapeHtml,
    getStatusClass,
    translateStatus,
    animateCounter,
    setButtonLoading,
    showToast,
    formatUserRole,
    formatDocument,
    getAvatarColor,
    formatRelativeDate
};

