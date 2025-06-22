document.addEventListener('DOMContentLoaded', () => {
    const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));

    // Mapeia todos os itens de navegação condicionais
    const navItems = {
        minhasDoacoes: document.getElementById('minhasDoacoesNavItem'),
        meusPedidos: document.getElementById('meusPedidosNavItem'),
        precisoComputador: document.getElementById('precisoComputadorNavItem')
    };

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
    }
});