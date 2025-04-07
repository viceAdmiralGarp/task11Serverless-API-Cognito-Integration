package com.task11.handler;

import com.task11.context.ApiClientContext;

import java.util.Map;

@FunctionalInterface
public interface RouteHandler {
    Map<String, Object> handle(ApiClientContext context);
}
