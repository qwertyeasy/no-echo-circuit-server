package com.qwertyeasy.service.impl

import com.qwertyeasy.data.dto.SessionData
import com.qwertyeasy.data.entity.User
import com.qwertyeasy.service.SessionService
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionServiceImpl(): SessionService {

    val sessionsMap = ConcurrentHashMap<WebSocketSession, SessionData>()

    override fun getUserFromSession(session: WebSocketSession): User? {
        return sessionsMap[session]?.user
    }

    override fun getDataFromSession(session: WebSocketSession): SessionData? {
        return sessionsMap[session]
    }

    override fun saveSessionWithUser(session: WebSocketSession, user: User) {
        sessionsMap[session] = SessionData(user)
    }

    override fun removeSession(session: WebSocketSession) {
        sessionsMap.remove(session)
    }

    override fun findSessionByNickname(nickname: String): Optional<WebSocketSession> {
        return sessionsMap.entries.stream()
            .filter { entry ->
                entry.value.user!!.nickname == nickname }
            .map{ entry -> entry.key }
            .findFirst()
    }

    override fun closeOpenSessions() {
        sessionsMap.entries
            .map { (key, _) -> key }
            .forEach { session -> session.close(CloseStatus.SERVER_ERROR) }
    }
}