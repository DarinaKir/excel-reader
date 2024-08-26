package org.example;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    public static void main(String[] args) {
        String excelFilePath = "src/main/files/סיווג בגדים GPT.xlsx";
        List<String> description = new ArrayList<>();
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
                            description.add("NULL");
                        }else {
                            description.add(cell.getStringCellValue());
                        }

                        // Add the cell value to the list based on its type
//                        switch (cell.getCellType()) {
//                            case STRING:
//                                description.add(cell.getStringCellValue());
//                                break;
//                            case NUMERIC:
//                                description.add(String.valueOf(cell.getNumericCellValue()));
//                                break;
//                            case BOOLEAN:
//                                description.add(String.valueOf(cell.getBooleanCellValue()));
//                                break;
//                            default:
//                                description.add("Unknown Type");
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
        for (String value : description) {
            System.out.println(value);
        }
    }
}
