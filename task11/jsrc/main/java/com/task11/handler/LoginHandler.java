package com.task11.handler;

import com.task11.context.ApiClientContext;
import com.task11.service.AuthService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class LoginHandler implements RouteHandler {
    private final AuthService authService;

    @Override
    public Map<String, Object> handle(ApiClientContext context) {
        Map<String, Object> body = context.getBody();
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        return authService.signin(email, password);
    }
}
