package com.example.demo.Controllers;

import com.example.demo.Dtos.*;
import com.example.demo.Dtos.ResponseDtos.TransactionResponseDto;
import com.example.demo.Dtos.ResponseDtos.UserResponseDto;
import com.example.demo.Enums.GreaterOrLess;
import com.example.demo.Enums.Role;
import com.example.demo.Models.Transaction;
import com.example.demo.Models.User;
import com.example.demo.Repos.*;
import com.example.demo.Services.EmailService;
import com.example.demo.Services.JwtService;
import com.example.demo.Specifications.TransactionSpecs;
import com.example.demo.Specifications.UserSpecs;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController
{
    private final EmailService emailService;

    private final JwtService jwtService;

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final TransactionRepo transactionRepo;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/super/role/update")
    public ResponseEntity<?> updateRole(@Valid @RequestBody UpdateRoleDto dto, HttpServletRequest req)
    {
        try {
            String jwt = jwtService.setJwt(req);
            User authUser = jwtService.getUser(jwt);
            Optional<User> chk = userRepo.findByEmail(dto.getEmail());
            if(chk.isEmpty()) {
                return new ResponseEntity<>("User does not exist", HttpStatus.BAD_REQUEST);
            }
            User user = chk.get();
            if(!authUser.getRole().equals(Role.SUPER_ADMIN)) {
                return new ResponseEntity<>("Invalid Credentials", HttpStatus.BAD_REQUEST);
            }
            if(user.getRole().equals(dto.getRole())) {
                String msg = "User is already "+ (user.getRole().equals(Role.SUPER_ADMIN) ?
                        "a " : "an ")+user.getRole().name();
                return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
            }
            user.setRole(dto.getRole());
            user = userRepo.save(user);
            String body = "Your account role have been updated to "+user.getRole().name();
            emailService.sendEmail(user.getEmail(), "Role updated", body);

            UserResponseDto resDto = new UserResponseDto(user);

            return ResponseEntity.ok(resDto);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> getUsers(@Valid @RequestBody UsersDto dto)
    {
        try {
            Specification<User> spec = Specification.where(null);

            boolean all = true;

            if (dto.getFname() != null) {
                spec = spec.and(UserSpecs.fnameEquals(dto.getFname()));
                all = false;
            }
            if(dto.getLname() != null) {
                spec = spec.and(UserSpecs.lnameEquals(dto.getLname()));
                if(all) {
                    all = false;
                }
            }
            if(dto.getGender() != null) {
                spec = spec.and(UserSpecs.genderEquals(dto.getGender()));
                if(all) {
                    all = false;
                }
            }
            if(dto.getStatus() != null) {
                spec = spec.and(UserSpecs.statusEquals(dto.getStatus()));
                if(all) {
                    all = false;
                }
            }
            if(dto.getRole() != null) {
                spec = spec.and(UserSpecs.roleEquals(dto.getRole()));
                if(all) {
                    all = false;
                }
            }
            if(dto.getLastHours() == 0) {
                LocalDateTime before = LocalDateTime.now();
                before = before.minusHours(dto.getLastHours());
                spec = spec.and(UserSpecs.createdBefore(before));
                if(all) {
                    all = false;
                }
            }

            if(dto.getCount()) {
                long count = userRepo.count(spec);
                return ResponseEntity.ok(count);
            } else {
                Pageable pageRequest = PageRequest.of(dto.getPage() - 1,
                        dto.getSize(), Sort.by("createdOn").descending());
                Page<User> usersPage = null;
                if(all) {
                    usersPage = userRepo.findAll(pageRequest);
                } else {
                    usersPage = userRepo.findAll(spec, pageRequest);
                }
                List<User> users = usersPage.getContent();

                List<UserResponseDto> userDtos = new ArrayList<>();

                users
                        .stream()
                        .map((user) -> {
                            UserResponseDto userRes = new UserResponseDto(user);
                            userDtos.add(userRes);
                            return user;
                        })
                        .collect(Collectors.toList());;

                Map<String, Object> resDto = new HashMap<>();
                resDto.put("users", userDtos);
                resDto.put("total", usersPage.getTotalElements());
                resDto.put("totalPages", usersPage.getTotalPages());
                resDto.put("haveNextPage", usersPage.hasNext());
                resDto.put("havePrevPage", usersPage.hasPrevious());
                resDto.put("currentPage", dto.getPage());
                resDto.put("size", usersPage.getSize());

                return ResponseEntity.ok(resDto);
            }

        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@Valid @RequestParam String search)
    {
        try {
            Specification<User> spec = Specification.where(null);
            spec = spec.and(UserSpecs.searchUsers(search));

            List<User> users = userRepo.findAll(spec);

            List<UserResponseDto> userDtos = new ArrayList<>();

            users
                    .stream()
                    .map((user) -> {
                        UserResponseDto userRes = new UserResponseDto(user);
                        userDtos.add(userRes);
                        return user;
                    })
                    .collect(Collectors.toList());;

            return ResponseEntity.ok(userDtos);

        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/transaction/get")
    public ResponseEntity<?> getTransaction(@Valid @RequestParam long id, HttpServletRequest req)
    {
        try {
            Optional<Transaction> chk = transactionRepo.findById(id);
            if(chk.isPresent() == false) {
                return new ResponseEntity<>("Transaction does not exist", HttpStatus.BAD_REQUEST);
            }

            Transaction transaction = chk.get();

            TransactionResponseDto resDto = new TransactionResponseDto(transaction);
            return ResponseEntity.ok(resDto);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transaction/transactions")
    public ResponseEntity<?> getTransactions(@Valid @RequestBody TransactionsDto dto)
    {
        try {
            Specification<Transaction> spec = Specification.where(null);

            boolean all = true;

            if(dto.getStatus() != null) {
                spec = spec.and(TransactionSpecs.statusEquals(dto.getStatus()));
                if(all == true) {
                    all = false;
                }
            }

            if(dto.getPaid() != null) {
                Map paidMap = dto.getPaid();
                if(paidMap.get("greaterOrLess").equals(GreaterOrLess.EQUAL.name())) {
                    double paid = (double) paidMap.get("paid");
                    spec = spec.and(TransactionSpecs.paidEquals(paid));
                }
                if(paidMap.get("greaterOrLess").equals(GreaterOrLess.GREATER.name())) {
                    double paid = (double) paidMap.get("paid");
                    spec = spec.and(TransactionSpecs.paidGreater(paid));
                }
                if(paidMap.get("greaterOrLess").equals(GreaterOrLess.LESS.name())) {
                    double paid = (double) paidMap.get("paid");
                    spec = spec.and(TransactionSpecs.paidLess(paid));
                }
                if(all == true) {
                    all = false;
                }
            }
            if(dto.getLastHours() == 0) {
                LocalDateTime before = LocalDateTime.now();
                before = before.minusHours(dto.getLastHours());
                spec = spec.and(TransactionSpecs.createdBefore(before));
                if(all == true) {
                    all = false;
                }
            }

            if(dto.getCount()) {
                long count = transactionRepo.count(spec);
                return ResponseEntity.ok(count);
            } else {
                Pageable pageRequest = PageRequest.of(dto.getPage() - 1,
                        dto.getSize(), Sort.by("createdOn").descending());
                Page<Transaction> transactionsPage = null;
                if(all) {
                    transactionsPage = transactionRepo.findAll(pageRequest);
                } else {
                    transactionsPage = transactionRepo.findAll(spec, pageRequest);
                }
                List<Transaction> transactions = transactionsPage.getContent();

                List<TransactionResponseDto> transactionDtos = new ArrayList<>();

                transactions
                        .stream()
                        .map((transaction) -> {
                            TransactionResponseDto transactionRes = new TransactionResponseDto(transaction);
                            transactionDtos.add(transactionRes);
                            return transaction;
                        })
                        .collect(Collectors.toList());;

                Map<String, Object> resDto = new HashMap<>();
                resDto.put("transactions", transactionDtos);
                resDto.put("total", transactionsPage.getTotalElements());
                resDto.put("totalPages", transactionsPage.getTotalPages());
                resDto.put("haveNextPage", transactionsPage.hasNext());
                resDto.put("havePrevPage", transactionsPage.hasPrevious());
                resDto.put("currentPage", dto.getPage());
                resDto.put("size",transactionsPage.getSize());

                return ResponseEntity.ok(resDto);
            }

        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
