package com.quasar.fire.services;

import com.quasar.fire.model.dtos.CustomRequest;
import com.quasar.fire.model.dtos.CustomResponse;
import io.smallrye.mutiny.Uni;

public interface IPositionCalculateService {
    Uni<CustomResponse> calculatePosition(CustomRequest request);
}