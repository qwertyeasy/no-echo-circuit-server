package com.qwertyeasy.wshandler

import com.qwertyeasy.data.dto.SocketMessage
import com.qwertyeasy.data.dto.enums.ResponseType
import com.qwertyeasy.service.RedisTtlService
import com.qwertyeasy.service.SessionService
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.logging.Logger

@Component
class SignalingHandler(
    private val sessionService: SessionService,
    private val messageProcessor: MessageProcessor,
    private val redisTtlService: RedisTtlService,
    private val objectMapper: ObjectMapper
): TextWebSocketHandler() {

    private val log = Logger.getLogger(SignalingHandler::class.java.name)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessionService.sendResponseToSession(session, ResponseType.SERVER_CONNECT, null)
        log.info("Connection established for session: ${session.id}")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("Connection closed for session: ${session.id}, status: $status")
        val user = sessionService.getUserFromSession(session)
        if(user != null) {
            redisTtlService.markUserAsOffline(user.nickname)
        }
        sessionService.removeSession(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val msg = message.payload
        log.info("Got a message from session ${session.id}: $message")
        val socketMsg = objectMapper.readValue(msg, SocketMessage::class.java)

        messageProcessor.handleMessageByType(socketMsg, session)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        log.warning("Session with id ${session.id} fell with exception: ${exception.message}")
        sessionService.removeSession(session)
    }

    @PreDestroy
    private fun destroyBean(){
        sessionService.closeOpenSessions()
    }
}