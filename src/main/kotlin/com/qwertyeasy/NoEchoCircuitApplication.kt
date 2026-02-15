package com.qwertyeasy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
class NoEchoCircuitApplication

fun main(args: Array<String>) {
    runApplication<NoEchoCircuitApplication>(*args)
}
