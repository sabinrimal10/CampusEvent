package com.edap.campusevents.config;

import com.edap.campusevents.security.StableCsrfTokenRepository;
import com.edap.campusevents.security.jwt.JwtAuthenticationFilter;
import com.edap.campusevents.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                     JwtService jwtService) throws Exception {
        http
                // No HttpSession is used for auth (SessionCreationPolicy.STATELESS below), so
                // the CSRF token has to live in its own cookie instead of the session. The
                // plain (non-XOR) request handler is required here: the default handler
                // BREACH-masks the token it hands to request attributes/forms, which would
                // never match a client that just echoes the raw cookie value back as a header.
                // StableCsrfTokenRepository keeps that cookie fixed once issued - see its
                // Javadoc for why that matters with stateless auth.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new StableCsrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/login", "/signup", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/new", "/events/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/events/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/events/*/attendees").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Unauthenticated browser requests get redirected to the login page; API
                // requests get a plain 401 instead of a redirect they can't follow.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> response.sendError(401, "Unauthorized"),
                                PathPatternRequestMatcher.pathPattern("/api/**"))
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) ->
                                response.addHeader(HttpHeaders.SET_COOKIE, jwtService.buildExpiredCookie().toString()))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
