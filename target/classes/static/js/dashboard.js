document.addEventListener('DOMContentLoaded', function () {

  const params      = new URLSearchParams(window.location.search);
  const alertEl     = document.getElementById('alertMsg');
  const successType = params.get('success');

  const messages = {
    created: '✓ Project created successfully!',
    updated: '✓ Project updated successfully!',
    deleted: '✓ Project deleted.'
  };

  if (successType && messages[successType]) {
    showAlert(
      messages[successType],
      successType === 'deleted' ? 'warning' : 'success'
    );

    window.history.replaceState(null, '', window.location.pathname);
  }

  document.querySelectorAll('.btn-delete').forEach(function (btn) {

    btn.addEventListener('click', function () {

      const id   = this.getAttribute('data-id');
      const name = this.getAttribute('data-name');

      if (
        !confirm(
          'Are you sure you want to delete "' +
          name +
          '"?\nThis action cannot be undone!'
        )
      ) return;

      btn.disabled    = true;
      fetch('/projects/' + id, { method: 'DELETE' })
      .then(res => {
          removeCard(id);
      })
      .catch(err => {
          btn.disabled = false;
          btn.textContent = 'Delete';
      });

    });

  });

  function removeCard(id) {

    const card = document.querySelector(
      '.project-card[data-id="' + id + '"]'
    );

    if (!card) return;

    card.style.opacity    = '0';
    card.style.transition = 'opacity 0.3s ease';

    setTimeout(function () {

      card.remove();
      updateCount(-1);
      checkEmpty();

    }, 300);

  }

  function updateCount(delta) {

    const countEl = document.getElementById('projectCount');
    const statEl  = document.getElementById('statProjects');

    if (countEl) {

      const n = Math.max(
        0,
        parseInt(countEl.textContent) + delta
      );

      countEl.textContent = n + ' projects';
    }

    if (statEl) {

      statEl.textContent = Math.max(
        0,
        parseInt(statEl.textContent) + delta
      );

    }

  }

  function checkEmpty() {

    const list  = document.getElementById('projectsList');
    const empty = document.getElementById('emptyState');

    if (
      list &&
      empty &&
      list.querySelectorAll('.project-card').length === 0
    ) {
      empty.style.display = 'block';
    }

  }

  function showAlert(msg, type) {

    alertEl.textContent = msg;
    alertEl.className   = 'alert ' + type + ' show';

    setTimeout(function () {
      alertEl.classList.remove('show');
    }, 4000);

  }

});