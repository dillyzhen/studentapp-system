-- SHLMS Demo Data
-- 演示数据：管理员、老师、家长、学生及关联关系

-- 插入用户（密码使用 BCrypt 加密，原始密码见注释）
-- 密码说明：所有演示账号密码均为 'pass123'
-- BCrypt 加密结果：$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq

INSERT INTO users (id, username, password, name, role, phone, email, enabled, created_at)
VALUES
-- 管理员
('admin-001', 'admin', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '系统管理员', 'ADMIN', '13800138000', 'admin@shlms.edu', TRUE, CURRENT_TIMESTAMP),

-- 老师
('teacher-001', 'teacher1', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '张老师', 'TEACHER', '13800138001', 'teacher1@shlms.edu', TRUE, CURRENT_TIMESTAMP),
('teacher-002', 'teacher2', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '李老师', 'TEACHER', '13800138002', 'teacher2@shlms.edu', TRUE, CURRENT_TIMESTAMP),

-- 家长
('parent-001', 'parent1', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '王家长', 'PARENT', '13900139001', 'parent1@example.com', TRUE, CURRENT_TIMESTAMP),
('parent-002', 'parent2', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '李家长', 'PARENT', '13900139002', 'parent2@example.com', TRUE, CURRENT_TIMESTAMP),
('parent-003', 'parent3', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '张家长', 'PARENT', '13900139003', 'parent3@example.com', TRUE, CURRENT_TIMESTAMP),
('parent-004', 'parent4', '$2a$10$NfJQzGXuiLQmrt.QlwHJ/e.aQ8UcCLi6JZlcmXUn9AUMHIX33romq', '刘家长', 'PARENT', '13900139004', 'parent4@example.com', TRUE, CURRENT_TIMESTAMP);

-- 插入学生
INSERT INTO students (id, name, age, birth_date, gender, student_no, class_id, class_name, enabled, created_at)
VALUES
('student-001', '王小明', 10, '2016-03-15', 'MALE', '202401001', 'class-001', '三年级一班', TRUE, CURRENT_TIMESTAMP),
('student-002', '李小红', 9, '2017-06-20', 'FEMALE', '202401002', 'class-001', '三年级一班', TRUE, CURRENT_TIMESTAMP),
('student-003', '张小华', 10, '2016-01-10', 'MALE', '202401003', 'class-001', '三年级一班', TRUE, CURRENT_TIMESTAMP),
('student-004', '刘小美', 9, '2017-08-05', 'FEMALE', '202401004', 'class-002', '三年级二班', TRUE, CURRENT_TIMESTAMP),
('student-005', '陈小强', 11, '2015-11-25', 'MALE', '202401005', 'class-002', '三年级二班', TRUE, CURRENT_TIMESTAMP);

-- 家长-学生绑定关系
INSERT INTO user_student_bindings (id, user_id, student_id, relationship, is_primary, created_at)
VALUES
('usb-001', 'parent-001', 'student-001', '父亲', TRUE, CURRENT_TIMESTAMP),
('usb-002', 'parent-002', 'student-002', '母亲', TRUE, CURRENT_TIMESTAMP),
('usb-003', 'parent-003', 'student-003', '父亲', TRUE, CURRENT_TIMESTAMP),
('usb-004', 'parent-004', 'student-004', '母亲', TRUE, CURRENT_TIMESTAMP),
('usb-005', 'parent-001', 'student-005', '叔叔', FALSE, CURRENT_TIMESTAMP);

-- 老师-学生分配关系
INSERT INTO teacher_student_assignments (id, teacher_id, student_id, notes, created_at)
VALUES
('tsa-001', 'teacher-001', 'student-001', '重点关注对象', CURRENT_TIMESTAMP),
('tsa-002', 'teacher-001', 'student-002', '', CURRENT_TIMESTAMP),
('tsa-003', 'teacher-001', 'student-003', '', CURRENT_TIMESTAMP),
('tsa-004', 'teacher-002', 'student-004', '', CURRENT_TIMESTAMP),
('tsa-005', 'teacher-002', 'student-005', '体育特长生', CURRENT_TIMESTAMP);

-- 示例原始记录（家长提交）
INSERT INTO raw_records (id, student_id, submitter_id, type, content, status, ip_address, submitted_at)
VALUES
('rr-001', 'student-001', 'parent-001', 'HEALTH',
'本周体检情况：身高135cm，体重32kg，视力左眼5.0右眼4.9。最近晚上睡觉有点磨牙，食欲正常，精神状态良好。周末有参加足球训练，运动量适中。',
'COMPLETED', '192.168.1.100', CURRENT_TIMESTAMP - INTERVAL '3' DAY),

('rr-002', 'student-001', 'parent-001', 'LEARNING',
'本周数学学习了分数的加减法，孩子掌握得还不错，练习题正确率约85%。语文背诵了《春晓》和《静夜思》，流利程度有提升。英语单词记忆需要加强，建议多复习。',
'COMPLETED', '192.168.1.100', CURRENT_TIMESTAMP - INTERVAL '2' DAY),

('rr-003', 'student-002', 'parent-002', 'HEALTH',
'孩子这周感冒了，有轻微咳嗽和流鼻涕，体温最高37.5度。已经服用感冒药，目前症状好转。饮食偏清淡，多喝水。建议老师在学校多关注孩子身体状况。',
'PENDING', '192.168.1.101', CURRENT_TIMESTAMP - INTERVAL '1' DAY),

('rr-004', 'student-003', 'parent-003', 'BEHAVIOR',
'最近孩子在家表现有些叛逆，不愿意完成作业，经常玩手机游戏。希望老师能在学校多引导，我们也会加强家庭教育。感谢老师的耐心和付出！',
'PROCESSING', '192.168.1.102', CURRENT_TIMESTAMP - INTERVAL '1' DAY);

-- 示例档案时间线
INSERT INTO timeline_events (id, student_id, event_type, event_title, event_data, source_type, source_id, created_at)
VALUES
('te-001', 'student-001', 'STUDENT_CREATED', '学生档案创建', '{"operator": "admin-001"}', 'SYSTEM', 'admin-001', CURRENT_TIMESTAMP - INTERVAL '30' DAY),
('te-002', 'student-001', 'PARENT_BOUND', '家长绑定', '{"parent_id": "parent-001", "relationship": "父亲"}', 'USER', 'admin-001', CURRENT_TIMESTAMP - INTERVAL '30' DAY),
('te-003', 'student-001', 'TEACHER_ASSIGNED', '老师分配', '{"teacher_id": "teacher-001"}', 'USER', 'admin-001', CURRENT_TIMESTAMP - INTERVAL '30' DAY),
('te-004', 'student-001', 'SUBMISSION_RECEIVED', '收到健康记录', '{"record_type": "HEALTH", "submitter": "parent-001"}', 'RAW_RECORD', 'rr-001', CURRENT_TIMESTAMP - INTERVAL '3' DAY),
('te-005', 'student-001', 'REPORT_DISTRIBUTED', 'AI建议报告已分发', '{"report_type": "HEALTH_ADVICE"}', 'ADVICE_REPORT', 'ar-001', CURRENT_TIMESTAMP - INTERVAL '2' DAY);
