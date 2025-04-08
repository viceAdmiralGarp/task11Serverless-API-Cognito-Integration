package com.task12.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.task12.util.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReservationService {
    private final DynamoDB dynamoDB;
    private final String reservationsTableName;
    private final String tablesTableName;

    public ReservationService(DynamoDB dynamoDB, String reservationsTableName, String tablesTableName) {
        this.dynamoDB = dynamoDB;
        this.reservationsTableName = reservationsTableName;
        this.tablesTableName = tablesTableName;
    }

    public Map<String, Object> fetchAllReservations() {
        Table table = dynamoDB.getTable(reservationsTableName);
        List<Map<String, Object>> reservations = new ArrayList<>();

        for (Item item : table.scan()) {
            reservations.add(item.asMap());
        }

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("reservations", reservations);
        return Response.generateResponse(200, responseBody);
    }

    public Map<String, Object> generateReservation(Map<String, Object> reservationData) {
        Table tablesTable = dynamoDB.getTable(tablesTableName);
        Integer tableNumber = (Integer) reservationData.get("tableNumber");
        boolean tableExists = false;

        for (Item item : tablesTable.scan()) {
            if (item.getInt("number") == tableNumber) {
                tableExists = true;
                break;
            }
        }

        if (!tableExists) {
            return Response.generateResponse(400, "Table not found");
        }

        String date = (String) reservationData.get("date");
        String newStart = (String) reservationData.get("slotTimeStart");
        String newEnd = (String) reservationData.get("slotTimeEnd");

        Table reservationsTable = dynamoDB.getTable(reservationsTableName);
        for (Item existing : reservationsTable.scan()) {
            if (existing.getInt("tableNumber") == tableNumber && existing.getString("date").equals(date)) {
                String existingStart = existing.getString("slotTimeStart");
                String existingEnd = existing.getString("slotTimeEnd");

                if (hasTimeOverlap(newStart, newEnd, existingStart, existingEnd)) {
                    return Response.generateResponse(400, "Reservation overlaps with an existing reservation");
                }
            }
        }

        String reservationId = UUID.randomUUID().toString();
        Item reservation = new Item()
                .withPrimaryKey("id", reservationId)
                .withInt("tableNumber", tableNumber)
                .withString("clientName", (String) reservationData.get("clientName"))
                .withString("phoneNumber", (String) reservationData.get("phoneNumber"))
                .withString("date", date)
                .withString("slotTimeStart", newStart)
                .withString("slotTimeEnd", newEnd);

        reservationsTable.putItem(reservation);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("reservationId", reservationId);
        return Response.generateResponse(200, responseBody);
    }

    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    private boolean hasTimeOverlap(String proposedStartTime, String proposedEndTime,
                                   String bookedStartTime, String bookedEndTime) {
        int proposedStart = convertToMinutes(proposedStartTime);
        int proposedEnd = convertToMinutes(proposedEndTime);
        int bookedStart = convertToMinutes(bookedStartTime);
        int bookedEnd = convertToMinutes(bookedEndTime);

        return proposedStart < bookedEnd && proposedEnd > bookedStart;
    }
}
