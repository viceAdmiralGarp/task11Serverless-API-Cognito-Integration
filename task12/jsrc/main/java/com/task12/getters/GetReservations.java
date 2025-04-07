package com.task11.getters;

import com.task11.context.ApiClientContext;
import com.task11.handler.RouteHandler;
import com.task11.service.ReservationService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GetReservations implements RouteHandler {
    private final ReservationService reservationService;

    @Override
    public Map<String, Object> handle(ApiClientContext context) {
        return reservationService.fetchAllReservations();
    }
}

