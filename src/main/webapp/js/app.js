/**
 * photogear.app – Aplicación principal
 * Vanilla JS, sin dependencias adicionales.
 */

/* ── Constantes ─────────────────────────────────────────────── */
const CATEGORIES = [
  { id: 'camera',    label: 'Cámara',       icon: '📷' },
  { id: 'lens',      label: 'Objetivo',     icon: '🔭' },
  { id: 'tripod',    label: 'Trípie',       icon: '📐' },
  { id: 'lighting',  label: 'Iluminación',  icon: '💡' },
  { id: 'bag',       label: 'Bolsa',        icon: '🎒' },
  { id: 'accessory', label: 'Accesorio',    icon: '⚙️' },
];
const CAT = Object.fromEntries(CATEGORIES.map(c => [c.id, c]));

const CONDITIONS = {
  excellent: { label: 'Excelente', color: '#52b788' },
  good:      { label: 'Bueno',     color: '#95d5b2' },
  fair:      { label: 'Regular',   color: '#d4a843' },
  poor:      { label: 'Deficiente',color: '#e05252' },
};

const STATUSES = {
  active:  { label: 'Activo',         css: 'active'  },
  repair:  { label: 'En reparación',  css: 'repair'  },
  lost:    { label: 'Perdido',        css: 'lost'    },
  stolen:  { label: 'Robado',         css: 'stolen'  },
  sold:    { label: 'Vendido',        css: 'sold'    },
};

/* ── Estado global ──────────────────────────────────────────── */
let state = {
  items:      [],
  view:       'dashboard',
  selectedId: null,
  editMode:   false,
  filter:     { category: 'all', status: 'all', search: '' },
};

/* ── Utilidades ─────────────────────────────────────────────── */
const fmx  = n => n != null ? new Intl.NumberFormat('es-MX', {
  style: 'currency', currency: 'MXN', maximumFractionDigits: 0
}).format(n) : '—';

const fdate = d => d ? new Date(d + (d.includes('T') ? '' : 'T12:00:00'))
  .toLocaleDateString('es-MX', { year: 'numeric', month: 'short', day: 'numeric' }) : '—';

