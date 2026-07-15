package com.auth;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.auth.config.SecurityConfig;
import com.auth.config.ratelimits.RateLimitingComponent;
import com.auth.user.UserController;
import com.auth.user.UserRepository;
import com.auth.user.UserService;
import com.auth.user.role.RoleEntity;
import com.auth.user.role.RoleRepository;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(printOnlyOnFailure=true)
public class AuthTest {
	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private RoleRepository roleRepository;
	@MockitoBean
	private UserService userService;
	@MockitoBean
	private PasswordEncoder passwordEncoder;
	@MockitoBean
	private RateLimitingComponent rateLimitingComponent;
	
	
	@Test
	@WithMockUser(roles = "USER")
	void canUsersGiveOthersRoles() throws Exception {
		mockMvc.perform(request(HttpMethod.POST, "/api/owner/add-role-to-user")).andExpect(status().isForbidden());
	}
	
	@Test
	void canRegister() throws Exception {
	    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
	    when(roleRepository.findByName("ROLE_USER")).thenReturn(
	    	    Optional.of(RoleEntity.builder().name("ROLE_USER").build())
	    	);

	    mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
	        .with(csrf())
	        .contentType(MediaType.APPLICATION_JSON)
	        .content("{\"username\":\"testuser\",\"password\":\"testpass\"}"))
	        .andExpect(status().isOk());
	}
}
