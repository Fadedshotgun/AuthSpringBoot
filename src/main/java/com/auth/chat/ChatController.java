package com.auth.chat;

import com.auth.user.UserRepository;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.auth.chat.conversation.ConversationData;
import com.auth.chat.message.MessageData;
import com.auth.chat.message.MessageDisplayData;
import com.auth.chat.message.MessageRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api")
public class ChatController {
	
	ChatService chatService;
	UserRepository userRepository;
	MessageRepository messageRepository;
	
	@MessageMapping("/chat/send/{receiverId}")
	public void send(@DestinationVariable String receiverId, @Valid MessageData messageData, Principal principal) {
		chatService.sendMessage(receiverId, messageData, principal);
	}
	
	@GetMapping("/user/conversations")
	public ResponseEntity<List<ConversationData>> getConversations(Principal principal) {
	    return chatService.getConversations(principal);
	}
	
	@PostMapping("/user/conversation")
	public ResponseEntity<String> getConversationId(@RequestParam String username, Principal principal) {
		return chatService.getConversationId(username, principal);
	}
	
	@GetMapping("/user/messages/{conversationId}")
	public ResponseEntity<List<MessageDisplayData>> getMessages(@PathVariable String conversationId, @RequestParam(defaultValue = "0") int page, Principal principal) {
		return chatService.getMessages(conversationId, principal, page);
	}
	
	@MessageMapping("/chat/typing/{receiverUsername}")
	public void typing(@DestinationVariable String receiverUsername, @Payload Map<String, String> body, Principal principal) {
	    chatService.typing(receiverUsername, principal, body.get("conversationId"));
	}
	
	@MessageMapping("/chat/read/{conversationId}")
	public void markRead(@DestinationVariable String conversationId, Principal principal) {
	    chatService.markRead(conversationId, principal);
	}
}
