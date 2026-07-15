package com.auth.chat.conversation;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document("conversations")
@Data
@NoArgsConstructor
public class ConversationEntity {
	@Id
	private String id;
	private Set<String> users;
	private String lastMessage;
	private long lastTimestamp;
}