const esc = s => (s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

function badge(status) {
  const s = STATUSES[status] || STATUSES.active;
  return `<span class="badge badge-${s.css}">${s.label}</span>`;
}

function condDot(condition) {
  const c = CONDITIONS[condition] || CONDITIONS.good;
  return `<span class="cond-dot"><span class="dot" style="background:${c.color}"></span>${c.label}</span>`;
}

function catIcon(cat) {
  return CAT[cat]?.icon || '📷';
}

/* ── Datos de muestra ───────────────────────────────────────── */
const SAMPLE = [
  { id:'s1', category:'camera',    brand:'Canon',      model:'EOS R5',               serialNumber:'CR5-082341',    purchaseDate:'2021-08-15', purchasePrice:65000, condition:'excellent', status:'active',  warrantyHas:true,  warrantyExpiry:'2023-08-15', warrantyProvider:'Canon México', insuranceHas:true,  insuranceProvider:'GNP Seguros', insurancePolicyNumber:'GNP-4892', insuranceExpiry:'2025-12-31', notes:'Body principal. Batería LP-E6NH.', createdAt:'2021-08-15T10:00:00' },
  { id:'s2', category:'lens',      brand:'Canon',      model:'RF 50mm f/1.2L USM',   serialNumber:'RF50-29183',    purchaseDate:'2022-01-10', purchasePrice:48000, condition:'excellent', status:'active',  warrantyHas:true,  warrantyExpiry:'2024-01-10', warrantyProvider:'Canon México', insuranceHas:true,  insuranceProvider:'GNP Seguros', insurancePolicyNumber:'GNP-4892', insuranceExpiry:'2025-12-31', notes:'Lente principal para retratos. Filtro UV 77mm.', createdAt:'2022-01-10T10:00:00' },
  { id:'s3', category:'lens',      brand:'Canon',      model:'RF 24-70mm f/2.8L IS', serialNumber:'RF2470-11047',  purchaseDate:'2022-06-20', purchasePrice:55000, condition:'good',      status:'active',  warrantyHas:true,  warrantyExpiry:'2024-06-20', warrantyProvider:'Canon México', insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'Zoom polivalente. Marca menor en barrel.', createdAt:'2022-06-20T10:00:00' },
  { id:'s4', category:'camera',    brand:'DIY',        model:'Pinhole 4×5"',         serialNumber:'PINHOLE-001',   purchaseDate:'2019-04-28', purchasePrice:850,   condition:'good',      status:'active',  warrantyHas:false, warrantyExpiry:'',           warrantyProvider:'',              insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'Construida en cedro. f/138. WPPD 2019.', createdAt:'2019-04-28T10:00:00' },
  { id:'s5', category:'tripod',    brand:'Manfrotto',  model:'055XPRO3 + 496RC2',    serialNumber:'MNF055-88321',  purchaseDate:'2020-03-05', purchasePrice:9500,  condition:'good',      status:'active',  warrantyHas:false, warrantyExpiry:'',           warrantyProvider:'',              insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'Incluye cabezal 496RC2.', createdAt:'2020-03-05T10:00:00' },
  { id:'s6', category:'lighting',  brand:'Canon',      model:'Speedlite 600EX II-RT',serialNumber:'FL600-77412',   purchaseDate:'2021-11-20', purchasePrice:12000, condition:'excellent', status:'active',  warrantyHas:true,  warrantyExpiry:'2023-11-20', warrantyProvider:'Canon México', insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'Flash principal. Pilas Eneloop Pro.', createdAt:'2021-11-20T10:00:00' },
  { id:'s7', category:'bag',       brand:'Lowepro',    model:'ProTactic 450 AW II',  serialNumber:'LP-PT450-39182',purchaseDate:'2021-09-01', purchasePrice:6500,  condition:'fair',      status:'active',  warrantyHas:false, warrantyExpiry:'',           warrantyProvider:'',              insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'Zipper lateral con desgaste.', createdAt:'2021-09-01T10:00:00' },
  { id:'s8', category:'accessory', brand:'Hoya',       model:'Kit Filtros ND 77mm',  serialNumber:'HOYA-ND77-21', purchaseDate:'2021-10-15', purchasePrice:3200,  condition:'good',      status:'stolen', warrantyHas:false, warrantyExpiry:'',           warrantyProvider:'',              insuranceHas:false, insuranceProvider:'',            insurancePolicyNumber:'',         insuranceExpiry:'',           notes:'ND4, ND8, ND64, ND1000.', reportDate:'2023-09-10', reportDetails:'Sustraído en Xochimilco. Denuncia FGJ-CDMX folio #2023-XC-08821.', createdAt:'2021-10-15T10:00:00' },
];

/* ════════════════════════════════════════════════════════════
   AUTENTICACIÓN – Google Sign-In + JWT
═══════════════════════════════════════════════════════════ */

let googleReady = false;

// Llamado automáticamente por el script de Google cuando carga
window.onGoogleLibraryLoad = async function() {
  googleReady = true;
  if (state.view === 'login') await initGoogleButton();
};

async function initGoogleButton() {
  if (!googleReady || !window.google) return;
  try {
    const config = await ApiService.getAuthConfig();
    if (!config.googleClientId) {
      document.getElementById('login-no-config').style.display = 'block';
      return;
    }
    google.accounts.id.initialize({
      client_id:   config.googleClientId,
      callback:    handleGoogleCredential,
      auto_select: false
    });
    google.accounts.id.renderButton(
      document.getElementById('google-signin-btn'),
      { type: 'standard', size: 'large', theme: 'outline',
        text: 'signin_with', shape: 'rectangular', width: 300 }
    );
  } catch (e) {
    console.warn('Google Sign-In no disponible', e);
  }
}

async function handleGoogleCredential(response) {
  document.getElementById('login-error').style.display = 'none';
  const res = await ApiService.googleSignIn(response.credential);
  if (res.success) {
    Auth.set(res.token, res.user);
    updateUserUI();
    loadDashboard();
  } else {
    const el = document.getElementById('login-error');
    el.textContent = res.message || 'Error al iniciar sesión. Inténtalo de nuevo.';
    el.style.display = 'block';
  }
}

function showLoginPage() {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.getElementById('page-login').classList.add('active');
  state.view = 'login';
  document.getElementById('navbar').style.display = 'none';
  initGoogleButton();
}

function updateUserUI() {
  const user = Auth.getUser();
  if (!user) return;
  const info = document.getElementById('user-info');
  info.style.display = 'flex';
  document.getElementById('user-display-name').textContent = user.name || user.email;
  const avatar = document.getElementById('user-avatar');
  if (user.picture) {
    avatar.src   = user.picture;
    avatar.style.display = 'inline-block';
  } else {
    avatar.style.display = 'none';
  }
  document.getElementById('navbar').style.display = '';
}

function signOut() {
  if (!confirm('¿Cerrar sesión?')) return;
  Auth.clear();
  document.getElementById('user-info').style.display = 'none';
  showLoginPage();
}

/* ── Navegación ─────────────────────────────────────────────── */
function showPage(name) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById('page-' + name);
  if (page) page.classList.add('active');

  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
  const link = document.querySelector(`.nav-link[data-page="${name}"]`);
  if (link) link.classList.add('active');

  state.view = name;
  window.scrollTo(0, 0);
}

