document.addEventListener('DOMContentLoaded', function () {

  const urlParams = new URLSearchParams(window.location.pathname);
  // Get projectId from URL like /projects/1. Wait, window.location.pathname is /projects/1
  const parts = window.location.pathname.split('/');
  const projectId = parts[parts.length - 1] === 'projects' ? null : parts[parts.length - 1];
  const isEdit = projectId !== null && projectId !== 'projects' && !isNaN(parseInt(projectId));

  const form       = document.getElementById('projectForm');
  const nameInput  = document.getElementById('projectName');
  const descInput  = document.getElementById('projectDesc');
  const submitBtn  = document.getElementById('submitBtn');
  const deleteBtn  = document.getElementById('deleteBtn');

  if (descInput) {
    descInput.addEventListener('input', function () {
      updateCounter(this);
    });
  }

  function updateCounter(el) {
    const len = el.value.length;
    const max = parseInt(el.getAttribute('maxlength') || 500);
    const counter = document.getElementById('charCounter');
    if(counter) {
        counter.textContent = len + ' / ' + max;
        counter.className   = 'char-counter';
        if (len > max * 0.9) counter.classList.add('over');
        else if (len > max * 0.7) counter.classList.add('warn');
    }
  }

  window.updateCounter = updateCounter;

  if (nameInput) {
    nameInput.addEventListener('input', function () {
      if (this.value.trim()) clearError(this, 'nameError');
    });

    nameInput.addEventListener('blur', function () {
      if (!this.value.trim()) showError(this, 'nameError', 'Project name is required');
    });
  }

  if (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!validateName()) return;

      submitBtn.disabled = true;
      submitBtn.textContent = isEdit ? 'Saving...' : 'Creating...';

      const formData = new FormData(form);
      const data = Object.fromEntries(formData.entries());
      const url = isEdit ? '/projects/' + projectId : '/projects';
      const method = isEdit ? 'PUT' : 'POST';

      fetch(url, {
          method: method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
      })
      .then(res => {
          window.location.href = '/dashboard?success=' + (isEdit ? 'updated' : 'created');
      })
      .catch(err => {
          submitBtn.disabled = false;
          submitBtn.textContent = isEdit ? 'Update project' : 'Create project';
      });
    });
  }

  if (deleteBtn) {
    deleteBtn.addEventListener('click', function (e) {
      e.preventDefault();
      if (!confirm('"' + (nameInput.value || 'this project') + '" will be deleted.\nThis action cannot be undone!')) return;

      deleteBtn.disabled    = true;
      deleteBtn.textContent = 'Deleting...';

      fetch('/projects/' + projectId, { method: 'DELETE' })
      .then(res => {
          window.location.href = '/dashboard?success=deleted';
      })
      .catch(err => {
          deleteBtn.disabled = false;
          deleteBtn.textContent = 'Delete this project';
      });
    });
  }

  let changed = false;
  if(nameInput) nameInput.addEventListener('input', function () { changed = true; });
  if(descInput) descInput.addEventListener('input', function () { changed = true; });

  window.addEventListener('beforeunload', function (e) {
    if (changed && submitBtn && !submitBtn.disabled) {
      e.preventDefault();
      e.returnValue = '';
    }
  });

  if (form) form.addEventListener('submit', function () { changed = false; });

  function validateName() {
    if(!nameInput) return true;
    const v = nameInput.value.trim();
    if (!v) {
      showError(nameInput, 'nameError', 'Project name is required');
      return false;
    }
    if (v.length < 2) {
      showError(nameInput, 'nameError', 'Project name must contain at least 2 characters');
      return false;
    }
    clearError(nameInput, 'nameError');
    return true;
  }

  function showError(input, id, msg) {
    input.classList.add('error');
    const el = document.getElementById(id);
    if (el) {
      el.textContent = msg;
      el.classList.add('show');
    }
  }

  function clearError(input, id) {
    input.classList.remove('error');
    const el = document.getElementById(id);
    if (el) el.classList.remove('show');
  }

});
