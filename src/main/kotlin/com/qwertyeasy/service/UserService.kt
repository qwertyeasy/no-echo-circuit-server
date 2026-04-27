package com.qwertyeasy.service

import com.qwertyeasy.data.entity.User

interface UserService {

    fun getOrCreate(nickname: String): User

    fun findUser(nickname: String): User?

    fun addCrewMemberToUser(user: User, friendNickname: String, description: String?): Boolean

    fun removeCrewMember(user: User, removeNickname: String)

    fun checkNotifications(nickname: String): Set<String>

    fun findOnlineCrewMembers(user: User): List<User>
}