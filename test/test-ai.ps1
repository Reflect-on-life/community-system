# Test AI Recognition Function
$imagePath = "c:\Users\fengchunzhao\Desktop\future-community-system\monitor\ScreenShot_2026-02-23_103602_641.png"

Write-Host "=== Testing AI Recognition ===" -ForegroundColor Green
Write-Host "Image path: $imagePath" -ForegroundColor Yellow

# Read image
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
Write-Host "Image size: $($imageBytes.Length) bytes" -ForegroundColor Yellow

# Create temp file
$tempFile = [System.IO.Path]::GetTempFileName() + ".png"
[System.IO.File]::WriteAllBytes($tempFile, $imageBytes)

try {
    # Upload image
    Write-Host "`n1. Uploading image..." -ForegroundColor Cyan
    $uploadResult = Invoke-RestMethod -Uri "http://localhost:8080/api/file/upload" -Method Post -InFile $tempFile -Form @{file = Get-Item $tempFile}
    
    if ($uploadResult.success) {
        Write-Host "Upload successful!" -ForegroundColor Green
        $imageUrl = $uploadResult.data
        Write-Host "Image URL: $imageUrl" -ForegroundColor Green
        
        # Call AI recognition
        Write-Host "`n2. Calling AI recognition..." -ForegroundColor Cyan
        $recognizeBody = @{imageUrl = $imageUrl} | ConvertTo-Json
        $recognizeResult = Invoke-RestMethod -Uri "http://localhost:8080/api/risk/recognize" -Method Post -Body $recognizeBody -ContentType "application/json"
        
        Write-Host "`nAI Recognition Result:" -ForegroundColor Green
        Write-Host "  Risk Type: $($recognizeResult.riskType)" -ForegroundColor Yellow
        Write-Host "  Risk Level: $($recognizeResult.riskLevel)" -ForegroundColor Yellow
        
        if ($recognizeResult.riskLevel -eq "HIGH") {
            Write-Host "`n[WARNING] High risk detected!" -ForegroundColor Red
        }
    } else {
        Write-Host "Upload failed!" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    if (Test-Path $tempFile) {
        Remove-Item $tempFile -Force
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
