package org.example.hospital.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        authorities.forEach(authority -> {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                try {
                    response.sendRedirect("/admin/page");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if(authority.getAuthority().equals("ROLE_PATIENT")){
                try {
                    response.sendRedirect("/patient");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (authority.getAuthority().equals("ROLE_DOCTOR")) {
                try {
                    response.sendRedirect("/doctor");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (authority.getAuthority().equals("ROLE_SUPER_ADMIN")){
                try {
                    response.sendRedirect("/super/doctors");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
