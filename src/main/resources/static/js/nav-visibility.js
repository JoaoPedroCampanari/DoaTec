document.addEventListener('DOMContentLoaded', async () => {
    // Verifica sessão com backend (se Auth estiver disponível)
    let loggedInUser = null;
    if (window.Auth) {
        loggedInUser = await Auth.checkSession();
    } else {
        loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
    }

    // Mapeia todos os itens de navegação condicionais
    const navItems = {
        minhasDoacoes: document.getElementById('minhasDoacoesNavItem'),
        meusPedidos: document.getElementById('meusPedidosNavItem'),
        precisoComputador: document.getElementById('precisoComputadorNavItem')
    };

    const profileButton = document.getElementById('profileButton');

    // Esconde todos por padrão para evitar que pisquem na tela
    for (const key in navItems) {
        if (navItems[key]) {
            navItems[key].style.display = 'none';
        }
    }

    if (loggedInUser) {
        const userType = loggedInUser.tipoUsuario;

        // Itens visíveis para qualquer usuário logado
        if (navItems.minhasDoacoes) {
            navItems.minhasDoacoes.style.display = 'block';
        }

        // Itens visíveis apenas para ALUNO
        if (userType === 'ALUNO') {
            if (navItems.meusPedidos) {
                navItems.meusPedidos.style.display = 'block';
            }
            if (navItems.precisoComputador) {
                navItems.precisoComputador.style.display = 'block';
            }
        }

        // Botão de perfil: "Perfil" -> perfil.html
        if (profileButton) {
            profileButton.textContent = 'Perfil';
            profileButton.onclick = () => {
                window.location.href = 'perfil.html';
            };
        }
    } else {
        // Usuário deslogado: botão de "Login" -> login.html
        if (profileButton) {
            profileButton.textContent = 'Entrar';
            profileButton.onclick = () => {
                window.location.href = 'login.html';
            };
        }
    }
});