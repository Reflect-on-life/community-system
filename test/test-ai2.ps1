$imagePath = "c:\Users\fengchunzhao\Desktop\future-community-system\monitor\ScreenShot_2026-02-23_103602_641.png"

Write-Host "=== Testing AI Recognition ===" -ForegroundColor Green

# Upload image using curl
Write-Host "`n1. Uploading image..." -ForegroundColor Cyan
$uploadResult = curl.exe -X POST -F "file=@$imagePath" http://localhost:8080/api/file/upload

Write-Host "Upload response: $uploadResult"

# Parse the result to get image URL
$uploadJson = $uploadResult | ConvertFrom-Json
if ($uploadJson.success) {
    $imageUrl = $uploadJson.data
    Write-Host "Image URL: $imageUrl" -ForegroundColor Green
    
    # Call AI recognition
    Write-Host "`n2. Calling AI recognition..." -ForegroundColor Cyan
    $recognizeResult = curl.exe -X POST -H "Content-Type: application/json" -d "{`"imageUrl`":`"$imageUrl`"}" http://localhost:8080/api/risk/recognize
    
    Write-Host "`nAI Recognition Result:" -ForegroundColor Green
    Write-Host $recognizeResult
} else {
    Write-Host "Upload failed!" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green
