package com.qwertyeasy.data.entity

import com.qwertyeasy.data.entity.enums.StatusEnum
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.Instant

@RedisHash
class User(
    @Id val nickname: String,
    var status: StatusEnum,
    var lastConnect: Instant,
    val crewNames: MutableList<String> = mutableListOf()
)