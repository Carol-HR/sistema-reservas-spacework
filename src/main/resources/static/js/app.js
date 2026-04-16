// ============================================================
// MODAL DE CONFIRMACIÓN BOOTSTRAP
// ============================================================
function showConfirm({ message, icon = '⚠️', title = 'Confirmar acción', btnLabel = 'Confirmar', btnClass = 'btn-danger', onConfirm }) {
    document.getElementById('confirmModalIcon').textContent = icon;
    document.getElementById('confirmModalTitle').textContent = title;
    document.getElementById('confirmModalMessage').textContent = message;
    const okBtn = document.getElementById('confirmModalOkBtn');
    okBtn.textContent = btnLabel;
    okBtn.className = `btn ${btnClass}`;
    // Estilo del header según tipo
    const header = document.getElementById('confirmModalHeader');
    header.className = 'modal-header ' + (btnClass === 'btn-danger' ? 'bg-danger text-white' : btnClass === 'btn-warning' ? 'bg-warning' : 'bg-primary text-white');
    // Clonar botón para limpiar eventos anteriores
    const newOkBtn = okBtn.cloneNode(true);
    okBtn.parentNode.replaceChild(newOkBtn, okBtn);
    newOkBtn.className = `btn ${btnClass}`;
    newOkBtn.textContent = btnLabel;
    const modal = getModal('confirmModal');
    newOkBtn.addEventListener('click', () => {
        modal.hide();
        onConfirm();
    });
    modal.show();
}

// SpaceWork API Client
const API_BASE_URL = '/api';
let currentUser = null;

function getModal(id) {
    const el = document.getElementById(id);
    if (!el) return { show: () => {}, hide: () => {} };
    if (typeof bootstrap !== 'undefined') {
        return bootstrap.Modal.getInstance(el) || new bootstrap.Modal(el);
    }
    // Fallback si Bootstrap no carga
    return {
        show: () => { el.style.display = 'flex'; el.classList.add('show'); document.body.classList.add('modal-open'); },
        hide: () => { el.style.display = 'none'; el.classList.remove('show'); document.body.classList.remove('modal-open'); }
    };
}

// ============================================================
// INICIALIZACIÓN
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('login-form').addEventListener('submit', handleLogin);

    const savedUser = localStorage.getItem('user');
    if (savedUser) {
        try {
            currentUser = JSON.parse(savedUser);
            showPage('dashboard');
            showSection('dashboard');
        } catch (e) {
            localStorage.clear();
        }
    }
});

// ============================================================
// AUTENTICACIÓN
// ============================================================
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('login-error');
    errorDiv.classList.add('d-none');

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (data.success) {
            currentUser = data.user;
            localStorage.setItem('user', JSON.stringify(data.user));
            showPage('dashboard');
            showSection('dashboard');
        } else {
            errorDiv.textContent = data.error || 'Credenciales inválidas';
            errorDiv.classList.remove('d-none');
        }
    } catch (error) {
        errorDiv.textContent = 'Error de conexión. ¿Está el servidor corriendo?';
        errorDiv.classList.remove('d-none');
    }
}

function logout() {
    localStorage.clear();
    currentUser = null;
    showPage('login');
    document.getElementById('login-form').reset();
}

// ============================================================
// NAVEGACIÓN
// ============================================================
function showPage(pageName) {
    const loginPage = document.getElementById('login-page');
    const dashPage = document.getElementById('dashboard-page');

    if (pageName === 'login') {
        loginPage.classList.remove('d-none');
        dashPage.classList.add('d-none');
    } else {
        loginPage.classList.add('d-none');
        dashPage.classList.remove('d-none');
    }
}

function showSection(sectionName) {
    document.querySelectorAll('.section').forEach(s => s.classList.add('d-none'));
    const sec = document.getElementById(sectionName);
    if (sec) {
        sec.classList.remove('d-none');
        if (sectionName === 'dashboard') loadDashboard();
        if (sectionName === 'reservas') loadReservas();
        if (sectionName === 'espacios') loadEspacios();
        if (sectionName === 'clientes') loadClientes();
        if (sectionName === 'horarios') loadHorariosBloqueados();
        if (sectionName === 'pagos') cargarPagos();
        if (sectionName === 'descuentos') cargarDescuentos();
        if (sectionName === 'evaluaciones') cargarEvaluaciones();
        if (sectionName === 'notificaciones') cargarNotificaciones();
    }
}

// ============================================================
// DASHBOARD
// ============================================================
async function loadDashboard() {
    try {
        const resRes = await fetch(`${API_BASE_URL}/reservas`);
        const resData = await resRes.json();
        const espaciosRes = await fetch(`${API_BASE_URL}/espacios`);
        const espaciosData = await espaciosRes.json();
        const clientesRes = await fetch(`${API_BASE_URL}/clientes`);
        const clientesData = await clientesRes.json();

        let totalReservas = 0, confirmadas = 0, completadas = 0, espaciosActivos = 0, totalClientes = 0, totalIngresos = 0;

        if (resData.success && resData.data) {
            totalReservas = resData.data.length;
            confirmadas = resData.data.filter(r => r.estado === 'CONFIRMADA').length;
            completadas = resData.data.filter(r => r.estado === 'COMPLETADA').length;
            totalIngresos = resData.data.filter(r => r.estado === 'COMPLETADA').reduce((sum, r) => sum + (r.montoTotal || 0), 0);
        }

        if (espaciosData.success && espaciosData.data) {
            espaciosActivos = espaciosData.data.filter(e => e.estado === 'ACTIVO').length;
        }

        if (clientesData.success && clientesData.data) {
            totalClientes = clientesData.data.length;
        }

        const ocupacion = espaciosActivos > 0 ? Math.round((confirmadas / espaciosActivos) * 100) : 0;

        document.getElementById('kpi-reservas').textContent = totalReservas;
        document.getElementById('kpi-reservas-detail').textContent = completadas + ' completadas';
        document.getElementById('kpi-confirmadas').textContent = confirmadas;
        document.getElementById('kpi-espacios').textContent = espaciosActivos;
        document.getElementById('kpi-ocupacion').textContent = ocupacion + '%';
        document.getElementById('kpi-clientes').textContent = totalClientes;
        document.getElementById('kpi-ingresos').textContent = 'S/. ' + totalIngresos.toFixed(2);
        
        // Cargar calendario semanal
        await loadCalendarioSemanal();
    } catch (e) {
        console.error('Error loading dashboard:', e);
        showAlert('error', 'Error al cargar dashboard');
    }
}

let _calendario_data = null;

async function loadCalendarioSemanal() {
    try {
        const res = await fetch(`${API_BASE_URL}/calendario/semanal`);
        const data = await res.json();
        if (data.success && data.data) {
            _calendario_data = data.data;
            const selectEspacio = document.getElementById('filtro-calendario-espacio');
            if (selectEspacio && data.data.espacios) {
                let optionsHtml = '<option value="">-- Todos los espacios --</option>';
                data.data.espacios.forEach(e => {
                    optionsHtml += '<option value="' + e.idEspacio + '">' + e.nombre + '</option>';
                });
                selectEspacio.innerHTML = optionsHtml;
            }
            renderCalendarioSemanal(data.data);
        }
    } catch (e) {
        console.error('Error loading calendario:', e);
    }
}

function filtrarCalendarioPorEspacio() {
    if (!_calendario_data) return;
    const idEspacioSeleccionado = document.getElementById('filtro-calendario-espacio').value;
    
    if (idEspacioSeleccionado) {
        const espaciosFiltrados = _calendario_data.espacios.filter(e => e.idEspacio == idEspacioSeleccionado);
        const datosFilterados = {
            espacios: espaciosFiltrados,
            bloques: _calendario_data.bloques
        };
        renderCalendarioSemanal(datosFilterados);
    } else {
        renderCalendarioSemanal(_calendario_data);
    }
}

