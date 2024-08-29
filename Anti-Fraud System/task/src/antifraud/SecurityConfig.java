package antifraud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures security settings for the application, including authentication,
 * authorization rules, and password encoding.
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .httpBasic()
                .and()
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll() // Allow shutdown without authentication
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll() // Allow user registration
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT") // Only ADMINISTRATOR and SUPPORT roles can access /list
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*").hasRole("ADMINISTRATOR") // Only ADMINISTRATOR can delete users
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role/**").hasRole("ADMINISTRATOR") // Only ADMINISTRATOR can change roles
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access/**").hasRole("ADMINISTRATOR") // Only ADMINISTRATOR can lock/unlock users
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction/**").hasRole("MERCHANT") // Only MERCHANT can process transactions
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT") // Only SUPPORT can add suspicious IPs
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/**").hasRole("SUPPORT") // Only SUPPORT can delete suspicious IPs
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip").hasRole("SUPPORT") // Only SUPPORT can view suspicious IPs
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard").hasRole("SUPPORT") // Only SUPPORT can add stolen cards
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/**").hasRole("SUPPORT") // Only SUPPORT can delete stolen cards
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard").hasRole("SUPPORT") // Only SUPPORT can view stolen cards
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasRole("SUPPORT")
                        .requestMatchers("/api/antifraud/history/**").hasRole("SUPPORT")


                        .anyRequest().authenticated()
                )

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
