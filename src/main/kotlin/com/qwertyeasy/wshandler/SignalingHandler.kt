package com.qwertyeasy.wshandler

import com.qwertyeasy.data.dto.SessionData
import com.qwertyeasy.data.dto.SocketMessage
import com.qwertyeasy.data.entity.enums.StatusEnum
import com.qwertyeasy.service.UserService
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

@Component
class SignalingHandler(
    private val userService: UserService,
    private val messageProcessor: MessageProcessor,
    private val objectMapper: ObjectMapper
): TextWebSocketHandler() {

    @PreDestroy
    private fun closeOpenSessions(){
        sessionsMap.entries
            .map { (key, _) -> key }
            .forEach { session -> session.close(CloseStatus.SERVER_ERROR) }
    }

    private val log = Logger.getLogger(SignalingHandler::class.java.name)
    private val sessionsMap = ConcurrentHashMap<WebSocketSession, SessionData>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        session.sendMessage(TextMessage("Connection established"))
        log.info("Connection established for session: ${session.id}")

        sessionsMap[session] = SessionData(null)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("Connection closed for session: ${session.id}, status: $status")
        val user = sessionsMap[session]?.user
        if(user != null) {
            userService.changeUserStatus(user.nickname, StatusEnum.OFFLINE)
        }
        sessionsMap.remove(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val msg = message.payload
        log.info("Got a message from session ${session.id}: $message")
        val socketMsg = objectMapper.readValue(msg, SocketMessage::class.java)

        messageProcessor.handleMessageByType(socketMsg, session, sessionsMap)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.warning("Session with id ${session.id} fell with exception: ${exception.message}")
        sessionsMap.remove(session)
    }
}