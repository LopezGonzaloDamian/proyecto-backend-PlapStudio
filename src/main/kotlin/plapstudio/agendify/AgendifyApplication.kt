package plapstudio.agendify

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AgendifyApplication

fun main(args: Array<String>) {
    runApplication<AgendifyApplication>(*args)
}
