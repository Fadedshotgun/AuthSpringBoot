package com.auth.chat.message;

import org.springframework.data.annotation.Id;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageData {
	@Id
	private String id;
	
    @Size(min = 1, max = 2000)
    @NotBlank
	private String content;
}
