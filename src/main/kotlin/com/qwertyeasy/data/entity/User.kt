package com.qwertyeasy.data.entity

import com.qwertyeasy.data.entity.enums.StatusEnum
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.time.Instant
import java.util.concurrent.TimeUnit

@RedisHash
class User(

    @Id val nickname: String,

    var status: StatusEnum = StatusEnum.ONLINE,

    var lastConnect: Instant = Instant.now(),

    val crewNames: MutableSet<String> = mutableSetOf(),

    val fingerprints: MutableList<DeviceFingerprint> = mutableListOf(),

    @TimeToLive(unit = TimeUnit.DAYS)
    val ttl: Long = 45,
)