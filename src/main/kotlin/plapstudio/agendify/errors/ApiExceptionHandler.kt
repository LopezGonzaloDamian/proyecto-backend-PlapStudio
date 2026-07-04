package plapstudio.agendify.errors

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String> = emptyList()
)

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(
        BusinessException::class,
        NotFoundException::class,
        UnauthorizedException::class,
        ForbiddenException::class,
        ConflictException::class
    )
    fun handleDomainException(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val status = when (ex) {
            is NotFoundException -> HttpStatus.NOT_FOUND
            is UnauthorizedException -> HttpStatus.UNAUTHORIZED
            is ForbiddenException -> HttpStatus.FORBIDDEN
            is ConflictException -> HttpStatus.CONFLICT
            else -> HttpStatus.BAD_REQUEST
        }
        return build(status, ex.message ?: status.reasonPhrase, request.requestURI)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val details = ex.bindingResult.fieldErrors
            .map { error -> "${error.field}: ${error.defaultMessage ?: "valor invalido"}" }
            .ifEmpty { listOf("Payload invalido") }
        return build(HttpStatus.BAD_REQUEST, "Payload invalido", request.requestURI, details)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> {
        val details = ex.constraintViolations
            .map { violation -> "${violation.propertyPath}: ${violation.message}" }
            .ifEmpty { listOf("Parametro invalido") }
        return build(HttpStatus.BAD_REQUEST, "Solicitud invalida", request.requestURI, details)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiErrorResponse> =
        build(
            HttpStatus.BAD_REQUEST,
            ex.mostSpecificCause.message ?: "No se pudo interpretar el payload",
            request.requestURI
        )

    private fun build(
        status: HttpStatus,
        message: String,
        path: String,
        details: List<String> = emptyList()
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(status).body(
            ApiErrorResponse(
                status = status.value(),
                error = status.reasonPhrase,
                message = message,
                path = path,
                details = details
            )
        )
}
