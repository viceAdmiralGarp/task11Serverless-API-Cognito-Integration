package com.task12.creators;

import com.task12.context.ApiClientContext;
import com.task12.handler.RouteHandler;
import com.task12.service.ReservationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ReservationCreator implements RouteHandler {
    private final ReservationService reservationService;

    @Override
    public Map<String, Object> handle(ApiClientContext context) {
        return reservationService.generateReservation(context.getBody());
    }
}

