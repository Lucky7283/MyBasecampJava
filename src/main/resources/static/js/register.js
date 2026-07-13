document.addEventListener('DOMContentLoaded', function () {

  const form          = document.getElementById('registerForm');
  const fullnameInput = document.getElementById('name');
  const emailInput    = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmInput  = document.getElementById('password_confirm');
  const submitBtn     = document.getElementById('submitBtn');

  fullnameInput.addEventListener('input', validateFullname);

  emailInput.addEventListener('input', validateEmail);

  passwordInput.addEventListener('input', function () {

    validatePassword();
    updateStrength(this.value);

    if (confirmInput.value !== '') {
      validateConfirm();
    }

  });

  confirmInput.addEventListener('input', validateConfirm);

  form.addEventListener('submit', function (e) {

    const isFullnameValid = validateFullname();
    const isEmailValid = validateEmail();
    const isPasswordValid = validatePassword();
    const isConfirmValid = validateConfirm();

    const ok = isFullnameValid && isEmailValid && isPasswordValid && isConfirmValid;

    if (!ok) {
      e.preventDefault();
    } else {
      submitBtn.disabled    = true;
      submitBtn.textContent = 'Submitting...';
    }

  });

  function validateFullname() {

    const v = fullnameInput.value.trim();

    if (v === '') {
      return err(
        fullnameInput,
        'fullname-error',
        'Full name is required'
      );
    }

    if (v.length < 2) {
      return err(
        fullnameInput,
        'fullname-error',
        'Must contain at least 2 characters'
      );
    }

    return ok(fullnameInput, 'fullname-error');

  }

  function validateEmail() {

    const v = emailInput.value.trim();

    const re = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

    if (v === '') {
      return err(
        emailInput,
        'email-error',
        'Email is required'
      );
    }

    if (!re.test(v)) {
      return err(
        emailInput,
        'email-error',
        'Enter a valid email address'
      );
    }

    return ok(emailInput, 'email-error');

  }

  function validatePassword() {

    const v = passwordInput.value;

    if (v === '') {
      return err(
        passwordInput,
        'password-error',
        'Password is required'
      );
    }

    if (v.length < 6) {
      return err(
        passwordInput,
        'password-error',
        'Must contain at least 6 characters'
      );
    }

    return ok(passwordInput, 'password-error');

  }

  function validateConfirm() {

    const v = confirmInput.value;

    if (v === '') {
      return err(
        confirmInput,
        'confirm-error',
        'Please confirm your password'
      );
    }

    if (v !== passwordInput.value) {
      return err(
        confirmInput,
        'confirm-error',
        'Passwords do not match'
      );
    }

    return ok(confirmInput, 'confirm-error');

  }

  function err(input, errorId, msg) {

    input.classList.add('error');

    const el = document.getElementById(errorId);

    el.textContent = msg;
    el.classList.add('visible');

    return false;

  }

  function ok(input, errorId) {

    input.classList.remove('error');

    document
      .getElementById(errorId)
      .classList.remove('visible');

    return true;

  }

  function updateStrength(password) {

    const bar  = document.getElementById('strengthBar');
    const text = document.getElementById('strengthText');

    if (!bar || !text) return;

    let score = 0;

    if (password.length >= 6) {
      score++;
    }

    if (password.length >= 10) {
      score++;
    }

    if (/[A-Z]/.test(password)) {
      score++;
    }

    if (/[0-9]/.test(password)) {
      score++;
    }

    if (/[^A-Za-z0-9]/.test(password)) {
      score++;
    }

    const levels = [

      {
        label: '',
        color: '#e0e4ea',
        width: '0%'
      },

      {
        label: 'Very Weak',
        color: '#E24B4A',
        width: '20%'
      },

      {
        label: 'Weak',
        color: '#F5A623',
        width: '40%'
      },

      {
        label: 'Medium',
        color: '#F5C842',
        width: '60%'
      },

      {
        label: 'Strong',
        color: '#1D9E75',
        width: '80%'
      },

      {
        label: 'Very Strong',
        color: '#0F6E56',
        width: '100%'
      }

    ];

    const level =
      password.length === 0
        ? levels[0]
        : (levels[score] || levels[5]);

    bar.style.width           = level.width;
    bar.style.backgroundColor = level.color;

    text.textContent = level.label;
    text.style.color = level.color;

  }

});