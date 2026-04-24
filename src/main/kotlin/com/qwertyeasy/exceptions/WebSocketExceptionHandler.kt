package com.qwertyeasy.exceptions

import com.qwertyeasy.exceptions.ErrorResponse
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice

//TODO: Проверить как это вообще работает и надо ли оно мне

@ControllerAdvice
class WebSocketExceptionHandler {

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  fun handleException(ex: Exception): ErrorResponse {
    return ErrorResponse(ex.message!!)
  }
}
