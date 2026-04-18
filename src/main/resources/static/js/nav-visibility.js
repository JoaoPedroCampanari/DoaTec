/**
 * Controle de visibilidade do menu de navegação
 * CSS esconde itens por padrão, JavaScript mostra quando autorizado
 * Baseado em role (ADMIN/USER) e tipoPessoa (ALUNO, DOADOR_PF, DOADOR_PJ)
 */
document.addEventListener('DOMContentLoaded', async () => {
    // Verifica sessão com backend
    let loggedInUser = null;
    if (window.Auth) {
        loggedInUser = await Auth.checkSession();
        console.log('nav-visibility: Usuário retornado do Auth.checkSession:', loggedInUser);
    } else {
        const stored = localStorage.getItem('loggedInUser');
        if (stored) {
            try {
                loggedInUser = JSON.parse(stored);
                console.log('nav-visibility: Usuário carregado do localStorage:', loggedInUser);
            } catch (e) {
                loggedInUser = null;
            }
        }
    }

    // Mapeia itens de navegação
    const navItems = {
        minhasDoacoes: document.getElementById('minhasDoacoesNavItem'),
        meusPedidos: document.getElementById('meusPedidosNavItem'),
        precisoComputador: document.getElementById('precisoComputadorNavItem'),
        adminPanel: document.getElementById('adminPanelNavItem')
    };

    console.log('nav-visibility: Elementos de navegação:', {
        minhasDoacoes: navItems.minhasDoacoes ? 'encontrado' : 'NÃO encontrado',
        meusPedidos: navItems.meusPedidos ? 'encontrado' : 'NÃO encontrado',
        precisoComputador: navItems.precisoComputador ? 'encontrado' : 'NÃO encontrado'
    });

    const profileButton = document.getElementById('profileButton');
    const notificationsContainer = document.getElementById('notificationsContainer');

    if (loggedInUser) {
        const role = loggedInUser.role; // 'ADMIN' ou 'USER'
        const tipoPessoa = loggedInUser.tipoPessoa; // 'ALUNO', 'DOADOR_PF', 'DOADOR_PJ'
        const isAdmin = role === 'ADMIN';

        console.log('nav-visibility: Dados do usuário:', { role, tipoPessoa, isAdmin });

        // Exibe notificações para usuários logados
        if (notificationsContainer) {
            notificationsContainer.classList.add('visible');
            // Inicializa sistema de notificações
            if (window.Notifications) {
                Notifications.init();
            }
        }

        // Menu "Minhas Doações" - visível para DOADORES (PF ou PJ)
        if (navItems.minhasDoacoes && (tipoPessoa === 'DOADOR_PF' || tipoPessoa === 'DOADOR_PJ')) {
            console.log('nav-visibility: Mostrando Minhas Doações para', tipoPessoa);
            navItems.minhasDoacoes.classList.add('nav-item-visible');
        }

        // Menus exclusivos de ALUNO
        if (tipoPessoa === 'ALUNO') {
            console.log('nav-visibility: Usuário é ALUNO, mostrando menus de aluno');
            if (navItems.meusPedidos) {
                navItems.meusPedidos.classList.add('nav-item-visible');
                console.log('nav-visibility: meusPedidos visível');
            }
            if (navItems.precisoComputador) {
                navItems.precisoComputador.classList.add('nav-item-visible');
                console.log('nav-visibility: precisoComputador visível');
            }
        }

        // Menu de Admin (se existir)
        if (isAdmin && navItems.adminPanel) {
            navItems.adminPanel.classList.add('nav-item-visible');
        }

        // Botão de perfil
        if (profileButton) {
            if (isAdmin) {
                profileButton.textContent = 'Admin';
                profileButton.onclick = () => {
                    window.location.href = 'admin.html';
                };
            } else {
                profileButton.textContent = 'Perfil';
                profileButton.onclick = () => {
                    window.location.href = 'perfil.html';
                };
            }
        }
    } else {
        // Usuário deslogado - notificações permanecem ocultas (já ocultas por CSS)
        // Botão de login
        if (profileButton) {
            profileButton.textContent = 'Entrar';
            profileButton.onclick = () => {
                window.location.href = 'login.html';
            };
        }
    }
});