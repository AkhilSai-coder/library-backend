package com.griet.library.security;

import com.griet.library.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow CORS preflight
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip authentication for auth endpoints
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // Ensure token format is correct
        if (token.split("\\.").length != 3) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String collegeId = jwtService.extractUsername(token);

            if (collegeId != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                var user = userRepository.findByCollegeId(collegeId).orElse(null);

                if (user != null) {
                    var authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole())
                    );

                    var authToken = new UsernamePasswordAuthenticationToken(
                            user.getCollegeId(),
                            null,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception ignored) {}

        filterChain.doFilter(request, response);
    }

}