function renderCalendarioSemanal(calendarioData) {
    const espacios = calendarioData.espacios || [];
    const bloques = calendarioData.bloques || [];
    
    if (espacios.length === 0) {
        document.getElementById('calendario-body').innerHTML = '<tr><td colspan="100" class="text-center text-muted">No hay espacios activos</td></tr>';
        return;
    }
    
    // Agrupar bloques por fecha y hora
    const bloquesByKey = {};
    bloques.forEach(b => {
        const key = b.fecha + '|' + b.hora;
        bloquesByKey[key] = b.espacios || {};
    });
    
    const fechas = [...new Set(bloques.map(b => b.fecha))].sort();
    const horas = [...new Set(bloques.map(b => b.hora))].sort();
    
    if (fechas.length === 0 || horas.length === 0) {
        document.getElementById('calendario-body').innerHTML = '<tr><td colspan="100" class="text-center">Cargando...</td></tr>';
        return;
    }
    
    // Renderizar header (fechas)
    let headerHtml = '<tr><th style="width: 120px; background: #f0f0f0;">Espacio / Hora</th>';
    fechas.forEach(f => {
        const d = new Date(f + 'T00:00:00');
        const formato = d.toLocaleDateString('es-PE', {weekday: 'short', month: 'short', day: 'numeric'});
        headerHtml += '<th style="text-align: center; background: #f0f0f0;"><strong>' + formato + '</strong></th>';
    });
    headerHtml += '</tr>';
    document.getElementById('calendario-header').innerHTML = headerHtml;
    
    // Renderizar body (espacios x fechas/horas)
    let bodyHtml = '';
    
    espacios.forEach(esp => {
        horas.forEach(hora => {
            bodyHtml += '<tr>';
            
            // Encabezado espacio + hora
            bodyHtml += '<td style="font-weight: bold; background: #f9f9f9; font-size: 0.9rem;">' + 
                        esp.nombre + '<br><small style="font-weight: normal; color: #666;">' + hora + '</small></td>';
            
            // Celdas de cada fecha
            fechas.forEach(fecha => {
                const key = fecha + '|' + hora;
                const espaciosEnBloque = bloquesByKey[key] || {};
                const estado = espaciosEnBloque[esp.idEspacio] || 'disponible';
                
                const colorMap = {
                    'disponible': '#d4edda',
                    'ocupado': '#f8d7da',
                    'bloqueado': '#fff3cd'
                };
                const borderColorMap = {
                    'disponible': '#28a745',
                    'ocupado': '#dc3545',
                    'bloqueado': '#ff9800'
                };
                
                const bgColor = colorMap[estado] || '#d4edda';
                const borderColor = borderColorMap[estado] || '#28a745';
                const emoji = estado === 'disponible' ? '✓' : (estado === 'ocupado' ? '✗' : '🔒');
                
                bodyHtml += '<td style="background: ' + bgColor + '; border-left: 3px solid ' + borderColor + 
                           '; text-align: center; cursor: pointer; padding: 8px;" title="' + 
                           esp.nombre + ' - ' + hora + ' - ' + estado + '">' + emoji + '</td>';
            });
            
            bodyHtml += '</tr>';
        });
    });
    
    document.getElementById('calendario-body').innerHTML = bodyHtml;
}

// ============================================================
// RESERVAS
// ============================================================
async function loadReservas() {
    const container = document.getElementById('reservas-list');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando reservas...</p></div>';
    try {
        // Precargar clientes y espacios para rellenar datos en las tarjetas
        if (_allClientes.length === 0) {
            try {
                const resClientes = await fetch(`${API_BASE_URL}/clientes`);
                const dataClientes = await resClientes.json();
                if (dataClientes.success) _allClientes = dataClientes.data;
            } catch (e) {
                console.warn('No se pudieron precargar clientes');
            }
        }
        
        if (_allEspacios.length === 0) {
            try {
                const resEspacios = await fetch(`${API_BASE_URL}/espacios`);
                const dataEspacios = await resEspacios.json();
                if (dataEspacios.success) _allEspacios = dataEspacios.data;
            } catch (e) {
                console.warn('No se pudieron precargar espacios');
            }
        }
        
        const res = await fetch(`${API_BASE_URL}/reservas`);
        const data = await res.json();
        if (data.success && data.data) {
            _allReservas = data.data;
            // Ordenar por idReserva (descendente para que los nuevos aparezan primero)
            _allReservas.sort((a, b) => b.idReserva - a.idReserva);
            renderReservas(_allReservas);
        } else {
            container.innerHTML = '<div class="col-12"><p class="text-danger">Error al cargar reservas</p></div>';
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><p class="text-danger">Error de conexión</p></div>';
    }
}

function renderReservas(reservas) {
    const container = document.getElementById('reservas-list');
    container.innerHTML = '';

    if (!reservas || reservas.length === 0) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay reservas registradas</div></div>';
        return;
    }

    reservas.forEach(r => {
        // Cargar datos completos de cliente y espacio desde caché si no están en r
        let clienteCompleto = null;
        let espacioCompleto = null;
        
        if (_allClientes) {
            clienteCompleto = _allClientes.find(c => c.idCliente === r.idCliente);
        }
        if (_allEspacios) {
            espacioCompleto = _allEspacios.find(e => e.idEspacio === r.idEspacio);
        }

        const fechaIni = r.fechaInicio ? new Date(r.fechaInicio) : null;
        const fechaFin = r.fechaFin ? new Date(r.fechaFin) : null;
        const fechaIniStr = fechaIni ? fechaIni.toLocaleString('es-PE') : '-';
        const fechaFinStr = fechaFin ? fechaFin.toLocaleString('es-PE') : '-';
        
        // Calcular duración en horas
        let duracionHoras = '-';
        if (fechaIni && fechaFin) {
            const diffMs = fechaFin - fechaIni;
            duracionHoras = (diffMs / (1000 * 60 * 60)).toFixed(1) + ' hrs';
        }
        
        const badgeColor = r.estado === 'CONFIRMADA' ? 'success' : r.estado === 'CANCELADA' ? 'danger' : r.estado === 'COMPLETADA' ? 'primary' : 'warning';
        const clienteNombre = r.nombreCliente || (clienteCompleto ? clienteCompleto.nombreCompleto : '-');
        const clienteDni = clienteCompleto ? clienteCompleto.dni : '-';
        const clienteEmail = clienteCompleto ? clienteCompleto.email : '-';
        const espacioNombre = r.nombreEspacio || (espacioCompleto ? espacioCompleto.nombre : '-');
        const espacioTipo = espacioCompleto ? espacioCompleto.tipo : '-';
        const precioPorHora = espacioCompleto ? espacioCompleto.precioPorHora || 0 : 0;

        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';

        let botones = '';
        if (r.estado === 'PENDIENTE') {
            botones = `
                <button class="btn btn-sm btn-success" onclick="confirmarReserva(${r.idReserva})">✅ Confirmar</button>
                <button class="btn btn-sm btn-danger" onclick="cancelarReserva(${r.idReserva})">❌ Cancelar</button>
            `;
        } else if (r.estado === 'CONFIRMADA') {
            botones = `
                <button class="btn btn-sm btn-primary" onclick="completarReserva(${r.idReserva})">🏁 Completada</button>
                <button class="btn btn-sm btn-danger" onclick="cancelarReserva(${r.idReserva})">❌ Cancelar</button>
            `;
        }

        col.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-header bg-light d-flex justify-content-between align-items-center">
                    <h5 class="card-title mb-0">Reserva #${r.idReserva}</h5>
                    <span class="badge bg-${badgeColor}">${r.estado}</span>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">📝 Datos del Cliente</h6>
                        <p class="card-text mb-1"><strong>👤 Nombre:</strong> ${clienteNombre}</p>
                        <p class="card-text mb-1"><strong>🆔 DNI:</strong> ${clienteDni}</p>
                        <p class="card-text mb-2"><strong>📧 Email:</strong> ${clienteEmail}</p>
                    </div>
                    <hr class="my-2">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">🏛️ Espacio Reservado</h6>
                        <p class="card-text mb-1"><strong>Nombre:</strong> ${espacioNombre}</p>
                        <p class="card-text mb-1"><strong>Tipo:</strong> ${espacioTipo}</p>
                        <p class="card-text mb-2"><strong>Precio/Hora:</strong> S/. ${precioPorHora.toFixed(2)}</p>
                    </div>
                    <hr class="my-2">
                    <div class="mb-3">
                        <h6 class="text-secondary mb-2">📅 Fechas y Duración</h6>
                        <p class="card-text mb-1"><small><strong>Inicio:</strong> ${fechaIniStr}</small></p>
                        <p class="card-text mb-1"><small><strong>Fin:</strong> ${fechaFinStr}</small></p>
                        <p class="card-text mb-2"><small><strong>Duración:</strong> ${duracionHoras}</small></p>
                    </div>
                    <hr class="my-2">
                    <p class="card-text fw-bold text-success mb-0">💰 Total: S/. ${(r.montoTotal || 0).toFixed(2)}</p>
                </div>
                ${botones ? `<div class="card-footer bg-white d-flex gap-2 flex-wrap">${botones}</div>` : ''}
            </div>
        `;
        container.appendChild(col);
    });
}

// Caché de clientes/espacios para el modal de reservas
let _cacheClientes = [];
let _cacheEspacios = [];

async function abrirFormularioReserva() {
    // Cargar clientes y espacios si no están en caché
    if (_cacheClientes.length === 0) {
        const res = await fetch(`${API_BASE_URL}/clientes`);
        const data = await res.json();
        _cacheClientes = data.success ? data.data : [];
    }
    if (_cacheEspacios.length === 0) {
        const res = await fetch(`${API_BASE_URL}/espacios`);
        const data = await res.json();
        _cacheEspacios = data.success ? data.data : [];
    }

    // Poblar selects
    const selCliente = document.getElementById('reservaIdCliente');
    const selEspacio = document.getElementById('reservaIdEspacio');
    selCliente.innerHTML = '<option value="">-- Seleccione un cliente --</option>';
    selEspacio.innerHTML = '<option value="">-- Seleccione un espacio --</option>';

    _cacheClientes.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.idCliente;
        opt.textContent = (c.nombreCompleto || `${c.nombre} ${c.apellido}`) + ` (DNI: ${c.dni})`;
        selCliente.appendChild(opt);
    });

    _cacheEspacios.filter(e => e.estado === 'ACTIVO').forEach(e => {
        const opt = document.createElement('option');
        opt.value = e.idEspacio;
        opt.textContent = `${e.nombre} — S/. ${(e.precioPorHora||0).toFixed(2)}/hr`;
        opt.dataset.precio = e.precioPorHora || 0;
        selEspacio.appendChild(opt);
    });

    document.getElementById('reservaForm').reset();
    document.getElementById('monto-preview').classList.add('d-none');
    getModal('reservaModal').show();
}

function calcularMonto() {
    const selEspacio = document.getElementById('reservaIdEspacio');
    const fechaIni = document.getElementById('reservaFechaInicio').value;
    const fechaFin = document.getElementById('reservaFechaFin').value;
    const preview = document.getElementById('monto-preview');

    if (!selEspacio.value || !fechaIni || !fechaFin) { preview.classList.add('d-none'); return; }

    const opt = selEspacio.selectedOptions[0];
    const precio = parseFloat(opt.dataset.precio || 0);
    const ini = new Date(fechaIni);
    const fin = new Date(fechaFin);
    const horas = (fin - ini) / (1000 * 60 * 60);

    if (horas <= 0) { preview.classList.add('d-none'); return; }

    const monto = (precio * horas).toFixed(2);
    document.getElementById('monto-valor').textContent = `S/. ${monto} (${horas.toFixed(1)} horas × S/. ${precio.toFixed(2)}/hr)`;
    preview.classList.remove('d-none');
}

async function guardarReserva() {
    const idCliente  = document.getElementById('reservaIdCliente').value;
    const idEspacio  = document.getElementById('reservaIdEspacio').value;
    const fechaInicio = document.getElementById('reservaFechaInicio').value;
    const fechaFin   = document.getElementById('reservaFechaFin').value;

    if (!idCliente || !idEspacio || !fechaInicio || !fechaFin) {
        showAlert('error', 'Complete todos los campos');
        return;
    }
    if (new Date(fechaFin) <= new Date(fechaInicio)) {
        showAlert('error', 'La fecha fin debe ser posterior a la fecha inicio');
        return;
    }

    // Validar horarios bloqueados
    const sinBloqueo = await validarDisponibilidadEspacio(idEspacio, fechaInicio, fechaFin);
    if (!sinBloqueo) {
        showAlert('error', '⚠️ El espacio tiene un horario bloqueado en esa fecha. Selecciona otro horario.');
        return;
    }

    // Validar que no exista otra reserva en el mismo rango
    const sinConflicto = await validarSinReservaExistente(idEspacio, fechaInicio, fechaFin);
    if (!sinConflicto) {
        showAlert('error', '⚠️ El espacio ya tiene una reserva en ese rango de horario. Selecciona otra sala u otro horario.');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/reservas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idCliente: parseInt(idCliente), idEspacio: parseInt(idEspacio), fechaInicio, fechaFin })
        });
        const data = await res.json();
        if (data.success) {
            showAlert('success', `✅ Reserva creada. Monto: S/. ${(data.monto||0).toFixed(2)}`);
            getModal('reservaModal').hide();
            loadReservas();
        } else {
            showAlert('error', data.error || data.message || 'Error al crear reserva');
        }
    } catch (e) {
        showAlert('error', 'Error de conexión al crear reserva');
    }
}

async function confirmarReserva(id) {
    showConfirm({
        message: '¿Confirmar esta reserva?',
        icon: '✅',
        title: 'Confirmar Reserva',
        btnLabel: 'Sí, confirmar',
        btnClass: 'btn-success',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}/confirmar`, { method: 'PUT' });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '✅ Reserva confirmada' : data.error);
            if (data.success) loadReservas();
        }
    });
}

