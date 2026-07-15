package com.auth.user;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document("users")
@NoArgsConstructor
public class UserEntity {
	@Id
	private String id;
	@Indexed
	private String username;
	private String password;
	private boolean active;
	private Set<String> roles;
}
