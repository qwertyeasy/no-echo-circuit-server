package com.qwertyeasy.service.impl

import com.qwertyeasy.data.dto.NotificationData
import com.qwertyeasy.data.dto.enums.ResponseType
import com.qwertyeasy.data.entity.AddNotification
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.repository.NotificationRepository
import com.qwertyeasy.repository.UserRepository
import com.qwertyeasy.service.RedisTtlService
import com.qwertyeasy.service.SessionService
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@Service
class UserServiceImpl (
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val sessionService: SessionService,
    private val redisTtlService: RedisTtlService,
    private val objectMapper: ObjectMapper
): UserService{

    override fun getOrCreate(nickname: String): User {
        var user = findUser(nickname)

        if(user != null){
            user.lastConnect = Instant.now()
        } else {
            user = User(nickname)
        }
        saveAndResetUserTtl(user)

        return user
    }

    override fun findUser(nickname: String): User? {
        return userRepository.findById(nickname).getOrNull()
    }

    override fun saveAndResetUserTtl(user: User){
        userRepository.save(user)
        redisTtlService.markUserAsActive(user.nickname)
    }

    override fun addCrewMemberToUser(user: User, friendNickname: String, description: String?): Boolean {
        val friend = findUser(friendNickname)
        if(friend != null) {
            user.crewNames.add(friendNickname)

            saveAndResetUserTtl(user)

            notifyMemberAboutUser(friend, user.nickname, description)
            return true
        }
        return false
    }

    private fun notifyMemberAboutUser(member: User, user: String, description: String?){
        if(!member.crewNames.contains(user)) {
            // может быть пересмотреть работу с нотификациями, чтобы вынести прямую работу с сессиями из этого класса
            val memberSessionOpt = sessionService.findSessionByNickname(member.nickname)

            val notifyData = NotificationData(user, description)

            if(memberSessionOpt.isPresent && redisTtlService.isUserOnline(member.nickname)){
                val session = memberSessionOpt.get()
                sessionService.sendResponseToSession(
                    session, ResponseType.NOTIFY_ABOUT_ADD,
                    objectMapper.writeValueAsString(setOf(notifyData))
                )

            } else {
                val notifications = notificationRepository.findById(member.nickname)
                    .getOrElse { AddNotification(member.nickname) }

                notifications.notifyAbout.add(notifyData)

                notificationRepository.save(notifications)
                redisTtlService.renewNotificationTtl(member.nickname)
            }
        }
    }

    override fun removeCrewMember(user: User, removeNickname: String) {
        if(user.crewNames.contains(removeNickname)){
            user.crewNames.remove(removeNickname)
            saveAndResetUserTtl(user)
        }
    }

    // TODO: перевести findById с Optional на ?
    override fun checkNotifications(nickname: String): Set<NotificationData> {
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
        return userRepository.findAllById(user.crewNames)
            .filter { member ->
                redisTtlService.isUserOnline(member.nickname) &&
                        member.crewNames.contains(user.nickname)
        }
    }
}