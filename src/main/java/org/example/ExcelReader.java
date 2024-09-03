package org.example;

import com.google.gson.*;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExcelReader {
    private static final String API_KEY = "";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final List<OutfitItem> outfits = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("For what event would you like your outfits to be chosen for?");
        String answer = scanner.nextLine();
        extractOutfitItemsFromExcel();
        sendOutfitRequest(answer);
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

    private static List<OutfitSuggestion> sendOutfitRequest (String answer) {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();

        for (OutfitItem outfitItem : outfits) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", outfitItem.getId());
            jsonObject.addProperty("type", outfitItem.getType());
            jsonObject.addProperty("style", outfitItem.getStyle());
            jsonObject.addProperty("color", outfitItem.getColor());

            //makes smaller JsonArray for each outfitItem's seasons
            JsonArray seasonsArray = new JsonArray();
            for (String season : outfitItem.getSeason()) {
                seasonsArray.add(season);
            }
            jsonObject.add("seasons", seasonsArray);

            jsonObject.addProperty("description", outfitItem.getDescription());
            jsonArray.add(jsonObject);
        }
        String clothes = gson.toJson(jsonArray);

        String requestPayload = gson.toJson(Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant."),
                        Map.of("role", "user", "content", "You are a stylist. Choose 3 outfits (each must include either a top, bottom, or dress, plus shoes; bag and other accessories are optional) for" + answer + "from the following items. Ensure the colors match. Return a JsonArray with each outfit as a JsonObject. Use the following naming convention for the item IDs in the JSON: \"top\", \"bottom\", \"dress\", \"shoes\", \"accessory\". Each outfit should also include an explanation for your choices. Only include the IDs and explanation in the JSON: " + clothes
                        )
                )

               // old request in case we want to use it:
               //You are a stylist, choose a look (shirt and pants/skirt or dresses/suits, you can add accessories and suitable shoes) for a party from the following items. Note that the colors match, return JSON with only their ID:" + clothes
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

        List<OutfitSuggestion> outfitSuggestions = new ArrayList<>();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response from GPT API:");
            System.out.println(response.body());

            // Step 1: Parse the main JSON response
            String responseBody = response.body();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choicesArray = jsonResponse.getAsJsonArray("choices");

            // Get the content string from the big Json inside choicesArray
            JsonObject firstChoice = choicesArray.get(0).getAsJsonObject();
            String content = firstChoice.getAsJsonObject("message").get("content").getAsString();

            String cleanedContent = content.replaceAll("```json\\n|\\n```", "").replace("\\n", "\n");

            try {
                JsonArray outfitSuggestionsArray = JsonParser.parseString(cleanedContent).getAsJsonArray();

                for (int i = 0; i < outfitSuggestionsArray.size(); i++) {
                    List<OutfitItem> itemsOfSuggestion = new LinkedList<>();
                    JsonObject outfitSuggestionJson = outfitSuggestionsArray.get(i).getAsJsonObject();
                    String explanation = outfitSuggestionJson.get("explanation").getAsString();
                    itemsOfSuggestion = parseOutfitJson(outfitSuggestionJson);
                    OutfitSuggestion outfitSuggestion = new OutfitSuggestion(itemsOfSuggestion, explanation);
                    outfitSuggestions.add(outfitSuggestion);
                }
                //PRINTS OUTFIT SUGGESTION ARRAY !!! :)
                for (OutfitSuggestion outfitSuggestion : outfitSuggestions) {
                    System.out.println(outfitSuggestion);
                    System.out.println(" ");
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                System.out.println("Malformed JSON: " + e.getMessage());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return outfitSuggestions;
    }

    private static List<OutfitItem> parseOutfitJson(JsonObject outfitSuggestionJson) {
        List<OutfitItem> outfitItems = new LinkedList<>();
        for (String key : outfitSuggestionJson.keySet()) {
            if (!key.equals("explanation")) {
                JsonElement element = outfitSuggestionJson.get(key);

                // Assuming all IDs are integers
                int id = element.getAsInt();

                OutfitItem outfit = outfits.stream()
                        .filter(item -> item.getId() == id)
                        .findFirst()
                        .orElse(null);
                if (outfit != null) {
                    // Do something with the outfit
                    outfitItems.add(outfit);
                } else {
                    // Handle the case where the outfit is not found
                    System.out.println("oopsie");
                }
            }
        }
        return outfitItems;
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