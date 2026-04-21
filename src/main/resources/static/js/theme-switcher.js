document.addEventListener('DOMContentLoaded', () => {
    const themeToggleButton = document.getElementById('theme-toggle');
    const html = document.documentElement;

    const applyTheme = (theme) => {
        if (theme === 'dark') {
            html.classList.add('dark-mode');
            themeToggleButton.textContent = '☀️';
        } else {
            html.classList.remove('dark-mode');
            themeToggleButton.textContent = '🌙';
        }
    };

    const savedTheme = localStorage.getItem('theme');

    if (savedTheme) {
        applyTheme(savedTheme);
    } else {
        applyTheme('light');
    }

    themeToggleButton.addEventListener('click', () => {
        const currentTheme = html.classList.contains('dark-mode') ? 'dark' : 'light';
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        applyTheme(newTheme);
        localStorage.setItem('theme', newTheme);
    });
});