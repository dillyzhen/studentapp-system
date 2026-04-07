# StudentApp 教育管理系统

## 🚀 快速开始

### 后端启动
```bash
cd server
javac -cp lib/h2.jar Server.java
java -cp .:lib/h2.jar Server
```
访问 http://localhost:3000

### API 接口
- `GET /api/students` - 学生列表
- `POST /api/students` - 添加学生 `{"name":"张三","age":10}`
- `GET /api/health` - 健康检查

### 测试用户
- 张三 (10岁)
- 李四 (9岁)  
- 王五 (11岁)

## 功能
- [x] 学生列表
- [x] 添加学生
- [ ] 学生详情
- [ ] 健康记录
- [ ] 学习记录
- [ ] AI报告

## 技术
- 后端: Java + H2数据库
- 前端: HTML + JS
