package com.qwertyeasy.data.dto

import com.qwertyeasy.data.dto.enums.ResponseType

data class ResponseMessage(
    var type: ResponseType,
    val payload: String?
)
