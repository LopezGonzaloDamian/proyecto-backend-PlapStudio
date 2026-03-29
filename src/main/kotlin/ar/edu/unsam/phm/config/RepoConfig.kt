package ar.edu.unsam.phm.config

import ar.edu.unsam.phm.repository.RepositorioLibros
import ar.edu.unsam.phm.repository.RepositorioReservas
import ar.edu.unsam.phm.repository.RepositorioResenias
import ar.edu.unsam.phm.repository.RepositorioUsuarios
import ar.edu.unsam.phm.repository.SearchLibro
import ar.edu.unsam.phm.repository.SearchReserva
import ar.edu.unsam.phm.repository.SearchResenia
import ar.edu.unsam.phm.repository.SearchUsuario
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepoConfig {

    @Bean("repoUsuarios")
    fun repoUsuarios() = RepositorioUsuarios(SearchUsuario())

    @Bean("repoLibros")
    fun repoLibros() = RepositorioLibros(SearchLibro())

    @Bean("repoReservas")
    fun repoReservas() = RepositorioReservas(SearchReserva())

    @Bean("repoResenias")
    fun repoResenias() = RepositorioResenias(SearchResenia())

}
