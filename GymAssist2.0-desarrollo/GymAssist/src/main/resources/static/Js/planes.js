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

document.querySelectorAll('.open-modal-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    let planId = btn.getAttribute("data-id");
    let modal = document.getElementById(`editModal-${planId}`);

    if (modal) {
      modal.style.display = 'flex';

      // Asigna dinámicamente la acción del formulario
      const form = modal.querySelector("form");
      form.action = `/planes/${planId}/actualizar`;
    } else {
      console.error(`No se encontró el modal para el plan con ID: ${planId}`);
    }
  });
});

// Cerrar modal con la 'X' y con botón Cancelar
const closeModal = (overlay) => overlay.style.display = 'none';

// Botón de cierre (X)
document.querySelectorAll('.close-modal-btn').forEach(btn => btn.addEventListener('click', () => {
  closeModal(btn.closest('.modal-overlay'));
}));

// Botón Cancelar dentro del footer
document.querySelectorAll('.modal-footer a.action_btn').forEach(anchor => anchor.addEventListener('click', (e) => {
  e.preventDefault();
  closeModal(anchor.closest('.modal-overlay'));
}));

// Cerrar al clic fuera
document.querySelectorAll('.modal-overlay').forEach(overlay => overlay.addEventListener('click', e => {
  if (e.target === overlay) closeModal(overlay);
}));