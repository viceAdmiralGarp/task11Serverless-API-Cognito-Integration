package com.task12.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.task12.util.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableService {
    private final DynamoDB dynamoDB;
    private final String tablesTableName;

    public TableService(DynamoDB dynamoDB, String tablesTableName) {
        this.dynamoDB = dynamoDB;
        this.tablesTableName = tablesTableName;
    }

    public Map<String, Object> getAllTables() {
        Table table = dynamoDB.getTable(tablesTableName);
        List<Map<String, Object>> tables = new ArrayList<>();

        for (Item item : table.scan()) {
            Map<String, Object> tableData = addDataToTable(item);
            tables.add(tableData);
        }

        tables.sort(Comparator.comparingInt(t -> (Integer) t.get("id")));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("tables", tables);
        return Response.generateResponse(200, responseBody);
    }

    public Map<String, Object> createTable(Map<String, Object> tableData) {
        if (!tableData.containsKey("id") || tableData.get("id") == null) {
            return Response.generateResponse(400, "Field 'id' is required!");
        }
        if (!tableData.containsKey("number") || tableData.get("number") == null) {
            return Response.generateResponse(400, "Field 'number' is required!");
        }
        if (!tableData.containsKey("places") || tableData.get("places") == null) {
            return Response.generateResponse(400, "Field 'places' is required!");
        }
        if (!tableData.containsKey("isVip") || tableData.get("isVip") == null) {
            return Response.generateResponse(400, "Field 'isVip' is required!");
        }

        try {
            Integer id = (Integer) tableData.get("id");
            Integer number = (Integer) tableData.get("number");
            Integer places = (Integer) tableData.get("places");
            Boolean isVip = (Boolean) tableData.get("isVip");

            Table table = dynamoDB.getTable(tablesTableName);
            Item item = new Item()
                    .withPrimaryKey("id", String.valueOf(id))
                    .withInt("number", number)
                    .withInt("places", places)
                    .withBoolean("isVip", isVip);

            if (tableData.containsKey("minOrder")) {
                item.withInt("minOrder", (Integer) tableData.get("minOrder"));
            }

            table.putItem(item);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", id);
            return Response.generateResponse(200, responseBody);

        } catch (Exception e) {
            return Response.generateResponse(400, "Error creating table: " + e.getMessage());
        }
    }

    public Map<String, Object> getTableById(String tableId, Context context) {
        context.getLogger().log("Retrieving table with id: " + tableId);

        Table table = dynamoDB.getTable(tablesTableName);
        Item item = table.getItem(new GetItemSpec().withPrimaryKey("id", tableId));

        if (item == null) {
            return Response.generateResponse(400, "Table not found");
        }

        Map<String, Object> tableData = addDataToTable(item);

        return Response.generateResponse(200, tableData);
    }

    private Map<String, Object> addDataToTable(Item item) {
        Map<String, Object> dataTable = new LinkedHashMap<>();
        dataTable.put("id", Integer.parseInt(item.getString("id")));
        dataTable.put("number", item.getInt("number"));
        dataTable.put("places", item.getInt("places"));
        dataTable.put("isVip", item.getBoolean("isVip"));
        if (item.isPresent("minOrder")) {
            dataTable.put("minOrder", item.getInt("minOrder"));
        }
        return dataTable;
    }
}
