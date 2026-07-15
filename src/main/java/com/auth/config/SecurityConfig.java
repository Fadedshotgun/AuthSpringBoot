package com.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth.config.ratelimits.LoginRateLimitFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private LoginRateLimitFilter loginRateLimitFilter;
	
	@Bean
	SecurityFilterChain defaultFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.csrf(csrf->csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests((auth)-> auth
						.requestMatchers("/api/owner/**").hasRole("OWNER")
						.requestMatchers("/api/user/**").hasRole("USER")
						.requestMatchers("/api/public/**", "/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().permitAll()
						)
				//.httpBasic(Customizer.withDefaults())
				.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.formLogin((form) -> form
						.loginProcessingUrl("/api/public/login")
						.successHandler((request, response, auth) -> {
							response.setStatus(HttpStatus.OK.value());
							response.setContentType("application/json");
							response.getWriter().write("{\"message\":\"Login successful!\"}");
						})
						.failureHandler((request, response, exception) -> {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.setContentType("application/json");
							response.getWriter().write("{\"message\":\"Login failed :(\"}");
						})
						.permitAll()
						)
				.exceptionHandling(e -> e.authenticationEntryPoint((request, response, ex) -> {
					response.setStatus(HttpStatus.UNAUTHORIZED.value());
				}))
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation().migrateSession())
				.build();
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
