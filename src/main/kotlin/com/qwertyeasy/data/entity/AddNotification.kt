package com.qwertyeasy.data.entity

import com.qwertyeasy.data.dto.NotificationData
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value="notifications")
class AddNotification (

    @Id val targetNick: String,

    /**
     * Each notification can contain nickname of user that added you and description about who adds,
     * if user added it. So notification can be like 'username' or 'username:description'
     */
    val notifyAbout: MutableSet<NotificationData> = mutableSetOf(),
){
}