package com.qwertyeasy.service

import com.qwertyeasy.data.dto.NotificationData
import com.qwertyeasy.data.entity.User

interface UserService {

    fun saveAndResetUserTtl(user: User)

    fun getOrCreate(nickname: String): User

    fun findUser(nickname: String): User?

    fun addCrewMemberToUser(user: User, friendNickname: String, description: String?): Boolean

    fun removeCrewMember(user: User, removeNickname: String)

    fun checkNotifications(nickname: String): Set<NotificationData>

    fun findOnlineCrewMembers(user: User): List<User>
}