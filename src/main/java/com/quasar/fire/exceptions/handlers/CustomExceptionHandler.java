package com.quasar.fire.exceptions.handlers;

import com.quasar.fire.exceptions.custom.CustomFuntionalException;
import com.quasar.fire.exceptions.custom.CustomTechnicalException;
import com.quasar.fire.model.dtos.CustomResposeError;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CustomExceptionHandler implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        Response response;
        switch (throwable){
            case CustomFuntionalException funtionalException ->
                response = buildCustomResponseError(Response.Status.NOT_FOUND, funtionalException.getMessage());
            case CustomTechnicalException technicalException ->
                response = buildCustomResponseError(Response.Status.INTERNAL_SERVER_ERROR, technicalException.getMessage());
            case ConstraintViolationException violationException ->
                response = buildCustomResponseError(Response.Status.BAD_REQUEST, violationException.getMessage());
            case null, default ->
                response = buildCustomResponseError(Response.Status.SERVICE_UNAVAILABLE, throwable == null ? "Internal error server" : throwable.getMessage());
        }
        return response;
    }
    private Response buildCustomResponseError(Response.Status status, String message){
        return Response.status(status).entity(new CustomResposeError(status.getStatusCode()
                ,message)).build();
    }
}
