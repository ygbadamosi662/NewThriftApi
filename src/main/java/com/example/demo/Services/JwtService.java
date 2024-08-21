package com.example.demo.Services;

import com.example.demo.Models.JwtBlacklist;
import com.example.demo.Models.User;
import com.example.demo.Repos.JwtBlacklistRepo;
import com.example.demo.Repos.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class JwtService
{
    private final String SECRET_KEY = "secretwayneyusufffffffffffffff" +
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
            "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
            "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JwtBlacklistRepo jwtBlacklistRepo;
    private String jwt;

    public String generateJwt(User user)
    {
        return generateJwt(user, new HashMap<>(), new Date(System.currentTimeMillis() + 1000*60*60*24*7));
    }

    public String generateJwt(User user, Map<String, Object> extraClaims, Date expiry)
    {
        return Jwts
                .builder()
                .addClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuer("gk")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expiry)
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();

    }

    public boolean validateToken(UserDetails user, String jwt)
    {
        String sub = getSubject(jwt);
        return sub.equals(user.getUsername()) && jwtNotExpired(jwt);
    }

    public String getSubject(String jwt)
    {
        return extractClaim(jwt, Claims::getSubject);
    }

    public Date getExpiration(String jwt)
    {
        return extractClaim(jwt,Claims::getExpiration);
    }

    public boolean jwtNotExpired(String jwt)
    {
        return getExpiration(jwt).after(new Date(System.currentTimeMillis()));
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver)
    {
        Claims allClaims = getAllClaims(token);
        return claimsResolver.apply(allClaims);
    }

    public Claims getAllClaims(String token)
    {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey()
    {
        byte[] key = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(key);
    }

    public User getUser(String jwt)
    {
        return userRepository.findByEmail(this.getSubject(jwt)).get();
    }

    public boolean blacklisted(String jw)
    {
//        returns true if jwt has been blacklisted;
        try
        {
            Optional<JwtBlacklist> blacklisted = jwtBlacklistRepo.findByJwt(jw);
            if(blacklisted.isPresent())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (NoResultException e)
        {
            return false;
        }
    }

    public String setJwt(HttpServletRequest req)
    {
//        also returns the jwt
        if(req != null)
        {
            this.jwt = req.getHeader("Authorization").substring("Bearer ".length());
        }

        return this.jwt;
    }
}