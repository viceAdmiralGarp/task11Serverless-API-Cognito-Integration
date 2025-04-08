package com.task12.handler;

import com.task12.context.ApiClientContext;

import java.util.Map;

@FunctionalInterface
public interface RouteHandler {
    Map<String, Object> handle(ApiClientContext context);
}
