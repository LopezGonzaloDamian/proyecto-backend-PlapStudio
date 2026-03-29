package plapstudio.agendify.config

import plapstudio.agendify.repository.RepositorioLibros
import plapstudio.agendify.repository.RepositorioReservas
import plapstudio.agendify.repository.RepositorioResenias
import plapstudio.agendify.repository.RepositorioUsuarios
import plapstudio.agendify.repository.SearchLibro
import plapstudio.agendify.repository.SearchReserva
import plapstudio.agendify.repository.SearchResenia
import plapstudio.agendify.repository.SearchUsuario
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
