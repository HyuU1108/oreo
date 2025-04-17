package com.oreo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // *** @Configuration 확인! ***
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 임포트 확인
import org.springframework.security.web.SecurityFilterChain;

@Configuration // *** 이 어노테이션이 반드시 있어야 합니다! ***
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // ... filterChain 설정 내용 ...
        http
            // ... csrf, authorizeHttpRequests 등 ...
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
             )
             .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
             );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); 
    }
}