# 🔧 AI API 调用检查 & 故障排查指南

> 本指南帮助你验证和修复 DeepSeek AI 调用问题

## 快速诊断

### 症状 1: ⚠️ "DeepSeek API 认证失败"

**原因分析:**
- DeepSeek API Key 未配置
- API Key 已过期或无效
- API Key 格式错误

**解决方案:**

```bash
# ✅ 方案 A: 配置环境变量 (Windows PowerShell)
$env:AI_DEEPSEEK_API_KEY = "ysk-xxxxxxxxxxxxxxxxxxxxx"

# ✅ 方案 B: 配置环境变量 (Linux/Mac)
export AI_DEEPSEEK_API_KEY="ysk-xxxxxxxxxxxxxxxxxxxxx"

# ✅ 方案 C: 修改 application.yml
# yuoj-backend-master/src/main/resources/application.yml
ai:
  deepseek:
    api-key: ysk-xxxxxxxxxxxxxxxxxxxxx  # 替换为你的真实密钥
```

---

## 验证 API 是否正确调用

### 方法 1: 使用 PowerShell 脚本 (Windows)

```powershell
# 进入后端目录
cd yuoj-backend-master

# 运行测试脚本
.\test-ai-api.ps1
```

**预期输出:**
```
✅ 后端服务正常运行
✅ API 调用成功！
AI 分析结果:
你的代码在执行中遇到了...
```

---

### 方法 2: 使用 Bash 脚本 (Linux/Mac)

```bash
# 进入后端目录
cd yuoj-backend-master

# 赋予执行权限
chmod +x test-ai-api.sh

# 运行测试脚本
./test-ai-api.sh
```

---

### 方法 3: 使用 cURL 手动测试 (任何系统)

```bash
# 1. 先确保后端运行在 http://localhost:8121

# 2. 发送测试请求
curl -X POST http://localhost:8121/api/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "questionId": 1,
    "code": "print(\"hello world\")",
    "language": "python",
    "judgeInfo": "{\"status\": \"Wrong Answer\"}"
  }'
```

**预期响应:**
```json
{
  "code": 0,
  "message": "ok",
  "data": "你是一名资深的编程导师...或者系统预设建议"
}
```

---

### 方法 4: 使用 Postman 测试

1. **新建 POST 请求**
   - URL: `http://localhost:8121/api/ai/analyze`
   - Headers: `Content-Type: application/json`

2. **请求体 (Body - raw JSON)**
```json
{
  "questionId": 1,
  "code": "def hello():\n    print('hello world')",
  "language": "python",
  "judgeInfo": "{\"status\": \"Accepted\"}"
}
```

3. **点击 Send 发送请求**

---

## 完整故障排查流程

### 步骤 1️⃣: 检查后端是否运行

```bash
# 测试后端是否在线
curl http://localhost:8121/api/user/get/login -v
```

**✅ 成功:** 返回 401 Unauthorized (这是正确的，说明后端正在运行)
**❌ 失败:** Connection refused → 启动后端

---

### 步骤 2️⃣: 检查环境变量是否设置

```bash
# Windows PowerShell
echo $env:AI_DEEPSEEK_API_KEY

# Linux/Mac Bash
echo $AI_DEEPSEEK_API_KEY
```

**✅ 成功:** 显示你的 API Key (如 `ysk-xxxxx`)
**❌ 失败:** 为空 → 按照上面的方法设置环境变量

---

### 步骤 3️⃣: 检查 application.yml 配置

```bash
# 检查当前配置
grep -A 5 "ai:" yuoj-backend-master/src/main/resources/application.yml
```

**✅ 正确配置:**
```yaml
ai:
  deepseek:
    api-key: ${AI_DEEPSEEK_API_KEY:}  # 使用环境变量
    model: ${AI_DEEPSEEK_MODEL:deepseek-chat}
    url: ${AI_DEEPSEEK_URL:https://api.deepseek.com/v1/chat/completions}
```

