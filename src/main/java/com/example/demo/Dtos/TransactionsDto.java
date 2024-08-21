package com.example.demo.Dtos;

import com.example.demo.Enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TransactionsDto
{
    private boolean count = false;

    private TransactionStatus status;

    private Map<String, Object> paid;

    private int lastHours = 0;

    private int page = 1;

    private int size = 3;

    public boolean getCount() {
        return this.count;
    }
}
