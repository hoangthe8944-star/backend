package com.example.beatboxcompany.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SubscriptionDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String duration;
    private List<String> features;
    private String status;
}