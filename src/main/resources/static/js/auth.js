document.addEventListener('DOMContentLoaded', () => {
    const logoutButton = document.getElementById('logoutButton');

    // Verifica se o botão de logout existe nesta página
    if (logoutButton) {
        // Verifica se há um usuário logado no localStorage
        const loggedInUser = localStorage.getItem('loggedInUser');

        if (loggedInUser) {
            // Se houver um usuário logado, mostra o botão Sair.
            logoutButton.style.display = 'block';
        } else {
            // Se não houver usuário logado, esconde o botão Sair.
            logoutButton.style.display = 'none';
        }

        // Adiciona um "ouvinte" de clique ao botão de sair
        logoutButton.addEventListener('click', () => {
            // Remove os dados do usuário do localStorage, "desconectando" a sessão simulada.
            localStorage.removeItem('loggedInUser');
            alert('Você foi desconectado. Até mais!');
            // Redireciona o usuário para a página de login.
            window.location.href = 'login.html';
        });
    }
});