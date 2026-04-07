const express = require('express');
const { v4: uuidv4 } = require('uuid');
const db = require('../db');

const router = express.Router();

// Get all students for teacher
router.get('/', (req, res) => {
  const teacherId = req.user?.id;
  const students = db.prepare(
    `SELECT * FROM students WHERE teacher_id = ? OR parent_id = ? ORDER BY name`
  ).all(teacherId, teacherId);
  res.json(students);
});

// Add student
router.post('/', (req, res) => {
  const { name, age, parentId } = req.body;
  const id = uuidv4();
  const teacherId = req.user?.id;
  
  db.prepare(`INSERT INTO students (id, name, age, parent_id, teacher_id) VALUES (?, ?, ?, ?, ?)`)
    .run(id, name, age, parentId, teacherId);
  
  res.json({ id, name, age });
});

// Get student with records
router.get('/:id', (req, res) => {
  const { id } = req.params;
  const student = db.prepare(`SELECT * FROM students WHERE id = ?`).get(id);
  const health = db.prepare(`SELECT * FROM health_records WHERE student_id = ? ORDER BY created_at DESC`).all(id);
  const learning = db.prepare(`SELECT * FROM learning_records WHERE student_id = ? ORDER BY created_at DESC`).all(id);
  const reports = db.prepare(`SELECT * FROM ai_reports WHERE student_id = ? ORDER BY created_at DESC`).all(id);
  
  res.json({ ...student, health, learning, reports });
});

module.exports = router;
