package com.example.recomendation.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Manejo global de errores para toda la aplicación MVC.
 * Devuelve nombres de vista (no JSON), ya que el proyecto es Thymeleaf puro.
 * Las plantillas referenciadas (error-404, error-403, error-409, error genérico)
 * se crean en la FASE 20 junto con error.html y access-denied.html.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException ex, Model model, HttpServletRequest request) {
        log.warn("Regla de negocio violada en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/generic";
    }

    @ExceptionHandler(OptimisticLockConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOptimisticLockConflict(OptimisticLockConflictException ex, Model model, HttpServletRequest request) {
        log.warn("Conflicto de concurrencia en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/generic";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model, HttpServletRequest request) {
        log.warn("Acceso denegado en {} para un usuario sin permisos", request.getRequestURI());
        model.addAttribute("path", request.getRequestURI());
        return "access-denied";
    }

    @ExceptionHandler(MLServiceException.class)
    @ResponseStatus(HttpStatus.OK)
    public String handleMLServiceException(MLServiceException ex, Model model, HttpServletRequest request) {
        // Este handler es una red de seguridad: en condiciones normales, MLServiceException
        // debe resolverse DENTRO del servicio (fallback al método tradicional) y nunca
        // debería llegar hasta aquí. Si llega, lo tratamos como un error genérico no fatal.
        log.error("MLServiceException llegó sin resolverse hasta el controller en {}: {}",
                request.getRequestURI(), ex.getMessage());
        model.addAttribute("message", "El servicio de predicciones no está disponible en este momento.");
        model.addAttribute("path", request.getRequestURI());
        return "error/generic";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletRequest request) {
        log.warn("Argumento inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/generic";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, Model model, HttpServletRequest request) {
        // No se expone el stack trace al usuario; solo se loguea en el servidor.
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        model.addAttribute("path", request.getRequestURI());
        return "error/generic";
    }
}