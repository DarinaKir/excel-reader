package org.example;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class ExcelReader {

    // יצירת קוד API וכתובת URL לשליחת הבקשה POST
    private static final String API_KEY = "sk-proj-Zbb2hJm0XgqKpmZSKdzTT3BlbkFJShq32tPAUZa8o24o2qr2";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) {
        String excelFilePath = "src/main/files/סיווג בגדים GPT.xlsx";
        List<String> descriptions = new ArrayList<>();
        int startRow = 1; // Start from row 2 (index 1)
        int endRow = 49;  // End at row 50 (index 49)

        try (FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fileInputStream)) {

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate over the rows from startRow to endRow
            for (int i = startRow; i <= endRow && i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Get the sixth cell (index 5 because it's zero-based)
                    Cell cell = row.getCell(5);
                    if (cell != null) {
                        if (cell.getStringCellValue() == ""){
                            descriptions.add("NULL");
                        }else {
                            descriptions.add(cell.getStringCellValue());
                        }

                        // Add the cell value to the list based on its type
//                        switch (cell.getCellType()) {
//                            case STRING:
//                                descriptions.add(cell.getStringCellValue());
//                                break;
//                            case NUMERIC:
//                                descriptions.add(String.valueOf(cell.getNumericCellValue()));
//                                break;
//                            case BOOLEAN:
//                                descriptions.add(String.valueOf(cell.getBooleanCellValue()));
//                                break;
//                            default:
//                                descriptions.add("Unknown Type");
//                                break;
//                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print the values of the sixth column
        System.out.println("Values in the sixth column from row " + (startRow + 1) + " to row " + (endRow + 1) + ":");
        for (String value : descriptions) {
            System.out.println(value);
        }

        // Convert the list to JSON format
        String clothingItemsJson = descriptions.stream()
                .map(item -> "\"" + item.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        // Create the request payload
        String requestPayload = "{"
                + "  \"model\": \"gpt-3.5-turbo\","
                + "  \"messages\": ["
                + "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},"
                + "    {\"role\": \"user\", \"content\": \"Choose an outfit for a party from the following items: " + clothingItemsJson + "\"}"
                + "  ]"
                + "}";

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
}
