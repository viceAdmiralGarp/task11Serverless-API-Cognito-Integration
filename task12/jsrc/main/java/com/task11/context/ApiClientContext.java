package com.task11.context;

import com.amazonaws.services.lambda.runtime.Context;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ApiClientContext {
    private final Map<String, Object> request;
    private final Map<String, Object> body;
    private final Map<String, String> pathParams;
    private final Map<String, String> headers;
    private final Context lambdaContext;
}
