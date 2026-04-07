# StudentApp 教育管理系统

## 🏃 快速测试

### 启动后端
```bash
cd server
javac -cp lib/h2.jar Server.java
java -cp .:lib/h2.jar Server
```

### API测试
```bash
# 学生列表
curl localhost:3000/api/students

# 登录 (teacher1/pass123 或 admin/admin123)
curl -X POST localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"username":"teacher1","password":"pass123"}'

# AI健康报告
curl -X POST localhost:3000/api/ai/generate-report -H 'Content-Type: application/json' -d '{"studentId":"s1","type":"health"}'
```

### 演示账户
- 老师: teacher1 / pass123
- 管理员: admin / admin123

## 功能
- [x] 登录
- [x] 学生列表
- [x] 学生详情
- [x] AI报告生成
- [ ] 添加学生(PHP问题)