async function completarReserva(id) {
    showConfirm({
        message: '¿Marcar esta reserva como completada? Se creará un pago pendiente.',
        icon: '🏁',
        title: 'Completar Reserva',
        btnLabel: 'Sí, completar',
        btnClass: 'btn-primary',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}/completar`, { method: 'PUT' });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '✅ Reserva completada - Ve a Pagos para pagar' : data.error);
            if (data.success) loadReservas();
        }
    });
}

async function cancelarReserva(id) {
    showConfirm({
        message: '¿Cancelar esta reserva? Esta acción no se puede deshacer.',
        icon: '🚫',
        title: 'Cancelar Reserva',
        btnLabel: 'Sí, cancelar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            const res = await fetch(`${API_BASE_URL}/reservas/${id}`, { method: 'DELETE' });
            const data = await res.json();
            showAlert(data.success ? 'success' : 'error', data.success ? '❌ Reserva cancelada' : data.error);
            if (data.success) loadReservas();
        }
    });
}

// ============================================================
// ESPACIOS
// ============================================================
async function loadEspacios() {
    const container = document.getElementById('espacios-grid');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando espacios...</p></div>';
    try {
        const res = await fetch(`${API_BASE_URL}/espacios`);
        const data = await res.json();
        if (data.success && data.data) {
            _allEspacios = data.data;
            renderEspacios(data.data);
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><p class="text-danger">Error de conexión</p></div>';
    }
}

function renderEspacios(espacios) {
    const container = document.getElementById('espacios-grid');
    container.innerHTML = '';

    if (!espacios || espacios.length === 0) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay espacios registrados</div></div>';
        return;
    }

    espacios.forEach(e => {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';
        col.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">${e.nombre}</h5>
                    <p class="card-text mb-1"><strong>Tipo:</strong> ${e.tipo}</p>
                    <p class="card-text mb-1"><strong>Capacidad:</strong> ${e.capacidad} personas</p>
                    <p class="card-text mb-1"><strong>Ubicación:</strong> ${e.ubicacion}</p>
                    <p class="card-text mb-2"><strong>Precio:</strong> S/. ${(e.precioPorHora || 0).toFixed(2)}/hora</p>
                    <span class="badge bg-${e.estado === 'ACTIVO' ? 'success' : 'danger'}">${e.estado}</span>
                </div>
                <div class="card-footer bg-white border-top">
                    <button class="btn btn-sm btn-primary" onclick="editarEspacio(${e.idEspacio})">✏️ Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarEspacio(${e.idEspacio})">🗑️ Eliminar</button>
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}

function abrirFormularioEspacio() {
    document.getElementById('espacioId').value = '';
    document.getElementById('espacioForm').reset();
    document.getElementById('espacioModalTitle').textContent = 'Nuevo Espacio';
    const modal = getModal('espacioModal');
    modal.show();
}

async function editarEspacio(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/espacios/${id}`);
        const data = await res.json();
        if (!data.success || !data.data) {
            showAlert('error', 'No se pudo cargar el espacio');
            return;
        }
        const e = data.data;
        document.getElementById('espacioId').value = e.idEspacio;
        document.getElementById('espacioNombre').value = e.nombre || '';
        document.getElementById('espacioTipo').value = e.tipo || '';
        document.getElementById('espacioCapacidad').value = e.capacidad || '';
        document.getElementById('espacioUbicacion').value = e.ubicacion || '';
        document.getElementById('espacioPrecio').value = e.precioPorHora || '';
        document.getElementById('espacioModalTitle').textContent = 'Editar Espacio';
        getModal('espacioModal').show();
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function eliminarEspacio(id) {
    showConfirm({
        message: '¿Eliminar este espacio? Ya no estará disponible para reservas.',
        icon: '🗑️',
        title: 'Eliminar Espacio',
        btnLabel: 'Sí, eliminar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/espacios/${id}`, { method: 'DELETE' });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Espacio eliminado correctamente');
                    loadEspacios();
                } else {
                    showAlert('error', data.error || 'No se pudo eliminar');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

async function guardarEspacio() {
    const id = document.getElementById('espacioId').value;
    const nombre = document.getElementById('espacioNombre').value;
    const tipo = document.getElementById('espacioTipo').value;
    const capacidad = parseInt(document.getElementById('espacioCapacidad').value);
    const ubicacion = document.getElementById('espacioUbicacion').value;
    const precio = parseFloat(document.getElementById('espacioPrecio').value);

    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/espacios/${id}` : `${API_BASE_URL}/espacios`;
        
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, tipo, capacidad, ubicacion, precioPorHora: precio })
        });

        const data = await res.json();
        if (data.success) {
            showAlert('success', id ? 'Espacio actualizado' : 'Espacio creado');
            const modal = getModal('espacioModal');
            modal.hide();
            loadEspacios();
        } else {
            showAlert('error', data.message || 'Error');
        }
    } catch (e) {
        showAlert('error', 'Error al guardar');
    }
}

// ============================================================
// CLIENTES
// ============================================================
async function loadClientes() {
    const container = document.getElementById('clientes-tbody');
    container.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Cargando...</td></tr>';
    try {
        const res = await fetch(`${API_BASE_URL}/clientes`);
        const data = await res.json();
        if (data.success && data.data) {
            _allClientes = data.data;
            renderClientes(data.data);
        }
    } catch (e) {
        container.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Error de conexión</td></tr>';
    }
}

function renderClientes(clientes) {
    const tbody = document.getElementById('clientes-tbody');
    tbody.innerHTML = '';

    if (!clientes || clientes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">No hay clientes registrados</td></tr>';
        return;
    }

    clientes.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${c.nombreCompleto || c.nombre + ' ' + (c.apellido || '')}</td>
            <td>${c.dni || '-'}</td>
            <td>${c.email || '-'}</td>
            <td>${c.telefono || '-'}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="editarCliente(${c.idCliente})">✏️</button>
                <button class="btn btn-sm btn-danger" onclick="eliminarCliente(${c.idCliente})">🗑️</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function abrirFormularioCliente() {
    document.getElementById('clienteId').value = '';
    document.getElementById('clienteForm').reset();
    document.getElementById('clienteModalTitle').textContent = 'Nuevo Cliente';
    const modal = getModal('clienteModal');
    modal.show();
}

async function editarCliente(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/clientes/${id}`);
        const data = await res.json();
        if (!data.success || !data.data) {
            showAlert('error', 'No se pudo cargar el cliente');
            return;
        }
        const c = data.data;
        document.getElementById('clienteId').value = c.idCliente;
        document.getElementById('clienteNombre').value = c.nombre || '';
        document.getElementById('clienteApellido').value = c.apellido || '';
        document.getElementById('clienteDni').value = c.dni || '';
        document.getElementById('clienteEmail').value = c.email || '';
        document.getElementById('clienteTelefono').value = c.telefono || '';
        document.getElementById('clienteModalTitle').textContent = 'Editar Cliente';
        getModal('clienteModal').show();
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function eliminarCliente(id) {
    showConfirm({
        message: '¿Eliminar este cliente? Se desactivará de la plataforma.',
        icon: '👤',
        title: 'Eliminar Cliente',
        btnLabel: 'Sí, eliminar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/clientes/${id}`, { method: 'DELETE' });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Cliente eliminado');
                    loadClientes();
                } else {
                    showAlert('error', data.message || 'Error');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

async function guardarCliente() {
    const id = document.getElementById('clienteId').value;
    const nombre = document.getElementById('clienteNombre').value;
    const apellido = document.getElementById('clienteApellido').value;
    const dni = document.getElementById('clienteDni').value;
    const email = document.getElementById('clienteEmail').value;
    const telefono = document.getElementById('clienteTelefono').value;

    try {
        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/clientes/${id}` : `${API_BASE_URL}/clientes`;
        
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, apellido, dni, email, telefono })
        });

        const data = await res.json();
        if (data.success) {
            showAlert('success', id ? 'Cliente actualizado' : 'Cliente creado');
            const modal = getModal('clienteModal');
            modal.hide();
            loadClientes();
        } else {
            showAlert('error', data.message || 'Error');
        }
    } catch (e) {
        showAlert('error', 'Error al guardar');
    }
}

// ============================================================
// HORARIOS BLOQUEADOS
// ============================================================
async function loadHorariosBloqueados() {
    const container = document.getElementById('horarios-list');
    container.innerHTML = '<div class="col-12"><p class="text-muted">Cargando horarios...</p></div>';
    try {
        const res = await fetch(`${API_BASE_URL}/horarios`);
        const data = await res.json();
        if (data.success && data.data) {
            _allHorarios = data.data;
            renderHorariosBloqueados(data.data);
        } else {
            container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay horarios bloqueados</div></div>';
        }
    } catch (e) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-warning">No hay horarios bloqueados</div></div>';
    }
}

function renderHorariosBloqueados(horarios) {
    const container = document.getElementById('horarios-list');
    container.innerHTML = '';

    if (!horarios || horarios.length === 0) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay horarios bloqueados registrados</div></div>';
        return;
    }

    horarios.forEach(h => {
        const inicio = h.fechaInicio ? new Date(h.fechaInicio).toLocaleString('es-PE') : '-';
        const fin = h.fechaFin ? new Date(h.fechaFin).toLocaleString('es-PE') : '-';
        
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';
        col.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">Bloqueo #${h.idHorarioBloqueado}</h5>

                    <p class="card-text mb-1"><strong>Espacio:</strong> ${h.nombreEspacio || h.idEspacio}</p>
                    <p class="card-text mb-1"><strong>Desde:</strong> ${inicio}</p>
                    <p class="card-text mb-1"><strong>Hasta:</strong> ${fin}</p>
                    <p class="card-text mb-2"><strong>Razón:</strong> ${h.razon || 'Sin especificar'}</p>
                </div>
                <div class="card-footer bg-white border-top">
                    <button class="btn btn-sm btn-danger" onclick="desbloquearHorario(${h.idHorarioBloqueado})">🔓 Desbloquear</button>
                </div>
            </div>
        `;
        container.appendChild(col);
    });
}

async function abrirBloqueoDialog() {
    // Load espacios into select
    const select = document.getElementById('bloqueoIdEspacio');
    select.innerHTML = '<option value="">-- Seleccione un espacio --</option>';
    try {
        const res = await fetch(`${API_BASE_URL}/espacios`);
        const data = await res.json();
        if (data.success && data.data) {
            data.data.forEach(e => {
                const opt = document.createElement('option');
                opt.value = e.idEspacio;
                opt.textContent = `${e.nombre} (${e.tipo})`;
                select.appendChild(opt);
            });
        }
    } catch (err) { /* ignore, select stays empty */ }

    document.getElementById('bloqueoForm').reset();
    getModal('bloqueoModal').show();
}

async function guardarBloqueo() {
    const idEspacio = document.getElementById('bloqueoIdEspacio').value;
    const fechaInicioRaw = document.getElementById('bloqueoFechaInicio').value;
    const fechaFinRaw = document.getElementById('bloqueoFechaFin').value;
    const razon = document.getElementById('bloqueoRazon').value;

    if (!idEspacio || !fechaInicioRaw || !fechaFinRaw || !razon) {
        showAlert('error', 'Complete todos los campos');
        return;
    }

    // Convert datetime-local "2025-03-15T10:00" → "2025-03-15 10:00"
    const fechaInicio = fechaInicioRaw.replace('T', ' ');
    const fechaFin = fechaFinRaw.replace('T', ' ');

    try {
        const res = await fetch(`${API_BASE_URL}/horarios`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idEspacio: parseInt(idEspacio), fechaInicio, fechaFin, razon })
        });
        const data = await res.json();
        if (data.success) {
            showAlert('success', 'Horario bloqueado correctamente');
            getModal('bloqueoModal').hide();
            loadHorariosBloqueados();
        } else {
            showAlert('error', data.message || 'No se pudo bloquear');
        }
    } catch (err) {
        showAlert('error', 'Error de conexión');
    }
}

async function desbloquearHorario(id) {
    showConfirm({
        message: '¿Desbloquear este horario? El espacio volverá a estar disponible.',
        icon: '🔓',
        title: 'Desbloquear Horario',
        btnLabel: 'Sí, desbloquear',
        btnClass: 'btn-warning',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/horarios/${id}`, { method: 'DELETE' });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Horario desbloqueado');
                    loadHorariosBloqueados();
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

// ============================================================
// REPORTES
// ============================================================
function exportarReservasCSV() {
    exportarDatos(`${API_BASE_URL}/reservas`, 'reservas', ['idReserva', 'estado', 'montoTotal', 'fechaInicio', 'fechaFin']);
}

function exportarEspaciosCSV() {
    exportarDatos(`${API_BASE_URL}/espacios`, 'espacios', ['idEspacio', 'nombre', 'tipo', 'capacidad', 'precioPorHora', 'estado']);
}

function exportarClientesCSV() {
    exportarDatos(`${API_BASE_URL}/clientes`, 'clientes', ['idCliente', 'nombreCompleto', 'dni', 'email', 'telefono']);
}

async function exportarDatos(url, nombreArchivo, columnas) {
    try {
        const res = await fetch(url);
        const data = await res.json();
        
        if (!data.success || !data.data) {
            showAlert('error', 'Error al obtener datos');
            return;
        }

        let csv = columnas.join(',') + '\n';
        data.data.forEach(row => {
            let fila = columnas.map(col => {
                let valor = row[col] || '';
                if (typeof valor === 'string') {
                    valor = '"' + valor.replace(/"/g, '""') + '"';
                }
                return valor;
            }).join(',');
            csv += fila + '\n';
        });

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url2 = URL.createObjectURL(blob);
        link.href = url2;
        link.download = nombreArchivo + '_' + new Date().toISOString().split('T')[0] + '.csv';
        link.click();
        
        showAlert('success', 'Archivo descargado');
    } catch (e) {
        showAlert('error', 'Error al generar CSV');
    }
}

// ============================================================
// UTILIDADES
// ============================================================
function showAlert(type, message) {
    // Crear alerta dinámica
    const alertHtml = `
        <div class="alert alert-${type === 'error' ? 'danger' : type === 'success' ? 'success' : 'info'} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    // Crear contenedor si no existe
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        alertContainer.style.position = 'fixed';
        alertContainer.style.top = '20px';
        alertContainer.style.right = '20px';
        alertContainer.style.zIndex = '9999';
        alertContainer.style.maxWidth = '400px';
        document.body.appendChild(alertContainer);
    }
    
    const alertDiv = document.createElement('div');
    alertDiv.innerHTML = alertHtml;
    alertContainer.appendChild(alertDiv.firstElementChild);
    
    // Auto-remove después de 4 segundos
    setTimeout(() => {
        const alerts = alertContainer.querySelectorAll('.alert');
        if (alerts.length > 0) {
            alerts[0].remove();
        }
    }, 4000);
}

// ============================================================
// FILTROS
// ============================================================
let _allReservas = [];
let _allEspacios = [];
let _allClientes = [];
let _allHorarios = [];

function filtrarReservas() {
    const filtro = document.getElementById('filtroReservas').value.toLowerCase();
    const filtrados = _allReservas.filter(r => {
        const cliente = r.nombreCliente || (r.cliente ? (r.cliente.nombreCompleto || r.cliente.nombre || '') : '');
        const espacio = r.nombreEspacio || (r.espacio ? r.espacio.nombre : '');
        const texto = (cliente + ' ' + espacio).toLowerCase();
        return texto.includes(filtro);
    });
    renderReservas(filtrados);
}

function filtrarEspacios() {
    const filtro = document.getElementById('filtroEspacios').value.toLowerCase();
    const filtrados = _allEspacios.filter(e => {
        const texto = (e.nombre + ' ' + e.tipo).toLowerCase();
        return texto.includes(filtro);
    });
    renderEspacios(filtrados);
}

function filtrarClientes() {
    const filtro = document.getElementById('filtroClientes').value.toLowerCase();
    const filtrados = _allClientes.filter(c => {
        const nombre = c.nombreCompleto || c.nombre + ' ' + (c.apellido || '');
        const texto = (nombre + ' ' + (c.dni || '') + ' ' + (c.email || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderClientes(filtrados);
}

function filtrarHorarios() {
    const filtro = document.getElementById('filtroHorarios').value.toLowerCase();
    const filtrados = _allHorarios.filter(h => {
        const espacio = h.nombreEspacio || '';
        const razon = h.razon || '';
        const texto = (espacio + ' ' + razon).toLowerCase();
        return texto.includes(filtro);
    });
    renderHorariosBloqueados(filtrados);
}

// ============================================================
// VALIDACIÓN DE HORARIOS BLOQUEADOS
// ============================================================
async function validarDisponibilidadEspacio(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/horarios`);
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const h of data.data) {
            if (h.idEspacio === parseInt(idEspacio)) {
                const hInicio = new Date(h.fechaInicio).getTime();
                const hFin = new Date(h.fechaFin).getTime();
                
                // Verificar solapamiento
                if (!(fin <= hInicio || inicio >= hFin)) {
                    return false; // Hay conflicto
                }
            }
        }
        return true; // Sin conflictos
    } catch (e) {
        console.error('Error validando disponibilidad:', e);
        return true; // Si hay error, permitir continuar
    }
}

async function validarSinReservaExistente(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`);
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const r of data.data) {
            if (r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA') {
                const rInicio = new Date(r.fechaInicio).getTime();
                const rFin = new Date(r.fechaFin).getTime();
                if (!(fin <= rInicio || inicio >= rFin)) {
                    return false; // Hay conflicto con reserva existente
                }
            }
        }
        return true;
    } catch (e) {
        console.error('Error validando reservas existentes:', e);
        return true;
    }
}

// ============================================================
// CRUD DE PAGOS
// ============================================================
let _allPagos = [];

async function cargarPagos() {
    try {
        const res = await fetch(`${API_BASE_URL}/pagos`);
        const data = await res.json();
        _allPagos = data.success ? (data.data || []) : [];
        renderPagos(_allPagos);
    } catch (e) {
        console.error('Error cargando pagos:', e);
        _allPagos = [];
    }
}

function renderPagos(pagos) {
    const tbody = document.getElementById('pagos-tbody');
    if (!tbody) return;
    if (!pagos || pagos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-3">No hay pagos registrados</td></tr>';
        return;
    }
    tbody.innerHTML = pagos.map(p => {
        const estadoBadge = p.estadoPago === 'COMPLETADO'
            ? 'success' : p.estadoPago === 'RECHAZADO' ? 'danger' : 'warning';
        const metodoBadge = p.estadoPago === 'COMPLETADO' ? 'success' : 'secondary';
        const fechaCreacionStr = p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleDateString('es-PE') : '-';
        const fechaPagoStr = p.fechaPago ? new Date(p.fechaPago).toLocaleDateString('es-PE') : '-';
        const pagarBtn = p.estadoPago !== 'COMPLETADO'
            ? `<button class="btn btn-sm btn-success" onclick="procesarPago(${p.idPago}, ${p.monto}, '${(p.nombreCliente||'').replace(/'/g,'')}', '${(p.emailCliente||'').replace(/'/g,'')}')">💳 Pagar</button>`
            : `<span class="text-success fw-bold">✅ Pagado</span>`;
        return `
        <tr>
            <td><strong>#${p.idPago}</strong></td>
            <td><span class="badge bg-primary">R#${p.idReserva}</span></td>
            <td>${p.nombreCliente || '-'}</td>
            <td class="fw-bold text-success">S/. ${(p.monto || 0).toFixed(2)}</td>
            <td><span class="badge bg-${metodoBadge}">${p.metodoPago || 'Pendiente'}</span></td>
            <td><span class="badge bg-${estadoBadge}">${p.estadoPago || 'PENDIENTE'}</span></td>
            <td>${fechaCreacionStr}</td>
            <td>${fechaPagoStr}</td>
            <td>${pagarBtn}</td>
        </tr>`;
    }).join('');
}

async function procesarPago(idPago, monto, nombreCliente, emailCliente) {
    await seleccionarMetodoPago(idPago, monto, nombreCliente, emailCliente);
}

function seleccionarMetodoPago(idPago, monto, nombreCliente, emailCliente) {
    return new Promise((resolve) => {
        // Eliminar instancia previa del modal para evitar conflictos con Bootstrap
        const viejo = document.getElementById('pagoMetodoModal');
        if (viejo) {
            const instancia = bootstrap.Modal.getInstance(viejo);
            if (instancia) instancia.dispose();
            viejo.remove();
        }

        let montoFinal = typeof monto === 'number' ? monto : parseFloat(monto);
        let idDescuentoAplicado = null;

        const modalEl = document.createElement('div');
        modalEl.id = 'pagoMetodoModal';
        modalEl.className = 'modal fade';
        modalEl.tabIndex = -1;
        modalEl.innerHTML = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header bg-success text-white">
                        <h5 class="modal-title">💳 Procesar Pago</h5>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3 p-3 bg-light rounded">
                            <p class="mb-1"><strong>👤 Cliente:</strong> <span id="pm-cliente"></span></p>
                            <p class="mb-1"><strong>📧 Email:</strong> <span id="pm-email"></span></p>
                            <p class="mb-0 text-success fw-bold"><strong>💰 Monto:</strong> S/. <span id="pm-monto"></span></p>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">🎁 Código de Descuento <span class="text-muted fw-normal">(opcional)</span></label>
                            <div class="input-group">
                                <input type="text" class="form-control text-uppercase" id="pm-codigo-descuento" placeholder="Ej: PROMO25">
                                <button class="btn btn-outline-success" type="button" id="pm-validar-codigo">Validar</button>
                            </div>
                            <div id="pm-descuento-msg" class="mt-1 small"></div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-bold">Método de Pago</label>
                            <select class="form-select" id="pm-metodo">
                                <option value="EFECTIVO">💵 Efectivo</option>
                                <option value="TARJETA">💳 Tarjeta de Crédito/Débito</option>
                                <option value="TRANSFERENCIA">🏦 Transferencia Bancaria</option>
                                <option value="YAPE">📱 Yape / Plin</option>
                            </select>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" id="pm-cancelar">Cancelar</button>
                        <button type="button" class="btn btn-success" id="pm-confirmar">✅ Confirmar Pago</button>
                    </div>
                </div>
            </div>`;
        document.body.appendChild(modalEl);

        document.getElementById('pm-cliente').textContent = nombreCliente;
        document.getElementById('pm-email').textContent   = emailCliente || 'Sin email';
        document.getElementById('pm-monto').textContent   = montoFinal.toFixed(2);

        const modal = new bootstrap.Modal(modalEl);
        modal.show();

        // Validar código de descuento
        document.getElementById('pm-validar-codigo').onclick = async () => {
            const codigo = document.getElementById('pm-codigo-descuento').value.trim().toUpperCase();
            const msgEl = document.getElementById('pm-descuento-msg');
            if (!codigo) { msgEl.innerHTML = '<span class="text-danger">Ingrese un código</span>'; return; }
            try {
                const res = await fetch(`${API_BASE_URL}/descuentos/validar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ codigo, monto: montoFinal })
                });
                const data = await res.json();
                if (data.success) {
                    const descuento = (montoFinal * data.porcentaje) / 100;
                    montoFinal = montoFinal - descuento;
                    idDescuentoAplicado = data.idDescuento;
                    document.getElementById('pm-monto').textContent = montoFinal.toFixed(2);
                    msgEl.innerHTML = `<span class="text-success fw-bold">✅ Descuento aplicado: ${data.porcentaje}% — ${data.descripcion} (−S/. ${descuento.toFixed(2)})</span>`;
                    document.getElementById('pm-validar-codigo').disabled = true;
                    document.getElementById('pm-codigo-descuento').disabled = true;
                } else {
                    idDescuentoAplicado = null;
                    msgEl.innerHTML = `<span class="text-danger">❌ ${data.error || 'Código inválido'}</span>`;
                }
            } catch (e) {
                msgEl.innerHTML = '<span class="text-danger">Error de conexión</span>';
            }
        };

        document.getElementById('pm-cancelar').onclick = () => { modal.hide(); resolve(null); };

        document.getElementById('pm-confirmar').onclick = async () => {
            const metodo = document.getElementById('pm-metodo').value;
            modal.hide();
            try {
                const res = await fetch(`${API_BASE_URL}/pagos/${idPago}/pagar`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ metodoPago: metodo, montoFinal, idDescuento: idDescuentoAplicado })
                });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', `✅ Pago procesado por ${metodo}. Se envió confirmación a ${emailCliente || 'cliente'}.`);
                    await cargarPagos();
                    if (_allReservas.length > 0) await loadReservas();
                } else {
                    showAlert('danger', '❌ Error: ' + (data.error || 'No se pudo procesar el pago'));
                }
            } catch (e) {
                showAlert('danger', '❌ Error de conexión al procesar el pago');
            }
            resolve(metodo);
        };
    });
}

