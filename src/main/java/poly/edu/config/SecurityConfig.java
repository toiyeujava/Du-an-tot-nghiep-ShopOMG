package poly.edu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để test cho dễ (Bật lại sau nếu cần)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/account/sign-up", "/register", "/login",
                                "/forgot-password", "/reset-password",
                                "/verify-email", "/verify-email-sent", "/resend-verification",
                                "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**")
                        .permitAll()
                        .requestMatchers("/account/**", "/checkout/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(login -> login
                        .loginPage("/login") // URL controller trả về view login
                        .loginProcessingUrl("/login") // URL form submit (POST)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // Điều hướng theo vai trò sau khi đăng nhập thành công
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/home");
                            }
                        })
                        .failureHandler((request, response, exception) -> {
                            String email = request.getParameter("email");
                            String contextPath = request.getContextPath();

                            // Kiểm tra nếu tài khoản bị disable (chưa verify email)
                            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                response.sendRedirect(
                                        contextPath + "/login?notVerified=true&email=" + (email != null ? email : ""));
                            }
                            // Kiểm tra nếu tài khoản bị khóa
                            else if (exception instanceof org.springframework.security.authentication.LockedException) {
                                response.sendRedirect(
                                        contextPath + "/login?locked=true&email=" + (email != null ? email : ""));
                            }
                            // Đăng nhập sai - hiển thị số lần còn lại
                            else {
                                response.sendRedirect(
                                        contextPath + "/login?error=true&email=" + (email != null ? email : ""));
                            }
                        })
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("shopOMGRememberKey")
                        .userDetailsService(userDetailsService)
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/403")); // Tạo trang 403 sau

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
