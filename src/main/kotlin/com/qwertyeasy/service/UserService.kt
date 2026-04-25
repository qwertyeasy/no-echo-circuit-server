package com.qwertyeasy.service

import com.qwertyeasy.data.entity.User
import com.qwertyeasy.data.entity.enums.StatusEnum
import java.util.Optional

interface UserService {

    fun getUser(nickname: String): User

    fun findUser(nickname: String): Optional<User>

    fun isOnline(nickname: String): Boolean

    fun changeUserStatus(nickname: String, status: StatusEnum)

    fun addCrewMemberToUser(user: User, friendNickname: String, description: String?): Boolean

    fun removeCrewMember(user: User, removeNickname: String)

    fun checkNotifications(nickname: String): Set<String>

    fun findOnlineCrewMembers(user: User): List<User>
}