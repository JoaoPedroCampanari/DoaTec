document.addEventListener('DOMContentLoaded', () => {
    const profileButton = document.getElementById('profileButton'); // Alterado para profileButton

    if (profileButton) {
        const loggedInUser = localStorage.getItem('loggedInUser');

        if (loggedInUser) {
            profileButton.style.display = 'block'; // Mostra o botão se estiver logado
        } else {
            profileButton.style.display = 'none'; // Esconde o botão se não estiver logado
        }

        profileButton.addEventListener('click', () => {
            // Ao clicar em Perfil, redireciona para a página de perfil
            window.location.href = 'perfil.html';
        });
    }

    // A lógica de logout será movida para a página perfil.html
    // Se você tiver outros botões de logout em outras páginas, precisará mantê-los.
});