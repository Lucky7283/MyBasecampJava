document.addEventListener('DOMContentLoaded', function () {

  const animElements = [
    document.getElementById('logoIcon'),
    document.getElementById('brandName'),
    document.getElementById('tagline'),
    document.getElementById('btnGroup'),
    document.getElementById('footerText'),
    document.querySelector('.container')
  ];

  setTimeout(function () {
    animElements.forEach(function (el) {
      if (el) el.classList.add('loaded');
    });
  }, 100);

  const logoIcon = document.getElementById('logoIcon');

  if (logoIcon) {
    logoIcon.addEventListener('mouseenter', function () {
      this.style.transform = 'scale(1.08)';
      this.style.transition = 'transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)';
    });

    logoIcon.addEventListener('mouseleave', function () {
      this.style.transform = 'scale(1)';
    });
  }

  const buttons = document.querySelectorAll('.btn');

  buttons.forEach(function (btn) {
    btn.addEventListener('click', function (e) {

      const oldRipple = this.querySelector('.ripple');
      if (oldRipple) oldRipple.remove();

      const ripple = document.createElement('span');
      ripple.classList.add('ripple');

      const size = Math.max(btn.offsetWidth, btn.offsetHeight);

      const rect = btn.getBoundingClientRect();

      const x = e.clientX - rect.left - size / 2;
      const y = e.clientY - rect.top  - size / 2;

      ripple.style.cssText =
        'position:absolute;' +
        'width:'  + size + 'px;' +
        'height:' + size + 'px;' +
        'left:'   + x + 'px;' +
        'top:'    + y + 'px;' +
        'background:rgba(255,255,255,0.35);' +
        'border-radius:50%;' +
        'transform:scale(0);' +
        'animation:rippleAnim 0.5s ease-out forwards;' +
        'pointer-events:none;';

      btn.appendChild(ripple);

      setTimeout(function () { ripple.remove(); }, 500);
    });
  });

  const style = document.createElement('style');
  style.textContent =
    '@keyframes rippleAnim {' +
    '  to { transform: scale(2.5); opacity: 0; }' +
    '}';
  document.head.appendChild(style);

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
      const focused = document.activeElement;

      if (focused && focused.classList.contains('btn')) {
        focused.click();
      }
    }
  });

  const brandName = document.getElementById('brandName');

  if (brandName) {
    const text = brandName.textContent;

    brandName.innerHTML = '';

    text.split('').forEach(function (char, index) {

      const span = document.createElement('span');
      span.textContent = char;

      span.style.cssText =
        'display:inline-block;' +
        'opacity:0;' +
        'transform:translateY(20px);' +
        'transition:opacity 0.4s ease ' + (0.3 + index * 0.05) + 's,' +
        'transform 0.4s ease ' + (0.3 + index * 0.05) + 's;';

      brandName.appendChild(span);
    });

    setTimeout(function () {
      brandName.querySelectorAll('span').forEach(function (span) {
        span.style.opacity   = '1';
        span.style.transform = 'translateY(0)';
      });
    }, 150);
  }


  const circles = document.querySelectorAll('.bg-circle');

  document.addEventListener('mousemove', function (e) {

    const x = e.clientX / window.innerWidth;
    const y = e.clientY / window.innerHeight;

    circles.forEach(function (circle, i) {
      const speed  = (i + 1) * 8;

      const moveX  = (x - 0.5) * speed;
      const moveY  = (y - 0.5) * speed;

      circle.style.transform = 'translate(' + moveX + 'px,' + moveY + 'px)';
      circle.style.transition = 'transform 0.8s ease';
    });
  });

});