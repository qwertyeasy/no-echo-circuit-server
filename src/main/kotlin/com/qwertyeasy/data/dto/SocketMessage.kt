package com.qwertyeasy.data.dto

import com.qwertyeasy.data.dto.enums.MessageType

data class SocketMessage(
    var type: MessageType,
    val payload: String?
)