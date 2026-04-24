package com.qwertyeasy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NoEchoCircuitApplication

fun main(args: Array<String>) {
    runApplication<NoEchoCircuitApplication>(*args)
}
