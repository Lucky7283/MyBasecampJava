document.addEventListener('DOMContentLoaded', function () {

  const form          = document.getElementById('loginForm');
  const emailInput    = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const alertError    = document.getElementById('alertError');
  const submitBtn     = document.getElementById('submitBtn');
  const toggleBtn     = document.getElementById('togglePassword');
  const rememberBox   = document.getElementById('remember');

  const savedEmail = localStorage.getItem('rememberedEmail');

  if (savedEmail) {

    emailInput.value    = savedEmail;
    rememberBox.checked = true;

  }

  toggleBtn.addEventListener('click', function () {

    const hidden = passwordInput.type === 'password';

    passwordInput.type = hidden
      ? 'text'
      : 'password';

    this.textContent = hidden
      ? '🙈 Hide'
      : '👁 Show';

  });

  emailInput.addEventListener('input', function () {

    if (this.value.trim()) {

      this.classList.remove('error');
      hideAlert();

    }

  });

  passwordInput.addEventListener('input', function () {

    if (this.value) {

      this.classList.remove('error');
      hideAlert();

    }

  });



  form.addEventListener('submit', function () {
    if (rememberBox.checked) {
      localStorage.setItem('rememberedEmail', emailInput.value);
    } else {
      localStorage.removeItem('rememberedEmail');
    }
  });

  function showAlert(msg) {

    alertError.textContent = msg;

    alertError.classList.add('show');

    setTimeout(hideAlert, 5000);

  }

  function hideAlert() {

    alertError.classList.remove('show');

  }

  function setLoading(on) {

    submitBtn.disabled = on;

    submitBtn.textContent = on
      ? 'Loading...'
      : 'Login';

  }

});