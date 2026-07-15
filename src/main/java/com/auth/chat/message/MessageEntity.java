package com.auth.chat.message;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document("messages")
@CompoundIndex(name = "conv_timestamp_idx", def = "{'conversationId': 1, 'timestamp': 1}")
public class MessageEntity {
	@Id
	private String id;

	@Indexed
	private String conversationId;

	private String sender;
	private String receiver;
	private String content;
	private Long timestamp;

	private Long readAt;

}
