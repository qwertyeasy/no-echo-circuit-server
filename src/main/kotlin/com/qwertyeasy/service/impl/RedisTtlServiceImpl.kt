package com.qwertyeasy.service.impl

import com.qwertyeasy.service.RedisTtlService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisTtlServiceImpl (
    private val redisTemplate: RedisTemplate<String, Any>,
    @Value($$"${redis.ttl.user-days}") val profileTTL: Long,
    @Value($$"${redis.ttl.online-mins}") val onlineTTL: Long,
    @Value($$"${redis.ttl.notification-days}") val notificationTTL: Long
): RedisTtlService{

    override fun markUserAsActive(nickname: String) {
        redisTemplate.expire("users:profile:${nickname}",
            profileTTL, TimeUnit.DAYS)
        redisTemplate.opsForValue().set("users:online:${nickname}",
            "", onlineTTL, TimeUnit.MINUTES)
    }

    override fun renewNotificationTtl(nickname: String) {
        redisTemplate.expire("notifications:${nickname}",
            notificationTTL, TimeUnit.DAYS)
    }

    override fun markUserAsOffline(nickname: String) {
        redisTemplate.delete("users:online:${nickname}")
    }

    override fun isUserOnline(nickname: String): Boolean {
        return redisTemplate.hasKey("users:online:${nickname}")
    }
}