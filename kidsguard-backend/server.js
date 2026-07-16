'use strict';

/*
 * Servidor de referencia de KidsGuard.
 *
 * Almacenamiento en un único fichero JSON (data.json) — pensado para uso
 * doméstico/familiar, no para producción a gran escala. Ofrece:
 *   - Emparejamiento de dispositivos con un código de familia.
 *   - Recepción de snapshots (uso + configuración) de cada dispositivo.
 *   - Cola de comandos remotos que el dispositivo recoge y aplica.
 *   - Un panel web sencillo para el adulto.
 *
 * Autenticación mínima: cada dispositivo recibe un token al emparejarse; el
 * panel de administración se protege con ADMIN_PASSWORD. Para exponerlo a
 * Internet, colócalo SIEMPRE detrás de HTTPS (proxy inverso).
 */

const express = require('express');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || 'cambia-esta-clave';
const FAMILY_CODE = (process.env.FAMILY_CODE || 'FAMILIA123').toUpperCase();
const DATA_FILE = path.join(__dirname, 'data.json');

const app = express();
app.use(express.json({ limit: '256kb' }));

// ---------- Persistencia ----------

function loadData() {
  try {
    return JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
  } catch (e) {
    return { devices: {} };
  }
}

function saveData(data) {
  fs.writeFileSync(DATA_FILE, JSON.stringify(data, null, 2));
}

let db = loadData();

function token() {
  return crypto.randomBytes(24).toString('hex');
}

function deviceFromAuth(req) {
  const header = req.headers['authorization'] || '';
  const t = header.replace(/^Bearer\s+/i, '');
  if (!t) return null;
  return Object.values(db.devices).find((d) => d.token === t) || null;
}

// ---------- API del dispositivo (app Android) ----------

// Emparejar: el niño introduce el código de familia.
app.post('/api/pair', (req, res) => {
  const { familyCode, deviceName } = req.body || {};
  if (!familyCode || String(familyCode).toUpperCase() !== FAMILY_CODE) {
    return res.status(403).json({ error: 'Código de familia incorrecto' });
  }
  const id = crypto.randomUUID();
  db.devices[id] = {
    id,
    name: deviceName || 'Dispositivo',
    token: token(),
    snapshot: null,
    commands: [],
    pairedAt: Date.now(),
  };
  saveData(db);
  res.json({ deviceId: id, token: db.devices[id].token });
});

// Recibir snapshot del dispositivo.
app.post('/api/devices/:id/snapshot', (req, res) => {
  const device = deviceFromAuth(req);
  if (!device || device.id !== req.params.id) {
    return res.status(401).json({ error: 'No autorizado' });
  }
  device.snapshot = req.body || {};
  device.lastSeen = Date.now();
  saveData(db);
  res.json({ ok: true });
});

// Entregar y vaciar la cola de comandos.
app.get('/api/devices/:id/commands', (req, res) => {
  const device = deviceFromAuth(req);
  if (!device || device.id !== req.params.id) {
    return res.status(401).json({ error: 'No autorizado' });
  }
  const commands = device.commands;
  device.commands = [];
  saveData(db);
  res.json({ commands });
});

// ---------- Panel de administración (adulto) ----------

function checkAdmin(req, res) {
  const pass = req.query.pass || req.headers['x-admin-password'];
  if (pass !== ADMIN_PASSWORD) {
    res.status(401).json({ error: 'Contraseña de administración incorrecta' });
    return false;
  }
  return true;
}

// Listar dispositivos con su último snapshot.
app.get('/admin/devices', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const devices = Object.values(db.devices).map((d) => ({
    id: d.id,
    name: d.name,
    lastSeen: d.lastSeen || null,
    pendingCommands: d.commands.length,
    snapshot: d.snapshot,
  }));
  res.json({ devices });
});

// Encolar un comando remoto para un dispositivo.
app.post('/admin/devices/:id/commands', (req, res) => {
  if (!checkAdmin(req, res)) return;
  const device = db.devices[req.params.id];
  if (!device) return res.status(404).json({ error: 'Dispositivo no encontrado' });
  const { type, payload } = req.body || {};
  if (!type) return res.status(400).json({ error: 'Falta el tipo de comando' });
  device.commands.push({ id: crypto.randomUUID(), type, payload: payload || {} });
  saveData(db);
  res.json({ ok: true });
});

// Panel web.
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});
app.use(express.static(path.join(__dirname, 'public')));

app.listen(PORT, () => {
  console.log(`KidsGuard backend escuchando en http://localhost:${PORT}`);
  console.log(`Código de familia: ${FAMILY_CODE}`);
});
