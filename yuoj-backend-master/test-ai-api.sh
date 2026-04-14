#!/bin/bash
# ============================================
# AI API 测试脚本 (Bash / cURL)
# ============================================
# 功能: 验证 DeepSeek AI 调用是否正确
# 使用: chmod +x test-ai-api.sh && ./test-ai-api.sh
# ============================================

BACKEND_URL="http://localhost:8121"
API_PATH="/api/ai/analyze"
TIMEOUT=70

echo "========================================"
echo "AI API 调用测试"
echo "========================================"
echo ""
echo "后端地址: $BACKEND_URL"
echo "测试路由: $API_PATH"
echo ""

# 检查后端是否在线
echo "1️⃣ 检查后端服务状态..."
if curl -s -m 5 "$BACKEND_URL/api/user/get/login" > /dev/null 2>&1; then
    echo "✅ 后端服务正常运行"
else
    echo "❌ 后端服务未运行或无法访问"
    echo "   请在 $BACKEND_URL 上启动后端服务"
    exit 1
fi

echo ""
echo "2️⃣ 发送 AI 分析请求..."
echo ""

# 构建 JSON 请求体
REQUEST_BODY=$(cat <<'EOF'
{
  "questionId": 1,
  "code": "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nresult = fibonacci(10)\nprint(result)",
  "language": "python",
  "judgeInfo": "{\"status\":\"Wrong Answer\",\"message\":\"Test case 1 failed\",\"input\":\"10\",\"expectedOutput\":\"55\",\"actualOutput\":\"0\"}"
}
EOF
)

echo "请求数据:"
echo "$REQUEST_BODY" | jq '.' 2>/dev/null || echo "$REQUEST_BODY"
echo ""

# 发送请求
RESPONSE=$(curl -s -X POST "$BACKEND_URL$API_PATH" \
    -H "Content-Type: application/json" \
    -d "$REQUEST_BODY" \
    -m $TIMEOUT)

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL$API_PATH" \
    -H "Content-Type: application/json" \
    -d "$REQUEST_BODY" \
    -m $TIMEOUT)

echo "状态码: $HTTP_CODE"
echo ""
echo "响应体:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# 解析响应
if [ "$HTTP_CODE" = "200" ]; then
    CODE=$(echo "$RESPONSE" | jq -r '.code' 2>/dev/null)
    
    if [ "$CODE" = "0" ]; then
        echo "✅ API 调用成功！"
        echo ""
        echo "AI 分析结果:"
        echo "$RESPONSE" | jq -r '.data' 2>/dev/null || echo "$RESPONSE"
        echo ""
        
        if echo "$RESPONSE" | grep -q "⚠️"; then
            echo "⚠️ 注意: API 认证失败，返回了系统预设建议"
            echo "原因: DeepSeek API Key 可能未配置或已过期"
            echo "解决: 按照 AI_CONFIG_GUIDE.md 配置正确的 API Key"
        fi
    else
        MESSAGE=$(echo "$RESPONSE" | jq -r '.message' 2>/dev/null)
        echo "❌ API 返回错误代码: $CODE"
        echo "错误信息: $MESSAGE"
    fi
else
    echo "❌ HTTP 错误: $HTTP_CODE"
    echo "响应: $RESPONSE"
    echo ""
    echo "排查步骤:"
    echo "1. 确保后端运行在 http://localhost:8121"
    echo "2. 检查是否配置了 AI_DEEPSEEK_API_KEY 环境变量"
    echo "3. 检查后端日志中是否有错误信息"
    exit 1
fi

echo ""
echo "========================================"
echo "✅ 测试完成！"
echo "========================================"
