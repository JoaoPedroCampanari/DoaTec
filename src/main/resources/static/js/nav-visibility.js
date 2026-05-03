/**
 * Controle de visibilidade do menu de navegação
 * Dois dropdowns por intenção: "Quero Doar" e "Solicitar Doação"
 */

// ==================== SCROLL EFFECT (Glassmorphism) ====================
function handleScroll() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    if (window.scrollY > 20) {
        navbar.classList.add('scrolled');
    } else {
        navbar.classList.remove('scrolled');
    }
}

// Add scroll listener
window.addEventListener('scroll', handleScroll);
// Check on load
document.addEventListener('DOMContentLoaded', handleScroll);

// ==================== MOBILE MENU TOGGLE ====================
function setupMobileMenu() {
    const menuToggle = document.querySelector('.menu-toggle');
    const navLinks = document.querySelector('.nav-links');

    if (!menuToggle || !navLinks) return;

    menuToggle.addEventListener('click', () => {
        menuToggle.classList.toggle('active');
        navLinks.classList.toggle('active');
    });

    // Close menu when clicking a link
    navLinks.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', () => {
            menuToggle.classList.remove('active');
            navLinks.classList.remove('active');
        });
    });
}

document.addEventListener('DOMContentLoaded', setupMobileMenu);

// ==================== SESSION & NAVIGATION ====================
document.addEventListener('DOMContentLoaded', () => {
    // Usa cache síncrono do localStorage para evitar flash de elementos ocultos
    // A validação real da sessão é feita por cada página individualmente
    let loggedInUser = null;
    if (window.Auth) {
        loggedInUser = Auth.getUser();
    } else {
        const stored = localStorage.getItem('loggedInUser');
        if (stored) {
            try {
                loggedInUser = JSON.parse(stored);
            } catch (e) {
                loggedInUser = null;
            }
        }
    }

    // Atualiza cache do backend em segundo plano (não bloqueia a UI)
    if (window.Auth) {
        Auth.checkSession();
    }

    const queroDoarDropdownItem = document.getElementById('queroDoarDropdownItem');
    const solicitarDoacaoDropdownItem = document.getElementById('solicitarDoacaoDropdownItem');
    const suporteDropdownItem = document.getElementById('suporteDropdownItem');
    const minhaContaDropdownItem = document.getElementById('minhaContaDropdownItem');
    const profileButton = document.getElementById('profileButton');
    const notificationsContainer = document.getElementById('notificationsContainer');

    if (loggedInUser) {
        const role = loggedInUser.role; // 'ADMIN', 'SUPER_ADMIN' ou 'USER'
        const tipoPessoa = loggedInUser.tipoPessoa; // 'ALUNO', 'DOADOR_PF', 'DOADOR_PJ'
        const isAdmin = role === 'ADMIN' || role === 'SUPER_ADMIN';
        const isAluno = tipoPessoa === 'ALUNO';

        console.log('nav-visibility: Dados do usuário:', { role, tipoPessoa, isAdmin });

        // Exibe notificações para usuários logados
        if (notificationsContainer) {
            notificationsContainer.classList.add('visible');
            if (window.Notifications) {
                Notifications.init();
            }
        }

        // Mostra dropdown "Quero Doar" - TODOS os usuários logados
        if (queroDoarDropdownItem) {
            queroDoarDropdownItem.classList.add('dropdown-visible');
            populateQueroDoarDropdown(queroDoarDropdownItem);
            setupDropdownToggle(queroDoarDropdownItem);
        }

        // Mostra dropdown "Solicitar Doção" - APENAS ALUNO e ADMIN
        if (solicitarDoacaoDropdownItem) {
            if (isAluno || isAdmin) {
                solicitarDoacaoDropdownItem.classList.add('dropdown-visible');
                populateSolicitarDoacaoDropdown(solicitarDoacaoDropdownItem);
                setupDropdownToggle(solicitarDoacaoDropdownItem);
            }
        }

        // Mostra dropdown "Suporte" - TODOS os usuários logados
        if (suporteDropdownItem) {
            suporteDropdownItem.classList.add('dropdown-visible');
            populateSuporteDropdown(suporteDropdownItem);
            setupDropdownToggle(suporteDropdownItem);
        }

        // Mostra dropdown "Minha Conta" - TODOS os usuários logados
        if (minhaContaDropdownItem) {
            minhaContaDropdownItem.classList.add('dropdown-visible');
            populateMinhaContaDropdown(minhaContaDropdownItem, isAdmin);
            setupDropdownToggle(minhaContaDropdownItem);
        }

        // Esconde botão de perfil antigo (agora está no dropdown)
        if (profileButton) {
            profileButton.style.display = 'none';
        }
    } else {
        // Usuário deslogado - dropdowns permanecem ocultos (já ocultos por CSS)
        // Suporte visível para deslogados (FAQ + formulário)
        if (suporteDropdownItem) {
            suporteDropdownItem.classList.add('dropdown-visible');
            populateSuporteDropdown(suporteDropdownItem);
            setupDropdownToggle(suporteDropdownItem);
        }
        // Mostra botão de login
        if (profileButton) {
            profileButton.textContent = 'Entrar';
            profileButton.style.display = 'inline-block';
            profileButton.onclick = () => {
                window.location.href = '/login.html';
            };
        }
    }
});

