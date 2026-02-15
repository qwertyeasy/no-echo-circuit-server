package com.qwertyeasy.service

import com.qwertyeasy.data.entity.User
import com.qwertyeasy.data.entity.enums.StatusEnum

interface UserService {

    fun saveNewUser(nickname: String): User

    fun getUser(nickname: String): User

    fun changeUserStatus(nickname: String, status: StatusEnum)

    fun deleteUser(nickname: String)

    fun addCrewMemberToUser(userNickname: String, friendNickname: String)
}