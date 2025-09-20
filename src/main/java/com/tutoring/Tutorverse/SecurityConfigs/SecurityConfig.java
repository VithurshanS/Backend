package com.tutoring.Tutorverse.SecurityConfigs;


import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.CustomOAuth2RequestServices;
import com.tutoring.Tutorverse.Services.Oauth2SuccessHandlerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Autowired
    @Lazy
    private userRepository userRepo;
    @Autowired
    @Lazy
    private CustomOAuth2RequestServices customOAuth2RequestServices;
    @Autowired
    @Lazy
    private Oauth2SuccessHandlerServices oAuth2AuthenticationSuccessHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Arrays.asList(frontendUrl));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    config.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));
                    config.setMaxAge(3600L);
                    return config;
                }))
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**","/").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/login", "/oauth2/**", "/error").permitAll()
                        .requestMatchers("/home/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(customOAuth2RequestServices)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
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
