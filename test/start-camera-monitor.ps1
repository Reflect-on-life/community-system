# 摄像头行为识别功能 - 一键启动脚本
# 使用方法：在 PowerShell 中执行 .\start-camera-monitor.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  摄像头行为识别功能 - 一键启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $projectRoot "backend"
$frontendDir = Join-Path $projectRoot "frontend"

Write-Host "项目根目录：$projectRoot" -ForegroundColor Yellow
Write-Host ""

# ========== 第一步：检查环境 ==========
Write-Host "[1/5] 检查运行环境..." -ForegroundColor Green

# 检查 Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "  ✓ Java 已安装：$javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Java 未安装，请先安装 JDK 17+" -ForegroundColor Red
    exit 1
}

# 检查 Node.js
try {
    $nodeVersion = node --version
    Write-Host "  ✓ Node.js 已安装：$nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Node.js 未安装，请先安装 Node.js 16+" -ForegroundColor Red
    exit 1
}

# 检查 MySQL
try {
    $mysqlService = Get-Service -Name "MySQL*" -ErrorAction Stop
    if ($mysqlService.Status -eq "Running") {
        Write-Host "  ✓ MySQL 服务正在运行" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ MySQL 服务未运行，正在启动..." -ForegroundColor Yellow
        Start-Service -Name $mysqlService.Name
    }
} catch {
    Write-Host "  ⚠ 未找到 MySQL 服务，请手动启动" -ForegroundColor Yellow
}

Write-Host ""

# ========== 第二步：创建必要目录 ==========
Write-Host "[2/5] 创建必要目录..." -ForegroundColor Green

$monitorDir = Join-Path $backendDir "upload\monitor"
if (!(Test-Path $monitorDir)) {
    New-Item -ItemType Directory -Path $monitorDir -Force | Out-Null
    Write-Host "  ✓ 创建告警图片目录：$monitorDir" -ForegroundColor Green
} else {
    Write-Host "  ✓ 告警图片目录已存在" -ForegroundColor Green
}

Write-Host ""

# ========== 第三步：启动后端服务 ==========
Write-Host "[3/5] 启动后端 SpringBoot 服务..." -ForegroundColor Green
Write-Host "  提示：后端将在 http://localhost:8080 启动" -ForegroundColor Cyan

$backendStartScript = {
    Set-Location $using:backendDir
    mvn spring-boot:run
}

$backendJob = Start-Job -ScriptBlock $backendStartScript
Write-Host "  ✓ 后端服务已在后台启动" -ForegroundColor Green

# 等待后端启动（约 30 秒）
Write-Host "  等待后端启动..." -ForegroundColor Cyan
Start-Sleep -Seconds 30

# 检查后端是否启动成功
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/alarm/list?page=1&size=1" -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ 后端服务启动成功！" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠ 后端服务可能还在启动中，请等待..." -ForegroundColor Yellow
}

Write-Host ""

# ========== 第四步：启动前端服务 ==========
Write-Host "[4/5] 启动前端 Vite 服务..." -ForegroundColor Green
Write-Host "  提示：前端将在 http://localhost:5173 启动" -ForegroundColor Cyan

$frontendStartScript = {
    Set-Location $using:frontendDir
    npm run dev
}

$frontendJob = Start-Job -ScriptBlock $frontendStartScript
Write-Host "  ✓ 前端服务已在后台启动" -ForegroundColor Green

# 等待前端启动（约 10 秒）
Write-Host "  等待前端启动..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

Write-Host ""

# ========== 第五步：显示访问信息 ==========
Write-Host "[5/5] 启动完成！" -ForegroundColor Green
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务访问地址" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  🌐 摄像头监控页面：" -ForegroundColor Yellow
Write-Host "     http://localhost:5173/camera-monitor" -ForegroundColor White
Write-Host ""
Write-Host "  📋 告警历史页面：" -ForegroundColor Yellow
Write-Host "     http://localhost:5173/alarm/history" -ForegroundColor White
Write-Host ""
Write-Host "  🔧 后端 API 接口：" -ForegroundColor Yellow
Write-Host "     http://localhost:8080/api/alarm/list" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  快速操作指南" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  1. 访问摄像头监控页面" -ForegroundColor White
Write-Host "  2. 点击'开始监控'按钮" -ForegroundColor White
Write-Host "  3. 允许浏览器访问摄像头" -ForegroundColor White
Write-Host "  4. 点击'模拟告警'按钮测试（开发环境专用）" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  后台服务信息" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  后端服务 Job ID: $($backendJob.Id)" -ForegroundColor White
Write-Host "  前端服务 Job ID: $($frontendJob.Id)" -ForegroundColor White
Write-Host ""
Write-Host "  停止服务命令：" -ForegroundColor Yellow
Write-Host "  Stop-Job -Id $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor White
Write-Host "  Remove-Job -Id $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 打开浏览器
Write-Host "正在打开摄像头监控页面..." -ForegroundColor Cyan
Start-Process "http://localhost:5173/camera-monitor"

Write-Host ""
Write-Host "提示：按 Ctrl+C 或关闭窗口不会停止后台服务" -ForegroundColor Yellow
Write-Host "如需停止服务，请运行：Stop-Job -Id $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor Yellow
Write-Host ""

# 保持窗口打开
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
