package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class EligibleVoucherResponse {
    private long id;

    private String code;

    private String title;

    private String discountType;

    private BigDecimal discountPreview;

}
