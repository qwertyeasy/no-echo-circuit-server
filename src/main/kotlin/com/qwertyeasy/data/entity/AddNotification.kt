package com.qwertyeasy.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.util.concurrent.TimeUnit

@RedisHash
class AddNotification (

    @Id val targetNick: String,

    val notifyAbout: MutableSet<String> = mutableSetOf(),

    @TimeToLive(unit = TimeUnit.DAYS)
    val ttl: Long = 30
){
}