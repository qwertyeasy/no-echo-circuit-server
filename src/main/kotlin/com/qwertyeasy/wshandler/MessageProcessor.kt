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
            MessageType.ANSWER -> handleAnswerMessage(socketMsg)
            MessageType.ICE -> handleIceMessage(socketMsg)
        }
    }

    private fun handleIceMessage(
        socketMsg: SocketMessage
    ){
        val payloadJson = objectMapper.readTree(socketMsg.payload)
        val to = payloadJson["to"].stringValue()

        val sessionTo = sessionService.findSessionByNickname(to)
        if (sessionTo.isPresent) {
            sessionService.sendResponseToSession(
                sessionTo.get(), ResponseType.ICE, socketMsg.payload
            )
        }
    }

    private fun handleRemoveMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val data = sessionService.getDataFromSession(session)
        if(data!!.user != null) {
            userService.removeCrewMember(data.user!!, socketMsg.payload!!)
            sendOnlineUserList(data.user!!, session)
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
            log.info("User ${data.user!!.nickname} send online list refreshing request")
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
                log.info("User ${user.nickname} successfully added $addingUser to its contacts")
                sessionService.sendResponseToSession(
                    session, ResponseType.ADD_OK, addingUser
                )
                sendOnlineUserList(user, session)
            } else {
                log.warning("Adding user $addingUser not exist")
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

    //TODO: понять, нужно ли выполнять двойную проверку - сначала по онлайну, а потом по сессии.
    // По сути, если сессии корректно очищаются, то не должно возникнуть ситуации,
    // при которой сессия присутствует, но пользователь не активен.

    private fun handleConnectMessage(
        socketMsg: SocketMessage, session: WebSocketSession
    ){
        val requestingUser = objectMapper.readTree(socketMsg.payload)["to"].stringValue()
        log.info("Somebody requested to connect with user: $requestingUser")

//        if(redisTtlService.isUserOnline(requestingUser)) {

            val secondSessionOpt = sessionService.findSessionByNickname(requestingUser)
            if (secondSessionOpt.isPresent) {
                sessionService.sendResponseToSession(
                    secondSessionOpt.get(),
                    ResponseType.ANSWER_REQUEST, socketMsg.payload
                )
            } else {
                log.warning("Requested session was not found")
                sessionService.sendResponseToSession(
                    session, ResponseType.USER_OFFLINE, requestingUser
                )
            }
//        } else {
//            log.warning("Somebody requested to connect with offline user: $requestingUser")
//            sessionService.sendResponseToSession(
//                session, ResponseType.USER_OFFLINE, requestingUser
//            )
//        }
    }

    private fun handleAnswerMessage(
        socketMsg: SocketMessage
    ){
        val to = objectMapper.readTree(socketMsg.payload)["to"].stringValue()
        val receivingSessionOpt = sessionService.findSessionByNickname(to)

        if (receivingSessionOpt.isPresent) {
            val receivingSession = receivingSessionOpt.get()

            sessionService.sendResponseToSession(
                receivingSession, ResponseType.ANSWER_RESPONSE, socketMsg.payload
            )
        }
    }
}