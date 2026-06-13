package com.example.futurecommunity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.futurecommunity.exception.ExpressException;
import com.example.futurecommunity.mapper.ExpressMapper;
import com.example.futurecommunity.model.Express;
import com.example.futurecommunity.service.ExpressService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ExpressServiceImpl extends ServiceImpl<ExpressMapper, Express> implements ExpressService {

    @Override
    public Express createExpressWithPickupCode(Express express) {
        express.setPickupCode(generatePickupCode());
        express.setStatus("in");
        express.setReceivedAt(new Date());
        save(express);
        return express;
    }

    @Override
    public Express pickupExpress(String pickupCode) {
        // 验证取件码是否存在
        Express express = lambdaQuery().eq(Express::getPickupCode, pickupCode).one();
        if (express == null) {
            throw new ExpressException("EXPRESS_NOT_FOUND", "取件码不存在");
        }
        
        // 验证快递状态是否为"in"（已取件返回错误）
        if ("picked".equals(express.getStatus())) {
            throw new ExpressException("EXPRESS_ALREADY_PICKED", "该快递已被取件");
        }
        
        // 更新状态为已取件
        express.setStatus("picked");
        express.setPickedAt(new Date());
        updateById(express);
        
        return express;
    }

    @Override
    public List<Express> listByUserId(Long userId) {
        return lambdaQuery().eq(Express::getUserId, userId).list();
    }

    @Override
    public List<Map<String, Object>> getDailyInboundCount(LocalDateTime start, LocalDateTime end) {
        return baseMapper.getDailyInboundCount(start, end);
    }

    @Override
    public List<Map<String, Object>> getDailyOutboundCount(LocalDateTime start, LocalDateTime end) {
        return baseMapper.getDailyOutboundCount(start, end);
    }

    private String generatePickupCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
