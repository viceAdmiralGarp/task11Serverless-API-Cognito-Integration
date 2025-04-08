package com.task12;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task12.context.ApiClientContext;
import com.task12.creators.ReservationCreator;
import com.task12.creators.TableCreator;
import com.task12.getters.GetReservations;
import com.task12.getters.GetTableById;
import com.task12.getters.GetTables;
import com.task12.handler.RouteHandler;
import com.task12.handler.LoginHandler;
import com.task12.handler.RegisterHandler;
import com.task12.service.AuthService;
import com.task12.service.ReservationService;
import com.task12.service.TableService;
import com.task12.util.Response;

import java.util.HashMap;
import java.util.Map;


import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${booking_userpool}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "${tables_table}")
@DependsOn(resourceType = ResourceType.DYNAMODB_TABLE, name = "${reservations_table}")
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "CLIENT_ID", value = "${booking_userpool}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID),
		@EnvironmentVariable(key = "TABLES_TABLE", value = "${tables_table}"),
		@EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}")
})
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	private AWSCognitoIdentityProvider cognito;
	private DynamoDB dynamoDB;

	private static final ObjectMapper mapper = new ObjectMapper();
	private final Map<String, RouteHandler> handlers = new HashMap<>();

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {

		context.getLogger().log("REGION: " + System.getenv("REGION"));
		context.getLogger().log("COGNITO_ID: " + System.getenv("COGNITO_ID"));
		context.getLogger().log("CLIENT_ID: " + System.getenv("CLIENT_ID"));
		context.getLogger().log("tablesTable: " + System.getenv("tablesTable"));
		context.getLogger().log("reservationsTable: " + System.getenv("reservationsTable"));

		initServices();
		initHeaders();

		try {
			String resource = (String) request.get("resource");
			String httpMethod = (String) request.get("httpMethod");

			ApiClientContext requestContext = new ApiClientContext(
					request,
					parseBody((String) request.get("body")),
					(Map<String, String>) request.get("pathParameters"),
					(Map<String, String>) request.get("headers"),
					context
			);

			String handlerKey = resource + ":" + httpMethod;
			RouteHandler handler = handlers.get(handlerKey);

			if (handler != null) {
				return handler.handle(requestContext);
			}

			return Response.generateResponse(400, "Incorrect request");
		} catch (Exception e) {
			return Response.generateResponse(400, "Misstake: " + e.getMessage());
		}
	}

	private void initServices() {
		String region = System.getenv("REGION");

		cognito = AWSCognitoIdentityProviderClientBuilder.standard()
				.withRegion(region)
				.build();

		AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion(region)
				.build();
		dynamoDB = new DynamoDB(amazonDynamoDB);
	}

	private void initHeaders() {
		String cognitoId = System.getenv("COGNITO_ID");
		String clientId = System.getenv("CLIENT_ID");
		String tablesTableName = System.getenv("TABLES_TABLE");
		String resTableName = System.getenv("RESERVATIONS_TABLE");

		ReservationService reservationService = new ReservationService(dynamoDB, resTableName, tablesTableName);
		handlers.put("/reservations:GET", new GetReservations(reservationService));
		handlers.put("/reservations:POST", new ReservationCreator(reservationService));

		TableService tableService = new TableService(dynamoDB, tablesTableName);
		handlers.put("/tables:GET", new GetTables(tableService));
		handlers.put("/tables:POST", new TableCreator(tableService));
		handlers.put("/tables/{tableId}:GET", new GetTableById(tableService));

		AuthService authService = new AuthService(cognito, cognitoId, clientId);
		handlers.put("/signup:POST", new RegisterHandler(authService));
		handlers.put("/signin:POST", new LoginHandler(authService));
	}

	private Map<String, Object> parseBody(String body) throws JsonProcessingException {
		return body != null ? mapper.readValue(body, Map.class) : null;
	}
}

