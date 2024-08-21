package com.example.demo.Config;

import com.example.demo.Enums.Status;
import com.example.demo.Models.User;
import com.example.demo.ResponseStatus;
import com.example.demo.Services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter
{
    private final JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if(request.getServletPath().startsWith("/api/v1/auth"))
        {
//            System.out.println(request.getServletPath());
            filterChain.doFilter(request,response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or missing Authorization header");
            objectMapper.writeValue(response.getOutputStream(), unauthorized);
            response.flushBuffer();
            return;
        }

        String jwt = authHeader.substring("Bearer ".length());
        boolean blacklisted = jwtService.blacklisted(jwt);
        if(blacklisted) {
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED,
                    "This token is signed out, user should login again");
            objectMapper.writeValue(response.getOutputStream(), unauthorized);
            response.flushBuffer();
            return;
        }

        try {
            User user = jwtService.getUser(jwt);
            if(user.getStatus().equals(Status.INACTIVE) || user.getStatus().equals(Status.DELETED)) {
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED,
                        "Account is "+user.getStatus().name());
                objectMapper.writeValue(response.getOutputStream(), unauthorized);
                response.flushBuffer();
                return;
            }
            String email = user.getEmail();

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid JWT token");
                objectMapper.writeValue(response.getOutputStream(), unauthorized);
                response.flushBuffer();

                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.validateToken(userDetails, jwt)) {
                response.addHeader("Content-Type", "application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid JWT token");
                objectMapper.writeValue(response.getOutputStream(), unauthorized);
                response.flushBuffer();
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails.getUsername(),
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );
            authToken.setDetails(request);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Continue the filter chain
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED,
                    e.getMessage()+" get a new access token here /api/v1/auth/refresh-token");
            objectMapper.writeValue(response.getOutputStream(), unauthorized);
            response.flushBuffer();
        } catch(Exception e) {
            response.addHeader("Content-Type", "application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ResponseStatus unauthorized = new ResponseStatus(HttpServletResponse.SC_UNAUTHORIZED,
                    e.getMessage());
            objectMapper.writeValue(response.getOutputStream(), unauthorized);
            response.flushBuffer();
        }

    }
}
