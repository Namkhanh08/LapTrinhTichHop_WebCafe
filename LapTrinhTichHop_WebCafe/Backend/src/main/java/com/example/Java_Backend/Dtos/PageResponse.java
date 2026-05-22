package com.example.Java_Backend.Dtos;

import java.util.List;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class PageResponse<T> {
    private List<T> items;
    private int totalItems;
    private int page;
    private int pageSize;

    public PageResponse(List<T> items, int totalItems, int page, int pageSize) {
        this.items = items;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
    }
}