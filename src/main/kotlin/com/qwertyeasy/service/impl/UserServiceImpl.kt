package com.qwertyeasy.service.impl

import com.qwertyeasy.data.entity.AddNotification
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.data.entity.enums.StatusEnum
import com.qwertyeasy.repository.NotificationRepository
import com.qwertyeasy.repository.UserRepository
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Optional
import kotlin.jvm.optionals.getOrElse

@Service
class UserServiceImpl (
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
): UserService{

    override fun getUser(nickname: String): User {
        val userOpt = findUser(nickname)

        val user = if(userOpt.isPresent){
            val user = userOpt.get()
            user.lastConnect = Instant.now()
            user.status = StatusEnum.ONLINE
            user
        } else {
            User(nickname)
        }
        return userRepository.save(user)
    }

    override fun findUser(nickname: String): Optional<User> {
        return userRepository.findById(nickname)
    }

    override fun isOnline(nickname: String): Boolean {
        val user = findUser(nickname).get()
        return user.status == StatusEnum.ONLINE
    }

    override fun changeUserStatus(nickname: String, status: StatusEnum) {
        val user = getUser(nickname)
        user.status = status
        userRepository.save(user)
    }

    override fun addCrewMemberToUser(user: User, friendNickname: String, description: String?): Boolean {
        val friend = findUser(friendNickname)
        if(friend.isPresent) {
            user.crewNames.add(friendNickname)
            userRepository.save(user)

            notifyMemberAboutUser(friend.get(), user.nickname, description)
            return true
        }
        return false
    }

    private fun notifyMemberAboutUser(member: User, user: String, description: String?){
        if(!member.crewNames.contains(user)) {
            val notifications = notificationRepository.findById(member.nickname)
                .getOrElse { AddNotification(member.nickname) }
            val notifyMessage = if(description != null){
                                    "$user:$description"
                                } else { user }
            notifications.notifyAbout.add(notifyMessage)
            notificationRepository.save(notifications)
        }
    }

    override fun removeCrewMember(user: User, removeNickname: String) {
        if(user.crewNames.contains(removeNickname)){
            user.crewNames.remove(removeNickname)
        }
    }

    override fun checkNotifications(nickname: String): Set<String> {
        val notificationsOpt = notificationRepository.findById(nickname)
        if(notificationsOpt.isPresent){
            // может быть здесь не нужно удалять их сразу, но пока пусть будет так
            // в случае чего потом можно настроить очистку уведомления только после решения пользователя
            notificationRepository.deleteById(nickname)
            return notificationsOpt.get().notifyAbout
        }
        return emptySet()
    }

    /**
     * Friends must add each other.
     * Only in this coincidence they can see each other online in friends list.
     */
    override fun findOnlineCrewMembers(user: User): List<User> {
        val originNick = user.nickname
        val crewNames = user.crewNames
        return userRepository.findAllById(crewNames)
            .filter { user -> user.status == StatusEnum.ONLINE &&
                    user.crewNames.contains(originNick)
            }
    }
}