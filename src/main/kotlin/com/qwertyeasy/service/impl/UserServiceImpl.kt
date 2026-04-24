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

    override fun addCrewMemberToUser(user: User, friendNickname: String): Boolean {
        val friend = findUser(friendNickname)
        if(friend.isPresent) {
            user.crewNames.add(friendNickname)
            userRepository.save(user)

            notifyMemberAboutUser(friend.get().nickname, user.nickname)
            return true
        }
        return false
        // продумать возврат другу уведомления о том, что его добавил такой то человек
        // возможно сразу после добавления проверка о том, есть ли у искомого человека тот, который запросил
        // если нет, то передаем в redis, что такого то пользователя надо уведомить о том, что на него подписался такой-то человек
        // и спросить, хочет ли он добавить его в ответ
        // в любом случае после запись удаляется
    }

    private fun notifyMemberAboutUser(member: String, user: String){
        // преждевременно надо добавить проверку
        // может быть этот пользователь уже добавлен в друзья с обратной стороны

        val notifications = notificationRepository.findById(member)
                .getOrElse{ AddNotification(member) }
            notifications.notifyAbout.add(user)
            notificationRepository.save(notifications)

        TODO()
        // нужно добавить функцию после которой залогинившийся пользователь будет искать
        // записи в таблице нотификаций и узнавать о добавлении в список другим человеком
    }

    override fun removeCrewMember(user: User, removeNickname: String) {
        if(user.crewNames.contains(removeNickname)){
            user.crewNames.remove(removeNickname)
        }
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

    override fun changeUserStatus(nickname: String, status: StatusEnum) {
        val user = getUser(nickname)
        user.status = status
        userRepository.save(user)
    }
}