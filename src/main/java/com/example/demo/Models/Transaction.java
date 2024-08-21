package com.example.demo.Models;

import com.example.demo.Enums.OrderStatus;
import com.example.demo.Enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Transaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column( name="transaction_id")
    private long id;

    private TransactionStatus status;

    private double paid = 0;

    @ManyToOne
    @JoinColumn(name = "user")
    private User user;

    @CreationTimestamp
    private LocalDateTime createdOn;

    @UpdateTimestamp
    private LocalDateTime updatedOn;
}
