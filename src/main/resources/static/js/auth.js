document.addEventListener('DOMContentLoaded', () => {
    const profileButton = document.getElementById('profileButton');

    if (profileButton) {
        const loggedInUser = localStorage.getItem('loggedInUser');

        if (loggedInUser) {
            profileButton.style.display = 'block';
        } else {
            profileButton.style.display = 'none';
        }

        profileButton.addEventListener('click', () => {
            window.location.href = 'perfil.html';
        });
    }
});