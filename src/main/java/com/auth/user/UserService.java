package com.auth.user;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.user.role.RoleEntity;
import com.auth.user.role.RoleRepository;

import java.util.UUID;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	
	private final PasswordEncoder encoder;

	public ResponseEntity<String> createUser(UserData userData) {
		try {
			if (userRepository.findByUsername(userData.getUsername()).isPresent()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken. Please try again");
			}
			
			userData.setPassword(encoder.encode(userData.getPassword()));
			
			Set<String> set = new HashSet<String>();
			set.add(roleRepository.findByName("ROLE_USER").get().getName());
			
			UUID newID = UUID.randomUUID();
			
			UserEntity newUser = new UserEntity();
			newUser.setUsername(userData.getUsername());
			newUser.setPassword(userData.getPassword());
			newUser.setRoles(set);
			newUser.setActive(true);
			userRepository.save(newUser);
			
			return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created account!");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong!");
		}
	}
	
	public ResponseEntity<String> addRoleToUser(String username, String roleName) {
		
		Optional<UserEntity> user = userRepository.findByUsername(username);
		if (!user.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Username "+username+" not found");
		
		Optional<RoleEntity> role = roleRepository.findByName(roleName);
		if (!role.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role "+roleName+" not found");
		
		if (user.get().getRoles().contains(roleName)) return ResponseEntity.status(HttpStatus.CONFLICT).body("User "+username+" already has role "+ roleName);
		
		user.get().getRoles().add(role.get().getName());
		
		userRepository.save(user.get());
		return ResponseEntity.status(HttpStatus.OK).body("Role " + roleName + " added to user "+ username);
	}
}