/**
 * SVG icons for dropdown items
 */
const DROPDOWN_ICONS = {
    'Nova Doação': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
    'Minhas Doações': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
    '⭐ Minhas Doações': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>',
    'Solicitar Equipamento': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>',
    'Meus Pedidos': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>',
    'Criar um Ticket': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>',
    'Meus Tickets': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>',
    'Perfil': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
    'Sair': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>',
    'Entrar': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>',
    'Painel Admin': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>',
    'Meu Perfil': '<svg class="dropdown-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
};

/**
 * Adiciona um item ao dropdown
 */
function addDropdownItem(menu, text, href) {
    const li = document.createElement('li');
    const a = document.createElement('a');
    a.href = href;
    const iconSvg = DROPDOWN_ICONS[text] || '';
    a.innerHTML = iconSvg + DoaTec.escapeHtml(text);
    li.appendChild(a);
    menu.appendChild(li);
}

/**
 * Popula o dropdown "Quero Doar" - Visível para TODOS os usuários logados
 */
function populateQueroDoarDropdown(dropdownItem) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    addDropdownItem(dropdownMenu, 'Nova Doação', '/donate.html');
    addDropdownItem(dropdownMenu, '⭐ Minhas Doações', '/minhas-doacoes.html');

    console.log('nav-visibility: Dropdown Quero Doar populado');
}

/**
 * Popula o dropdown "Solicitar Doação" - Visível apenas para ALUNO e ADMIN
 */
function populateSolicitarDoacaoDropdown(dropdownItem) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    addDropdownItem(dropdownMenu, 'Solicitar Equipamento', '/aluno.html');
    addDropdownItem(dropdownMenu, 'Meus Pedidos', '/meus-pedidos.html');

    console.log('nav-visibility: Dropdown Solicitar Doação populado');
}

/**
 * Popula o dropdown "Suporte" - Visível para TODOS (logados e deslogados)
 */
function populateSuporteDropdown(dropdownItem) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    addDropdownItem(dropdownMenu, 'Criar um Ticket', '/suporte.html');

    // "Meus Tickets" apenas para logados
    const loggedInUser = Auth.getUser();
    if (loggedInUser) {
        addDropdownItem(dropdownMenu, 'Meus Tickets', '/meus-tickets.html');
    }

    console.log('nav-visibility: Dropdown Suporte populado');
}

/**
 * Popula o dropdown "Minha Conta"
 */
function populateMinhaContaDropdown(dropdownItem, isAdmin) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    // Painel Admin (se for admin)
    if (isAdmin) {
        addDropdownItem(dropdownMenu, 'Painel Admin', '/admin.html');

        // Separador
        const separator = document.createElement('li');
        separator.className = 'dropdown-separator';
        dropdownMenu.appendChild(separator);
    }

    // Meu Perfil
    addDropdownItem(dropdownMenu, 'Meu Perfil', '/perfil.html');

    // Separador
    const separator = document.createElement('li');
    separator.className = 'dropdown-separator';
    dropdownMenu.appendChild(separator);

    // Sair
    const logoutItem = document.createElement('li');
    const logoutLink = document.createElement('a');
    logoutLink.href = '#';
    logoutLink.innerHTML = (DROPDOWN_ICONS['Sair'] || '') + 'Sair';
    logoutLink.addEventListener('click', async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (window.Auth) {
            await Auth.logout('/index.html');
        } else {
            localStorage.removeItem('loggedInUser');
            window.location.href = '/index.html';
        }
    });
    logoutItem.appendChild(logoutLink);
    dropdownMenu.appendChild(logoutItem);

    console.log('nav-visibility: Dropdown Minha Conta populado');
}

/**
 * Configura o toggle do dropdown (abrir/fechar)
 */
function setupDropdownToggle(dropdownItem) {
    const toggle = dropdownItem.querySelector('a');
    const dropdown = dropdownItem.querySelector('.dropdown-menu');

    if (!toggle || !dropdown) return;

    // Toggle ao clicar
    toggle.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();

        // Fecha outros dropdowns abertos
        document.querySelectorAll('.nav-dropdown.dropdown-open').forEach(el => {
            if (el !== dropdownItem) {
                el.classList.remove('dropdown-open');
            }
        });

        dropdownItem.classList.toggle('dropdown-open');
    });

    // Fecha ao clicar fora
    document.addEventListener('click', (e) => {
        if (!dropdownItem.contains(e.target)) {
            dropdownItem.classList.remove('dropdown-open');
        }
    });

    // Fecha ao clicar em um item do menu
    dropdown.addEventListener('click', (e) => {
        if (e.target.tagName === 'A' && !e.target.getAttribute('href').startsWith('#')) {
            dropdownItem.classList.remove('dropdown-open');
        }
    });

    // Fecha ao pressionar ESC
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            dropdownItem.classList.remove('dropdown-open');
        }
    });
}