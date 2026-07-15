package com.auth.chat.message;

import lombok.Data;

@Data
public class MessageDisplayData {
    private String id;
    private String sender;
    private String receiver;
    private String content;
    private long timestamp;
    private String conversationId;
	private Long readAt; 
}