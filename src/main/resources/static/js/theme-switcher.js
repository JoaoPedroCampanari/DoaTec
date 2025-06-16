document.addEventListener('DOMContentLoaded', () => {
    const themeToggleButton = document.getElementById('theme-toggle');
    const body = document.body;

    // Função para aplicar o tema e atualizar o ícone do botão
    const applyTheme = (theme) => {
        if (theme === 'dark') {
            body.classList.add('dark-mode');
            themeToggleButton.textContent = '☀️'; // Ícone de sol para indicar que o próximo clique ativa o modo claro
        } else {
            body.classList.remove('dark-mode');
            themeToggleButton.textContent = '🌙'; // Ícone de lua para indicar que o próximo clique ativa o modo escuro
        }
    };

    // Verifica a preferência salva no localStorage do navegador
    const savedTheme = localStorage.getItem('theme');
    // Verifica a preferência do sistema operacional do usuário
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

    // Aplica o tema salvo, ou o do sistema, ou o padrão (claro)
    if (savedTheme) {
        applyTheme(savedTheme);
    } else if (prefersDark) {
        applyTheme('dark');
    } else {
        applyTheme('light'); // Padrão é o modo claro
    }

    // Adiciona o evento de clique ao botão
    themeToggleButton.addEventListener('click', () => {
        // Verifica qual é o tema atual para determinar o novo tema
        const currentTheme = body.classList.contains('dark-mode') ? 'dark' : 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        // Aplica o novo tema
        applyTheme(newTheme);
        // Salva a nova preferência no localStorage
        localStorage.setItem('theme', newTheme);
    });
});