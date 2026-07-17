document.addEventListener('DOMContentLoaded', function () {
  const backBtn = document.querySelector('.btn-primary');

  if (backBtn) {
    backBtn.addEventListener('click', function (e) {
      if (window.history.length > 1) {
        e.preventDefault();
        window.history.back();
      }
    });
  }

});
