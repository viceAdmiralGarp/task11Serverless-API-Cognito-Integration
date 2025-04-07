package com.task11.creators;

import com.task11.context.ApiClientContext;
import com.task11.handler.RouteHandler;
import com.task11.service.ReservationService;
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