/* ════════════════════════════════════════════════════════════
   DASHBOARD
═══════════════════════════════════════════════════════════ */
async function loadDashboard() {
  const res = await ApiService.getAll();
  if (!res.success) return;
  state.items = res.data || [];

  const items   = state.items;
  const total   = items.length;
  const value   = items.reduce((s, i) => s + (i.purchasePrice || 0), 0);
  const critical = items.filter(i => i.status === 'stolen' || i.status === 'lost').length;
  const repair  = items.filter(i => i.status === 'repair').length;

  // Stat cards
  document.getElementById('stat-total').textContent   = total;
  document.getElementById('stat-value').textContent   = fmx(value);
  document.getElementById('stat-critical').textContent = critical;
  document.getElementById('stat-repair').textContent  = repair;
  document.getElementById('stat-critical').style.color =
    critical > 0 ? 'var(--red)' : 'var(--dim)';

  // Categories
  const catList = document.getElementById('cat-list');
  catList.innerHTML = CATEGORIES
    .map(c => {
      const count = items.filter(i => i.category === c.id).length;
      const val   = items.filter(i => i.category === c.id).reduce((s,i) => s+(i.purchasePrice||0),0);
      if (!count) return '';
      const pct = total ? Math.round((count / total) * 100) : 0;
      return `
        <div class="cat-row" onclick="filterAndShow('${c.id}')">
          <div class="cat-info">
            <span class="cat-name">${c.icon} ${c.label}</span>
            <span class="cat-meta">${count} · ${fmx(val)}</span>
          </div>
          <div class="bar-track"><div class="bar-fill" style="width:${pct}%"></div></div>
        </div>`;
    }).join('');

  // Recent items
  const recent = [...items].sort((a,b) => new Date(b.createdAt)-new Date(a.createdAt)).slice(0,5);
  document.getElementById('recent-list').innerHTML = recent.map(item => `
    <div class="recent-row" onclick="openDetail('${item.id}')">
      <div class="thumb">${thumbHtml(item)}</div>
      <div style="flex:1;min-width:0">
        <div class="recent-name">${esc(item.brand)} ${esc(item.model)}</div>
        <div class="recent-serial">${esc(item.serialNumber || '—')}</div>
      </div>
      ${badge(item.status)}
    </div>`).join('');

  // Incident banner
  const incidents = items.filter(i => i.status === 'lost' || i.status === 'stolen');
  const banner = document.getElementById('incident-banner');
  if (incidents.length) {
    banner.style.display = 'block';
    document.getElementById('incident-list').innerHTML = incidents.map(item => `
      <div class="recent-row" onclick="openDetail('${item.id}')" style="border-bottom:1px solid rgba(224,82,82,.15)">
        ${badge(item.status)}
        <span style="font-size:13px;margin-left:4px">${esc(item.brand)} ${esc(item.model)}</span>
        <span style="font-family:var(--font-m);font-size:11px;color:var(--muted);margin-left:8px">S/N: ${esc(item.serialNumber||'—')}</span>
      </div>`).join('');
  } else {
    banner.style.display = 'none';
  }

  showPage('dashboard');
}

function thumbHtml(item) {
  return catIcon(item.category);
}

/* ════════════════════════════════════════════════════════════
   INVENTARIO
═══════════════════════════════════════════════════════════ */
async function loadInventory() {
  renderFilterBar();
  await refreshInventory();
  showPage('inventory');
}

