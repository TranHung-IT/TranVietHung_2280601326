package com.example.TranVietHung_2280601326.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.TranVietHung_2280601326.daos.Item;
import com.example.TranVietHung_2280601326.models.Book;
import com.example.TranVietHung_2280601326.models.BookImage;
import com.example.TranVietHung_2280601326.models.BookVideo;
import com.example.TranVietHung_2280601326.services.BookService;
import com.example.TranVietHung_2280601326.services.CartService;
import com.example.TranVietHung_2280601326.services.CategoryService;
import com.example.TranVietHung_2280601326.services.ExcelService;
import com.example.TranVietHung_2280601326.services.FileStorageService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
@Validated
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final FileStorageService fileStorageService;
    private final ExcelService excelService;

    @GetMapping
    public String showAllBooks(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy) {
        model.addAttribute("books", bookService.getAllBooks(pageNo, pageSize, sortBy));
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totalPages", bookService.getAllBooks(pageNo, pageSize, sortBy).size() / pageSize);
        return "book/list";
    }

    // Them sach moi - Hien thi form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    // Them sach moi - Xu ly form
    @PostMapping("/add")
    public String addBook(@ModelAttribute("book") Book book, 
                         BindingResult result, 
                         Model model,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
        
        // Xử lý upload ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {
            int order = 0;
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty() && fileStorageService.isValidImageFile(imageFile)) {
                    try {
                        String imagePath = fileStorageService.storeFile(imageFile, "images");
                        BookImage bookImage = BookImage.builder()
                            .imageUrl(imagePath)
                            .imageName(imageFile.getOriginalFilename())
                            .displayOrder(order++)
                            .book(book)
                            .build();
                        book.getImages().add(bookImage);
                    } catch (IOException e) {
                        redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh: " + e.getMessage());
                        return "redirect:/books";
                    }
                }
            }
        }
        
        // Xử lý upload video
        if (videoFile != null && !videoFile.isEmpty() && fileStorageService.isValidVideoFile(videoFile)) {
            try {
                // Kiểm tra dung lượng (giới hạn khoảng 60s ~ 50MB tùy chất lượng)
                if (videoFile.getSize() > 50 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Video quá lớn (tối đa 50MB)");
                    return "redirect:/books";
                } else {
                    String videoPath = fileStorageService.storeFile(videoFile, "videos");
                    BookVideo bookVideo = BookVideo.builder()
                        .videoUrl(videoPath)
                        .videoName(videoFile.getOriginalFilename())
                        .durationSeconds(60) // Giới hạn 60s
                        .book(book)
                        .build();
                    book.setVideo(bookVideo);
                }
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi khi upload video: " + e.getMessage());
                return "redirect:/books";
            }
        }
        
        // Lưu book với images và video
        bookService.addBook(book);
        
        redirectAttributes.addFlashAttribute("success", "Thêm sách thành công!");
        return "redirect:/books";
    }

    // Xoa sach theo ID
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return "redirect:/books";
    }

    // Sua thong tin sach - Hien thi form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            model.addAttribute("book", book);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        return "redirect:/books";
    }

    // Sua thong tin sach - Xu ly form
    @PostMapping("/edit")
    public String updateBook(@Valid Book book, 
                            BindingResult result, 
                            Model model,
                            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                            @RequestParam(value = "deleteVideo", required = false) Boolean deleteVideo) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        
        // Lấy book hiện tại từ database
        Book existingBook = bookService.getBookById(book.getId());
        if (existingBook == null) {
            return "redirect:/books";
        }
        
        // Cập nhật thông tin cơ bản
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        
        // Xóa ảnh được chọn
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            existingBook.getImages().removeIf(img -> {
                if (deleteImageIds.contains(img.getId())) {
                    fileStorageService.deleteFile(img.getImageUrl());
                    return true;
                }
                return false;
            });
        }
        
        // Thêm ảnh mới
        if (imageFiles != null && !imageFiles.isEmpty()) {
            int maxOrder = existingBook.getImages().stream()
                .mapToInt(BookImage::getDisplayOrder)
                .max()
                .orElse(-1);
            
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty() && fileStorageService.isValidImageFile(imageFile)) {
                    try {
                        String imagePath = fileStorageService.storeFile(imageFile, "images");
                        BookImage bookImage = BookImage.builder()
                            .imageUrl(imagePath)
                            .imageName(imageFile.getOriginalFilename())
                            .displayOrder(++maxOrder)
                            .book(existingBook)
                            .build();
                        existingBook.getImages().add(bookImage);
                    } catch (IOException e) {
                        model.addAttribute("error", "Lỗi khi upload ảnh: " + e.getMessage());
                    }
                }
            }
        }
        
        // Xóa video nếu được yêu cầu
        if (Boolean.TRUE.equals(deleteVideo) && existingBook.getVideo() != null) {
            fileStorageService.deleteFile(existingBook.getVideo().getVideoUrl());
            existingBook.setVideo(null);
        }
        
        // Thêm/cập nhật video
        if (videoFile != null && !videoFile.isEmpty() && fileStorageService.isValidVideoFile(videoFile)) {
            try {
                if (videoFile.getSize() > 50 * 1024 * 1024) {
                    model.addAttribute("error", "Video quá lớn (tối đa 50MB)");
                } else {
                    // Xóa video cũ nếu có
                    if (existingBook.getVideo() != null) {
                        fileStorageService.deleteFile(existingBook.getVideo().getVideoUrl());
                    }
                    
                    String videoPath = fileStorageService.storeFile(videoFile, "videos");
                    if (existingBook.getVideo() != null) {
                        existingBook.getVideo().setVideoUrl(videoPath);
                        existingBook.getVideo().setVideoName(videoFile.getOriginalFilename());
                    } else {
                        BookVideo bookVideo = BookVideo.builder()
                            .videoUrl(videoPath)
                            .videoName(videoFile.getOriginalFilename())
                            .durationSeconds(60)
                            .book(existingBook)
                            .build();
                        existingBook.setVideo(bookVideo);
                    }
                }
            } catch (IOException e) {
                model.addAttribute("error", "Lỗi khi upload video: " + e.getMessage());
            }
        }
        
        bookService.updateBook(existingBook);
        return "redirect:/books";
    }

    // Hien thi chi tiet sach theo ID
    @GetMapping("/{id}")
    public String viewBookDetails(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            model.addAttribute("book", book);
            return "book/details";
        }
        return "redirect:/books";
    }
    
    // Them sach vao gio hang
    @PostMapping("/add-to-cart")
    public String addToCart(HttpSession session, 
                           @RequestParam long id, 
                           @RequestParam String name, 
                           @RequestParam double price, 
                           @RequestParam(defaultValue = "1") int quantity) {
        var cart = cartService.getCart(session);
        cart.addItems(new Item(id, name, price, quantity));
        cartService.updateCart(session, cart);
        return "redirect:/books";
    }
    
    // Tim kiem sach theo tu khoa
    @GetMapping("/search")
    public String searchBooks(@RequestParam("keyword") String keyword, Model model) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/books";
        }
        model.addAttribute("books", bookService.searchBooks(keyword));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        return "book/list";
    }
    
    // Export danh sach sach ra Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        try {
            List<Book> books = bookService.getAllBooks(0, Integer.MAX_VALUE, "id");
            ByteArrayInputStream in = excelService.exportToExcel(books);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=books_export.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Download file Excel template rong
    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        try {
            ByteArrayInputStream in = excelService.downloadTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=books_template.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Import sach tu file Excel
    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, 
                                  RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to import");
            return "redirect:/books";
        }
        
        try {
            List<Book> books = excelService.importFromExcel(file);
            
            // Lưu từng book vào database
            int successCount = 0;
            int errorCount = 0;
            
            for (Book book : books) {
                try {
                    if (book.getId() != null && bookService.getBookById(book.getId()) != null) {
                        // Update existing book
                        bookService.updateBook(book);
                    } else {
                        // Add new book
                        book.setId(null); // Ensure new ID is generated
                        bookService.addBook(book);
                    }
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("Error saving book: " + book.getTitle() + " - " + e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Import completed! Success: " + successCount + ", Failed: " + errorCount);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/books";
    }
}