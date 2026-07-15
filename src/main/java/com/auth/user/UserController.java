package com.auth.user;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth.config.ratelimits.RateLimited;
import com.auth.websocket.WebSocketEventListener;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class UserController {
	UserService userService;
	UserRepository userRepository;
	WebSocketEventListener webSocketEventListener;
	
	@RateLimited(capacity = 1, periodInSeconds = 3600, tokensPerPeriod = 5)
	@PostMapping("/public/register")
	@ResponseBody
	public ResponseEntity<String> registerUser(@RequestBody @Valid UserData userData) {
		ResponseEntity<String> response = userService.createUser(userData);
		return response;
	}
	
	@PostMapping("/owner/add-role-to-user")
	@ResponseBody
	public ResponseEntity<String> addRoleToUser(@RequestParam String username, @RequestParam String roleName) {
		ResponseEntity<String> response = userService.addRoleToUser(username, roleName);
		return response;
	}
	
	@GetMapping("/user/me")
	public ResponseEntity<Map<String, String>> me(Principal principal) {
		UserEntity user = userRepository.findByUsername(principal.getName()).get();
	    return ResponseEntity.ok(Map.of(
	        "username", user.getUsername(),
	        "id", user.getId()
	    ));
	}
	
	@GetMapping("/user/id")
	public ResponseEntity<String> getUserId(@RequestParam String username) {
	    return userRepository.findByUsername(username)
	        .map(u -> ResponseEntity.ok(u.getId()))
	        .orElse(ResponseEntity.notFound().build());
	}
	
	
	@GetMapping("/user/online/{username}")
	public ResponseEntity<Boolean> isOnline(@PathVariable String username) {
	    return ResponseEntity.ok(webSocketEventListener.isOnline(username));
	}
	
	@GetMapping("/user/online")
	public ResponseEntity<Set<String>> getOnlineUsers(Principal principal) {
	    return ResponseEntity.ok(webSocketEventListener.getOnlineUsersFor(principal.getName()));
	}
}
 