# 教育管理系统 - StudentApp

## 1. 项目概述

- **项目名称**: StudentApp 教育管理系统
- **项目类型**: Web + 微信小程序 (AI 驱动)
- **核心功能**: 学生健康与学习信息管理，AI 分析报告生成
- **目标用户**: 老师、家长、管理员

## 2. 技术架构

### 技术栈
- **前端**: React + TypeScript (PC) / Taro (小程序)
- **后端**: Node.js + Express
- **数据库**: SQLite (开发) / PostgreSQL (生产)
- **AI**: Kimi kimi-for-coding (Claude Code)
- **部署**: Docker

### 系统架构图
```
┌─────────────────────────────────────┐
│           客户端层                  │
│  ┌──────────┐    ┌──────────┐       │
│  │ 微信小程序│    │  PC网页  │       │
│  └────┬─────┘    └────┬─────┘       │
└───────┼──────────────┼─────────────┘
        │              │
┌───────▼──────────────▼─────────────┐
│           API 网关                  │
│      (Node.js Express)             │
└───────────────┬───────────────────┘
                │
┌───────────────▼───────────────────┐
│           业务服务                 │
│  ┌─────────┐  ┌─────────┐          │
│  │用户服务 │  │AI服务   │          │
│  └────┬────┘  └────┬────┘          │
└───────┼────────────┼────────────────┘
        │            │
┌───────▼───────────▼────────────────┐
│           数据层                    │
│  ┌────────┐  ┌────────┐            │
│  │PostgreSQL│ │  日志  │            │
│  └────────┘  └────────┘            │
└─────────────────────────────────────┘
```

## 3. 数据库设计

### 用户表 (users)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| username | VARCHAR | 用户名 |
| password_hash | VARCHAR | 密码 |
| role | ENUM | teacher/admin/parent |
| name | VARCHAR | 姓名 |
| created_at | TIMESTAMP | 创建时间 |

### 学生表 (students)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| name | VARCHAR | 学生姓名 |
| age | INT | 年龄 |
| parent_id | UUID | 家长ID |
| teacher_id | UUID | 负责老师ID |
| created_at | TIMESTAMP | 创建时间 |

### 健康记录表 (health_records)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| student_id | UUID | 学生ID |
| height | FLOAT | 身高 |
| weight | FLOAT | 体重 |
| vision | FLOAT | 视力 |
| health_notes | TEXT | 健康备注 |
| created_at | TIMESTAMP | 创建时间 |

### 学习记录表 (learning_records)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| student_id | UUID | 学生ID |
| subject | VARCHAR | 科目 |
| score | INT | 分数 |
| notes | TEXT | 备注 |
| created_at | TIMESTAMP | 创建时间 |

### AI报告表 (ai_reports)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| student_id | UUID | 学生ID |
| report_type | ENUM | health/learning |
| content | TEXT | AI生成内容 |
| created_at | TIMESTAMP | 创建时间 |

### 交互日志表 (interaction_logs)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | UUID | 主键 |
| user_id | UUID | 用户ID |
| student_id | UUID | 学生ID |
| message | TEXT | 用户消息 |
| ai_response | TEXT | AI回复 |
| created_at | TIMESTAMP | 创建时间 |

## 4. 功能模块

### 4.1 用户管理
- [ ] 注册/登录
- [ ] 角色权限管理
- [ ] 老师管理（管理员）

### 4.2 学生管理
- [ ] 添加/编辑学生信息
- [ ] 查看学生列表
- [ ] 学生详情

### 4.3 信息录入
- [ ] 健康信息录入
- [ ] 学习信息录入
- [ ] 批量导入

### 4.4 AI 分析
- [ ] 健康报告生成
- [ ] 学习分析生成
- [ ] 报告查看/下载

### 4.5 数据隔离
- [ ] 老师间数据隔离
- [ ] 会话管理（每学生独立session）

## 5. API 接口

### 认证
- POST /api/auth/login
- POST /api/auth/register
- POST /api/auth/logout

### 用户
- GET /api/users
- POST /api/users
- PUT /api/users/:id
- DELETE /api/users/:id

### 学生
- GET /api/students
- POST /api/students
- GET /api/students/:id
- PUT /api/students/:id
- DELETE /api/students/:id

### 健康记录
- GET /api/students/:id/health
- POST /api/students/:id/health

### 学习记录
- GET /api/students/:id/learning
- POST /api/students/:id/learning

### AI 报告
- POST /api/ai/generate-report
- GET /api/reports/:id
- GET /api/students/:id/reports

## 6. 开发计划

### Phase 1: 基础架构 (Week 1)
- [ ] 项目初始化
- [ ] 数据库设计
- [ ] 用户认证

### Phase 2: 核心功能 (Week 2)
- [ ] 学生CRUD
- [ ] 信息录入
- [ ] AI 集成

### Phase 3: 增值功能 (Week 3)
- [ ] 报告导出
- [ ] 数据隔离
- [ ] 日志系统

### Phase 4: 小程序 (Week 4)
- [ ] Taro 接入
- [ ] 移动端适配

## 7. 环境变量

```bash
# Backend
DATABASE_URL=postgresql://user:pass@localhost:5432/studentapp
JWT_SECRET=your-secret-key
AI_API_KEY=sk-kimi-xxx

# Frontend
REACT_APP_API_URL=http://localhost:3000
```
