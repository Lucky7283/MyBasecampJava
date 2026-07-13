// show.js — client-side enhancements for project#show and thread#show pages

document.addEventListener('DOMContentLoaded', function () {

  // ── File input label ─────────────────────────────────────────
  const fileInput  = document.getElementById('fileInput');
  const fileChosen = document.getElementById('fileChosen');
  if (fileInput && fileChosen) {
    fileInput.addEventListener('change', function () {
      fileChosen.textContent = this.files.length > 0
        ? this.files[0].name
        : 'No file chosen';
    });
  }

  // ── Auto-resize textareas ─────────────────────────────────────
  document.querySelectorAll('.msg-textarea').forEach(function (ta) {
    ta.addEventListener('input', function () {
      this.style.height = 'auto';
      this.style.height = (this.scrollHeight) + 'px';
    });
  });

  // ── Scroll to bottom of message list on thread page ──────────
  const msgList = document.getElementById('messageList');
  if (msgList) {
    msgList.scrollTop = msgList.scrollHeight;
  }

  // ── Flash message auto-dismiss ────────────────────────────────
  const flash = document.querySelector('.flash');
  if (flash) {
    setTimeout(function () {
      flash.style.transition = 'opacity 0.5s';
      flash.style.opacity    = '0';
      setTimeout(function () { flash.remove(); }, 500);
    }, 4000);
  }

});
