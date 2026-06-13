# 测试 AI 识别功能
# 1. 先上传图片
# 2. 然后调用 AI 识别接口

$imagePath = "c:\Users\fengchunzhao\Desktop\future-community-system\monitor\ScreenShot_2026-02-23_103602_641.png"

Write-Host "=== 测试 AI 识别功能 ===" -ForegroundColor Green
Write-Host "图片路径：$imagePath" -ForegroundColor Yellow

# 读取图片并转换为 Base64
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64Image = [System.Convert]::ToBase64String($imageBytes)

Write-Host "图片大小：$($imageBytes.Length) 字节" -ForegroundColor Yellow
Write-Host ""

# 调用后端上传接口
Write-Host "1. 上传图片到服务器..." -ForegroundColor Cyan

# 创建一个临时文件用于上传
$tempFile = [System.IO.Path]::GetTempFileName() + ".png"
[System.IO.File]::WriteAllBytes($tempFile, $imageBytes)

try {
    # 使用 curl 上传图片
    $uploadResult = Invoke-RestMethod -Uri "http://localhost:8080/api/file/upload" -Method Post -InFile $tempFile -Form @{file = Get-Item $tempFile}
    
    Write-Host "上传结果：" -NoNewline
    if ($uploadResult.success) {
        Write-Host "成功" -ForegroundColor Green
        $imageUrl = $uploadResult.data
        Write-Host "图片 URL: $imageUrl" -ForegroundColor Green
        Write-Host ""
        
        # 调用 AI 识别接口
        Write-Host "2. 调用 AI 识别接口..." -ForegroundColor Cyan
        $recognizeBody = @{imageUrl = $imageUrl} | ConvertTo-Json
        $recognizeResult = Invoke-RestMethod -Uri "http://localhost:8080/api/risk/recognize" -Method Post -Body $recognizeBody -ContentType "application/json"
        
        Write-Host "AI 识别结果：" -ForegroundColor Green
        Write-Host "  风险类型：$($recognizeResult.riskType)" -ForegroundColor Yellow
        Write-Host "  风险等级：$($recognizeResult.riskLevel)" -ForegroundColor Yellow
        Write-Host ""
        
        # 根据风险等级显示不同颜色
        if ($recognizeResult.riskLevel -eq "HIGH") {
            Write-Host "⚠️ 警告：检测到高风险事件！" -ForegroundColor Red
        } elseif ($recognizeResult.riskLevel -eq "MEDIUM") {
            Write-Host "⚡ 提示：检测到中等风险事件" -ForegroundColor Yellow
        } else {
            Write-Host "✓ 低风险事件" -ForegroundColor Green
        }
    } else {
        Write-Host "失败" -ForegroundColor Red
        Write-Host "错误信息：$($uploadResult.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "发生错误：$($_.Exception.Message)" -ForegroundColor Red
} finally {
    # 清理临时文件
    if (Test-Path $tempFile) {
        Remove-Item $tempFile -Force
    }
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
