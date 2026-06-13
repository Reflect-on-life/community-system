package com.example.futurecommunity.test;

import com.example.futurecommunity.model.Activity;
import com.example.futurecommunity.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 活动服务高并发测试
 * 直接调用ActivityService方法，绕过HTTP认证
 */
@SpringBootTest
public class ActivityServiceConcurrentTest {

    @Autowired
    private ActivityService activityService;

    private static final Long ACTIVITY_ID = 1L;
    private static final int THREAD_COUNT = 50;

    @BeforeEach
    public void setup() {
        // 确保活动存在并设置足够的名额
        Activity activity = new Activity();
        activity.setId(ACTIVITY_ID);
        activity.setTitle("测试活动");
        activity.setDescription("测试高并发报名");
        activity.setQuota(20); // 设置20个名额
        activity.setStatus(1); // 活动状态为进行中
        activityService.publishActivity(activity);
    }

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
                    // 直接调用服务方法
                    activityService.signupActivity(ACTIVITY_ID, userId);
                    successCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 报名成功");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("用户 " + userId + " 报名失败: " + e.getMessage());
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
        
        // 验证最终报名人数是否等于活动名额
        Activity activity = activityService.getActivityById(ACTIVITY_ID);
        System.out.println("\n=== 活动最终状态 ===");
        System.out.println("活动ID: " + activity.getId());
        System.out.println("活动标题: " + activity.getTitle());
        System.out.println("活动名额: " + activity.getQuota());
        System.out.println("已报名人数: " + activity.getEnrolledCount());
    }
}
