# 📅 Agendify — Backend

Agendify es una plataforma SaaS de gestión de turnos orientada a profesionales, diseñada para automatizar la agenda, centralizar la gestión de clientes y simplificar el proceso de reservas, notificaciones y cobros.

Este repositorio contiene la API REST del backend, desarrollada en Kotlin con Spring Boot.

🔗 **Repositorio del frontend:** [proyecto-frontend-PlapStudio](https://github.com/LopezGonzaloDamian/proyecto-frontend-PlapStudio)


# Cómo correr el proyecto
La aplicación utiliza datos iniciales cargados automáticamente (bootstrap). No requiere configuración adicional.

## Requisitos previos
- [JDK temurin 21 - versión 21.0.6](https://adoptium.net/es/temurin/releases?version=21)
- kotestVersion 5.8.0

## Ejecución
```bash
./gradlew bootRun
```

La API estará disponible en http://localhost:9000


## Tests

```bash
./gradlew test
```

## 📖 Wiki

Para información detallada consultá la [Wiki de este repositorio](https://github.com/LopezGonzaloDamian/proyecto-backend-PlapStudio/wiki).

## Docker

Para poder levantar el proyecto completo en docker, primero hay que tener ambos repositorios (backend y frontend) en un mismo directorio raiz, por ejemplo:

```text
agendify/
├── proyecto-backend-PlapStudio/
├── proyecto-frontend-PlapStudio/
└── n8n/
```

Una vez creada esta estructura de directorios, mover el archivo "docker-compose(KOTLIN-REACT-POSTGRES-N8N).yml" en el directorio "agendify", pero renombrado a "docker-compose.yml" y la carpeta n8n_data.zip descomprimida dentro de "n8n" quedandonos así:

```text
agendify/
├── proyecto-backend-PlapStudio/
├── proyecto-frontend-PlapStudio/
├── n8n/
│   └── n8n_data/
└── docker-compose.yml
```

Una vez logrado esto, nos ubicamos en "agendify" y levantamos los contenedores definidos en el archivo docker-compose.yml:

```bash
docker compose up -d
```

Una vez que creó los contenedores, revisamos si están activos:

```bash
docker ps
```

## Levantar Backend

Tenemos que entrar al contenedor del backend, para eso ejecutamos:

```bash
docker exec -it plapstudio-backend bash
```

Y una vez dentro, ejecutamos el gradlew para levantar el backend:

```bash
./gradlew bootRun --args='--server.port=9000'
```