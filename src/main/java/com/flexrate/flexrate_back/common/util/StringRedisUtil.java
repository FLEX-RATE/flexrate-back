package com.flexrate.flexrate_back.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class StringRedisUtil {
    private final StringRedisTemplate template;

    public String get(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public void set(String key, String value, Duration timeout) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        valueOperations.set(key, value, timeout);
    }

    public void delete(String key) {
        template.delete(key);
    }

    public String getAndExpire(String key, Duration timeout) {
        return template.opsForValue().getAndExpire(key, timeout);
    }
}
