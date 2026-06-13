package com.example.futurecommunity.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 活动高并发报名测试
 */
@SpringBootTest
public class ActivityConcurrentTest {

    private static final String BASE_URL = "http://localhost:8080/api/activity";
    private static final Long ACTIVITY_ID = 1L; // 假设活动ID为1
    private static final int THREAD_COUNT = 50; // 并发线程数

    /**
     * 测试高并发报名
     */
    @Test
    public void testConcurrentSignup() throws InterruptedException {
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        // 计数器，用于等待所有线程执行完成
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        // 成功计数
        AtomicInteger successCount = new AtomicInteger(0);
        // 失败计数
        AtomicInteger failureCount = new AtomicInteger(0);

        // 模拟多个用户同时报名
        for (int i = 1; i <= THREAD_COUNT; i++) {
            final Long userId = (long) i;
            executorService.execute(() -> {
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    // 设置认证头，这里使用模拟的token
                    headers.set("Authorization", "Bearer mock-token");
                    HttpEntity<?> entity = new HttpEntity<>(headers);

                    // 发送报名请求
                    String url = BASE_URL + "/signup?activityId=" + ACTIVITY_ID + "&userId=" + userId;
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                        System.out.println("用户 " + userId + " 报名成功");
                    } else {
                        failureCount.incrementAndGet();
                        System.out.println("用户 " + userId + " 报名失败: " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 报名异常: " + e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        // 等待所有线程执行完成
        countDownLatch.await();
        executorService.shutdown();

        // 输出测试结果
        System.out.println("\n=== 高并发报名测试结果 ===");
        System.out.println("总请求数: " + THREAD_COUNT);
        System.out.println("成功数: " + successCount.get());
        System.out.println("失败数: " + failureCount.get());
        System.out.println("成功率: " + (double) successCount.get() / THREAD_COUNT * 100 + "%");
    }
}
