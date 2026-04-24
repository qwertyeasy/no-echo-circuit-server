package com.qwertyeasy.wshandler

import com.qwertyeasy.data.dto.MessageType
import com.qwertyeasy.data.dto.SessionData
import com.qwertyeasy.data.dto.SocketMessage
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.logging.Logger

@Component
class MessageProcessor(
    private val userService: UserService
) {
    private val log = Logger.getLogger(MessageProcessor::class.java.name)

    fun handleMessageByType(
        socketMsg: SocketMessage, session: WebSocketSession,
        sessions: Map<WebSocketSession, SessionData>
    ) {
        when (socketMsg.type) {
            MessageType.AUTH -> handleAuthMessage(socketMsg, session, sessions)
            MessageType.ADD -> handleAddMessage(socketMsg, session, sessions)
            MessageType.REMOVE -> handleRemoveMessage(socketMsg, session, sessions)
            MessageType.CONNECT -> handleConnectMessage(socketMsg, session, sessions)
            MessageType.SCAN -> handleScanMessage(session, sessions)
        }
    }

    private fun handleRemoveMessage(
        socketMsg: SocketMessage, session: WebSocketSession,
        sessions: Map<WebSocketSession, SessionData>
    ){
        val data = sessions[session]
        if(data!!.user != null) {
            userService.removeCrewMember(data.user!!, socketMsg.payload!!)
        } else {
            session.sendMessage(TextMessage("Have to login before removing"))
        }
    }

    private fun handleScanMessage(
        session: WebSocketSession, sessions: Map<WebSocketSession, SessionData>
    ) {
        val data = sessions[session]
        if(data!!.user != null){
            sendOnlineUserList(data.user!!, session)
        } else {
            session.sendMessage(TextMessage("Have to login before scanning"))
        }
    }

    private fun handleAuthMessage(
        socketMsg: SocketMessage, session: WebSocketSession,
        sessions: Map<WebSocketSession, SessionData>
    ){
        val user = userService.getUser(socketMsg.payload!!)
        log.info("User ${user.nickname} was login")
        sessions[session]?.user = user
        session.sendMessage(TextMessage("Login with nickname: ${user.nickname}"))

        sendOnlineUserList(user, session)
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
        socketMsg: SocketMessage, session: WebSocketSession,
        sessions: Map<WebSocketSession, SessionData>
    ){
        val user = sessions[session]?.user
        if (user != null) {
            log.info("User ${user.nickname} is trying to add user with name ${socketMsg.payload}")
            val isAdded = userService.addCrewMemberToUser(user, socketMsg.payload!!)
            if (isAdded) {
                log.info("User ${socketMsg.payload} successfully added to your contacts")
                session.sendMessage(TextMessage("User ${socketMsg.payload} was added to your contacts"))
            } else {
                log.warning("Adding user ${socketMsg.payload} not exist")
                session.sendMessage(TextMessage("User ${socketMsg.payload} was not found"))
            }
        } else {
            log.warning("User is trying to add another user without login")
            session.sendMessage(TextMessage("Have to login before requesting anyone"))
        }
    }

    private fun handleConnectMessage(
        socketMsg: SocketMessage, session: WebSocketSession,
        sessions: Map<WebSocketSession, SessionData>
    ){
        val secondSession = if(userService.isOnline(socketMsg.payload!!)) {
                sessions.filterValues { data -> data.user?.nickname.equals(socketMsg.payload) }
            // тут надо настроить правильную фильтрацию
        } else {
            log.warning("Somebody requested to connect with not existing user")
        }
        TODO("Дописать работающий коннект друг к другу - обмен кандидатами для webRTC")
    }
}