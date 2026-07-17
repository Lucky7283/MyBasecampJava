document.addEventListener('DOMContentLoaded', function () {

  const alertBox = document.getElementById('alertBox');

  function showAlert(msg, type) {
    alertBox.textContent = msg;
    alertBox.className   = 'alert ' + type + ' show';
    setTimeout(function () { alertBox.classList.remove('show'); }, 4000);
  }

  document.querySelectorAll('.btn-make-admin').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const id   = this.getAttribute('data-user-id');
      const name = this.getAttribute('data-user-name');
      if (!confirm('Make "' + name + '" an admin?')) return;
      changeRole(id, name, true, this);
    });
  });

  document.querySelectorAll('.btn-remove-admin').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const id   = this.getAttribute('data-user-id');
      const name = this.getAttribute('data-user-name');
      if (!confirm('Remove admin rights from "' + name + '"?')) return;
      changeRole(id, name, false, this);
    });
  });

  function changeRole(id, name, makeAdmin, btn) {
    btn.disabled = true;
    btn.textContent = 'Please wait...';

    const url = makeAdmin ? '/users/' + id + '/make_admin' : '/users/' + id + '/remove_admin';
    fetch(url, { method: 'POST' })
      .then(res => {
         updateRow(id, makeAdmin);
         showAlert('✓ ' + name + (makeAdmin ? ' is now an admin!' : ' admin rights removed!'), 'success');
      })
      .catch(err => {
         btn.disabled = false;
         btn.textContent = makeAdmin ? 'Make Admin' : 'Remove Admin';
         showAlert('Error changing role', 'warning');
      });
  }

  function updateRow(id, isAdmin) {
    const row = document.querySelector('tr[data-user-id="' + id + '"]');
    if (!row) return;

    const badge = row.querySelector('.role-badge');
    if (badge) {
      badge.textContent = isAdmin ? 'Admin' : 'User';
      badge.className   = 'role-badge ' + (isAdmin ? 'admin' : 'user');
    }

    const cell = row.querySelector('.actions-cell');
    const name = row.querySelector('.user-fullname').textContent;

    if (cell) {
      if (isAdmin) {
        cell.innerHTML =
          '<button class="btn-sm remove-admin btn-remove-admin" data-user-id="' + id + '" data-user-name="' + name + '">Remove Admin</button>' +
          '<button class="btn-sm delete-user btn-delete-user" data-user-id="' + id + '" data-user-name="' + name + '">Delete</button>';
      } else {
        cell.innerHTML =
          '<button class="btn-sm make-admin btn-make-admin" data-user-id="' + id + '" data-user-name="' + name + '">Make Admin</button>' +
          '<button class="btn-sm delete-user btn-delete-user" data-user-id="' + id + '" data-user-name="' + name + '">Delete</button>';
      }

      bindButtons(cell);
    }

    updateStats();
  }

  document.querySelectorAll('.btn-delete-user').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const id   = this.getAttribute('data-user-id');
      const name = this.getAttribute('data-user-name');

      if (!confirm('Delete "' + name + '"? This action cannot be undone!')) return;

      deleteUser(id, name, this);
    });
  });

  function deleteUser(id, name, btn) {
    btn.disabled    = true;
    btn.textContent = 'Deleting...';

    fetch('/users/' + id, { method: 'DELETE' })
      .then(res => {
        removeRow(id);
        showAlert('✓ ' + name + ' deleted!', 'success');
      })
      .catch(err => {
        btn.disabled = false;
        btn.textContent = 'Delete';
        showAlert('Error deleting user', 'warning');
      });
  }

  function removeRow(id) {
    const row = document.querySelector('tr[data-user-id="' + id + '"]');
    if (!row) return;

    row.style.opacity    = '0';
    row.style.transition = 'opacity 0.3s ease';

    setTimeout(function () {
      row.remove();
      updateStats();
    }, 300);
  }

  function updateStats() {
    const total  = document.querySelectorAll('tbody tr').length;
    const admins = document.querySelectorAll('.role-badge.admin').length;

    const tEl = document.getElementById('statTotal');
    const aEl = document.getElementById('statAdmin');
    const uEl = document.getElementById('statUser');

    if (tEl) tEl.querySelector('span').textContent = total;
    if (aEl) aEl.querySelector('span').textContent = admins;
    if (uEl) uEl.querySelector('span').textContent = total - admins;
  }

  function bindButtons(container) {

    container.querySelectorAll('.btn-make-admin').forEach(function (btn) {
      btn.addEventListener('click', function () {

        const id = this.getAttribute('data-user-id');
        const name = this.getAttribute('data-user-name');

        if (!confirm('Make "' + name + '" an admin?')) return;

        changeRole(id, name, true, this);
      });
    });

    container.querySelectorAll('.btn-remove-admin').forEach(function (btn) {
      btn.addEventListener('click', function () {

        const id = this.getAttribute('data-user-id');
        const name = this.getAttribute('data-user-name');

        if (!confirm('Remove admin rights from "' + name + '"?')) return;

        changeRole(id, name, false, this);
      });
    });

    container.querySelectorAll('.btn-delete-user').forEach(function (btn) {
      btn.addEventListener('click', function () {

        const id = this.getAttribute('data-user-id');
        const name = this.getAttribute('data-user-name');

        if (!confirm('Delete "' + name + '"?')) return;

        deleteUser(id, name, this);
      });
    });
  }

});