const $ = selector => document.querySelector(selector);
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, character => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
})[character]);
const friendlyErrors = {
  'Invalid email or password': 'Email hoặc mật khẩu chưa đúng.',
  'Email is already registered': 'Email này đã được đăng ký.'
};
const dialog = $('#account-dialog');
let user = null;
let addresses = [];
let loading = false;
sessionStorage.removeItem('mss-user-session');
sessionStorage.removeItem('mss-admin-session');

function showMessage(message, error = false) {
  const target = $('#account-message');
  target.textContent = message;
  target.className = message ? error ? 'account-error' : 'account-success' : '';
}

async function signOut(message = '') {
  await fetch('/auth/logout', {method: 'POST'}).catch(() => {});
  user = null;
  addresses = [];
  render();
  showMessage(message, Boolean(message));
}

async function request(path, {method = 'GET', body, anonymous = false} = {}) {
  const response = await fetch(`/backend/user${path}`, {
    method, headers: {'Content-Type': 'application/json'},
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(45000)
  });
  const result = await response.json().catch(() => ({}));
  if (response.status === 401 && !anonymous) await signOut('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
  if (!response.ok || result.success === false) {
    const message = result.message || `Không thể thực hiện yêu cầu (HTTP ${response.status}).`;
    throw new Error(friendlyErrors[message] || message);
  }
  return result.data;
}

function joinAddress(address) {
  return [address.addressLine, address.ward, address.district, address.city, address.postalCode]
    .filter(Boolean).join(', ');
}

function prefillCheckout() {
  if (!user) return;
  const form = $('#checkout-form');
  for (const [field, value] of Object.entries({name: user.fullName, email: user.email, phone: user.phone})) {
    if (!form.elements[field].value && value) form.elements[field].value = value;
  }
}

function renderAddresses() {
  const target = $('#account-addresses');
  target.innerHTML = addresses.length ? addresses.map(address => `
    <article class="account-address">
      <div class="account-address-heading"><strong>${escapeHtml(address.receiverName)}</strong>${address.defaultAddress ? '<span>Mặc định</span>' : ''}</div>
      <p>${escapeHtml(address.receiverPhone)}<br>${escapeHtml(joinAddress(address))}</p>
      <div class="account-address-actions">
        <button type="button" data-address-action="use" data-id="${address.id}">Dùng khi đặt hàng</button>
        <button type="button" data-address-action="edit" data-id="${address.id}">Sửa</button>
        <button type="button" data-address-action="delete" data-id="${address.id}">Xóa</button>
      </div>
    </article>`).join('') : '<p class="account-empty">Bạn chưa lưu địa chỉ nào. Thêm địa chỉ để điền nhanh khi đặt hàng.</p>';
}

function render() {
  const signedIn = Boolean(user);
  $('#account-auth').hidden = signedIn;
  $('#account-dashboard').hidden = !signedIn;
  $('#account-open').textContent = user?.fullName ? user.fullName.trim().split(/\s+/).at(-1) : 'Tài khoản';
  $('#account-admin-link').hidden = user?.role !== 'ADMIN';
  if (!signedIn) return;
  $('#account-name').textContent = user?.fullName || 'Đang tải hồ sơ…';
  $('#account-email').textContent = user?.email || '';
  if (user) {
    const form = $('#account-profile');
    form.elements.fullName.value = user.fullName || '';
    form.elements.email.value = user.email || '';
    form.elements.phone.value = user.phone || '';
  }
  renderAddresses();
  prefillCheckout();
}

async function refresh() {
  const response = await fetch('/auth/session', {signal: AbortSignal.timeout(10000)});
  if (!response.ok) throw new Error('Không thể kiểm tra phiên đăng nhập.');
  const state = await response.json();
  user = state.authenticated ? state.user : null;
  if (!user) { addresses = []; render(); return; }
  addresses = await request(`/api/addresses?userId=${encodeURIComponent(user.id)}`);
  render();
}

async function withLoading(action) {
  if (loading) return;
  loading = true;
  dialog.querySelectorAll('button').forEach(button => button.disabled = true);
  showMessage('');
  try {
    await action();
  } catch (error) {
    showMessage(error.message || 'Không thể kết nối User Service.', true);
  } finally {
    loading = false;
    dialog.querySelectorAll('button').forEach(button => button.disabled = false);
  }
}

function editAddress(address = null) {
  const form = $('#account-address-form');
  form.reset();
  for (const field of ['id', 'receiverName', 'receiverPhone', 'addressLine', 'ward', 'district', 'city', 'postalCode']) {
    form.elements[field].value = address?.[field] ?? '';
  }
  form.elements.defaultAddress.checked = address?.defaultAddress ?? addresses.length === 0;
  form.hidden = false;
  form.scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

export function initAccount({onAddressSelect}) {
  const open = () => {
    showMessage('');
    dialog.showModal();
    withLoading(refresh);
  };
  $('#account-open').onclick = open;
  $('#saved-address-open').onclick = open;
  $('#account-logout').onclick = () => withLoading(() => signOut());
  $('#account-add-address').onclick = () => editAddress();
  $('#account-cancel-address').onclick = () => { $('#account-address-form').hidden = true; };

  document.querySelectorAll('[data-auth-tab]').forEach(button => button.onclick = () => {
    const register = button.dataset.authTab === 'register';
    $('#account-login').hidden = register;
    $('#account-register').hidden = !register;
    document.querySelectorAll('[data-auth-tab]').forEach(tab => tab.classList.toggle('selected', tab === button));
    showMessage('');
  });

  for (const [formId, path] of [['account-login', '/api/v1/auth/login'], ['account-register', '/api/v1/auth/register']]) {
    $(`#${formId}`).onsubmit = event => {
      event.preventDefault();
      const form = event.currentTarget;
      const data = Object.fromEntries(new FormData(form));
      withLoading(async () => {
        const result = await request(path, {method: 'POST', body: data, anonymous: true});
        user = result.user;
        form.reset();
        render();
        if (user.role === 'ADMIN') {
          window.location.assign('/admin');
          return;
        }
        await refresh();
        showMessage('Tài khoản đã sẵn sàng. Bạn có thể chọn hoặc thêm địa chỉ giao hàng.');
      });
    };
  }

  $('#account-profile').onsubmit = event => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    withLoading(async () => {
      user = await request('/api/v1/users/me', {method: 'PUT', body: data});
      render();
      showMessage('Đã lưu thông tin cá nhân.');
    });
  };

  $('#account-address-form').onsubmit = event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = Object.fromEntries(new FormData(form));
    const id = data.id;
    delete data.id;
    data.defaultAddress = form.elements.defaultAddress.checked;
    if (!id) data.userId = user.id;
    withLoading(async () => {
      await request(id ? `/api/addresses/${encodeURIComponent(id)}` : '/api/addresses', {
        method: id ? 'PUT' : 'POST', body: data
      });
      addresses = await request(`/api/addresses?userId=${encodeURIComponent(user.id)}`);
      form.hidden = true;
      renderAddresses();
      showMessage('Đã lưu địa chỉ giao hàng.');
    });
  };

  $('#account-addresses').onclick = event => {
    const button = event.target.closest('[data-address-action]');
    if (!button || loading) return;
    const address = addresses.find(item => String(item.id) === button.dataset.id);
    if (!address) return;
    if (button.dataset.addressAction === 'edit') editAddress(address);
    if (button.dataset.addressAction === 'use') {
      onAddressSelect({...address, formattedAddress: joinAddress(address), email: user.email});
      dialog.close();
    }
    if (button.dataset.addressAction === 'delete' && confirm('Xóa địa chỉ này khỏi tài khoản?')) {
      withLoading(async () => {
        await request(`/api/addresses/${encodeURIComponent(address.id)}`, {method: 'DELETE'});
        addresses = await request(`/api/addresses?userId=${encodeURIComponent(user.id)}`);
        renderAddresses();
        showMessage('Đã xóa địa chỉ.');
      });
    }
  };

  render();
  withLoading(refresh);
  if (new URLSearchParams(location.search).has('login')) {
    history.replaceState(null, '', location.pathname);
    open();
  }
}
