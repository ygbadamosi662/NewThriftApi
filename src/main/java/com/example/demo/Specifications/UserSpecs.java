package com.example.demo.Specifications;

import com.example.demo.Enums.Gender;
import com.example.demo.Enums.Role;
import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class UserSpecs
{
    public static Specification<User> fnameEquals(String fname) {
        return (root, query, cb) -> cb.equal(root.get("fname"), fname);
    }

    public static Specification<User> lnameEquals(String lname) {
        return (root, query, cb) -> cb.equal(root.get("lname"), lname);
    }

    public static Specification<User> genderEquals(Gender gender) {
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    public static Specification<User> roleEquals(Role role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> statusEquals(Status status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<User> createdBefore(LocalDateTime before) {
        return (root, query, cb) -> cb.lessThan(root.get("createdOn"), before);
    }

    public static Specification<User> searchUsers(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return null;
            }
            String pattern = "%" + search + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fname")), pattern.toLowerCase()),
                    cb.like(cb.lower(root.get("lname")), pattern.toLowerCase()),
                    cb.like(cb.lower(root.get("email")), pattern.toLowerCase()),
                    cb.like(cb.lower(root.get("phone")), pattern.toLowerCase())
            );
        };
    }
}
