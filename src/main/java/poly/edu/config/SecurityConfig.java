package poly.edu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import poly.edu.security.CustomOAuth2UserService; // Đảm bảo bạn đã tạo class này

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService; // 1. Tiêm service xử lý OAuth2

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.disable())
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
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(commonSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            String email = request.getParameter("email");
                            String contextPath = request.getContextPath();
                            if (exception instanceof org.springframework.security.authentication.DisabledException) {
                                response.sendRedirect(
                                        contextPath + "/login?notVerified=true&email=" + (email != null ? email : ""));
                            } else if (exception instanceof org.springframework.security.authentication.LockedException) {
                                response.sendRedirect(
                                        contextPath + "/login?locked=true&email=" + (email != null ? email : ""));
                            } else {
                                response.sendRedirect(
                                        contextPath + "/login?error=true&email=" + (email != null ? email : ""));
                            }
                        })
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        // 2. Cấu hình điểm cuối nhận diện UserInfo để lưu vào DB
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(commonSuccessHandler()))
                .rememberMe(remember -> remember
                        .key("shopOMGRememberKey")
                        .userDetailsService(userDetailsService)
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler commonSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            String contextPath = request.getContextPath();
            String redirectUrl = "/home";

            if (isAdmin) {
                redirectUrl = "/admin/dashboard";
            } else {
                // Check for pending redirect (guest tried to access restricted page)
                jakarta.servlet.http.HttpSession session = request.getSession(false);
                if (session != null) {
                    String pendingRedirect = (String) session.getAttribute("redirectAfterLogin");
                    if (pendingRedirect != null && !pendingRedirect.isEmpty()) {
                        redirectUrl = pendingRedirect;
                        session.removeAttribute("redirectAfterLogin");

                        // Note: Pending cart action will be processed by the product detail page
                        // or we could process it here, but it's cleaner to let the user review first
                    }
                }
            }

            response.sendRedirect(contextPath + redirectUrl);
        };
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