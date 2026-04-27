package com.qwertyeasy.service

interface RedisTtlService {

    fun markUserAsActive(nickname: String)

    fun markUserAsOffline(nickname: String)

    fun isUserOnline(nickname: String): Boolean

    fun renewNotificationTtl(nickname: String)
}