function renderFilterBar() {
  // Category pills
  const catPills = document.getElementById('cat-pills');
  catPills.innerHTML = [{ id:'all', label:'Todo', icon:'◎' }, ...CATEGORIES]
    .map(c => `
      <button class="filter-pill ${state.filter.category === c.id ? 'active' : ''}"
              onclick="setCatFilter('${c.id}')">
        ${c.icon || ''} ${c.label}
      </button>`).join('');

  // Status pills
  const stPills = document.getElementById('status-pills');
  const allStatuses = [{ id:'all', label:'Todos' }, ...Object.entries(STATUSES).map(([id,v]) => ({id, label:v.label}))];
  stPills.innerHTML = allStatuses
    .map(s => `
      <button class="filter-pill ${state.filter.status === s.id ? 'active' : ''}"
              onclick="setStatFilter('${s.id}')">
        ${s.label}
      </button>`).join('');
}

async function refreshInventory() {
  const res = await ApiService.getAll(state.filter);
  const items = res.success ? (res.data || []) : [];
  state.items = items;

  const grid = document.getElementById('eq-grid');
  document.getElementById('inv-count').textContent = `${items.length} equipo${items.length !== 1 ? 's' : ''}`;

  if (!items.length) {
    grid.innerHTML = `
      <div class="empty-state" style="grid-column:1/-1">
        <div class="empty-icon">📷</div>
        <div class="empty-text">Sin resultados</div>
        <div class="empty-sub">Prueba con otros filtros o agrega equipo nuevo</div>
      </div>`;
    return;
  }

  grid.innerHTML = items.map(item => `
    <div class="w3-card-4 eq-card" onclick="openDetail('${item.id}')">
      <div class="eq-card-top">
        <div class="thumb" style="width:42px;height:42px;font-size:22px">${thumbHtml(item)}</div>
        ${badge(item.status)}
      </div>
      <div class="eq-brand">${esc(item.brand)}</div>
      <div class="eq-model">${esc(item.model)}</div>
      <div class="eq-serial">S/N: ${esc(item.serialNumber || '—')}</div>
      <div class="eq-footer">
        ${condDot(item.condition)}
        <span style="font-family:var(--font-m);font-size:12px;color:var(--muted)">${fmx(item.purchasePrice)}</span>
      </div>
    </div>`).join('');
}

function setCatFilter(cat) {
  state.filter.category = cat;
  renderFilterBar();
  refreshInventory();
}
function setStatFilter(st) {
  state.filter.status = st;
  renderFilterBar();
  refreshInventory();
}
function filterAndShow(cat) {
  state.filter.category = cat;
  state.filter.status   = 'all';
  state.filter.search   = '';
  loadInventory();
}

