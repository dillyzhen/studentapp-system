const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');

const router = express.Router();
const JWT_SECRET = process.env.JWT_SECRET || 'dev-secret-key';

// Register
router.post('/register', async (req, res) => {
  try {
    const { username, password, name, role } = req.body;
    const hash = await bcrypt.hash(password, 10);
    const id = uuidv4();
    
    db.prepare(`INSERT INTO users (id, username, password_hash, name, role) VALUES (?, ?, ?, ?, ?)`)
      .run(id, username, hash, name, role || 'teacher');
    
    res.json({ success: true, user: { id, username, name, role } });
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const user = db.prepare(`SELECT * FROM users WHERE username = ?`).get(username);
    
    if (!user || !(await bcrypt.compare(password, user.password_hash))) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }
    
    const token = jwt.sign({ id: user.id, role: user.role }, JWT_SECRET);
    res.json({ token, user: { id: user.id, username: user.username, name: user.name, role: user.role } });
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});

module.exports = router;
