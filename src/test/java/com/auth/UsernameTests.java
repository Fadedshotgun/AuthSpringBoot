package com.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.auth.config.SecurityConfig;
import com.auth.user.UserController;
import com.auth.user.UserRepository;
import com.auth.user.UserService;
import com.auth.user.role.RoleEntity;
import com.auth.user.role.RoleRepository;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(printOnlyOnFailure = true)
public class UsernameTests {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext context;

	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private RoleRepository roleRepository;
	@MockitoBean
	private UserService userService;
	@MockitoBean
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
		when(roleRepository.findByName("ROLE_USER"))
				.thenReturn(Optional.of(RoleEntity.builder().name("ROLE_USER").build()));

		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()) // Optional: Adds security
																							// support
				.alwaysDo(print()).defaultRequest(post("/").contentType(MediaType.APPLICATION_JSON).with(csrf()))
				.build();
	}

	@Test
	void specialsOnEndOrBeginning() throws Exception {
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"testuser.\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());

		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\".testuser\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());

		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"testuser_\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());

		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"_testuser\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
	}

	@Test
	void twoSpecialsInARow() throws Exception {
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"test..user\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
		
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"test._user\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
		
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"test_.user\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
		
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"test__user\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
	}
	
	@Test
	void noAngleBrackets() throws Exception {
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"<script>ahahamaliciouspayloadhere</script>\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
		
		mockMvc.perform(request(HttpMethod.POST, "/api/public/register")
				.content("{\"username\":\"padding<script>ahahamaliciouspayloadhere</script>padding\",\"password\":\"testpass\"}")).andExpect(status().isBadRequest());
	}
}
