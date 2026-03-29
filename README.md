# Agendify Backend

Backend de **Agendify**, una plataforma SaaS de gestion de agenda y turnos para profesionales, pensada para automatizar reservas, organizar clientes y centralizar notificaciones y cobros en un solo sistema.

## Descripcion

Agendify busca resolver uno de los problemas mas comunes en servicios profesionales: la organizacion manual de turnos, seguimientos y pagos.

La plataforma permite que profesionales y asistentes administren agendas de forma simple, mientras que los clientes pueden reservar, consultar o cancelar turnos desde una experiencia digital centralizada.

## Objetivo del Proyecto

Construir una solucion escalable de gestion de turnos que permita:

- Automatizar la agenda profesional.
- Facilitar reservas online.
- Centralizar la gestion de clientes.
- Integrar notificaciones y recordatorios.
- Simular procesos de facturacion y cobro online.

## Roles del Sistema

### Admin

Administrador general del sistema.

- Supervisa la plataforma.
- Gestiona configuraciones globales.
- Controla el funcionamiento general del SaaS.

### Profesional

Es dueno de una o mas agendas y administra su operacion diaria.

- Crea o da de baja agendas.
- Configura horarios, dias disponibles y duracion de turnos.
- Gestiona clientes, historial y documentos.
- Visualiza notificaciones, pagos y actividad.

### Asistente

Colabora con la gestion operativa de una agenda profesional.

- Crea, modifica o da de baja turnos.
- Edita la agenda del profesional.
- Ayuda en la organizacion diaria de pacientes o clientes.

### Cliente

Usuario final que interactua con la agenda para solicitar servicios.

- Busca profesionales.
- Reserva, visualiza o cancela turnos.
- Recibe notificaciones.
- Realiza pagos online mockeados.

## Funcionalidades Principales

### Profesionales

- Dashboard con vista general de actividad.
- Agenda inteligente.
- Gestion de turnos:
  configuracion de horarios, dias y duracion.
- Gestion de clientes:
  informacion, historial de turnos y documentos.
- Sistema de notificaciones:
  recordatorios de turnos, pagos y novedades.
- Facturacion mockeada.
- Cobro online mockeado.

### Clientes

- Dashboard con buscador de profesionales y favoritos.
- Reserva de turnos.
- Visualizacion y cancelacion de turnos.
- Notificaciones sobre turnos, pagos y documentos.
- Pago online mockeado.

## Propuesta de Valor

Agendify se apoya en cinco pilares principales:

- **Automatizacion de agenda**: menos trabajo manual y menos errores administrativos.
- **Reservas online**: disponibilidad accesible para clientes en cualquier momento.
- **Notificaciones inteligentes**: menos ausencias y mejor comunicacion.
- **Gestion centralizada de clientes**: informacion ordenada en un solo lugar.
- **Cobros simplificados**: integracion del flujo de turnos y pagos.

## Modelo de Negocio

El proyecto se piensa como un SaaS con distintas vias de monetizacion:

- Cobro por transaccion por cada turno reservado.
- Posicionamiento pago en resultados de busqueda.
- Publicidad en la landing page.
- Publicidad dentro de dashboards.

## Alcance Actual

Este repositorio contiene el backend de la plataforma, encargado de modelar la logica del negocio, exponer endpoints y sostener la gestion de usuarios, agendas, turnos y operaciones relacionadas.

Algunas integraciones, como facturacion y cobro online, se encuentran planteadas en modo **mock** como parte del alcance actual del proyecto.

## Stack Tecnologico

- Kotlin
- Spring Boot
- Gradle
- GitHub Actions

## Estado del Proyecto

Proyecto en desarrollo.

La base actual esta orientada a evolucionar hacia una plataforma SaaS completa para administracion de agendas, turnos, clientes y pagos.
