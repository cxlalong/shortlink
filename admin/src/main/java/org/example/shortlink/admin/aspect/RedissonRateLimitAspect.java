package org.example.shortlink.admin.aspect;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.shortlink.admin.annotation.RateLimit;
import org.example.shortlink.admin.common.biz.user.UserContext;
import org.example.shortlink.admin.common.convention.exception.ClientException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RedissonRateLimitAspect {

    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        if(redissonClient == null) {
            throw new RuntimeException("限流器无法使用！");
        }
    }
    @Before("@annotation(rateLimit)")
    public void rateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        if(rateLimit == null) {
            return;
        }
        RRateLimiter rateLimiter = getRateLimiter(generateRateLimiterKey(joinPoint), rateLimit);
        if (!rateLimiter.tryAcquire(1, rateLimit.time(), TimeUnit.MILLISECONDS)) {
            throw new ClientException("当前服务器忙碌，请稍后再试");
        }
    }

    private RRateLimiter getRateLimiter(String key, RateLimit rateLimit) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        if(!rateLimiter.isExists()) {
            rateLimiter.trySetRate(
                    RateType.OVERALL,
                    rateLimit.value(),
                    rateLimit.time(),
                    RateIntervalUnit.MILLISECONDS);
            return rateLimiter;
        }
        if(rateLimiter.getConfig().getRate() != rateLimit.value() ||
                rateLimiter.getConfig().getRateInterval() != rateLimit.time()) {
            rateLimiter.setRate(
                    RateType.OVERALL,
                    rateLimit.value(),
                    rateLimit.time(),
                    RateIntervalUnit.MILLISECONDS);
        }
        return rateLimiter;
    }

    private String generateRateLimiterKey(JoinPoint joinPoint) {
        // 生成限流器的唯一键，可以基于方法名、参数等生成
        // 这里仅以方法名作为示例
        return UserContext.getUsername() + "rateLimiter:" + joinPoint.getSignature().getName();
    }

}
