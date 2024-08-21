package com.example.demo.Repos;

import com.example.demo.Enums.Role;
import com.example.demo.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long>
{
    Optional<User> findByEmail(String email);

    Slice<User> findByRole(Role role, Pageable pageable);

    boolean existsByPhone(String phone);

    Optional<User> findByToken(String token);

    boolean existsByEmail(String email);

    boolean existsByRole(Role superAdmin);

    long count(Specification<User> spec);


    Page<User> findAll(Specification<User> spec, Pageable pageable);

    List<User> findAll(Specification<User> spec);
}
