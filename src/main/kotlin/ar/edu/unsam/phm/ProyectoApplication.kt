package ar.edu.unsam.phm

import ar.edu.unsam.phm.bootstrap.Bootstrap
import ar.edu.unsam.phm.repository.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
class ProyectoApplication {

    @Bean
    fun seedOnStartup(
        @Qualifier("repoUsuarios") usuarios: RepositorioUsuarios,
        @Qualifier("repoLibros") libros: RepositorioLibros,
        @Qualifier("repoReservas") reservas: RepositorioReservas,
        @Qualifier("repoResenias") resenias: RepositorioResenias,

    ) = CommandLineRunner {
        if (usuarios.elementos.isEmpty()) {
            Bootstrap.cargarInfo(usuarios, libros, reservas, resenias)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ProyectoApplication>(*args)
}
