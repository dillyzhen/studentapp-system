-- SHLMS Database Schema
-- PostgreSQL / H2 Compatible

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(500),
    enabled BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    age INTEGER,
    birth_date DATE,
    gender VARCHAR(10),
    student_no VARCHAR(30),
    class_id VARCHAR(36),
    class_name VARCHAR(50),
    avatar_url VARCHAR(500),
    health_summary VARCHAR(2000),
    learning_summary VARCHAR(2000),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- User-Student Bindings (Parents)
CREATE TABLE IF NOT EXISTS user_student_bindings (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    relationship VARCHAR(20),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_student UNIQUE (user_id, student_id),
    CONSTRAINT fk_usb_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_usb_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Teacher-Student Assignments
CREATE TABLE IF NOT EXISTS teacher_student_assignments (
    id VARCHAR(36) PRIMARY KEY,
    teacher_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_student UNIQUE (teacher_id, student_id),
    CONSTRAINT fk_tsa_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT fk_tsa_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Raw Records (Parent Submissions)
CREATE TABLE IF NOT EXISTS raw_records (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    submitter_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    images VARCHAR(2000),
    digital_signature VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',
    ip_address VARCHAR(50),
    device_fingerprint VARCHAR(200),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rr_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_rr_submitter FOREIGN KEY (submitter_id) REFERENCES users(id)
);

-- AI Interpretations
CREATE TABLE IF NOT EXISTS ai_interpretations (
    id VARCHAR(36) PRIMARY KEY,
    raw_record_id VARCHAR(36) NOT NULL,
    session_id VARCHAR(100) NOT NULL,
    system_prompt VARCHAR(4000),
    user_prompt VARCHAR(4000),
    raw_response VARCHAR(8000),
    interpretation_content VARCHAR(4000),
    input_tokens INTEGER,
    output_tokens INTEGER,
    cost_usd DECIMAL(10, 6),
    model_version VARCHAR(50),
    trace_id VARCHAR(100),
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_raw_record FOREIGN KEY (raw_record_id) REFERENCES raw_records(id)
);

-- Diff Records (Teacher Edits)
CREATE TABLE IF NOT EXISTS diff_records (
    id VARCHAR(36) PRIMARY KEY,
    interpretation_id VARCHAR(36) NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    before_value VARCHAR(4000),
    after_value VARCHAR(4000),
    editor_id VARCHAR(36) NOT NULL,
    edit_reason VARCHAR(500),
    edited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dr_interpretation FOREIGN KEY (interpretation_id) REFERENCES ai_interpretations(id),
    CONSTRAINT fk_dr_editor FOREIGN KEY (editor_id) REFERENCES users(id)
);

-- Advice Reports
CREATE TABLE IF NOT EXISTS advice_reports (
    id VARCHAR(36) PRIMARY KEY,
    interpretation_id VARCHAR(36) NOT NULL,
    student_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    auditor_id VARCHAR(36),
    audited_at TIMESTAMP,
    audit_comment VARCHAR(500),
    distributed_at TIMESTAMP,
    first_viewed_at TIMESTAMP,
    view_count INTEGER DEFAULT 0,
    download_count INTEGER DEFAULT 0,
    pdf_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_ar_interpretation FOREIGN KEY (interpretation_id) REFERENCES ai_interpretations(id),
    CONSTRAINT fk_ar_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_ar_auditor FOREIGN KEY (auditor_id) REFERENCES users(id)
);

-- Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    action_type VARCHAR(30) NOT NULL,
    actor_id VARCHAR(36) NOT NULL,
    actor_role VARCHAR(20),
    actor_name VARCHAR(50),
    target_type VARCHAR(30),
    target_id VARCHAR(36),
    details VARCHAR(4000),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Timeline Events
CREATE TABLE IF NOT EXISTS timeline_events (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    event_title VARCHAR(200) NOT NULL,
    event_data VARCHAR(4000),
    source_type VARCHAR(30),
    source_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_te_student FOREIGN KEY (student_id) REFERENCES students(id)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE INDEX IF NOT EXISTS idx_students_class ON students(class_id);

CREATE INDEX IF NOT EXISTS idx_usb_user ON user_student_bindings(user_id);
CREATE INDEX IF NOT EXISTS idx_usb_student ON user_student_bindings(student_id);

CREATE INDEX IF NOT EXISTS idx_tsa_teacher ON teacher_student_assignments(teacher_id);
CREATE INDEX IF NOT EXISTS idx_tsa_student ON teacher_student_assignments(student_id);

CREATE INDEX IF NOT EXISTS idx_rr_student ON raw_records(student_id);
CREATE INDEX IF NOT EXISTS idx_rr_submitter ON raw_records(submitter_id);
CREATE INDEX IF NOT EXISTS idx_rr_status ON raw_records(status);

CREATE INDEX IF NOT EXISTS idx_ai_raw_record ON ai_interpretations(raw_record_id);
CREATE INDEX IF NOT EXISTS idx_ai_session ON ai_interpretations(session_id);

CREATE INDEX IF NOT EXISTS idx_dr_interpretation ON diff_records(interpretation_id);
CREATE INDEX IF NOT EXISTS idx_dr_editor ON diff_records(editor_id);

CREATE INDEX IF NOT EXISTS idx_ar_student ON advice_reports(student_id);
CREATE INDEX IF NOT EXISTS idx_ar_status ON advice_reports(status);
CREATE INDEX IF NOT EXISTS idx_ar_interpretation ON advice_reports(interpretation_id);

CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_target ON audit_logs(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_audit_time ON audit_logs(created_at);

CREATE INDEX IF NOT EXISTS idx_timeline_student ON timeline_events(student_id, created_at);
