import http from 'node:http';
import {readFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';

const files = {
  '/': 'index.html', '/app.js': 'app.js', '/account.js': 'account.js',
  '/style.css': 'style.css', '/account.css': 'account.css',
  '/admin': 'admin.html', '/admin/': 'admin.html',
  '/admin/payment': 'admin.html', '/admin/payment/': 'admin.html',
  '/admin/products': 'management.html', '/admin/products/': 'management.html',
  '/admin/users': 'management.html', '/admin/users/': 'management.html',
  '/admin.js': 'admin.js', '/admin.css': 'admin.css',
  '/management.js': 'management.js', '/management.css': 'management.css',
  '/access-denied': 'access-denied.html',
  '/payment/success': 'index.html', '/payment/cancel': 'index.html'
};
const services = {order: 8084, payment: 8085, product: 8082, user: 8081};
const adminAssets = new Set(['/admin.js', '/admin.css', '/management.js', '/management.css']);

function cookies(req) {
  return Object.fromEntries((req.headers.cookie || '').split(';').map(part => part.trim())
    .filter(part => part.includes('=')).map(part => {
      const separator = part.indexOf('=');
      return [part.slice(0, separator), part.slice(separator + 1)];
    }));
}

function appendCookie(res, value) {
  const current = res.getHeader('Set-Cookie');
  res.setHeader('Set-Cookie', [...(current ? Array.isArray(current) ? current : [current] : []), value]);
}

function json(res, status, data) {
  res.writeHead(status, {'Content-Type': 'application/json; charset=utf-8'});
  res.end(JSON.stringify(data));
}

function clearAuth(res) {
  appendCookie(res, 'mss_auth=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0');
}

async function currentUser(token) {
  if (!token || token.length > 4096 || !/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$/.test(token)) return null;
  const response = await fetch('http://127.0.0.1:8081/api/v1/users/me', {
    headers: {Authorization: `Bearer ${token}`}, signal: AbortSignal.timeout(5000)
  });
  if (response.status === 401 || response.status === 403) return null;
  if (!response.ok) throw new Error(`User Service returned HTTP ${response.status}`);
  const result = await response.json();
  return result.success === false ? null : result.data;
}

function requiresAdminApi(service, path, method) {
  if (service === 'order') return !path.startsWith('/api/shop/') && path !== '/actuator/health';
  if (service === 'payment') return path !== '/actuator/health' &&
    !(method === 'GET' && path === '/api/payments/capabilities');
  return false;
}

http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://localhost');
  const stored = cookies(req);
  const shopSession = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(stored.mss_session || '')
    ? stored.mss_session : randomUUID();
  if (shopSession !== stored.mss_session) appendCookie(res,
    `mss_session=${shopSession}; HttpOnly; SameSite=Strict; Path=/; Max-Age=31536000`);
  res.setHeader('Cache-Control', 'no-store');

  const sameOrigin = !req.headers.origin || ['http://localhost:5173', 'http://127.0.0.1:5173'].includes(req.headers.origin);
  if (!['GET', 'HEAD'].includes(req.method) && !sameOrigin) {
    json(res, 403, {message: 'Yêu cầu không được phép.'});
    return;
  }

  try {
    if (url.pathname === '/auth/logout' && req.method === 'POST') {
      clearAuth(res);
      json(res, 200, {authenticated: false});
      return;
    }
    if (url.pathname === '/auth/session' && req.method === 'GET') {
      const user = await currentUser(stored.mss_auth);
      if (!user || user.status !== 'ACTIVE') clearAuth(res);
      json(res, 200, {authenticated: Boolean(user && user.status === 'ACTIVE'),
        user: user?.status === 'ACTIVE' ? user : null});
      return;
    }

    const match = url.pathname.match(/^\/backend\/(order|payment|product|user)(\/.*)$/);
    if (match) {
      const [, service, path] = match;
      if (requiresAdminApi(service, path, req.method)) {
        const user = await currentUser(stored.mss_auth);
        if (user?.status !== 'ACTIVE' || user.role !== 'ADMIN') {
          json(res, 403, {success: false, message: 'Bạn không có quyền quản trị.'});
          return;
        }
      }
      let body = '';
      for await (const chunk of req) {
        body += chunk;
        if (body.length > 1000000) { json(res, 413, {message: 'Dữ liệu quá lớn.'}); return; }
      }
      const headers = {'Content-Type': 'application/json'};
      if (service === 'order') headers['X-Shop-Session'] = shopSession;
      if (['user', 'product'].includes(service) && stored.mss_auth)
        headers.Authorization = `Bearer ${stored.mss_auth}`;
      const response = await fetch(`http://127.0.0.1:${services[service]}${path}${url.search}`, {
        method: req.method, headers, body: ['GET', 'HEAD'].includes(req.method) ? undefined : body,
        signal: AbortSignal.timeout(45000)
      });
      const text = await response.text();
      if (service === 'user' && req.method === 'POST' &&
          ['/api/v1/auth/login', '/api/v1/auth/register'].includes(path) && response.ok) {
        const result = JSON.parse(text);
        const auth = result.data;
        if (!auth?.accessToken || auth.user?.status !== 'ACTIVE') {
          json(res, 502, {message: 'Không thể tạo phiên đăng nhập.'});
          return;
        }
        appendCookie(res, `mss_auth=${auth.accessToken}; HttpOnly; SameSite=Strict; Path=/; Max-Age=${Math.max(0, Math.floor(auth.expiresInSeconds))}`);
        result.data = {user: auth.user, expiresInSeconds: auth.expiresInSeconds};
        json(res, response.status, result);
        return;
      }
      res.writeHead(response.status, {'Content-Type': response.headers.get('content-type') || 'application/json'});
      res.end(text);
      return;
    }

    if (/^\/admin(?:\/|$)/.test(url.pathname) || adminAssets.has(url.pathname)) {
      const user = await currentUser(stored.mss_auth);
      if (user?.status !== 'ACTIVE' || user.role !== 'ADMIN') {
        if (stored.mss_auth && !user) clearAuth(res);
        if (adminAssets.has(url.pathname)) json(res, 403, {message: 'Bạn không có quyền quản trị.'});
        else { res.writeHead(302, {Location: '/access-denied'}); res.end(); }
        return;
      }
    }

    const file = files[url.pathname];
    if (!file) { res.writeHead(404).end(); return; }
    res.writeHead(url.pathname === '/access-denied' ? 403 : 200, {
      'Content-Type': file.endsWith('.js') ? 'text/javascript; charset=utf-8'
        : file.endsWith('.css') ? 'text/css; charset=utf-8' : 'text/html; charset=utf-8'
    });
    res.end(await readFile(new URL(file, import.meta.url)));
  } catch {
    json(res, 502, {message: 'Không kết nối được dịch vụ. Vui lòng thử lại sau.'});
  }
}).listen(5173, '127.0.0.1', () => console.log('MSS Studio: http://localhost:5173'));