---

### 步骤 4️⃣: 查看后端日志

```bash
# 后端启动时应该显示类似的日志:
# INFO  DeepSeek API 调用成功，返回内容长度: 256 字符
# 或
# WARN  DeepSeek API 认证失败，自动回退到 Mock 反馈。错误: ...
```

**关键日志信息:**
- `DeepSeek API 调用成功` → API 配置正确 ✅
- `DeepSeek API 认证失败` → API Key 无效 ❌
- `DeepSeek API 调用异常` → 网络或服务问题 ❌

---

### 步骤 5️⃣: 测试 API 调用

使用上面提供的脚本之一进行测试，检查是否有以下错误:

| 错误信息 | 原因 | 解决方案 |
|---------|------|--------|
| `401 Unauthorized` | 参数缺少或不正确 | 检查请求体中的 `questionId`, `code`, `language` |
| `404 Not Found` | 后端路由不存在 | 确认后端已添加 `AiController` 类 |
| `500 Internal Server Error` | 后端异常 | 查看后端日志 |
| `⚠️ DeepSeek API 认证失败` | API Key 无效 | 重新检查 API Key 是否正确 |

---

## 常见问题 (FAQ)

### Q: 如何获取 DeepSeek API Key?

**A:** 
1. 访问 https://platform.deepseek.com
2. 注册账户或登录
3. 创建 API Key
4. 复制 Key 值 (格式: `ysk-xxxxxxxxxxxxxxxxxxxxx`)

---

### Q: 为什么要用环境变量隐藏 API Key?

**A:** 
- 防止 API Key 被 Git 暴露
- 符合安全最佳实践
- 便于不同环境使用不同的 Key

---

### Q: 系统预设建议是什么?

**A:** 当 DeepSeek API 不可用时，系统会根据判题结果自动返回预设建议，包括:
- ✅ 通过 (Accepted) → 建议优化时空复杂度
- ❌ 答案错误 (Wrong Answer) → 建议检查边界情况和输出格式
- ⏳ 超时 (Time Limit Exceeded) → 建议优化算法

---

### Q: 可以在没有 API Key 的情况下测试吗?

**A:** 可以！系统会自动回退到系统预设建议。但为了体验完整的 AI 功能，建议配置真实的 API Key。

---

## 日志位置

后端运行日志通常包含 AI 调用的详细信息:

```bash
# 查看实时日志（如果使用 IDE 运行）
# 在 IDE 的 Console 或 Log 窗口查看

# 或查看文件日志（如果配置了）
tail -f logs/application.log | grep AI
```

---

## 关键文件清单

| 文件 | 用途 | 状态 |
|-----|------|------|
| `AI_CONFIG_GUIDE.md` | 配置指南 | 📄 文档 |
| `.env.example` | 环境变量示例 | 📄 配置 |
| `test-ai-api.ps1` | Windows 测试脚本 | 🔧 工具 |
| `test-ai-api.sh` | Linux/Mac 测试脚本 | 🔧 工具 |
| `AiManager.java` | AI 逻辑类 | ✅ 已改进 |
| `AiController.java` | AI 接口类 | ✅ 已改进 |
| `ViewQuestionView.vue` | 前端界面 | ✅ 已改进 |
| `application.yml` | 应用配置 | ✅ 已改进 |

---

## 总结检查清单

- [ ] 配置了 `AI_DEEPSEEK_API_KEY` 环境变量
- [ ] 修改了 `application.yml` 使用环境变量
- [ ] 后端正常运行 (`http://localhost:8121`)
- [ ] 运行了测试脚本
- [ ] 前端调用返回成功响应
- [ ] 日志中显示 "DeepSeek API 调用成功" 或 "系统预设建议"

**都完成了? 🎉 恭喜，AI 调用已经修复！**

---

需要帮助? 提交日志和错误信息，我们会尽快帮你排查问题。
