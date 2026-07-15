package com.auth.chat.conversation;

import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversationData {
	private String id;
	private Set<String> users;
	private Set<String> usernames;
	private String lastMessage;
	private long lastTimestamp;
	private boolean hasUnread;
//	private int unreadCount;
}