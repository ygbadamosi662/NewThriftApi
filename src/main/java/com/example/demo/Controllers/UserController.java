package com.example.demo.Controllers;

import com.example.demo.Dtos.*;
import com.example.demo.Dtos.ResponseDtos.*;
import com.example.demo.Enums.*;
import com.example.demo.Models.*;
import com.example.demo.Repos.*;
import com.example.demo.Services.EmailService;
import com.example.demo.Services.JwtService;
import com.example.demo.Specifications.TransactionSpecs;
import jakarta.persistence.PersistenceException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserController
{
    private final JwtService jwtService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final UserRepo userRepo;

    @Autowired
    private final TransactionRepo transactionRepo;

    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtBlacklistRepo blackRepo;

    @GetMapping("/signout")
    public ResponseEntity<?> signOut(HttpServletRequest req)
    {
        String jwt = jwtService.setJwt(req);

        try
        {
            JwtBlacklist blacklisted = new JwtBlacklist();
            blacklisted.setJwt(jwt);
            blackRepo.save(blacklisted);
        }
        catch (PersistenceException e)
        {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("User is successfully signed out", HttpStatus.OK);
    }

    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileDto dto, HttpServletRequest req)
    {
        try {
            String jwt = jwtService.setJwt(req);
            User user = jwtService.getUser(jwt);

            user = dto.updateUser(user, userRepo, passwordEncoder, authenticationManager);

            if(user == null) {
                return new ResponseEntity<>(dto.getMessage(), HttpStatus.BAD_REQUEST);
            }
            UserResponseDto resDto = new UserResponseDto(user);
            return ResponseEntity.ok(resDto);
        } catch(BadCredentialsException e) {
            return new ResponseEntity<>("Password incorrect", HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            System.out.println(e);
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getUser(@Valid @RequestParam long id, HttpServletRequest req)
    {
        try {
            String jwt = jwtService.setJwt(req);
            User authUser = jwtService.getUser(jwt);
            List<Role> roles = new ArrayList<>();
            roles.add(Role.ADMIN);
            roles.add(Role.SUPER_ADMIN);

            boolean userExists = authUser.getId() != id || roles.contains(authUser.getRole()) ?
                    userRepo.existsById(id) : id == authUser.getId();
            if(!userExists) {
                return new ResponseEntity<>("User does not exist", HttpStatus.BAD_REQUEST);
            }

            User user = id != authUser.getId() ? userRepo.findById(id).get() : authUser;

            UserResponseDto resDto = new UserResponseDto(user);
            return ResponseEntity.ok(resDto);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/transaction/transactions")
    public ResponseEntity<?> getTransactions(@Valid @RequestBody TransactionsDto dto, HttpServletRequest req)
    {
        try {
            String jwt = jwtService.setJwt(req);
            User user = jwtService.getUser(jwt);
            Specification<Transaction> spec = Specification.where(null);
            spec = spec.and(TransactionSpecs.userEquals(user));

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

    @GetMapping("/transaction/get")
    public ResponseEntity<?> getTransaction(@Valid @RequestParam long id, HttpServletRequest req)
    {
        try {
            String jwt = jwtService.setJwt(req);
            User user = jwtService.getUser(jwt);

            Optional<Transaction> chk = transactionRepo.findByIdAndUser(id, user);
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

    @GetMapping("/wants")
    public ResponseEntity<?> deleteOrDeactivateAccount(@Valid @RequestParam String want, HttpServletRequest req)
    {
        try {
            List<String> wants = new ArrayList<>();
            wants.add(Status.DELETED.name());
            wants.add(Status.INACTIVE.name());
            if(!wants.contains(want)) {
                return new ResponseEntity<>("want can only be "+
                        Status.DELETED.name()+" or "+Status.INACTIVE.name(), HttpStatus.BAD_REQUEST);
            }
            String jwt = jwtService.setJwt(req);
            User user = jwtService.getUser(jwt);

            user.setStatus(Status.valueOf(want));
            user = userRepo.save(user);
            emailService.sendEmail(user.getEmail(),
                    "Account "+user.getStatus().name(),
                    "Your account have been succesfully "+user.getStatus().name());
            UserResponseDto resDto = new UserResponseDto(user);
            return ResponseEntity.ok(resDto);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Its not you, its us", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
