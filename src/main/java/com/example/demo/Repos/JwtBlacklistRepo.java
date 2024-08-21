package com.example.demo.Repos;

import com.example.demo.Models.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtBlacklistRepo extends JpaRepository<JwtBlacklist,Long>
{
    Optional<JwtBlacklist> findByJwt(String jwt);
}
