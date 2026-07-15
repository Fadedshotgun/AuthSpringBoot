package com.auth.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern.List;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserData {
	@NotBlank(message = "Username cannot be blank")
	@Size(min=3, max=64, message = "Username must be between 3 and 64 characters")
	@Pattern(regexp = "^(?![._])(?!.*[._]{2})[a-zA-Z0-9_.]+(?<![._])$", message = "Invalid username")
	private String username;
	
	@NotBlank(message = "Password cannot be blank")
	@Size(min=3, max=128, message = "Password must be between 3 and 128 characters")
	private String password;
}
