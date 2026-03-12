package poly.edu.config;

import lombok.RequiredArgsConstructor;
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
import poly.edu.security.CustomOAuth2UserService;

import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        		// Exclude SePay webhook from CSRF — it is called by SePay servers (not the browser)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/webhook/**"))
                .userDetailsService(userDetailsService)
                // FORCE EAGER CSRF TOKEN LOADING TO FIX THYMELEAF SESSION COMMIT EXCEPTIONS
                .addFilterAfter(new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                            FilterChain filterChain) throws ServletException, IOException {
                        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                        if (csrfToken != null) {
                            csrfToken.getToken(); // Forces generation and session creation
                        }
                        filterChain.doFilter(request, response);
                    }
                }, CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sales/**").hasAnyRole("SALES", "ADMIN")
                        .requestMatchers("/warehouse/**").hasAnyRole("WAREHOUSE", "ADMIN")
                         .requestMatchers("/account/sign-up", "/register", "/login",
                                "/forgot-password", "/reset-password",
                                "/verify-email", "/verify-email-sent", "/resend-verification",
                                "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**",
                                "/api/webhook/**",   // SePay calls this — no auth needed
                                "/api/payment-events/**" // SSE — browser connects before auth check
                         )
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
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedPage("/403"));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler commonSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isSales = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SALES"));
            boolean isWarehouse = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_WAREHOUSE"));

            String contextPath = request.getContextPath();
            String redirectUrl = "/home";

            if (isAdmin) {
                redirectUrl = "/admin/dashboard";
            } else if (isSales) {
                redirectUrl = "/sales/dashboard";
            } else if (isWarehouse) {
                redirectUrl = "/warehouse/dashboard";
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