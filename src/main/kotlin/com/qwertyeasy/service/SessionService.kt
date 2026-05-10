package com.qwertyeasy.service

import com.qwertyeasy.data.dto.SessionData
import com.qwertyeasy.data.dto.enums.ResponseType
import com.qwertyeasy.data.entity.User
import org.springframework.web.socket.WebSocketSession
import java.util.Optional

interface SessionService {

    fun getUserFromSession(session: WebSocketSession): User?

    fun getDataFromSession(session: WebSocketSession): SessionData?

    fun findSessionByNickname(nickname: String): Optional<WebSocketSession>

    fun saveSessionWithUser(session: WebSocketSession, user: User)

    fun sendResponseToSession(session: WebSocketSession, type: ResponseType, payload: String?)

    fun removeSession(session: WebSocketSession)

    fun closeOpenSessions()
}