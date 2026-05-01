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
        // Mostra botão de login
        if (profileButton) {
            profileButton.textContent = 'Entrar';
            profileButton.style.display = 'inline-block';
            profileButton.onclick = () => {
                window.location.href = '/login';
            };
        }
    }
});

/**
 * Popula o dropdown "Quero Doar" - Visível para TODOS os usuários logados
 */
function populateQueroDoarDropdown(dropdownItem) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    addDropdownItem(dropdownMenu, 'Nova Doação', '/donate');
    addDropdownItem(dropdownMenu, 'Minhas Doações ⭐', '/minhas-doacoes');

    console.log('nav-visibility: Dropdown Quero Doar populado');
}

/**
 * Popula o dropdown "Solicitar Doação" - Visível apenas para ALUNO e ADMIN
 */
function populateSolicitarDoacaoDropdown(dropdownItem) {
    const dropdownMenu = dropdownItem.querySelector('.dropdown-menu');
    if (!dropdownMenu) return;

    dropdownMenu.innerHTML = '';

    addDropdownItem(dropdownMenu, 'Solicitar Equipamento', '/aluno');
    addDropdownItem(dropdownMenu, 'Meus Pedidos', '/meus-pedidos');

    console.log('nav-visibility: Dropdown Solicitar Doação populado');
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
        addDropdownItem(dropdownMenu, 'Painel Admin', '/admin');

        // Separador
        const separator = document.createElement('li');
        separator.className = 'dropdown-separator';
        dropdownMenu.appendChild(separator);
    }

    // Meu Perfil
    addDropdownItem(dropdownMenu, 'Meu Perfil', '/perfil');

    // Separador
    const separator = document.createElement('li');
    separator.className = 'dropdown-separator';
    dropdownMenu.appendChild(separator);

    // Sair
    const logoutItem = document.createElement('li');
    const logoutLink = document.createElement('a');
    logoutLink.href = '#';
    logoutLink.textContent = 'Sair';
    logoutLink.addEventListener('click', async (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (window.Auth) {
            await Auth.logout('/index');
        } else {
            localStorage.removeItem('loggedInUser');
            window.location.href = '/index';
        }
    });
    logoutItem.appendChild(logoutLink);
    dropdownMenu.appendChild(logoutItem);

    console.log('nav-visibility: Dropdown Minha Conta populado');
}

/**
 * Adiciona um item ao dropdown
 */
function addDropdownItem(menu, text, href) {
    const li = document.createElement('li');
    const a = document.createElement('a');
    a.href = href;
    a.textContent = text;
    li.appendChild(a);
    menu.appendChild(li);
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