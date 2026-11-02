const fields = {
  announcement: 'Thông báo đầu trang',
  hero_tag: 'Nhãn banner', hero_title: 'Tiêu đề banner', hero_accent: 'Dòng nhấn banner',
  hero_description: 'Mô tả banner', hero_index: 'Chú thích banner',
  collection_tag: 'Nhãn bộ sưu tập', collection_title: 'Tiêu đề bộ sưu tập',
  collection_accent: 'Dòng nhấn bộ sưu tập', collection_description: 'Mô tả bộ sưu tập',
  editorial_tag: 'Nhãn phần giới thiệu', editorial_title: 'Tiêu đề giới thiệu',
  editorial_subtitle: 'Dòng thứ hai', editorial_description: 'Mô tả giới thiệu',
  delivery_estimate: 'Thời gian giao hàng dự kiến', size_intro: 'Giới thiệu bảng size',
  size_guide: 'Bảng size (mỗi dòng: Size|Ngực|Eo|Hông)'
};

const $ = selector => document.querySelector(selector);
let content = {};

async function api(method = 'GET', body) {
  const response = await fetch('/backend/order/api/shop/content', {
    method, headers: {'Content-Type': 'application/json'},
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const result = await response.json().catch(() => ({}));
  if (response.status === 401 || response.status === 403) location.replace('/access-denied');
  if (!response.ok || result.success === false) throw new Error(result.message || 'Không thể tải nội dung cửa hàng.');
  return result.data;
}

function render() {
  const container = $('#content-fields');
  container.replaceChildren();
  for (const [key, value] of Object.entries(content)) {
    const label = document.createElement('label');
    label.textContent = fields[key] || key;
    const input = document.createElement(key === 'size_guide' || key.endsWith('description') ? 'textarea' : 'input');
    input.name = key;
    input.value = value;
    input.required = true;
    input.maxLength = 4000;
    if (input.tagName === 'TEXTAREA') input.rows = key === 'size_guide' ? 6 : 3;
    label.append(input);
    container.append(label);
  }
}

async function load() {
  const response = await fetch('/auth/session');
  const session = await response.json();
  if (!session.authenticated || session.user?.role !== 'ADMIN') {
    location.replace('/access-denied');
    return;
  }
  $('#admin-identity').textContent = `${session.user.fullName} · ${session.user.email}`;
  $('#admin-avatar').textContent = session.user.fullName.trim().split(/\s+/).map(part => part[0]).slice(-2).join('').toUpperCase();
  content = await api();
  render();
}

$('#content-form').onsubmit = async event => {
  event.preventDefault();
  const button = event.currentTarget.querySelector('button');
  button.disabled = true;
  $('#content-message').textContent = 'Đang lưu…';
  try {
    content = await api('PUT', Object.fromEntries(new FormData(event.currentTarget)));
    render();
    $('#content-message').textContent = 'Đã lưu. Tải lại trang chủ để xem nội dung mới.';
  } catch (error) {
    $('#content-message').textContent = error.message;
  } finally {
    button.disabled = false;
  }
};
$('#reload').onclick = () => load().catch(error => $('#content-message').textContent = error.message);
$('#admin-logout').onclick = async () => {
  await fetch('/auth/logout', {method: 'POST'});
  location.assign('/');
};
load().catch(error => $('#content-message').textContent = error.message);
