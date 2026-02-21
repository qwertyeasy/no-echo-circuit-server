package com.qwertyeasy.exceptions

import com.qwertyeasy.data.dto.ErrorResponse
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class WebSocketExceptionHandler {

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  fun handleException(ex: Exception): ErrorResponse {
    return ErrorResponse(ex.message!!)
  }
}
