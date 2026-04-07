# StudentApp 教育管理系统

## 快速开始

### 启动后端
```bash
cd server
javac -cp lib/h2.jar Server.java
java -cp .:lib/h2.jar Server
```

### API 接口
- GET /api/health - 健康检查
- GET /api/students - 学生列表
- POST /api/students - 添加学生 {"name":"张三","age":10}
- POST /api/auth/register - 注册 {"username":"user","password":"pass","name":"姓名"}
- POST /api/auth/login - 登录 {"username":"user","password":"pass"}

### 技术栈
- 后端：Java + H2数据库
- 前端：待开发（React）

## 功能
- [x] 用户注册/登录
- [x] 学生管理（增删改查）
- [ ] 健康记录
- [ ] 学习记录
- [ ] AI报告生成
