/**
 * photogear.app – Capa de API
 *
 * Intenta comunicarse con el backend Java en Tomcat.
 * Si la API no está disponible, cae en modo offline con localStorage.
 *
 * Configuración:
 *   API_BASE → URL base del backend. Cuando el frontend se sirve
 *              desde el mismo Tomcat, '/photogear/api' funciona sin CORS.
 */

const API_BASE = '/photogear/api';       // Ajustar si el contexto cambia
let   apiOnline = false;                // Se detecta automáticamente

/* ── Detección de conectividad ──────────────────────────────── */
async function detectApi() {
  try {
    const r = await fetch(API_BASE + '/equipment', { method: 'GET', signal: AbortSignal.timeout(2000) });
    apiOnline = r.ok || r.status < 500;
  } catch {
    apiOnline = false;
  }
  updateApiPill();
  return apiOnline;
}

function updateApiPill() {
  const el = document.getElementById('api-status');
  if (!el) return;
  el.className = 'api-pill ' + (apiOnline ? 'online' : 'offline');
  el.innerHTML = apiOnline
    ? '● API conectada'
    : '● Modo local';
}

/* ── Local Storage (fallback / demo) ────────────────────────── */
const LocalDB = {
  KEY: 'photogear_v1',

  load() {
    try { return JSON.parse(localStorage.getItem(this.KEY) || '[]'); }
    catch { return []; }
  },

  save(items) {
    try { localStorage.setItem(this.KEY, JSON.stringify(items)); }
    catch (e) { console.error('LocalDB save error', e); }
  },

  findAll({ category, status, search } = {}) {
    let items = this.load();
    if (category && category !== 'all') items = items.filter(i => i.category === category);
    if (status   && status   !== 'all') items = items.filter(i => i.status   === status);
    if (search) {
      const q = search.toLowerCase();
      items = items.filter(i =>
        (i.brand + ' ' + i.model + ' ' + (i.serialNumber || '')).toLowerCase().includes(q)
      );
    }
    return items.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  },

  findById(id) { return this.load().find(i => i.id === id) || null; },

  create(item) {
    const items = this.load();
    item.id        = item.id        || crypto.randomUUID();
    item.createdAt = item.createdAt || new Date().toISOString();
    item.updatedAt = new Date().toISOString();
    items.unshift(item);
    this.save(items);
    return item;
  },

  update(item) {
    const items = this.load();
    const idx   = items.findIndex(i => i.id === item.id);
    if (idx === -1) return false;
    item.updatedAt = new Date().toISOString();
    items[idx] = item;
    this.save(items);
    return true;
  },

  delete(id) {
    const items = this.load();
    const filtered = items.filter(i => i.id !== id);
    if (filtered.length === items.length) return false;
    this.save(filtered);
    return true;
  },

  report(id, status, reportDate, details) {
    const items = this.load();
    const idx   = items.findIndex(i => i.id === id);
    if (idx === -1) return false;
    items[idx].status        = status;
    items[idx].reportDate    = reportDate;
    items[idx].reportDetails = details;
    items[idx].updatedAt     = new Date().toISOString();
    this.save(items);
    return true;
  }
};

/* ── Auth token helpers ──────────────────────────────────────── */
const Auth = {
  TOKEN_KEY: 'photogear_token',
  USER_KEY:  'photogear_user',

  getToken() { return localStorage.getItem(this.TOKEN_KEY); },

  getUser() {
    try { return JSON.parse(localStorage.getItem(this.USER_KEY)); }
    catch { return null; }
  },

  set(token, user) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  },

  clear() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  },

  isLoggedIn() { return !!this.getToken(); }
};

/* ── API HTTP calls ─────────────────────────────────────────── */
async function apiFetch(method, path, body) {
  const token = Auth.getToken();
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (body)  opts.body = JSON.stringify(body);

  let r;
  try {
    r = await fetch(API_BASE + path, opts);
  } catch (e) {
    console.error('Error de red', e);
    return { success: false, message: 'No se pudo conectar con el servidor.' };
  }

  if (r.status === 401) {
    Auth.clear();
    showLoginPage();
    return { success: false, message: 'Sesión expirada. Inicia sesión nuevamente.' };
  }

  try {
    return await r.json();
  } catch {
    return { success: false, message: `Respuesta inesperada del servidor (HTTP ${r.status}).` };
  }
}

/* ── Servicio unificado ──────────────────────────────────────── */
const ApiService = {

  async getAll(params = {}) {
    if (!apiOnline) {
      const data = LocalDB.findAll(params);
      return { success: true, data, total: data.length };
    }
    const qs = new URLSearchParams(
      Object.fromEntries(Object.entries(params).filter(([,v]) => v && v !== 'all'))
    ).toString();
    return apiFetch('GET', `/equipment${qs ? '?' + qs : ''}`);
  },

  async getOne(id) {
    if (!apiOnline) {
      const item = LocalDB.findById(id);
      return item ? { success: true, data: item } : { success: false, message: 'No encontrado' };
    }
    return apiFetch('GET', `/equipment/${id}`);
  },

  async create(item) {
    if (!apiOnline) {
      const created = LocalDB.create(item);
      return { success: true, data: created };
    }
    return apiFetch('POST', '/equipment', item);
  },

  async update(item) {
    if (!apiOnline) {
      const ok = LocalDB.update(item);
      return { success: ok, data: item };
    }
    return apiFetch('PUT', `/equipment/${item.id}`, item);
  },

  async delete(id) {
    if (!apiOnline) {
      const ok = LocalDB.delete(id);
      return { success: ok };
    }
    return apiFetch('DELETE', `/equipment/${id}`);
  },

  async report(id, payload) {
    if (!apiOnline) {
      const ok = LocalDB.report(id, payload.status, payload.reportDate, payload.details);
      return { success: ok };
    }
    return apiFetch('POST', `/equipment/${id}/report`, payload);
  },

  async googleSignIn(credential) {
    return apiFetch('POST', '/auth/google', { credential });
  },

  async getAuthConfig() {
    try {
      const r = await fetch(API_BASE + '/auth/config');
      return r.json();
    } catch { return {}; }
  }
};
