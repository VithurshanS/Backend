package com.tutoring.Tutorverse.SecurityConfigs;


import com.tutoring.Tutorverse.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    @Autowired
//    private CustomOAuth2UserService customOAuth2UserService;

//    @Autowired
//    private CustomOidcUserService customOidcUserService;

    @Autowired
    private userRepository userRepo;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for API endpoints
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/**").permitAll() // allow all API endpoints without authentication
                        .requestMatchers("/login", "/oauth2/**", "/error").permitAll() // allow OAuth2 endpoints and error page
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // allow OpenAPI/Swagger UI
                        .anyRequest().authenticated()
                )
//                .oauth2Login(oauth2 -> oauth2
//                        .defaultSuccessUrl("/home", true)
//                        .authorizationEndpoint(authorization -> authorization
//                                .authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository))
//                        )
//                        .userInfoEndpoint(userInfo -> {
//                            userInfo.userService(customOAuth2UserService);
//                            userInfo.oidcUserService(customOidcUserService);
//                        })
//                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();
                            // For API endpoints, return 401 instead of redirecting to OAuth2
                            if (requestURI.startsWith("/api/")) {
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
                            } else {
                                // For web endpoints, redirect to OAuth2 login
                                response.sendRedirect("/oauth2/authorization/google");
                            }
                        })
                );
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

//    @Bean
//    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
//            ClientRegistrationRepository clientRegistrationRepository) {
//        return new CustomAuthorizationRequestResolver(clientRegistrationRepository);
//    }
}