/* ════════════════════════════════════════════════════════════
   DETALLE
═══════════════════════════════════════════════════════════ */
async function openDetail(id) {
  const res = await ApiService.getOne(id);
  if (!res.success) { alert('No se pudo cargar el equipo'); return; }
  const item = res.data;
  state.selectedId = id;

  const cat = CAT[item.category] || { label: item.category, icon: '📷' };

  document.getElementById('detail-content').innerHTML = `
    <!-- Back + header -->
    <div class="detail-header">
      <div class="detail-back" onclick="loadInventory()">← Volver al inventario</div>
      <div style="display:flex;align-items:flex-start;gap:16px">
        <div style="font-size:36px;line-height:1">${cat.icon}</div>
        <div style="flex:1">
          <div class="detail-brand">${esc(item.brand)} · ${cat.label}</div>
          <div class="detail-title">${esc(item.model)}</div>
          <div style="display:flex;gap:8px;align-items:center;margin-top:8px;flex-wrap:wrap">
            ${badge(item.status)} ${condDot(item.condition)}
          </div>
        </div>
      </div>
      <div class="detail-actions">
        <button class="btn btn-outline" onclick="openEditForm('${id}')">✏️ Editar</button>
        ${item.status !== 'lost' && item.status !== 'stolen' ? `
          <button class="btn btn-danger btn-sm" onclick="showReportModal()">⚠ Reportar perdido/robado</button>` : ''}
        <button class="btn btn-danger btn-sm" onclick="confirmDelete('${id}')">🗑 Eliminar</button>
      </div>
    </div>

    <!-- Incident banner -->
    ${(item.status === 'lost' || item.status === 'stolen') ? `
      <div class="incident-banner">
        <div class="incident-title">⚠ ${item.status === 'stolen' ? 'Equipo robado' : 'Equipo perdido'}</div>
        <div class="incident-detail">
          <strong>Fecha del incidente:</strong> ${fdate(item.reportDate)}<br>
          ${item.reportDetails ? `<strong>Detalles:</strong> ${esc(item.reportDetails)}` : ''}
        </div>
      </div>` : ''}

    <!-- Información general -->
    <div class="w3-card-4 section-card">
      <div class="section-title">📋 Información general</div>
      <div class="info-grid">
        ${infoItem('Categoría',       cat.label)}
        ${infoItem('Marca',           item.brand)}
        ${infoItem('Modelo',          item.model)}
        ${infoItem('Número de serie', item.serialNumber || '—', true)}
        ${infoItem('Condición',       condDot(item.condition))}
        ${infoItem('Estado',          badge(item.status))}
      </div>
    </div>

    <!-- Compra -->
    <div class="w3-card-4 section-card">
      <div class="section-title">💳 Compra</div>
      <div class="info-grid">
        ${infoItem('Fecha de compra', fdate(item.purchaseDate))}
        ${infoItem('Precio de compra', fmx(item.purchasePrice))}
      </div>
    </div>

    <!-- Garantía -->
    <div class="w3-card-4 section-card">
      <div class="section-title">🛡 Garantía</div>
      ${item.warrantyHas ? `
        <div class="info-grid">
          ${infoItem('Proveedor',   item.warrantyProvider || '—')}
          ${infoItem('Vence',       fdate(item.warrantyExpiry))}
        </div>` : `<p style="color:var(--muted);font-size:13px;margin:0">Sin garantía registrada</p>`}
    </div>

    <!-- Seguro -->
    <div class="w3-card-4 section-card">
      <div class="section-title">🔐 Seguro</div>
      ${item.insuranceHas ? `
        <div class="info-grid">
          ${infoItem('Aseguradora',    item.insuranceProvider || '—')}
          ${infoItem('No. de póliza', item.insurancePolicyNumber || '—', true)}
          ${infoItem('Vigencia',       fdate(item.insuranceExpiry))}
        </div>` : `<p style="color:var(--muted);font-size:13px;margin:0">Sin seguro registrado</p>`}
    </div>

    <!-- Notas -->
    ${item.notes ? `
      <div class="w3-card-4 section-card">
        <div class="section-title">📝 Notas</div>
        <p style="font-family:var(--font-b);font-size:14px;color:var(--dim);line-height:1.6;margin:0">${esc(item.notes)}</p>
      </div>` : ''}
  `;

  showPage('detail');
}

function infoItem(label, value, mono = false) {
  return `
    <div class="info-item">
      <div class="info-label">${label}</div>
      <div class="info-value${mono ? ' mono' : ''}">${value || '—'}</div>
    </div>`;
}

/* ════════════════════════════════════════════════════════════
   FORMULARIO (Agregar / Editar)
═══════════════════════════════════════════════════════════ */
function openAddForm() {
  state.editMode   = false;
  state.selectedId = null;
  document.getElementById('form-title').textContent = 'Agregar equipo';
  fillForm({});
  showPage('form');
}

async function openEditForm(id) {
  const res = await ApiService.getOne(id);
  if (!res.success) { alert('No se pudo cargar'); return; }
  const item = res.data;
  state.editMode   = true;
  state.selectedId = id;
  document.getElementById('form-title').textContent = 'Editar equipo';
  fillForm(item);
  showPage('form');
}

function fillForm(item) {
  const f = id => document.getElementById(id);
  f('f-category').value          = item.category         || 'camera';
  f('f-brand').value             = item.brand             || '';
  f('f-model').value             = item.model             || '';
  f('f-serial').value            = item.serialNumber      || '';
  f('f-purchase-date').value     = item.purchaseDate       || '';
  f('f-purchase-price').value    = item.purchasePrice      || '';
  f('f-condition').value         = item.condition          || 'excellent';
  f('f-status').value            = item.status             || 'active';
  f('f-warranty-has').checked    = !!item.warrantyHas;
  f('f-warranty-expiry').value   = item.warrantyExpiry     || '';
  f('f-warranty-provider').value = item.warrantyProvider   || '';
  f('f-insurance-has').checked   = !!item.insuranceHas;
  f('f-insurance-provider').value     = item.insuranceProvider     || '';
  f('f-insurance-policy').value       = item.insurancePolicyNumber || '';
  f('f-insurance-expiry').value       = item.insuranceExpiry       || '';
  f('f-notes').value             = item.notes             || '';
  toggleWarrantyFields();
  toggleInsuranceFields();
}

