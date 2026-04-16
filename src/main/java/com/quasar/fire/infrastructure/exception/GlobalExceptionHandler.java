package com.quasar.fire.infrastructure.exception;

import com.quasar.fire.domain.exception.DomainException;
import com.quasar.fire.domain.exception.InsufficientSatellitesException;
import com.quasar.fire.domain.exception.NoUniqueIntersectionException;
import com.quasar.fire.domain.exception.SignalNotFoundException;
import com.quasar.fire.infrastructure.adapter.in.rest.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class GlobalExceptionHandler {

    @ServerExceptionMapper
    public Response handleDomainException(DomainException e) {
        int status = mapStatus(e);
        return Response.status(status)
                .entity(new ErrorResponse(status, e.getMessage()))
                .build();
    }

    @ServerExceptionMapper
    public Response handleConstraintViolation(ConstraintViolationException e) {
        return Response.status(400)
                .entity(new ErrorResponse(400, e.getMessage()))
                .build();
    }

    @ServerExceptionMapper
    public Response handleRuntimeException(RuntimeException e) {
        return Response.status(500)
                .entity(new ErrorResponse(500, e.getMessage()))
                .build();
    }

    private int mapStatus(DomainException e) {
        if (e instanceof InsufficientSatellitesException || e instanceof SignalNotFoundException) {
            return 404;
        }
        if (e instanceof NoUniqueIntersectionException) {
            return 500;
        }
        return 500;
    }
}
