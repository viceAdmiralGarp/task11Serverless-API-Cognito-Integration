package com.task12.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.services.lambda.runtime.Context;
import com.task12.util.ValidationUtility;
import com.task12.util.Response;


import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final AWSCognitoIdentityProvider cognitoClient;
    private final String cognitoId;
    private final String clientId;

    public AuthService(AWSCognitoIdentityProvider cognitoClient, String cognitoId, String clientId) {
        this.cognitoClient = cognitoClient;
        this.cognitoId = cognitoId;
        this.clientId = clientId;
    }

    public Map<String, Object> register(String email, String password, String firstName, String lastName, Context context) {
        context.getLogger().log("Attempt test for email: " + email + ", firstName: " + firstName + ", lastName: " + lastName);
        context.getLogger().log("Using cognitoId: " + cognitoId);

        if (!ValidationUtility.isValidPassword(password)) {
            context.getLogger().log("Invalid format password for email: " + email +
                                    ". The password must be at least 12 characters long, with uppercase and " +
                                    "lowercase letters, a number, and a special character.");
            return Response.generateResponse(400, "Invalid password format");
        }

        if (!ValidationUtility.isValidEmail(email)) {
            context.getLogger().log("Invalid format email: " + email);
            return Response.generateResponse(400, "Invalid format email");
        }

        if (firstName == null || lastName == null || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            context.getLogger().log("Missing or empty firstName or lastName: firstName=" + firstName +
                                    ", lastName=" + lastName);
            return Response.generateResponse(400, "Missing or empty firstName or lastName");
        }

        try {
            context.getLogger().log("Creating user in Cognito for email: " + email);
            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
                    .withUserPoolId(cognitoId)
                    .withUsername(email)
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(email),
                            new AttributeType().withName("given_name").withValue(firstName),
                            new AttributeType().withName("family_name").withValue(lastName),
                            new AttributeType().withName("email_verified").withValue("true")
                    )
                    .withTemporaryPassword(password)
                    .withMessageAction("SUPPRESS");

            cognitoClient.adminCreateUser(createUserRequest);
            context.getLogger().log("User successfully created for email: " + email);

            context.getLogger().log("Setting a permanent password для email: " + email);
            AdminSetUserPasswordRequest setPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(cognitoId)
                    .withUsername(email)
                    .withPassword(password)
                    .withPermanent(true);

            cognitoClient.adminSetUserPassword(setPasswordRequest);
            context.getLogger().log("Password successfully set for email: " + email);

            return Response.generateResponse(200, "Registration is successful");
        } catch (UsernameExistsException e) {
            context.getLogger().log("User already exists, attempting to update password for email: " + email);
            try {
                AdminSetUserPasswordRequest setPasswordRequest = new AdminSetUserPasswordRequest()
                        .withUserPoolId(cognitoId)
                        .withUsername(email)
                        .withPassword(password)
                        .withPermanent(true);
                cognitoClient.adminSetUserPassword(setPasswordRequest);
                context.getLogger().log("Password successfully updated for existing user: " + email);
                return Response.generateResponse(200, "\n" +
                                                      "Registration successful (user already existed, password updated)");
            } catch (Exception ex) {
                context.getLogger().log("\n" +
                                        "Failed to update password for existing user: " + ex.getMessage());
                return Response.generateResponse(400, "Failed to update password: " + ex.getMessage());
            }
        } catch (Exception e) {
            context.getLogger().log("Registration failed for email " + email + ": " + e.getMessage());
            return Response.generateResponse(400, "Registration failed: " + e.getMessage());
        }
    }

    public Map<String, Object> signin(String email, String password) {
        if (!ValidationUtility.isValidEmail(email) || !ValidationUtility.isValidPassword(password)) {
            return Response.generateResponse(400, "Incorrect credentials");
        }

        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(cognitoId)
                .withClientId(clientId)
                .withAuthParameters(new HashMap<String, String>() {{
                    put("USERNAME", email);
                    put("PASSWORD", password);
                }});

        AdminInitiateAuthResult authResult = cognitoClient.adminInitiateAuth(authRequest);
        String idToken = authResult.getAuthenticationResult().getIdToken();

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("idToken", idToken);

        return Response.generateResponse(200, responseBody);
    }
}
