package com.orbiafly.api.infrastructure.web;

import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, "Recurso não encontrado", exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(
      MethodArgumentNotValidException exception,
      HttpServletRequest request) {
    ProblemDetail detail = problem(
        HttpStatus.BAD_REQUEST,
        "Dados inválidos",
        "Revise os campos informados.",
        request);
    Map<String, String> violations = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(error ->
        violations.putIfAbsent(error.getField(), error.getDefaultMessage()));
    detail.setProperty("violations", violations);
    return detail;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail handleConstraint(
      ConstraintViolationException exception,
      HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Parâmetros inválidos",
        "Revise os parâmetros informados.",
        request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ProblemDetail handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.BAD_REQUEST,
        "Requisição inválida",
        "O conteúdo enviado não pôde ser interpretado.",
        request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ProblemDetail handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
    return problem(
        HttpStatus.CONFLICT,
        "Conflito de dados",
        "A operação viola uma regra de integridade.",
        request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ProblemDetail handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", exception.getMessage(), request);
  }

  private ProblemDetail problem(
      HttpStatus status,
      String title,
      String detail,
      HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    problem.setInstance(URI.create(request.getRequestURI()));
    return problem;
  }
}
