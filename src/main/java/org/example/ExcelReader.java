package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.google.gson.Gson;

public class ExcelReader {
    private static final String API_KEY = "sk-proj-Zbb2hJm0XgqKpmZSKdzTT3BlbkFJShq32tPAUZa8o24o2qr2";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final List<OutfitItem> outfits = new ArrayList<>();

    public static void main(String[] args) {
        extractOutfitItemsFromExcel();

        sendOutfitRequest();

    }
    private static void extractOutfitItemsFromExcel () {
        String excelFilePath = "src/main/files/Classification of clothes.xlsx";
        int startRow = 1; // Start from row 2 (index 1)
        int endRow = 49;  // End at row 50 (index 49)
        try (FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Iterate over the rows from startRow to endRow
            for (int i = startRow; i <= endRow && i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    OutfitItem outfitItem = new OutfitItem(i);
                    for (int j = 1; j < 6; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            switch (j) {
                                case 1:
                                    outfitItem.setType(cell.getStringCellValue());
                                    break;
                                case 2:
                                    outfitItem.setStyle(cell.getStringCellValue());
                                    break;
                                case 3:
                                    outfitItem.setColor(cell.getStringCellValue());
                                    break;
                                case 4:
                                    outfitItem.setSeason(getSeasonArray(cell.getStringCellValue()));
                                    break;
                                case 5:
                                    outfitItem.setDescription(cell.getStringCellValue());
                                    break;
                            }
                        }
                    }
                    outfits.add(outfitItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Values in the sixth column from row " + (startRow + 1) + " to row " + (endRow + 1) + ":");
        for (OutfitItem outfitItem : outfits) {
            System.out.println(outfitItem);
        }
    }

    private static void sendOutfitRequest () {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();

        for (OutfitItem outfitItem : outfits) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", outfitItem.getId());
            jsonObject.addProperty("description", outfitItem.getDescription());
            jsonArray.add(jsonObject);
        }
        String clothes = gson.toJson(jsonArray);

        String requestPayload = gson.toJson(Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", "Choose an look for a party from the following items, return only their ID: " + clothes)
                )
        ));

// Print the request payload to debug
        System.out.println("Request Payload:");
        System.out.println(requestPayload);

// Send request to OpenAI API
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestPayload))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Print the response
            System.out.println("Response from GPT API:");
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static List<String> getSeasonArray(String season) {
        List<String> seasons = new ArrayList<>();
        if (season.equals("all") || season.equals("all season")) {
            seasons = List.of("winter", "spring", "summer", "fall");
        }else {
            String[] splitSeasons = season.split("/");
            seasons = Arrays.asList(splitSeasons);
        }
        return seasons;
    }
}
