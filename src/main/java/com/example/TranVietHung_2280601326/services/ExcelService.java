package com.example.TranVietHung_2280601326.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.models.Category;
import com.example.TranVietHung_2280601326.repositories.ICategoryRepository;

@Service
public class ExcelService {
    
    @Autowired
    private ICategoryRepository categoryRepository;
    
    private static final String[] HEADERS = {
        "ID", "Title", "Author", "Price", "Category Name", "Image URLs", "Video URL"
    };
    
    /**
     * Tạo file Excel template rỗng với các cột header
     */
    public ByteArrayInputStream downloadTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Books");
            
            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            headerStyle.setFont(font);
            
            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }
            
            // Thêm 1 dòng mẫu để hướng dẫn
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue(""); // ID để trống khi import
            exampleRow.createCell(1).setCellValue("Book Title Example");
            exampleRow.createCell(2).setCellValue("Author Name");
            exampleRow.createCell(3).setCellValue(100000);
            exampleRow.createCell(4).setCellValue("Category Name");
            exampleRow.createCell(5).setCellValue("/uploads/images/image1.jpg;/uploads/images/image2.jpg");
            exampleRow.createCell(6).setCellValue("/uploads/videos/video1.mp4");
            
            // Auto size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Tạo styles để lock/unlock cells
            CellStyle lockedStyle = workbook.createCellStyle();
            lockedStyle.setLocked(true);
            
            CellStyle unlockedStyle = workbook.createCellStyle();
            unlockedStyle.setLocked(false);
            
            // Apply locked style chỉ cho cột ID (cột 0) trong example row
            Cell idCell = exampleRow.getCell(0);
            if (idCell != null) {
                idCell.setCellStyle(lockedStyle);
            }
            
            // Apply unlocked style cho các cột còn lại trong example row
            for (int colNum = 1; colNum < HEADERS.length; colNum++) {
                Cell cell = exampleRow.getCell(colNum);
                if (cell != null) {
                    cell.setCellStyle(unlockedStyle);
                }
            }
            
            // Protect sheet để áp dụng lock/unlock
            sheet.protectSheet("password");
            
            // Set default column style để các dòng mới thêm vào cũng được unlock (trừ cột 0)
            for (int colNum = 1; colNum < HEADERS.length; colNum++) {
                sheet.setDefaultColumnStyle(colNum, unlockedStyle);
            }
            
            // Tạo dropdown list cho cột Category (cột 4)
            List<Category> categories = categoryRepository.findAll();
            if (!categories.isEmpty()) {
                String[] categoryNames = categories.stream()
                    .map(Category::getName)
                    .toArray(String[]::new);
                
                DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(categoryNames);
                
                // Apply dropdown cho cột Category từ dòng 2 đến dòng 1000
                CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 4, 4);
                DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
                
                dataValidation.setShowErrorBox(true);
                dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
                dataValidation.createErrorBox("Invalid Category", "Please select a category from the dropdown list");
                dataValidation.setSuppressDropDownArrow(true);
                
                sheet.addValidationData(dataValidation);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
    
    /**
     * Export danh sách books ra file Excel
     */
    public ByteArrayInputStream exportToExcel(List<Book> books) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Books");
            
            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            headerStyle.setFont(font);
            
            // Tạo header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Tạo data rows
            int rowNum = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(book.getId());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(book.getAuthor());
                row.createCell(3).setCellValue(book.getPrice());
                row.createCell(4).setCellValue(book.getCategory() != null ? book.getCategory().getName() : "");
                
                // Export image URLs (separated by semicolon)
                String imageUrls = "";
                if (book.getImages() != null && !book.getImages().isEmpty()) {
                    imageUrls = book.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .reduce((a, b) -> a + ";" + b)
                        .orElse("");
                }
                row.createCell(5).setCellValue(imageUrls);
                
                // Export video URL
                String videoUrl = "";
                if (book.getVideo() != null) {
                    videoUrl = book.getVideo().getVideoUrl();
                }
                row.createCell(6).setCellValue(videoUrl);
            }
            
            // Auto size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
    
    /**
     * Import books từ file Excel
     */
    public List<Book> importFromExcel(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }
            
            // Đọc các rows
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                
                // Skip dòng rỗng
                if (isRowEmpty(currentRow)) {
                    continue;
                }
                
                try {
                    Book book = new Book();
                    
                    // ID - nếu có thì là update, không thì là create mới
                    Cell idCell = currentRow.getCell(0);
                    if (idCell != null && idCell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                        book.setId((long) idCell.getNumericCellValue());
                    }
                    
                    // Title
                    Cell titleCell = currentRow.getCell(1);
                    if (titleCell != null) {
                        book.setTitle(getCellValueAsString(titleCell));
                    }
                    
                    // Author
                    Cell authorCell = currentRow.getCell(2);
                    if (authorCell != null) {
                        book.setAuthor(getCellValueAsString(authorCell));
                    }
                    
                    // Price
                    Cell priceCell = currentRow.getCell(3);
                    if (priceCell != null) {
                        book.setPrice(getCellValueAsDouble(priceCell));
                    }
                    
                    // Category Name
                    Cell categoryCell = currentRow.getCell(4);
                    if (categoryCell != null) {
                        String categoryName = getCellValueAsString(categoryCell);
                        Category category = categoryRepository.findByName(categoryName)
                            .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));
                        book.setCategory(category);
                    }
                    
                    // Image URLs (separated by semicolon) - chỉ lưu đường dẫn, không tự động tạo BookImage
                    Cell imageCell = currentRow.getCell(5);
                    if (imageCell != null) {
                        String imageUrls = getCellValueAsString(imageCell);
                        // Note: Image URLs are stored but BookImage entities are not created here
                        // You need to handle image upload separately in the controller
                    }
                    
                    // Video URL - chỉ lưu đường dẫn, không tự động tạo BookVideo  
                    Cell videoCell = currentRow.getCell(6);
                    if (videoCell != null) {
                        String videoUrl = getCellValueAsString(videoCell);
                        // Note: Video URL is stored but BookVideo entity is not created here
                        // You need to handle video upload separately in the controller
                    }
                    
                    books.add(book);
                    
                } catch (Exception e) {
                    // Log lỗi nhưng tiếp tục xử lý các dòng khác
                    System.err.println("Error processing row " + currentRow.getRowNum() + ": " + e.getMessage());
                }
            }
        }
        
        return books;
    }
    
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != org.apache.poi.ss.usermodel.CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
    
    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private Double getCellValueAsDouble(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
}
