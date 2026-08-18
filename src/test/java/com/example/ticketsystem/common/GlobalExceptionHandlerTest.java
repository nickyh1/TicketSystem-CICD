package com.example.ticketsystem.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn404WhenResourceDoesNotExist() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        NoResourceFoundException exception =
                new NoResourceFoundException(
                        HttpMethod.GET,
                        "/v3/api-docs",
                        "No static resource v3/api-docs"
                );

        ResponseEntity<Result<Void>> response =
                handler.handleNoResourceFound(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().getCode())
                .isEqualTo(404);

        assertThat(response.getBody().getMessage())
                .isEqualTo("资源不存在");
    }
}