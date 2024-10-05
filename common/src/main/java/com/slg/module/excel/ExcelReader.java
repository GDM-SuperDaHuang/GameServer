//package com.slg.module.excel;
//
//
//import jakarta.annotation.PostConstruct;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.util.CellAddress;
//import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.io.FileInputStream;
//
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class ExcelReader {
//
////    @Autowired
////    private RedisTemplate<String, Object> redisTemplate;
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    public void readExcelAndSaveToRedis(String filePath) throws IOException {
//        List<Object> dataList = new ArrayList<>();
//        filePath = "C:/Users/gdm/Desktop";
//        File directory = new File(filePath);
//        if (!directory.isDirectory()) {
//            return;
//        }
//        File[] files = directory.listFiles((dir, name) -> name.endsWith(".xlsx"));
//        if (files == null) {
//            return;
//        }
//        for (File file : files) {
//            try (FileInputStream inputStream = new FileInputStream(file)) {
//                Workbook workbook = new XSSFWorkbook(inputStream);
//                for (Sheet sheet : workbook) {
//                    String sheetName = sheet.getSheetName();
//                    Map<CellAddress, ? extends Comment> cellComments = sheet.getCellComments();
//                    CellRangeAddress repeatingColumns = sheet.getRepeatingColumns();
//                    for (Row row : sheet) {
//                        for (Cell cell : row) {
//                            String stringCellValue = cell.getStringCellValue();
//                            double numericCellValue = cell.getNumericCellValue();
//                            System.out.println(cell+"cell"+stringCellValue);
//                        }
//
//                        Cell firstCell = row.getCell(0);
//                        if (firstCell != null && "DataType".equals(firstCell.getStringCellValue())) {
//                            String dataType = firstCell.getStringCellValue();
//                            String varName = row.getCell(1).getStringCellValue();
//                            Object data = parseCell(row.getCell(2), dataType);
//                            redisTemplate.opsForValue().set(varName, data);
//                        }
//                    }
//
//
//                }
//            }
////            catch (FileNotFoundException e) {
////                throw new RuntimeException(e);
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
//        }
//
//
//
//    }
//
//    private Object parseCell(Cell cell, String dataType) {
//        switch (dataType) {
//            case "String":
//                return cell.getStringCellValue();
//            case "Integer":
//                return (int) cell.getNumericCellValue();
//            // 更多数据类型...
//            default:
//                return cell.toString();
//        }
//    }
//}
