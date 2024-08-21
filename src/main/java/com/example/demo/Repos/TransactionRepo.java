package com.example.demo.Repos;

import com.example.demo.Models.Transaction;
import com.example.demo.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Long>
{
    long count(Specification<Transaction> spec);

    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    List<Transaction> findAll(Specification<Transaction> spec);

    Optional<Transaction> findByIdAndUser(long id, User user);
}
