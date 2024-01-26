package com.example.showtime.auth.jwt.filter;

import com.example.showtime.admin.service.imp.AdminDetailsServiceImplementation;
import com.example.showtime.auth.jwt.utlis.JwtUtil;
import com.example.showtime.user.enums.UserRole;
import com.example.showtime.user.service.imp.UserDetailsServiceImplementation;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImplementation userDetailsService;
    private final AdminDetailsServiceImplementation adminDetailsService;

    private final JwtUtil jwtTokenUtil;

    public JwtFilter(UserDetailsServiceImplementation userDetailsService, AdminDetailsServiceImplementation adminDetailsService, JwtUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.adminDetailsService = adminDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            int role = Integer.parseInt(jwtTokenUtil.getUserRoleFromToken(jwtToken));

            UserDetails userDetails = null;

            if (role == UserRole.ADMIN.getValue()) {
                userDetails = this.adminDetailsService.loadUserByUsername(username);
            } else if (role == UserRole.USER.getValue()) {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            }

            // if token is valid configure Spring Security to manually set
            // authentication
            if ((userDetails != null) && jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}
