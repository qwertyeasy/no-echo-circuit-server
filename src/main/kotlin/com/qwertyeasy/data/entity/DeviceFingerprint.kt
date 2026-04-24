package com.qwertyeasy.data.entity

import org.springframework.data.redis.core.RedisHash

@RedisHash
class DeviceFingerprint (

    val device: String,

    val agent: String,

    val macAddress: String?,

    val hardwareHash: Integer?,

    val instanceId: String?
)