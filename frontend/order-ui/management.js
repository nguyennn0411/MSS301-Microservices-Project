const $ = selector => document.querySelector(selector);
const section = location.pathname === '/admin/users' ? 'users' : 'products';
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const money = value => new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(value) || 0);
const sessionKey = 'mss-admin-session';
let session, products = [], categories = [], users = [], addresses = [];
let selectedProduct = null, selectedUser = null, busy = false;
try { session = JSON.parse(sessionStorage.getItem(sessionKey) || 'null'); } catch { session = null; }
if (!session?.token || session.expiresAt <= Date.now() || session.user?.role !== 'ADMIN') session = null;

function field(form, name) { return form.elements.namedItem(name); }
function notice(message = '', error = false) {
  $('#notice').textContent = message;
  $('#notice').className = message ? `message${error ? ' error' : ''}` : '';
}
function authView() {
  $('#admin-login-card').hidden = Boolean(session);
  $('#admin-workspace').hidden = !session;
  $('#product-workspace').hidden = !session || section !== 'products';
  $('#user-workspace').hidden = !session || section !== 'users';
  $('#admin-identity').textContent = session ? `${session.user.fullName} · ${session.user.email}` : '';
}
function signOut(message = '') {
  session = null;
  sessionStorage.removeItem(sessionKey);
  authView();
  notice(message, Boolean(message));
}
async function api(service, path, {method = 'GET', body, anonymous = false} = {}) {
  if (!anonymous && (!session || session.expiresAt <= Date.now())) {
    signOut('Phiên quản trị đã hết hạn. Vui lòng đăng nhập lại.');
    throw new Error('Phiên quản trị đã hết hạn.');
  }
  const headers = {'Content-Type':'application/json'};
  if (!anonymous) headers.Authorization = `Bearer ${session.token}`;
  const response = await fetch(`/backend/${service}${path}`, {
    method, headers, body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(45000)
  });
  const result = await response.json().catch(() => ({}));
  if (response.status === 401 && !anonymous) signOut('Phiên quản trị đã hết hạn. Vui lòng đăng nhập lại.');
  if (!response.ok || result.success === false) throw new Error(result.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
  return result.data;
}
async function run(action) {
  if (busy) return;
  busy = true;
  document.querySelectorAll('button').forEach(button => button.disabled = true);
  notice();
  try { await action(); }
  catch (error) { notice(error.message || 'Không thể kết nối dịch vụ.', true); }
  finally { busy = false; document.querySelectorAll('button').forEach(button => button.disabled = false); }
}

function renderProducts() {
  const term = $('#product-search').value.trim().toLocaleLowerCase('vi');
  const filtered = products.filter(product => [product.name, product.brand, product.category,
    ...(product.variants || []).map(variant => variant.sku)].join(' ').toLocaleLowerCase('vi').includes(term));
  $('#product-count').textContent = `${filtered.length} / ${products.length} sản phẩm`;
  $('#product-list').innerHTML = filtered.length ? filtered.map(product => `
    <button type="button" class="entity-row ${selectedProduct?.id === product.id ? 'selected' : ''}" data-product-id="${product.id}">
      <span><strong>${escapeHtml(product.name)}</strong><small>${escapeHtml(product.brand || 'Chưa có thương hiệu')} · ${escapeHtml(product.category || 'Chưa phân loại')} · ${product.variants.length} biến thể</small></span>
      <span class="meta">${money(product.price)}<small>${product.status === 'ACTIVE' ? 'Đang bán' : product.status === 'INACTIVE' ? 'Ẩn' : 'Ngừng bán'}</small></span>
    </button>`).join('') : '<div class="empty-list">Không có sản phẩm phù hợp.</div>';
}
function renderCategories() {
  $('#category-list').innerHTML = categories.length ? categories.map(category => `
    <div class="category-item"><strong>${escapeHtml(category.name)}</strong><p>${escapeHtml(category.description || 'Chưa có mô tả')}</p>
      <button type="button" data-category-action="edit" data-id="${category.id}">Sửa</button>
      <button type="button" class="danger" data-category-action="delete" data-id="${category.id}">Xóa</button></div>`).join('')
    : '<div class="empty-list">Chưa có danh mục.</div>';
  const select = field($('#product-form'), 'categoryId');
  const current = select.value;
  select.innerHTML = '<option value="">Chọn danh mục</option>' + categories.map(category =>
    `<option value="${category.id}">${escapeHtml(category.name)}</option>`).join('');
  select.value = current;
}
function variantRow(variant = {}) {
  const row = document.createElement('div');
  row.className = 'variant-row';
  row.dataset.id = variant.id || '';
  row.innerHTML = `<label>Size<input name="size" required maxlength="20" value="${escapeHtml(variant.size || '')}"></label>
    <label>Màu<input name="color" maxlength="50" value="${escapeHtml(variant.color || '')}"></label>
    <label>SKU<input name="sku" maxlength="100" value="${escapeHtml(variant.sku || '')}"></label>
    <button class="danger" type="button" data-remove-variant>Xóa</button>`;
  $('#variant-list').append(row);
}
function editProduct(product = null) {
  selectedProduct = product;
  const form = $('#product-form'); form.reset();
  for (const key of ['id','name','brand','collection','categoryId','price','status','image'])
    field(form,key).value = product?.[key] ?? (key === 'status' ? 'ACTIVE' : '');
  $('#variant-list').replaceChildren();
  (product?.variants?.length ? product.variants : [{}]).forEach(variantRow);
  $('#product-form-title').textContent = product ? `Sửa: ${product.name}` : 'Thêm sản phẩm';
  $('#discontinue-product').hidden = !product || product.status === 'DISCONTINUED';
  renderProducts();
}
async function loadProducts() {
  [categories, products] = await Promise.all([
    api('product','/api/v1/categories'), api('product','/api/v1/products/admin')
  ]);
  renderCategories();
  editProduct(products.find(product => product.id === selectedProduct?.id) || null);
}

function renderUsers() {
  const term = $('#user-search').value.trim().toLocaleLowerCase('vi');
  const filtered = users.filter(user => [user.fullName,user.email,user.phone].join(' ').toLocaleLowerCase('vi').includes(term));
  $('#user-count').textContent = `${filtered.length} / ${users.length} người dùng`;
  $('#user-list').innerHTML = filtered.length ? filtered.map(user => `
    <button type="button" class="entity-row ${selectedUser?.id === user.id ? 'selected' : ''}" data-user-id="${user.id}">
      <span><strong>${escapeHtml(user.fullName)}</strong><small>${escapeHtml(user.email)} · ${escapeHtml(user.phone || 'Chưa có SĐT')}</small></span>
      <span class="meta">${user.role === 'ADMIN' ? 'Quản trị' : 'Khách hàng'}<small>${user.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu hóa'}</small></span>
    </button>`).join('') : '<div class="empty-list">Không có người dùng phù hợp.</div>';
}
function editUser(user = null) {
  selectedUser = user;
  const form = $('#user-form'); form.reset();
  for (const key of ['id','fullName','email','phone','role','status'])
    field(form,key).value = user?.[key] ?? (key === 'role' ? 'CUSTOMER' : key === 'status' ? 'ACTIVE' : '');
  field(form,'password').required = !user;
  $('#user-password-label').hidden = Boolean(user);
  $('#user-role-label').hidden = Boolean(user);
  $('#user-status-label').hidden = !user;
  field(form,'status').disabled = Boolean(user && user.id === session?.user?.id);
  $('#deactivate-user').hidden = !user || user.status !== 'ACTIVE' || user.id === session?.user?.id;
  $('#user-form-title').textContent = user ? `Sửa: ${user.fullName}` : 'Thêm người dùng';
  $('#address-card').hidden = !user;
  $('#address-user-name').textContent = user?.fullName || '';
  if (!user) addresses = [];
  renderUsers();
  renderAddresses();
}
async function loadUsers() {
  users = await api('user','/api/v1/users');
  const selected = users.find(user => user.id === selectedUser?.id) || null;
  editUser(selected);
  if (selected) await loadAddresses();
}
function renderAddresses() {
  $('#address-list').innerHTML = !selectedUser ? '' : addresses.length ? addresses.map(address => `
    <div class="category-item"><strong>${escapeHtml(address.receiverName)} ${address.defaultAddress ? '· Mặc định' : ''}</strong>
      <p>${escapeHtml(address.receiverPhone)} · ${escapeHtml([address.addressLine,address.ward,address.district,address.city].filter(Boolean).join(', '))}</p>
      <button type="button" data-address-action="edit" data-id="${address.id}">Sửa</button>
      <button type="button" class="danger" data-address-action="delete" data-id="${address.id}">Xóa</button></div>`).join('')
    : '<div class="empty-list">Người dùng chưa lưu địa chỉ.</div>';
}
async function loadAddresses() {
  addresses = await api('user',`/api/addresses?userId=${encodeURIComponent(selectedUser.id)}`);
  renderAddresses();
}
function editAddress(address = null) {
  const form = $('#address-form'); form.reset();
  for (const key of ['id','receiverName','receiverPhone','addressLine','ward','district','city','postalCode'])
    field(form,key).value = address?.[key] ?? '';
  field(form,'defaultAddress').checked = address?.defaultAddress ?? addresses.length === 0;
  $('#address-submit').textContent = address ? 'Lưu địa chỉ' : 'Thêm địa chỉ';
  $('#address-cancel').hidden = !address;
}

$('#title').textContent = $('#crumb').textContent = section === 'users' ? 'Người dùng' : 'Sản phẩm';
$('#subtitle').textContent = section === 'users' ? 'Quản lý tài khoản và địa chỉ giao hàng.' : 'Quản lý danh mục, sản phẩm và biến thể hiển thị trên trang chủ.';
$(`#nav-${section}`).classList.add('active');
authView();
$('#admin-login').onsubmit = event => {
  event.preventDefault();
  run(async () => {
    const data = Object.fromEntries(new FormData(event.currentTarget));
    const result = await api('user','/api/v1/auth/login',{method:'POST',body:data,anonymous:true});
    if (result.user?.role !== 'ADMIN') throw new Error('Tài khoản này không có quyền quản trị.');
    session = {token:result.accessToken,expiresAt:Date.now()+result.expiresInSeconds*1000,user:result.user};
    sessionStorage.setItem(sessionKey,JSON.stringify(session));
    event.currentTarget.reset();
    authView();
    if (section === 'users') await loadUsers(); else await loadProducts();
    notice('Đã đăng nhập quản trị.');
  });
};
$('#admin-logout').onclick = () => signOut();
$('#reload').onclick = () => run(async () => { if (!session) throw new Error('Vui lòng đăng nhập quản trị.'); if (section === 'users') await loadUsers(); else await loadProducts(); notice('Đã cập nhật dữ liệu.'); });

$('#product-search').oninput = renderProducts;
$('#product-list').onclick = event => {
  const row = event.target.closest('[data-product-id]');
  if (row) editProduct(products.find(product => product.id === row.dataset.productId));
};
$('#new-product').onclick = () => editProduct();
$('#add-variant').onclick = () => variantRow();
$('#variant-list').onclick = event => { if (event.target.matches('[data-remove-variant]')) event.target.closest('.variant-row').remove(); };
$('#product-form').onsubmit = event => {
  event.preventDefault();
  run(async () => {
    const form = event.currentTarget;
    const id = field(form,'id').value;
    const input = Object.fromEntries(new FormData(form));
    delete input.id;
    input.price = Number(input.price);
    input.variants = [...$('#variant-list').children].map(row => ({
      id:row.dataset.id || null,size:row.querySelector('[name=size]').value.trim(),
      color:row.querySelector('[name=color]').value.trim() || null,
      sku:row.querySelector('[name=sku]').value.trim() || null
    }));
    if (!input.variants.length) throw new Error('Thêm ít nhất một biến thể size / màu.');
    const saved = await api('product',id ? `/api/v1/products/${id}` : '/api/v1/products',
      {method:id ? 'PUT' : 'POST',body:input});
    selectedProduct = saved;
    await loadProducts();
    notice('Đã lưu sản phẩm.');
  });
};
$('#discontinue-product').onclick = () => {
  if (!selectedProduct || !confirm(`Ngừng bán “${selectedProduct.name}”? Sản phẩm sẽ ẩn khỏi trang chủ.`)) return;
  run(async () => { selectedProduct = await api('product',`/api/v1/products/${selectedProduct.id}`,{method:'DELETE'}); await loadProducts(); notice('Đã ngừng bán sản phẩm.'); });
};
$('#category-list').onclick = event => {
  const button = event.target.closest('[data-category-action]'); if (!button) return;
  const category = categories.find(item => item.id === button.dataset.id); if (!category) return;
  if (button.dataset.categoryAction === 'edit') {
    const form = $('#category-form'); field(form,'id').value = category.id;
    field(form,'name').value = category.name; field(form,'description').value = category.description || '';
    $('#category-submit').textContent = 'Lưu danh mục'; $('#category-cancel').hidden = false;
  } else if (confirm(`Xóa danh mục “${category.name}”?`)) run(async () => {
    await api('product',`/api/v1/categories/${category.id}`,{method:'DELETE'});
    await loadProducts(); notice('Đã xóa danh mục.');
  });
};
$('#category-cancel').onclick = () => { $('#category-form').reset(); field($('#category-form'),'id').value = ''; $('#category-submit').textContent = 'Thêm danh mục'; $('#category-cancel').hidden = true; };
$('#category-form').onsubmit = event => {
  event.preventDefault(); run(async () => {
    const form = event.currentTarget, id = field(form,'id').value;
    await api('product',id ? `/api/v1/categories/${id}` : '/api/v1/categories',
      {method:id ? 'PUT' : 'POST',body:{name:field(form,'name').value.trim(),description:field(form,'description').value.trim()}});
    form.reset(); field(form,'id').value = '';
    $('#category-submit').textContent = 'Thêm danh mục'; $('#category-cancel').hidden = true;
    await loadProducts(); notice('Đã lưu danh mục.');
  });
};

$('#user-search').oninput = renderUsers;
$('#user-list').onclick = event => {
  const row = event.target.closest('[data-user-id]'); if (!row) return;
  run(async () => { editUser(users.find(user => user.id === row.dataset.userId)); await loadAddresses(); editAddress(); });
};
$('#new-user').onclick = () => editUser();
$('#user-form').onsubmit = event => {
  event.preventDefault(); run(async () => {
    const form = event.currentTarget, id = field(form,'id').value;
    const body = {email:field(form,'email').value.trim(),fullName:field(form,'fullName').value.trim(),phone:field(form,'phone').value.trim()};
    if (id) body.status = field(form,'status').value;
    else { body.password = field(form,'password').value; body.role = field(form,'role').value; }
    selectedUser = await api('user',id ? `/api/v1/users/${id}` : '/api/v1/users',
      {method:id ? 'PUT' : 'POST',body});
    await loadUsers(); editAddress(); notice('Đã lưu người dùng.');
  });
};
$('#deactivate-user').onclick = () => {
  if (!selectedUser || !confirm(`Vô hiệu hóa tài khoản “${selectedUser.fullName}”?`)) return;
  run(async () => { selectedUser = await api('user',`/api/v1/users/${selectedUser.id}`,{method:'DELETE'}); await loadUsers(); notice('Đã vô hiệu hóa tài khoản.'); });
};
$('#new-address').onclick = () => editAddress();
$('#address-list').onclick = event => {
  const button = event.target.closest('[data-address-action]'); if (!button) return;
  const address = addresses.find(item => String(item.id) === button.dataset.id); if (!address) return;
  if (button.dataset.addressAction === 'edit') editAddress(address);
  else if (confirm(`Xóa địa chỉ của “${address.receiverName}”?`)) run(async () => {
    await api('user',`/api/addresses/${address.id}`,{method:'DELETE'});
    await loadAddresses(); editAddress(); notice('Đã xóa địa chỉ.');
  });
};
$('#address-cancel').onclick = () => editAddress();
$('#address-form').onsubmit = event => {
  event.preventDefault(); run(async () => {
    const form = event.currentTarget, data = Object.fromEntries(new FormData(form)), id = data.id;
    delete data.id; data.defaultAddress = field(form,'defaultAddress').checked;
    if (!id) data.userId = selectedUser.id;
    await api('user',id ? `/api/addresses/${id}` : '/api/addresses',
      {method:id ? 'PUT' : 'POST',body:data});
    await loadAddresses(); editAddress(); notice('Đã lưu địa chỉ.');
  });
};
if (session) run(async () => { if (section === 'users') await loadUsers(); else await loadProducts(); });
