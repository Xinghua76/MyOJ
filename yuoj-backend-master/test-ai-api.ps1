# ============================================
# AI API 测试脚本 (PowerShell)
# ============================================
# 功能: 验证 DeepSeek AI 调用是否正确
# 使用: ./test-ai-api.ps1
# ============================================

# 配置参数
$BackendUrl = "http://localhost:8121"
$ApiPath = "/api/ai/analyze"
$FullUrl = "$BackendUrl$ApiPath"

# 测试数据
$TestPayload = @{
    questionId = 1
    code = @"
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

result = fibonacci(10)
print(result)
"@
    language = "python"
    judgeInfo = '{"status":"Wrong Answer","message":"Test case 1 failed","input":"10","expectedOutput":"55","actualOutput":"0"}'
} | ConvertTo-Json

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI API 调用测试" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "后端地址: $BackendUrl" -ForegroundColor Yellow
Write-Host "测试路由: $ApiPath" -ForegroundColor Yellow
Write-Host ""

# 检查后端是否在线
Write-Host "1️⃣ 检查后端服务状态..." -ForegroundColor Magenta
try {
    $healthCheck = Invoke-WebRequest -Uri "$BackendUrl/api/user/get/login" -Method GET -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
    Write-Host "✅ 后端服务正常运行" -ForegroundColor Green
} catch {
    Write-Host "❌ 后端服务未运行或无法访问" -ForegroundColor Red
    Write-Host "   请在 $BackendUrl 上启动后端服务" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "2️⃣ 发送 AI 分析请求..." -ForegroundColor Magenta
Write-Host ""
Write-Host "请求数据:" -ForegroundColor Gray
Write-Host $TestPayload -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri $FullUrl `
        -Method POST `
        -Headers @{
            "Content-Type" = "application/json"
        } `
        -Body $TestPayload `
        -UseBasicParsing `
        -TimeoutSec 70 `
        -SkipHttpErrorCheck

    $statusCode = $response.StatusCode
    $responseBody = $response.Content | ConvertFrom-Json

    Write-Host "状态码: $statusCode" -ForegroundColor Green
    Write-Host ""
    Write-Host "响应体:" -ForegroundColor Gray
    Write-Host ($responseBody | ConvertTo-Json -Depth 10) -ForegroundColor Gray
    Write-Host ""

    if ($statusCode -eq 200) {
        if ($responseBody.code -eq 0) {
            Write-Host "✅ API 调用成功！" -ForegroundColor Green
            Write-Host ""
            Write-Host "AI 分析结果:" -ForegroundColor Magenta
            Write-Host $responseBody.data -ForegroundColor White
            Write-Host ""
            
            if ($responseBody.data -like "*⚠️*") {
                Write-Host "⚠️ 注意: API 认证失败，返回了系统预设建议" -ForegroundColor Yellow
                Write-Host "原因: DeepSeek API Key 可能未配置或已过期" -ForegroundColor Yellow
                Write-Host "解决: 按照 AI_CONFIG_GUIDE.md 配置正确的 API Key" -ForegroundColor Yellow
            }
        } else {
            Write-Host "❌ API 返回错误代码: $($responseBody.code)" -ForegroundColor Red
            Write-Host "错误信息: $($responseBody.message)" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ HTTP 错误: $statusCode" -ForegroundColor Red
        Write-Host "响应: $($response.Content)" -ForegroundColor Red
    }

} catch {
    Write-Host "❌ 请求失败" -ForegroundColor Red
    Write-Host "错误信息: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "排查步骤:" -ForegroundColor Yellow
    Write-Host "1. 确保后端运行在 http://localhost:8121" -ForegroundColor Yellow
    Write-Host "2. 检查是否配置了 AI_DEEPSEEK_API_KEY 环境变量" -ForegroundColor Yellow
    Write-Host "3. 检查后端日志中是否有错误信息" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 测试完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
