package com.example.demo.Specifications;

import com.example.demo.Enums.TransactionStatus;
import com.example.demo.Models.Transaction;
import com.example.demo.Models.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TransactionSpecs
{
    public static Specification<Transaction> statusEquals(TransactionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> paidEquals(double paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Transaction> paidGreater(double paid) {
        return (root, query, cb) -> cb.greaterThan(root.get("paid"), paid);
    }

    public static Specification<Transaction> paidLess(double paid) {
        return (root, query, cb) -> cb.lessThan(root.get("paid"), paid);
    }

    public static Specification<Transaction> userEquals(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Transaction> createdBefore(LocalDateTime before) {
        return (root, query, cb) -> cb.lessThan(root.get("createdOn"), before);
    }
}
