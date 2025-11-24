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

  const abrirBtn = document.getElementById("abrirModal");
  const modal = document.getElementById("modalCliente");
  const cerrarBtn = document.getElementById("cerrarModal");

  abrirBtn.onclick = () => {
    modal.style.display = "flex";
  };

  cerrarBtn.onclick = () => {
    modal.style.display = "none";
  };

  window.onclick = (e) => {
    if (e.target == modal) {
      modal.style.display = "none";
    }
  };

  function abrirModalEditar() {
    document.getElementById("modal-editar").style.display = "block";
  }

  function cerrarModalEditar() {
    document.getElementById("modal-editar").style.display = "none";
  }

  window.onclick = function(event) {
    const modal = document.getElementById("modal-editar");
    if (event.target == modal) {
      modal.style.display = "none";
    }
  };