function toggleWarrantyFields() {
  const show = document.getElementById('f-warranty-has').checked;
  document.getElementById('warranty-fields').style.display = show ? 'grid' : 'none';
}
function toggleInsuranceFields() {
  const show = document.getElementById('f-insurance-has').checked;
  document.getElementById('insurance-fields').style.display = show ? 'grid' : 'none';
}

async function saveForm() {
  const f = id => document.getElementById(id);

  const brand = f('f-brand').value.trim();
  const model = f('f-model').value.trim();
  if (!brand || !model) {
    alert('Marca y modelo son obligatorios.');
    return;
  }

  const item = {
    id:                   state.selectedId || undefined,
    category:             f('f-category').value,
    brand,
    model,
    serialNumber:         f('f-serial').value.trim(),
    purchaseDate:         f('f-purchase-date').value || null,
    purchasePrice:        f('f-purchase-price').value ? Number(f('f-purchase-price').value) : null,
    condition:            f('f-condition').value,
    status:               f('f-status').value,
    warrantyHas:          f('f-warranty-has').checked,
    warrantyExpiry:       f('f-warranty-expiry').value || null,
    warrantyProvider:     f('f-warranty-provider').value.trim(),
    insuranceHas:         f('f-insurance-has').checked,
    insuranceProvider:    f('f-insurance-provider').value.trim(),
    insurancePolicyNumber:f('f-insurance-policy').value.trim(),
    insuranceExpiry:      f('f-insurance-expiry').value || null,
    notes:                f('f-notes').value.trim(),
  };

  const btn = document.getElementById('btn-save');
  btn.disabled = true; btn.textContent = 'Guardando…';

  const res = state.editMode
    ? await ApiService.update(item)
    : await ApiService.create(item);

  btn.disabled = false; btn.textContent = 'Guardar';

  if (res.success) {
    const savedId = res.data?.id || item.id;
    if (savedId) {
      state.selectedId = savedId;
      openDetail(savedId);
    } else {
      loadInventory();
    }
  } else {
    alert('Error al guardar: ' + (res.message || 'Desconocido'));
  }
}

/* ════════════════════════════════════════════════════════════
   REPORTE PERDIDO / ROBADO
═══════════════════════════════════════════════════════════ */
function showReportModal() {
  document.getElementById('r-status').value  = 'lost';
  document.getElementById('r-date').value    = new Date().toISOString().split('T')[0];
  document.getElementById('r-details').value = '';
  document.getElementById('modal-report').style.display = 'flex';
}
function closeReportModal() {
  document.getElementById('modal-report').style.display = 'none';
}

async function submitReport() {
  const status  = document.getElementById('r-status').value;
  const date    = document.getElementById('r-date').value;
  const details = document.getElementById('r-details').value.trim();

  if (!date) { alert('Indica la fecha del incidente'); return; }

  const res = await ApiService.report(state.selectedId, {
    status, reportDate: date, details
  });

  if (res.success) {
    closeReportModal();
    openDetail(state.selectedId); // Recargar detalle
  } else {
    alert('Error al guardar el reporte: ' + (res.message || ''));
  }
}

/* ── Eliminar ────────────────────────────────────────────────── */
async function confirmDelete(id) {
  if (!confirm('¿Eliminar este equipo del inventario? Esta acción no se puede deshacer.')) return;
  const res = await ApiService.delete(id);
  if (res.success) {
    loadInventory();
  } else {
    alert('Error al eliminar: ' + (res.message || ''));
  }
}

/* ════════════════════════════════════════════════════════════
   INIT
═══════════════════════════════════════════════════════════ */
async function init() {
  // Cargar datos de muestra si no hay nada guardado
  const existing = LocalDB.load();
  if (!existing.length) LocalDB.save(SAMPLE);

  // Detectar API
  await detectApi();

  if (apiOnline) {
    // Modo online: se requiere sesión activa
    if (!Auth.isLoggedIn()) {
      showLoginPage();
      return;
    }
    updateUserUI();
  }
  // Modo offline: usa LocalDB, sin auth

  loadDashboard();

  // Búsqueda en tiempo real
  let searchTimer;
  document.getElementById('search-input').addEventListener('input', e => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      state.filter.search = e.target.value;
      refreshInventory();
    }, 300);
  });
}

document.addEventListener('DOMContentLoaded', init);
