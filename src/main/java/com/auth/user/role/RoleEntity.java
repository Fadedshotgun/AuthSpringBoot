package com.auth.user.role;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Document("roles")
@AllArgsConstructor
public class RoleEntity {
	@Id
	private Long id;
	private String name;
}