function filtrarPagos() {
    const filtro = document.getElementById('filtroPagos').value.toLowerCase();
    // Filtrar solo pagos PENDIENTES que coincidan con el término de búsqueda
    const filtrados = _allPagos.filter(p => {
        if (p.estadoPago === 'COMPLETADO') return false;
        const texto = ((p.idReserva || '') + ' ' + (p.monto || '') + ' ' + (p.nombreCliente || '') + ' ' + (p.estadoPago || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderPagos(filtrados);
}

/* DESHABILITADO: Los pagos se crean automáticamente cuando se crea una reserva
function abrirFormularioPago() {
    const form = `
        <div class="card">
            <div class="card-header bg-warning">Registrar Nuevo Pago</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label>ID Reserva</label>
                        <input type="number" class="form-control" id="pago-reserva" min="1" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label>Monto (S/.)</label>
                        <input type="number" class="form-control" id="pago-monto" min="0.01" step="0.01" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label>Método de Pago</label>
                    <select class="form-select" id="pago-metodo" required>
                        <option value="">Seleccionar...</option>
                        <option value="TARJETA">Tarjeta de Crédito</option>
                        <option value="TRANSFERENCIA">Transferencia Bancaria</option>
                        <option value="EFECTIVO">Efectivo</option>
                    </select>
                </div>
                <div class="d-flex gap-2">
                    <button class="btn btn-primary" onclick="guardarPago()">💾 Guardar</button>
                    <button class="btn btn-secondary" onclick="cerrarFormulario()">Cancelar</button>
                </div>
            </div>
        </div>
    `;
    mostrarModal('Nuevo Pago', form);
}
*/

/* DESHABILITADO: Los pagos se crean automáticamente
async function guardarPago() {
    const idReserva = document.getElementById('pago-reserva').value;
    const monto = document.getElementById('pago-monto').value;
    const metodo = document.getElementById('pago-metodo').value;
    
    if (!idReserva || !monto || !metodo) {
        alert('Complete todos los campos');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/pagos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idReserva: parseInt(idReserva), monto: parseFloat(monto), metodoPago: metodo })
        });
        const data = await res.json();
        if (data.success) {
            alert('✓ Pago registrado exitosamente');
            cerrarFormulario();
            cargarPagos();
        } else {
            alert('Error: ' + (data.error || 'No se pudo registrar el pago'));
        }
    } catch (e) {
        alert('Error de conexión: ' + e.message);
    }
}
*/

function editarPago(id) {
    alert('Edición en desarrollo');
}

function eliminarPago(id) {
    if (confirm('¿Eliminar este pago?')) {
        alert('Eliminación en desarrollo');
    }
}

// ============================================================
// CRUD DE DESCUENTOS
// ============================================================
let _allDescuentos = [];

async function cargarDescuentos() {
    try {
        const res = await fetch(`${API_BASE_URL}/descuentos`);
        const data = await res.json();
        _allDescuentos = data.success ? (data.data || []) : [];
        renderDescuentos(_allDescuentos);
    } catch (e) {
        console.error('Error cargando descuentos:', e);
        _allDescuentos = [];
    }
}

function renderDescuentos(descuentos) {
    const tbody = document.getElementById('descuentos-tbody') || {};
    if (!tbody) return;
    if (!descuentos || descuentos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-3">No hay descuentos registrados</td></tr>';
        return;
    }
    tbody.innerHTML = descuentos.map(d => {
        const fIni = d.fechaInicio ? d.fechaInicio.substring(0,10) : '-';
        const fFin = d.fechaFin ? d.fechaFin.substring(0,10) : '-';
        return `
        <tr>
            <td><strong>${d.codigo}</strong></td>
            <td>${d.descripcion || '-'}</td>
            <td><span class="badge bg-success">${d.porcentaje}%</span></td>
            <td>S/. ${(d.montoMinimo || 0).toFixed(2)}</td>
            <td><small>${fIni} → ${fFin}</small></td>
            <td>${d.usosActuales}/${d.usosMaximos || '∞'}</td>
            <td><span class="badge bg-${d.estado === 'ACTIVO' ? 'success' : 'secondary'}">${d.estado}</span></td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editarDescuento(${d.idDescuento})">✏️</button>
                <button class="btn btn-sm btn-danger" onclick="eliminarDescuento(${d.idDescuento})">🗑️</button>
            </td>
        </tr>
        `;
    }).join('');
}

function filtrarDescuentos() {
    const filtro = document.getElementById('filtroDescuentos').value.toLowerCase();
    const filtrados = _allDescuentos.filter(d => {
        const texto = (d.codigo + ' ' + d.descripcion).toLowerCase();
        return texto.includes(filtro);
    });
    renderDescuentos(filtrados);
}

function abrirFormularioDescuento() {
    _abrirDescuentoModal(null);
}

async function editarDescuento(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/descuentos/${id}`);
        const data = await res.json();
        if (!data.success || !data.data) { showAlert('error', 'No se pudo cargar el descuento'); return; }
        _abrirDescuentoModal(data.data);
    } catch (e) {
        showAlert('error', 'Error de conexión');
    }
}

function _abrirDescuentoModal(d) {
    const esEdicion = d !== null && d !== undefined;
    const today = new Date().toISOString().split('T')[0];
    const futuro = new Date(Date.now() + 30*24*60*60*1000).toISOString().split('T')[0];

    // Eliminar instancia previa
    const viejo = document.getElementById('descuentoModal');
    if (viejo) {
        const inst = bootstrap.Modal.getInstance(viejo);
        if (inst) inst.dispose();
        viejo.remove();
    }

    const estadoHtml = esEdicion
        ? `<div class="mb-3">
                <label class="form-label">Estado</label>
                <select class="form-select" id="desc-estado">
                    <option value="ACTIVO" ${d.estado === 'ACTIVO' ? 'selected' : ''}>ACTIVO</option>
                    <option value="INACTIVO" ${d.estado === 'INACTIVO' ? 'selected' : ''}>INACTIVO</option>
                </select>
           </div>`
        : '';

    const modalEl = document.createElement('div');
    modalEl.id = 'descuentoModal';
    modalEl.className = 'modal fade';
    modalEl.tabIndex = -1;
    modalEl.innerHTML = `
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title">${esEdicion ? '✏️ Editar' : '➕ Nuevo'} Código de Descuento</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" id="desc-id" value="${esEdicion ? d.idDescuento : ''}">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Código *</label>
                            <input type="text" class="form-control text-uppercase" id="desc-codigo"
                                placeholder="Ej: PROMO25" value="${esEdicion ? (d.codigo || '') : ''}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Porcentaje (%) *</label>
                            <input type="number" class="form-control" id="desc-porcentaje"
                                min="1" max="100" value="${esEdicion ? d.porcentaje : ''}">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-bold">Descripción *</label>
                            <input type="text" class="form-control" id="desc-descripcion"
                                value="${esEdicion ? (d.descripcion || '') : ''}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Monto Mínimo (S/.)</label>
                            <input type="number" class="form-control" id="desc-monto"
                                min="0" step="0.01" value="${esEdicion ? d.montoMinimo : '0'}">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Fecha Inicio *</label>
                            <input type="date" class="form-control" id="desc-fecha-inicio"
                                value="${esEdicion ? (d.fechaInicio || today) : today}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Fecha Fin *</label>
                            <input type="date" class="form-control" id="desc-fecha-fin"
                                value="${esEdicion ? (d.fechaFin || futuro) : futuro}">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label fw-bold">Usos Máximos *</label>
                            <input type="number" class="form-control" id="desc-usos"
                                min="1" value="${esEdicion ? d.usosMaximos : '100'}">
                        </div>
                    </div>
                    ${estadoHtml}
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-success" id="desc-btn-guardar">💾 Guardar</button>
                </div>
            </div>
        </div>`;
    document.body.appendChild(modalEl);
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    document.getElementById('desc-btn-guardar').onclick = async () => {
        const id = document.getElementById('desc-id').value;
        const codigo = (document.getElementById('desc-codigo').value || '').toUpperCase().trim();
        const porcentaje = document.getElementById('desc-porcentaje').value;
        const descripcion = (document.getElementById('desc-descripcion').value || '').trim();
        const monto = document.getElementById('desc-monto').value || 0;
        const fechaInicio = document.getElementById('desc-fecha-inicio').value;
        const fechaFin = document.getElementById('desc-fecha-fin').value;
        const usosMaximos = document.getElementById('desc-usos').value || 100;
        const estadoEl = document.getElementById('desc-estado');
        const estado = estadoEl ? estadoEl.value : 'ACTIVO';

        if (!codigo || !porcentaje || !descripcion || !fechaInicio || !fechaFin) {
            showAlert('error', 'Complete todos los campos obligatorios'); return;
        }
        if (parseFloat(porcentaje) <= 0 || parseFloat(porcentaje) > 100) {
            showAlert('error', 'El porcentaje debe estar entre 1 y 100'); return;
        }
        if (fechaFin < fechaInicio) {
            showAlert('error', 'La fecha fin debe ser mayor a la fecha inicio'); return;
        }

        try {
            const method = id ? 'PUT' : 'POST';
            const url = id ? `${API_BASE_URL}/descuentos/${id}` : `${API_BASE_URL}/descuentos`;
            const res = await fetch(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    codigo, descripcion,
                    porcentaje: parseFloat(porcentaje),
                    montoMinimo: parseFloat(monto),
                    fechaInicio, fechaFin,
                    usosMaximos: parseInt(usosMaximos),
                    estado
                })
            });
            const data = await res.json();
            if (data.success) {
                showAlert('success', id ? '✅ Descuento actualizado' : '✅ Descuento creado');
                modal.hide();
                cargarDescuentos();
            } else {
                showAlert('error', data.error || 'No se pudo guardar');
            }
        } catch (e) {
            showAlert('error', 'Error de conexión: ' + e.message);
        }
    };
}

async function eliminarDescuento(id) {
    showConfirm({
        message: '¿Desactivar este código de descuento?',
        icon: '🎁',
        title: 'Desactivar Descuento',
        btnLabel: 'Sí, desactivar',
        btnClass: 'btn-danger',
        onConfirm: async () => {
            try {
                const res = await fetch(`${API_BASE_URL}/descuentos/${id}`, { method: 'DELETE' });
                const data = await res.json();
                if (data.success) {
                    showAlert('success', 'Descuento desactivado');
                    cargarDescuentos();
                } else {
                    showAlert('error', data.error || 'No se pudo desactivar');
                }
            } catch (e) {
                showAlert('error', 'Error de conexión');
            }
        }
    });
}

// ============================================================
// CRUD DE EVALUACIONES
// ============================================================
let _allEvaluaciones = [];

async function cargarEvaluaciones() {
    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones`);
        const data = await res.json();
        _allEvaluaciones = data.success ? (data.data || []) : [];
        renderEvaluaciones(_allEvaluaciones);
    } catch (e) {
        console.error('Error cargando evaluaciones:', e);
        _allEvaluaciones = [];
    }
}

function renderEvaluaciones(evaluaciones) {
    const tbody = document.getElementById('evaluaciones-tbody') || {};
    if (!tbody) return;
    tbody.innerHTML = evaluaciones.map(e => {
        const stars = '⭐'.repeat(e.calificacion) + '☆'.repeat(5 - e.calificacion);
        const nombreCliente = e.nombreCliente || '-';
        const emailCliente = e.emailCliente || '-';
        const comentario = e.comentario ? e.comentario.substring(0, 50) : '-';
        return `
        <tr>
            <td><strong>#${e.idReserva || '-'}</strong></td>
            <td>${nombreCliente}</td>
            <td>${emailCliente}</td>
            <td>${stars} (${e.calificacion}/5)</td>
            <td title="${e.comentario || ''}"><small>${comentario}${e.comentario && e.comentario.length > 50 ? '...' : ''}</small></td>
            <td><small>${new Date(e.fechaEvaluacion).toLocaleDateString('es-PE') || '-'}</small></td>
        </tr>
    `}).join('');
}

function filtrarEvaluaciones() {
    const filtro = document.getElementById('filtroEvaluaciones').value.toLowerCase();
    const filtrados = _allEvaluaciones.filter(e => {
        const texto = ((e.nombreCliente || '') + ' ' + (e.emailCliente || '')).toLowerCase();
        return texto.includes(filtro);
    });
    renderEvaluaciones(filtrados);
}

function abrirFormularioEvaluacion() {
    const form = `
        <div class="card">
            <div class="card-header bg-info text-white">Nueva Evaluación</div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label>ID Reserva</label>
                        <input type="number" class="form-control" id="eval-reserva" min="1" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label>ID Usuario</label>
                        <input type="number" class="form-control" id="eval-usuario" min="1" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label>Calificación (1-5 ⭐)</label>
                    <select class="form-select" id="eval-calificacion" required>
                        <option value="">Seleccionar...</option>
                        <option value="5">⭐⭐⭐⭐⭐ Excelente (5)</option>
                        <option value="4">⭐⭐⭐⭐☆ Muy Bueno (4)</option>
                        <option value="3">⭐⭐⭐☆☆ Bueno (3)</option>
                        <option value="2">⭐⭐☆☆☆ Regular (2)</option>
                        <option value="1">⭐☆☆☆☆ Malo (1)</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label>Comentario</label>
                    <textarea class="form-control" id="eval-comentario" rows="3" placeholder="Comparte tu experiencia..."></textarea>
                </div>
                <div class="d-flex gap-2">
                    <button class="btn btn-info text-white" onclick="guardarEvaluacion()">💾 Guardar</button>
                    <button class="btn btn-secondary" onclick="cerrarFormulario()">Cancelar</button>
                </div>
            </div>
        </div>
    `;
    mostrarModal('Nueva Evaluación', form);
}

async function guardarEvaluacion() {
    const idReserva = document.getElementById('eval-reserva').value;
    const idUsuario = document.getElementById('eval-usuario').value;
    const calificacion = document.getElementById('eval-calificacion').value;
    const comentario = document.getElementById('eval-comentario').value;
    
    if (!idReserva || !idUsuario || !calificacion) {
        alert('Complete los campos obligatorios');
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idReserva: parseInt(idReserva), idUsuario: parseInt(idUsuario), calificacion: parseInt(calificacion), comentario })
        });
        const data = await res.json();
        if (data.success) {
            alert('✓ Evaluación registrada exitosamente');
            cerrarFormulario();
            cargarEvaluaciones();
        } else {
            alert('Error: ' + (data.error || 'No se pudo guardar'));
        }
    } catch(e) {
        alert('Error de conexión');
    }
}

