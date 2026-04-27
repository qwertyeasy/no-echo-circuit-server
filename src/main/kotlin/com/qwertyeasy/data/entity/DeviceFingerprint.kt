package com.qwertyeasy.data.entity

import org.springframework.data.redis.core.RedisHash

// может быть здесь не нужен hash, если я храню их у пользователя
// или мне нужно только хранить хэши заблокированных устройств, а при создании пользователя
// проверять его устройство по этому списку заблокированных
@RedisHash
class DeviceFingerprint (

    val device: String,

    val agent: String,

    val macAddress: String?,

    val hardwareHash: Integer?,

    val instanceId: String?
)