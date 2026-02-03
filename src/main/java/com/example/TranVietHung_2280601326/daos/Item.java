package com.example.TranVietHung_2280601326.daos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private Long bookId;
    private String bookName;
    private double price;
    private int quantity;
}
