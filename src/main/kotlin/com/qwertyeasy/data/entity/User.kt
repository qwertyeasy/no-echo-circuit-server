package com.qwertyeasy.data.entity

import com.qwertyeasy.data.entity.enums.StatusEnum
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

// TODO настраивать ttl пользователя и статуса вручную через redisTemplate (45 дней и 10 минут?)
@RedisHash("users:profile")
class User(

    @Id val nickname: String,

    var status: StatusEnum = StatusEnum.ONLINE,

    var lastConnect: Instant = Instant.now(),

    val crewNames: MutableSet<String> = mutableSetOf(),

    val fingerprints: MutableList<DeviceFingerprint> = mutableListOf()
)