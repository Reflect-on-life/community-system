package com.example.futurecommunity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.futurecommunity.model.Repair;
import com.example.futurecommunity.service.RepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class SatisfactionStatisticsController {

    @Autowired
    private RepairService repairService;

    @GetMapping("/satisfaction")
    public Map<String, Object> getSatisfactionStatistics() {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Repair::getRating);
        List<Repair> repairs = repairService.list(wrapper);

        int total = repairs.size();
        double avgScore = 0.0;
        List<Map<String, Object>> scoreDistribution = new ArrayList<>();

        if (total > 0) {
            int sum = 0;
            Map<Integer, Integer> countMap = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                countMap.put(i, 0);
            }

            for (Repair repair : repairs) {
                int rating = repair.getRating();
                if (rating >= 1 && rating <= 5) {
                    sum += rating;
                    countMap.put(rating, countMap.get(rating) + 1);
                }
            }

            avgScore = (double) sum / total;

            for (int i = 1; i <= 5; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("score", i);
                item.put("count", countMap.get(i));
                scoreDistribution.add(item);
            }
        } else {
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("score", i);
                item.put("count", 0);
                scoreDistribution.add(item);
            }
        }

        result.put("total", total);
        result.put("avgScore", Math.round(avgScore * 10.0) / 10.0);
        result.put("scoreDistribution", scoreDistribution);

        return result;
    }
}
