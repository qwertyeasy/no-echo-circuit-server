package com.qwertyeasy.wshandler

import com.qwertyeasy.data.dto.MessageType
import com.qwertyeasy.data.dto.SocketMessage
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.service.RedisTtlService
import com.qwertyeasy.service.SessionService
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.logging.Logger

@Component
class MessageProcessor(
    private val userService: UserService,
    private val sessionService: SessionService,
    private val redisTtlService: RedisTtlService
) {
    private val log = Logger.getLogger(MessageProcessor::class.java.name)

    fun handleMessageByType(
        socketMsg: SocketMessage, session: WebSocketSession
    ) {
        when (socketMsg.type) {
            MessageType.AUTH -> handleAuthMessage(socketMsg, session)
            MessageType.ADD -> handleAddMessage(socketMsg, session)
            MessageType.REMOVE -> handleRemoveMessage(socketMsg, session)
            MessageType.CONNECT -> handleConnectMessage(socketMsg, session)
            MessageType.SCAN -> handleScanMessage(session)
        }
    }

    private fun handleRemoveMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val data = sessionService.getDataFromSession(session)
        if(data!!.user != null) {
            userService.removeCrewMember(data.user!!, socketMsg.payload!!)
        } else {
            session.sendMessage(TextMessage("Have to login before removing"))
        }
    }

    private fun handleScanMessage(
        session: WebSocketSession
    ) {
        val data = sessionService.getDataFromSession(session)
        if(data!!.user != null){
            sendOnlineUserList(data.user!!, session)
        } else {
            session.sendMessage(TextMessage("Have to login before scanning"))
        }
    }

    private fun handleAuthMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val user = userService.getOrCreate(socketMsg.payload!!)
        sessionService.saveSessionWithUser(session, user)

        log.info("User ${user.nickname} was login")
        session.sendMessage(TextMessage("Login with nickname: ${user.nickname}"))

        sendNotifications(user, session)
        sendOnlineUserList(user, session)
    }

    private fun sendNotifications(
        user: User, session: WebSocketSession
    ){
        val notifySet = userService.checkNotifications(user.nickname)
        if(notifySet.isNotEmpty()){
            session.sendMessage(TextMessage("Your contact was saved: ${notifySet.toString()}"))
        }
    }

    private fun sendOnlineUserList(
        user: User, session: WebSocketSession
    ){
        val onlineMembers = userService.findOnlineCrewMembers(user)
        val namesList = onlineMembers.stream()
            .map { user -> user.nickname }
            .toList()
        session.sendMessage(TextMessage(namesList.toString()))
    }

    private fun handleAddMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val user = sessionService.getUserFromSession(session)

        val split = socketMsg.payload!!.split(":")
        val addingUser = split[0]
        val description = if(split.size == 2){
            split[1]
        } else { null }

        if (user != null) {
            log.info("User ${user.nickname} is trying to add user with name ${addingUser}")
            val isAdded = userService.addCrewMemberToUser(user, addingUser, description)

            if (isAdded) {
                log.info("User ${addingUser} successfully added to your contacts")
                session.sendMessage(TextMessage("User ${addingUser} was added to your contacts"))
            } else {
                log.warning("Adding user ${addingUser} not exist")
                session.sendMessage(TextMessage("User ${addingUser} was not found"))
            }
        } else {
            log.warning("User is trying to add another user without login")
            session.sendMessage(TextMessage("Have to login before requesting anyone"))
        }
    }

    private fun handleConnectMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        if(redisTtlService.isUserOnline(socketMsg.payload!!)) {
            val secondSession = sessionService.findSessionByNickname(socketMsg.payload).get()

            session.sendMessage(TextMessage("Запрошена сессия c пользователем ${socketMsg.payload}"))
            secondSession.sendMessage(TextMessage("С вами пытается связаться пользователь ${sessionService.getUserFromSession(session)!!.nickname}"))

            // проверка прошла успешно, остаётся только обменяться нужными данными между сессиями
        } else {
            log.warning("Somebody requested to connect with not existing user")
        }
        // TODO: Дописать работающий коннект друг к другу - обмен кандидатами для webRTC
    }
}