# ⚙️ 本地开发环境配置指南

## 1. 配置 API 密钥

### DeepSeek API 配置
1. 访问 https://platform.deepseek.com 注册账户
2. 创建 API Key
3. 复制 API Key 到 `.env` 文件中

### 快速开始

```bash
# 2. 复制示例配置文件
cp .env.example .env

# 3. 编辑 .env，填入你的 API Key
# Windows: type .env (or use your text editor)
# Mac/Linux: cat .env (or use your text editor)
```

## 2. Spring Boot 读取环境变量

方式一：直接设置系统环境变量（Windows）
```powershell
$env:AI_DEEPSEEK_API_KEY = "your-api-key"
```

方式二：使用 Maven 运行
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--AI_DEEPSEEK_API_KEY=your-api-key"
```

方式三：使用 application-local.yml（推荐）
参考 Spring Profiles 配置

## 3. 安全最佳实践

✅ **正确做法：**
- 在 `.gitignore` 中添加 `.env` 和本地配置文件
- 使用环境变量传递敏感信息
- 生产环境使用密钥管理服务

❌ **禁止：**
- 在 Git 中提交 `.env` 或 `application.yml` 中的真实密钥
- 在代码中硬编码 API Key
- 在 GitHub 上公开密钥

## 4. 测试 API 连接

运行后端后，调用接口测试：

```bash
# 编码一个问题进行 AI 分析测试
curl -X POST http://localhost:8121/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 1,
    "code": "console.log(\"hello\");",
    "language": "javascript",
    "judgeInfo": "{\"status\": \"Wrong Answer\"}"
  }'
```

## 5. 常见问题

**Q: API 认证失败怎么办？**
A: 检查 API Key 是否正确，可以在 DeepSeek 官网验证

**Q: 如何查看 API 请求日志？**
A: 后端会输出 `DeepSeek API Response` 日志

**Q: 没有 API Key 可以测试吗？**
A: 可以，系统会自动回退到 Mock 反馈（预设建议）
