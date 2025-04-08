package com.task12.getters;

import com.task12.context.ApiClientContext;
import com.task12.handler.RouteHandler;
import com.task12.service.TableService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GetTables implements RouteHandler {
    private final TableService tableService;

    @Override
    public Map<String, Object> handle(ApiClientContext context) {
        return tableService.getAllTables();
    }
}