// ============================================================
// CALENDARIO DISPONIBILIDAD SEMANAL EN MODAL RESERVA
// ============================================================
let _calSemanaOffset = 0;

let _calSlotInicio = null; // Para tracking de slot seleccionado

async function selectSlotReserva(slotIniISO, slotFinISO) {
    const slotIni = new Date(slotIniISO);
    const slotFin = new Date(slotFinISO);
    
    const fIniEl = document.getElementById('reservaFechaInicio');
    const fFinEl = document.getElementById('reservaFechaFin');
    
    const fIniVal = fIniEl.value ? new Date(fIniEl.value) : null;
    
    if (!fIniVal) {
        // Si no hay inicio, este slot es el inicio
        fIniEl.value = slotIni.toISOString().slice(0, 16);
    } else if (slotIni.getTime() >= fIniVal.getTime()) {
        // Si hay inicio y este slot es posterior, es el fin
        fFinEl.value = slotFin.toISOString().slice(0, 16);
    } else {
        // Si este slot es anterior al inicio, intercambiar
        fFinEl.value = fIniEl.value;
        fIniEl.value = slotIni.toISOString().slice(0, 16);
    }
    
    calcularMonto();
    renderCalendarioDisponibilidad();
}

async function renderCalendarioDisponibilidad() {
    const idEspacio = document.getElementById('reservaIdEspacio').value;
    const container = document.getElementById('calendario-disponibilidad');
    if (!idEspacio) {
        container.innerHTML = '<div class="text-muted text-center py-5"><span style="font-size:2rem">📅</span><p class="mt-2 mb-0">Selecciona un espacio para ver<br>la disponibilidad semanal</p></div>';
        return;
    }

    // Calcular lunes de la semana actual + offset
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7) + _calSemanaOffset * 7);

    // Obtener reservas existentes
    let reservas = [];
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`);
        const data = await res.json();
        if (data.success) reservas = (data.data || []).filter(r => r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA');
    } catch(e) {}

    // Días de la semana (Lun-Dom)
    const dias = [];
    for (let i = 0; i < 7; i++) {
        const d = new Date(lunes);
        d.setDate(lunes.getDate() + i);
        dias.push(d);
    }

    const nombresDia = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];
    const hoyStr = hoy.toDateString();

    // Fechas seleccionadas en el form
    const fIniVal = document.getElementById('reservaFechaInicio').value;
    const fFinVal = document.getElementById('reservaFechaFin').value;
    const selIni = fIniVal ? new Date(fIniVal).getTime() : null;
    const selFin = fFinVal ? new Date(fFinVal).getTime() : null;

    // Rango de horas a mostrar (6:00 - 22:00)
    const HORA_INI = 6, HORA_FIN = 22;

    let html = `<div class="cal-header">
        <h6>📅 Disponibilidad Semanal</h6>
        <div class="cal-week-nav">
            <button class="btn btn-outline-secondary btn-sm" onclick="_calSemanaOffset--; renderCalendarioDisponibilidad()">‹</button>
            <button class="btn btn-outline-primary btn-sm" onclick="_calSemanaOffset=0; renderCalendarioDisponibilidad()">Hoy</button>
            <button class="btn btn-outline-secondary btn-sm" onclick="_calSemanaOffset++; renderCalendarioDisponibilidad()">›</button>
        </div>
    </div>
    <div class="cal-scroll">
    <table class="cal-table">
    <thead><tr><th class="cal-time-col"></th>`;

    dias.forEach((d, i) => {
        const esHoy = d.toDateString() === hoyStr;
        html += `<th class="${esHoy ? 'cal-today' : ''}">${nombresDia[i]}<br><span style="font-weight:400">${d.getDate()}/${d.getMonth()+1}</span></th>`;
    });
    html += '</tr></thead><tbody>';

    for (let h = HORA_INI; h < HORA_FIN; h++) {
        html += `<tr><td class="cal-time-col">${String(h).padStart(2,'0')}:00</td>`;
        dias.forEach(d => {
            const slotIni = new Date(d); slotIni.setHours(h, 0, 0, 0);
            const slotFin = new Date(d); slotFin.setHours(h + 1, 0, 0, 0);
            const siMs = slotIni.getTime(), sfMs = slotFin.getTime();

            // ¿Hay reserva que solapa este slot?
            const reserva = reservas.find(r => {
                const ri = new Date(r.fechaInicio).getTime();
                const rf = new Date(r.fechaFin).getTime();
                return ri < sfMs && rf > siMs;
            });

            // ¿Está dentro del rango seleccionado?
            const enSeleccion = selIni && selFin && selIni < sfMs && selFin > siMs;

            let cls, title;
            if (reserva) {
                cls = 'ocupado';
                title = `Ocupado: ${reserva.nombreCliente || 'Reservado'}`;
            } else if (enSeleccion) {
                cls = 'seleccionado';
                title = 'Tu selección';
            } else {
                cls = 'libre';
                title = 'Disponible';
            }
            const onclick = (reserva || !slotIni) ? '' : `onclick="selectSlotReserva('${slotIni.toISOString()}', '${slotFin.toISOString()}')"`;
            html += `<td><span class="cal-slot ${cls}" title="${title}" ${onclick} style="${!reserva ? 'cursor:pointer' : ''}"></span></td>`;
        });
        html += '</tr>';
    }

    html += `</tbody></table></div>
    <div class="cal-legend">
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#d4edda"></span> Libre</span>
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#f8d7da"></span> Ocupado</span>
        <span class="cal-legend-item"><span class="cal-legend-dot" style="background:#cce5ff"></span> Tu selección</span>
    </div>`;

    container.innerHTML = html;
}

async function validarSinReservaExistente(idEspacio, fechaInicio, fechaFin) {
    try {
        const res = await fetch(`${API_BASE_URL}/reservas`);
        const data = await res.json();
        if (!data.success || !data.data) return true;

        const inicio = new Date(fechaInicio).getTime();
        const fin = new Date(fechaFin).getTime();

        for (const r of data.data) {
            if (r.idEspacio === parseInt(idEspacio) && r.estado !== 'CANCELADA') {
                const rInicio = new Date(r.fechaInicio).getTime();
                const rFin = new Date(r.fechaFin).getTime();
                if (!(fin <= rInicio || inicio >= rFin)) {
                    return false;
                }
            }
        }
        return true;
    } catch (e) {
        console.error('Error validando reservas existentes:', e);
        return true;
    }
}



function editarEvaluacion(id) {
    alert('Edición en desarrollo');
}

function eliminarEvaluacion(id) {
    if (confirm('¿Eliminar esta evaluación?')) {
        alert('Eliminación en desarrollo');
    }
}

// ============================================================
// NOTIFICACIONES
// ============================================================
let _allNotificaciones = [];

async function cargarNotificaciones() {
    try {
        const res = await fetch(`${API_BASE_URL}/notificaciones`);
        const data = await res.json();
        _allNotificaciones = data.success ? (data.data || []) : [];
        renderNotificaciones(_allNotificaciones);
    } catch (e) {
        console.error('Error cargando notificaciones:', e);
        _allNotificaciones = [];
    }
}

function renderNotificaciones(notificaciones) {
    const container = document.getElementById('notificaciones-list');
    container.innerHTML = '';
    
    if (!notificaciones || notificaciones.length === 0) {
        container.innerHTML = '<div class="col-12"><p class="text-muted text-center">No hay notificaciones</p></div>';
        return;
    }

    notificaciones.forEach(n => {
        const col = document.createElement('div');
        col.className = 'col-md-6 col-lg-4';
        
        let iconoTipo = '📌';
        if (n.tipo === 'EVALUACION') iconoTipo = '⭐';
        else if (n.tipo === 'PAGO') iconoTipo = '💳';
        else if (n.tipo === 'RESERVA') iconoTipo = '📅';
        else if (n.tipo === 'RECORDATORIO') iconoTipo = '⏰';
        
        const botones = (n.tipo === 'EVALUACION' && !n.leida)
            ? `<button id="btn-eval-${n.idNotificacion}" class="btn btn-sm btn-success" onclick="enviarEvaluacion(${n.idNotificacion}, this)">📧 Enviar Evaluación</button>`
            : (n.tipo === 'EVALUACION' && n.leida)
            ? `<span class="badge bg-success fs-6">✅ Ya enviado</span>`
            : '';
        
        col.innerHTML = `
            <div class="card h-100 shadow-sm">
                <div class="card-header bg-${n.leida ? 'light' : 'primary text-white'}">
                    <h6 class="mb-0">${iconoTipo} ${n.tipo}</h6>
                </div>
                <div class="card-body">
                    <p class="card-title fw-bold">${n.asunto}</p>
                    <p class="card-text small">${n.mensaje}</p>
                    <small class="text-muted">${new Date(n.fechaCreacion).toLocaleDateString('es-PE')}</small>
                </div>
                ${botones ? `<div class="card-footer bg-white">${botones}</div>` : ''}
            </div>
        `;
        container.appendChild(col);
    });
}

function filtrarNotificaciones() {
    const filtro = document.getElementById('filtroNotificaciones').value;
    let filtrados = _allNotificaciones;
    if (filtro) {
        filtrados = _allNotificaciones.filter(n => n.tipo === filtro);
    }
    renderNotificaciones(filtrados);
}

async function enviarEvaluacion(idNotificacion, btn) {
    // Deshabilitar botón inmediatamente para evitar doble envío
    if (btn) { btn.disabled = true; btn.textContent = '⏳ Enviando...'; }
    try {
        const res = await fetch(`${API_BASE_URL}/evaluaciones/enviar/${idNotificacion}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const data = await res.json();
        if (data.success) {
            // Marcar como leída en BD
            await fetch(`${API_BASE_URL}/notificaciones/${idNotificacion}/leida`, { method: 'PUT' });
            // Actualizar dato local (sin recargar del servidor) para que el re-render muestre "Ya enviado"
            const notif = _allNotificaciones.find(n => n.idNotificacion === idNotificacion);
            if (notif) notif.leida = 1;
            // Re-renderizar usando datos locales ya actualizados
            filtrarNotificaciones();
        } else {
            alert('❌ Error: ' + (data.error || 'No se pudo enviar el email'));
            if (btn) { btn.disabled = false; btn.textContent = '📧 Enviar Evaluación'; }
        }
    } catch (e) {
        alert('❌ Error de conexión: ' + e.message);
        if (btn) { btn.disabled = false; btn.textContent = '📧 Enviar Evaluación'; }
    }
}

// ============================================================
// FUNCIONES AUXILIARES
// ============================================================
function mostrarModal(titulo, contenido) {
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.style.display = 'block';
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title">${titulo}</h5>
                    <button type="button" class="btn-close btn-close-white" onclick="cerrarFormulario()"></button>
                </div>
                <div class="modal-body">
                    ${contenido}
                </div>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
    modal.classList.add('show');
}

function cerrarFormulario() {
    const modales = document.querySelectorAll('.modal');
    modales.forEach(m => m.remove());
}

// Cargar datos al cambiar de sección
const _originalShowSection = window.showSection;
window.showSection = function(section) {
    _originalShowSection(section);
    if (section === 'pagos') cargarPagos();
    else if (section === 'descuentos') cargarDescuentos();
    else if (section === 'evaluaciones') cargarEvaluaciones();
};
