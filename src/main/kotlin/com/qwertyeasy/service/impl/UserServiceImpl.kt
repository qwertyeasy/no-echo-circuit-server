package com.qwertyeasy.service.impl

import com.qwertyeasy.data.entity.User
import com.qwertyeasy.data.entity.enums.StatusEnum
import com.qwertyeasy.repository.UserRepository
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserServiceImpl (
    private val userRepository: UserRepository
): UserService{

    override fun saveNewUser(nickname: String): User {
        val newUser = User(nickname, StatusEnum.ONLINE, Instant.now())
        return userRepository.save(newUser)
    }

    override fun getUser(nickname: String): User {
        return userRepository.findById(nickname).orElseThrow()
    }

    override fun deleteUser(nickname: String) {
        userRepository.deleteById(nickname)
    }

    override fun addCrewMemberToUser(userNickname: String, friendNickname: String) {
        val user = getUser(userNickname)
        user.crewNames.add(friendNickname)
        userRepository.save(user)
    }

    override fun changeUserStatus(nickname: String, status: StatusEnum) {
        val user = getUser(nickname)
        user.status = status
        userRepository.save(user)
    }
}