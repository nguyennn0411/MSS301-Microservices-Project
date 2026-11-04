export function initPasswordToggles(root = document) {
  for (const input of root.querySelectorAll('input[type="password"]')) {
    const wrapper = document.createElement('span');
    wrapper.className = 'password-field';
    input.before(wrapper);
    wrapper.append(input);

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'password-toggle';
    button.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z"/><circle cx="12" cy="12" r="3"/><path class="password-eye-slash" d="M3 21 21 3"/></svg>';
    wrapper.append(button);

    const fieldName = input.closest('label')?.firstChild?.textContent?.trim().toLowerCase() || 'mật khẩu';
    const setVisible = visible => {
      input.type = visible ? 'text' : 'password';
      button.setAttribute('aria-pressed', String(visible));
      button.setAttribute('aria-label', `${visible ? 'Ẩn' : 'Hiện'} ${fieldName}`);
      button.title = button.getAttribute('aria-label');
    };
    setVisible(false);
    button.addEventListener('click', () => setVisible(input.type === 'password'));
    input.form?.addEventListener('reset', () => setVisible(false));
  }
}
