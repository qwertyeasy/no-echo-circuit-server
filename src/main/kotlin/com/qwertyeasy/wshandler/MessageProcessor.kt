package com.qwertyeasy.wshandler

import com.qwertyeasy.data.dto.NotificationData
import com.qwertyeasy.data.dto.enums.MessageType
import com.qwertyeasy.data.dto.SocketMessage
import com.qwertyeasy.data.dto.enums.ResponseType
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.service.RedisTtlService
import com.qwertyeasy.service.SessionService
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import tools.jackson.databind.ObjectMapper
import java.util.logging.Logger

@Component
class MessageProcessor(
    private val userService: UserService,
    private val sessionService: SessionService,
    private val redisTtlService: RedisTtlService,
    private val objectMapper: ObjectMapper
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
            sessionService.sendResponseToSession(
                session, ResponseType.ERROR, null
            )
        }
    }

    private fun handleScanMessage(
        session: WebSocketSession
    ) {
        val data = sessionService.getDataFromSession(session)
        if(data!!.user != null){
            sendOnlineUserList(data.user!!, session)
        } else {
            sessionService.sendResponseToSession(
                session, ResponseType.ERROR, null
            )
        }
    }

    private fun handleAuthMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val user = userService.getOrCreate(socketMsg.payload!!)
        sessionService.saveSessionWithUser(session, user)

        log.info("User ${user.nickname} was login")
        sessionService.sendResponseToSession(
            session, ResponseType.SUCCESS_LOGIN, null
        )

        sendNotifications(user, session)
        sendOnlineUserList(user, session)
    }

    private fun sendNotifications(
        user: User, session: WebSocketSession
    ){
        val notifySet = userService.checkNotifications(user.nickname)
        if(notifySet.isNotEmpty()){
            sessionService.sendResponseToSession(
                session, ResponseType.NOTIFY_ABOUT_ADD,
                objectMapper.writeValueAsString(notifySet)
            )
        }
    }

    private fun sendOnlineUserList(
        user: User, session: WebSocketSession
    ){
        val onlineNamesList = userService.findOnlineCrewMembers(user)
            .map { it.nickname }

        sessionService.sendResponseToSession(
            session, ResponseType.USERS_LIST,
            objectMapper.writeValueAsString(onlineNamesList)
        )
    }

    private fun handleAddMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val user = sessionService.getUserFromSession(session)

        val payload = objectMapper.readValue(socketMsg.payload, NotificationData::class.java)
        val addingUser = payload.userNickname
        val description = payload.description

        if (user != null) {
            log.info("User ${user.nickname} is trying to add user with name ${payload.userNickname}")
            val isAdded = userService.addCrewMemberToUser(user, addingUser, description)

            if (isAdded) {
                log.info("User ${addingUser} successfully added to your contacts")
                sessionService.sendResponseToSession(
                    session, ResponseType.ADD_OK, addingUser
                )
            } else {
                log.warning("Adding user ${addingUser} not exist")
                sessionService.sendResponseToSession(
                    session, ResponseType.ADD_FAIL, addingUser
                )
            }
        } else {
            log.warning("User is trying to add another user without login")
            sessionService.sendResponseToSession(
                session, ResponseType.ERROR, null
            )
        }
    }

    private fun handleConnectMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        if(redisTtlService.isUserOnline(socketMsg.payload!!)) {
            log.info("Somebody requested to connect with user: ${socketMsg.payload}")

            val secondSession = sessionService.findSessionByNickname(socketMsg.payload).get()

            val connectingNickname = sessionService.getUserFromSession(session)!!.nickname
            sessionService.sendResponseToSession(
                secondSession, ResponseType.USER_CONNECTS, connectingNickname
            )

            // проверка прошла успешно, остаётся только обменяться нужными данными между сессиями
        } else {
            log.warning("Somebody requested to connect with offline user: ${socketMsg.payload}")
        }
        // TODO: Дописать работающий коннект друг к другу - обмен кандидатами для webRTC
    }
}