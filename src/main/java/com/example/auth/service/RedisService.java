package com.example.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String USER_STATUS_KEY_PREFIX = "UserStatus:";
    private static final long DEFAULT_TTL_MINUTES = 60;

    public void saveUserStatus(Long userId, String status) {
        String key = USER_STATUS_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, status, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    public void saveUserStatus(Long userId, String status, long ttlMinutes) {
        String key = USER_STATUS_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, status, ttlMinutes, TimeUnit.MINUTES);
    }

    public void deleteUserStatus(Long userId) {
        String key = USER_STATUS_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public String getUserStatus(Long userId) {
        String key = USER_STATUS_KEY_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
}
