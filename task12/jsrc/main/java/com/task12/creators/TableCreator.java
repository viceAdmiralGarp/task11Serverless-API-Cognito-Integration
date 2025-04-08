package com.task12.creators;

import com.task12.context.ApiClientContext;
import com.task12.handler.RouteHandler;
import com.task12.service.TableService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class TableCreator implements RouteHandler {
    private final TableService tableService;

    @Override
    public Map<String, Object> handle(ApiClientContext context) {
        Map<String, Object> body = context.getBody();
        context.getLambdaContext().getLogger().log("Taken off to feed on the folded tableTaken off to feed on the folded table: " + body);
        return tableService.createTable(body);

    }
}