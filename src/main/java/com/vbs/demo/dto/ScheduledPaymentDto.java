package com.vbs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledPaymentDto {
    int userId;
    String recipientUsername;
    double amount;
    String label;
    String frequency;
    int dayOfMonth;
}
