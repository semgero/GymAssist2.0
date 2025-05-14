function toggleSidebar() {
    document.body.classList.toggle('sidebar-collapsed');
  }
  function toggleDropdown(e) {
    e.stopPropagation();
    document.getElementById('dropdown').classList.toggle('show');
  }

  document.addEventListener('click', () => {
    document.getElementById('dropdown').classList.remove('show');
  })

  // Al cargar la página, aplica el tema guardado:
  document.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') {
      document.body.classList.add('theme-dark');
    }
  });

  // Función única para alternar tema y guardarlo
  function btnTheme() {
    const isDark = document.body.classList.toggle('theme-dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  }

  // Exponemos la función al scope global
  window.btnTheme = btnTheme;