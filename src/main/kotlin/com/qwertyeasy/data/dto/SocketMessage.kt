package com.qwertyeasy.data.dto

data class SocketMessage(
    var type: MessageType,
    val payload: String?
)