function toggleSidebar() {
    document.body.classList.toggle('sidebar-collapsed');
  }
  function toggleDropdown(e) {
    e.stopPropagation();
    document.getElementById('dropdown').classList.toggle('show');
  }
  function toggleTheme() {
    document.body.classList.toggle('theme-dark');
  }
  document.addEventListener('click', () => {
    document.getElementById('dropdown').classList.remove('show');
  })
  
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
