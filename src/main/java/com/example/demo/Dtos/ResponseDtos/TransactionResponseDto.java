package com.example.demo.Dtos.ResponseDtos;

import com.example.demo.Enums.TransactionStatus;
import com.example.demo.Models.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponseDto
{
    private long id;

    private TransactionStatus status;

    private double paid;

    private long user_id;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    public TransactionResponseDto() {}

    public TransactionResponseDto(Transaction trans)
    {
        this.id = trans.getId();
        this.status = trans.getStatus();
        this.paid = trans.getPaid();
        this.user_id = trans.getUser().getId();
        this.createdOn = trans.getCreatedOn();
        this.updatedOn = trans.getUpdatedOn();
    }
}
