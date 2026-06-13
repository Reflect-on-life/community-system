package com.example.futurecommunity.controller;

import com.example.futurecommunity.service.AIRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${upload.path:./upload}")
    private String uploadPath;

    @Autowired
    private AIRecognitionService aiRecognitionService;

    /**
     * 分析当前日期的监控截图（实时分析）
     * 只分析今天的截图目录，避免扫描历史目录产生费用
     */
    @PostMapping("/analyze-pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROPERTY')")
    public ResponseEntity<Map<String, Object>> analyzePendingSnapshots(
            @RequestParam(required = false) String cameraLocation) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> analysisResults = new ArrayList<>();
        int totalScanned = 0;
        int totalAnalyzed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        int highRiskCount = 0;
        int mediumRiskCount = 0;
        int lowRiskCount = 0;

        try {
            logger.info("=== 开始分析今日监控截图（实时分析）===");
            logger.info("摄像头位置：{}", cameraLocation != null ? cameraLocation : "默认");

            // 1. 获取当前日期（只分析今天）
            String currentDate = java.time.LocalDate.now().toString();
            logger.info("分析日期：{}", currentDate);

            // 2. 解析绝对路径
            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*")
                    ? System.getProperty("user.dir") + File.separator + uploadPath
                    : uploadPath;

            Path todayDir = Paths.get(absoluteUploadPath, "monitor", currentDate);
            if (!todayDir.toFile().exists()) {
                logger.info("今日监控目录不存在：{}", todayDir);
                result.put("code", 200);
                result.put("success", true);
                result.put("message", "今日暂无截图");
                result.put("summary", Map.of(
                        "totalScanned", 0,
                        "totalAnalyzed", 0,
                        "totalSuccess", 0,
                        "totalFailed", 0,
                        "highRiskCount", 0,
                        "mediumRiskCount", 0,
                        "lowRiskCount", 0
                ));
                result.put("data", analysisResults);
                return ResponseEntity.ok(result);
            }

            // 3. 获取今日目录下的所有图片
            File[] imageFiles = todayDir.toFile().listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".jpg") ||
                    name.toLowerCase().endsWith(".jpeg") ||
                    name.toLowerCase().endsWith(".png")
            );

            if (imageFiles == null || imageFiles.length == 0) {
                logger.info("今日目录暂无图片：{}", todayDir);
                result.put("code", 200);
                result.put("success", true);
                result.put("message", "今日暂无截图");
                result.put("summary", Map.of(
                        "totalScanned", 0,
                        "totalAnalyzed", 0,
                        "totalSuccess", 0,
                        "totalFailed", 0,
                        "highRiskCount", 0,
                        "mediumRiskCount", 0,
                        "lowRiskCount", 0
                ));
                result.put("data", analysisResults);
                return ResponseEntity.ok(result);
            }

            logger.info("找到 {} 张今日截图", imageFiles.length);
            totalScanned = imageFiles.length;

            // 4. 对每张图片进行 AI 分析
            for (File imageFile : imageFiles) {
                String relativePath = "/upload/monitor/" + currentDate + "/" + imageFile.getName();
                String localFilePath = imageFile.getAbsolutePath();

                logger.info("正在分析：{}", relativePath);

                try {
                    // 调用 AI 识别服务
                    Map<String, String> aiResult = aiRecognitionService.recognizeRiskWithCameraInfo(
                            localFilePath,
                            "实时分析 - " + currentDate,
                            cameraLocation != null ? cameraLocation : "监控区域",
                            "realtime_analysis"
                    );

                        totalAnalyzed++;

                        // 记录分析结果
                        Map<String, Object> itemResult = new HashMap<>();
                        itemResult.put("imageUrl", relativePath);
                        itemResult.put("fileName", imageFile.getName());
                        itemResult.put("riskType", aiResult.get("riskType"));
                        itemResult.put("riskLevel", aiResult.get("riskLevel"));
                        itemResult.put("description", aiResult.get("description"));
                        itemResult.put("glmResult", aiResult.get("glmResult"));
                        itemResult.put("baiduLabels", aiResult.get("baiduLabels"));
                        itemResult.put("riskEventId", aiResult.get("riskEventId"));

                        String riskLevel = aiResult.get("riskLevel");
                        if ("HIGH".equals(riskLevel)) {
                            highRiskCount++;
                            itemResult.put("status", "high_risk");
                        } else if ("MEDIUM".equals(riskLevel)) {
                            mediumRiskCount++;
                            itemResult.put("status", "medium_risk");
                        } else if ("LOW".equals(riskLevel)) {
                            lowRiskCount++;
                            itemResult.put("status", "low_risk");
                        } else {
                            itemResult.put("status", "unknown");
                        }

                        analysisResults.add(itemResult);
                        totalSuccess++;

                        logger.info("分析完成：{} -> {} ({})",
                                imageFile.getName(),
                                aiResult.get("riskType"),
                                aiResult.get("riskLevel"));

                    } catch (Exception e) {
                        logger.error("分析失败：{}", imageFile.getName(), e);
                        totalFailed++;

                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("imageUrl", relativePath);
                        errorResult.put("fileName", imageFile.getName());
                        errorResult.put("status", "error");
                        errorResult.put("error", e.getMessage());
                        analysisResults.add(errorResult);
                    }
            }

            logger.info("=== 批量分析完成 ===");
            logger.info("总扫描：{} 张", totalScanned);
            logger.info("总分析：{} 张", totalAnalyzed);
            logger.info("成功：{} 张", totalSuccess);
            logger.info("失败：{} 张", totalFailed);
            logger.info("高风险：{} 个", highRiskCount);
            logger.info("中风险：{} 个", mediumRiskCount);
            logger.info("低风险：{} 个", lowRiskCount);

            // 6. 返回统计结果
            result.put("code", 200);
            result.put("success", true);
            result.put("message", "批量分析完成");
            result.put("summary", Map.of(
                    "totalScanned", totalScanned,
                    "totalAnalyzed", totalAnalyzed,
                    "totalSuccess", totalSuccess,
                    "totalFailed", totalFailed,
                    "highRiskCount", highRiskCount,
                    "mediumRiskCount", mediumRiskCount,
                    "lowRiskCount", lowRiskCount
            ));
            result.put("data", analysisResults);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("批量分析异常", e);
            result.put("code", 500);
            result.put("success", false);
            result.put("message", "批量分析失败：" + e.getMessage());
            result.put("summary", Map.of(
                    "totalScanned", totalScanned,
                    "totalAnalyzed", totalAnalyzed,
                    "totalSuccess", totalSuccess,
                    "totalFailed", totalFailed,
                    "highRiskCount", highRiskCount,
                    "mediumRiskCount", mediumRiskCount,
                    "lowRiskCount", lowRiskCount
            ));
            result.put("data", analysisResults);
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 2. 验证文件类型（只允许图片格式）
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg") &&
                 !contentType.equals("image/gif") &&
                 !contentType.equals("image/webp"))) {
                result.put("code", 400);
                result.put("message", "只支持图片格式 (jpg、png、jpeg、gif、webp)");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 验证文件大小（不超过 2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过 2MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. 确保上传目录存在（使用绝对路径）
            // 如果是相对路径，转换为项目根目录的绝对路径
            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*") 
                ? System.getProperty("user.dir") + File.separator + uploadPath 
                : uploadPath;
            
            Path uploadDir = Paths.get(absoluteUploadPath, "images");
            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
                logger.info("创建上传目录：{}", uploadDir.toAbsolutePath());
            }

            // 6. 保存文件到服务器
            Path destFilePath = uploadDir.resolve(newFilename);
            logger.info("准备保存文件：{}", destFilePath.toAbsolutePath());
            
            // 使用绝对路径保存文件
            File destFile = destFilePath.toFile();
            file.transferTo(destFile);

            // 7. 返回相对路径（用于前端访问）
            String relativePath = "/upload/images/" + newFilename;
            
            logger.info("文件上传成功：{}", destFilePath.toAbsolutePath());
            logger.info("访问路径：{}", relativePath);

            result.put("code", 200);
            result.put("success", true);
            result.put("message", "上传成功");
            result.put("data", relativePath);
            result.put("url", relativePath);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("文件上传失败 - IO 异常", e);
            logger.error("上传路径：{}", uploadPath);
            logger.error("文件原始名称：{}", file.getOriginalFilename());
            logger.error("文件大小：{} bytes", file.getSize());
            result.put("code", 500);
            result.put("message", "文件上传失败：" + e.getMessage() + " - 请检查上传目录是否存在且有写权限");
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            logger.error("文件上传异常", e);
            logger.error("上传路径：{}", uploadPath);
            result.put("code", 500);
            result.put("message", "文件上传异常：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 活动海报上传专用接口
     */
    @PostMapping("/upload/activity")
    public ResponseEntity<Map<String, Object>> uploadActivityPoster(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 2. 验证文件类型（只允许图片格式）
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg") &&
                 !contentType.equals("image/gif") &&
                 !contentType.equals("image/webp"))) {
                result.put("code", 400);
                result.put("message", "只支持图片格式 (jpg、png、jpeg、gif、webp)");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 验证文件大小（不超过 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过 5MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. 确保上传目录存在（使用绝对路径）
            // 如果是相对路径，转换为项目根目录的绝对路径
            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*") 
                ? System.getProperty("user.dir") + File.separator + uploadPath 
                : uploadPath;
            
            Path uploadDir = Paths.get(absoluteUploadPath, "active");
            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
                logger.info("创建上传目录：{}", uploadDir.toAbsolutePath());
            }

            // 6. 保存文件到服务器
            Path destFilePath = uploadDir.resolve(newFilename);
            file.transferTo(destFilePath.toFile());

            // 7. 返回相对路径（用于前端访问）
            String relativePath = "/upload/active/" + newFilename;
            
            logger.info("活动海报上传成功：{}", destFilePath.toAbsolutePath());
            logger.info("访问路径：{}", relativePath);

            result.put("code", 200);
            result.put("success", true);
            result.put("message", "海报上传成功");
            result.put("data", relativePath);
            result.put("url", relativePath);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("活动海报上传失败", e);
            result.put("code", 500);
            result.put("message", "海报上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            logger.error("活动海报上传异常", e);
            result.put("code", 500);
            result.put("message", "海报上传异常：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 合同文件上传专用接口（支持JPG、PNG、PDF）
     */
    @PostMapping("/upload/contract")
    public ResponseEntity<Map<String, Object>> uploadContract(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            String contentType = file.getContentType();
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            boolean isImage = contentType != null && (
                contentType.equals("image/jpeg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/jpg")
            );
            boolean isPdf = "application/pdf".equals(contentType) || ".pdf".equals(extension);

            if (!isImage && !isPdf) {
                result.put("code", 400);
                result.put("message", "只支持 JPG、PNG、PDF 格式的文件");
                return ResponseEntity.badRequest().body(result);
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过 5MB");
                return ResponseEntity.badRequest().body(result);
            }

            String newFilename = UUID.randomUUID().toString() + extension;

            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*") 
                ? System.getProperty("user.dir") + File.separator + uploadPath 
                : uploadPath;
            
            Path uploadDir = Paths.get(absoluteUploadPath, "contract");
            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
                logger.info("创建合同上传目录：{}", uploadDir.toAbsolutePath());
            }

            Path destFilePath = uploadDir.resolve(newFilename);
            File destFile = destFilePath.toFile();
            file.transferTo(destFile);

            String relativePath = "/upload/contract/" + newFilename;
            
            logger.info("合同文件上传成功：{}", destFilePath.toAbsolutePath());
            logger.info("访问路径：{}", relativePath);

            result.put("code", 200);
            result.put("success", true);
            result.put("message", "合同文件上传成功");
            result.put("data", relativePath);
            result.put("url", relativePath);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("合同文件上传失败 - IO 异常", e);
            result.put("code", 500);
            result.put("message", "合同文件上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            logger.error("合同文件上传异常", e);
            result.put("code", 500);
            result.put("message", "合同文件上传异常：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 头像上传专用接口
     */
    @PostMapping("/upload/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 2. 验证文件类型（只允许图片格式）
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg") &&
                 !contentType.equals("image/gif") &&
                 !contentType.equals("image/webp"))) {
                result.put("code", 400);
                result.put("message", "只支持图片格式 (jpg、png、jpeg、gif、webp)");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 验证文件大小（不超过 2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过 2MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. 确保上传目录存在（使用绝对路径）
            // 如果是相对路径，转换为项目根目录的绝对路径
            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*") 
                ? System.getProperty("user.dir") + File.separator + uploadPath 
                : uploadPath;
            
            Path uploadDir = Paths.get(absoluteUploadPath, "avatar");
            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
            }

            // 6. 保存文件到服务器
            Path destFilePath = uploadDir.resolve(newFilename);
            file.transferTo(destFilePath.toFile());

            // 7. 返回相对路径（用于前端访问）
            String relativePath = "/upload/avatar/" + newFilename;
            
            logger.info("头像上传成功：{}", destFilePath.toAbsolutePath());
            logger.info("访问路径：{}", relativePath);

            result.put("code", 200);
            result.put("success", true);
            result.put("message", "头像上传成功");
            result.put("data", relativePath);
            result.put("url", relativePath);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("头像上传失败", e);
            result.put("code", 500);
            result.put("message", "头像上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            logger.error("头像上传异常", e);
            result.put("code", 500);
            result.put("message", "头像上传异常：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 监控截图上传专用接口
     */
    @PostMapping("/upload/monitor")
    public ResponseEntity<Map<String, Object>> uploadMonitorSnapshot(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 2. 验证文件类型（只允许 jpg, png）
            String contentType = file.getContentType();
            if (contentType == null || 
                (!contentType.equals("image/jpeg") && 
                 !contentType.equals("image/png") && 
                 !contentType.equals("image/jpg"))) {
                result.put("code", 400);
                result.put("message", "只支持 jpg 和 png 格式");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 验证文件大小（最大 5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                result.put("code", 400);
                result.put("message", "文件大小不能超过 5MB");
                return ResponseEntity.badRequest().body(result);
            }

            // 4. 生成唯一文件名（使用 UUID）
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. 按日期创建子目录 /upload/monitor/日期/
            String absoluteUploadPath = uploadPath.startsWith(".") || !uploadPath.matches("^[a-zA-Z]:.*") 
                ? System.getProperty("user.dir") + File.separator + uploadPath 
                : uploadPath;
            
            // 获取当前日期（格式：2026-04-15）
            String currentDate = java.time.LocalDate.now().toString();
            Path uploadDir = Paths.get(absoluteUploadPath, "monitor", currentDate);
            
            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
                logger.info("创建上传目录：{}", uploadDir.toAbsolutePath());
            }

            // 6. 保存文件到服务器
            Path destFilePath = uploadDir.resolve(newFilename);
            File destFile = destFilePath.toFile();
            file.transferTo(destFile);

            // 7. 返回相对路径（如 /upload/monitor/2026-04-15/abc123.jpg）
            String relativePath = "/upload/monitor/" + currentDate + "/" + newFilename;
            
            logger.info("监控截图上传成功：{}", destFilePath.toAbsolutePath());
            logger.info("访问路径：{}", relativePath);

            result.put("code", 200);
            result.put("success", true);
            result.put("message", "截图上传成功");
            result.put("data", relativePath);
            result.put("url", relativePath);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("监控截图上传失败 - IO 异常", e);
            logger.error("上传路径：{}", uploadPath);
            logger.error("文件原始名称：{}", file.getOriginalFilename());
            logger.error("文件大小：{} bytes", file.getSize());
            result.put("code", 500);
            result.put("message", "截图上传失败：" + e.getMessage() + " - 请检查上传目录是否存在且有写权限");
            return ResponseEntity.internalServerError().body(result);
        } catch (Exception e) {
            logger.error("监控截图上传异常", e);
            logger.error("上传路径：{}", uploadPath);
            result.put("code", 500);
            result.put("message", "截图上传异常：" + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}