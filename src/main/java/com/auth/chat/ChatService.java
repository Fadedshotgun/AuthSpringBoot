package com.auth.chat;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.auth.chat.conversation.ConversationData;
import com.auth.chat.conversation.ConversationEntity;
import com.auth.chat.conversation.ConversationRepository;
import com.auth.chat.message.MessageData;
import com.auth.chat.message.MessageDisplayData;
import com.auth.chat.message.MessageEntity;
import com.auth.chat.message.MessageRepository;
import com.auth.user.UserEntity;
import com.auth.user.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ChatService {
	UserRepository userRepository;
	MessageRepository messageRepository;
	ConversationRepository conversationRepository;
	SimpMessagingTemplate messagingTemplate;
	
	private String buildConversationId(String userId1, String userId2) {
		List<String> ids = new ArrayList<>(List.of(userId1, userId2));
		Collections.sort(ids);
	    return ids.get(0) + "_" + ids.get(1);
	}
	
	private void updateConversationData(ConversationEntity conversation) {
		ConversationData cData = new ConversationData();
	    cData.setId(conversation.getId());
	    cData.setLastMessage(conversation.getLastMessage());
	    cData.setLastTimestamp(conversation.getLastTimestamp());
	    cData.setUsers(conversation.getUsers());
	    
	    Set<String> usernames = new HashSet<>();
	    for (String userId : conversation.getUsers()) {
	        userRepository.findById(userId).ifPresent(u -> usernames.add(u.getUsername()));
	    }
	    cData.setUsernames(usernames);

	    for (String userId : conversation.getUsers()) {
	        userRepository.findById(userId).ifPresent(u ->
	            messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/conversations", cData)
	        );
	    }
	}
	
	public MessageDisplayData messageEntityToMessageDisplayData(MessageEntity messageEntity) {
		MessageDisplayData data = new MessageDisplayData();
	    data.setId(messageEntity.getId());
	    data.setContent(messageEntity.getContent());
	    data.setTimestamp(messageEntity.getTimestamp());
	    data.setConversationId(messageEntity.getConversationId());
	    data.setSender(userRepository.findById(messageEntity.getSender()).map(UserEntity::getUsername).orElse("Deleted User"));
	    data.setReceiver(userRepository.findById(messageEntity.getReceiver()).map(UserEntity::getUsername).orElse("Deleted User"));
	    return data;
	}
	
	public void sendMessage(String receiverId, MessageData messageData, Principal principal) {
		long time = System.currentTimeMillis();
		
		if (principal == null) {return;}
		
		Optional<UserEntity> senderUser = userRepository.findByUsername(principal.getName());
		Optional<UserEntity> receivingUser = userRepository.findById(receiverId);

		if (senderUser.isEmpty() || receivingUser.isEmpty()) return;

		String senderId = senderUser.get().getId();
		
		MessageEntity newMessage = new MessageEntity();
		newMessage.setContent(messageData.getContent());
		newMessage.setReceiver(receiverId);
		newMessage.setSender(senderId);
		newMessage.setTimestamp(time);

		String conversationId = buildConversationId(receiverId, senderId);
		conversationRepository.findById(conversationId).ifPresentOrElse(
		conversation -> {
			conversation.setLastMessage(messageData.getContent());
			conversation.setLastTimestamp(time);
			
			newMessage.setConversationId(conversation.getId());
			
			conversationRepository.save(conversation);
			messageRepository.save(newMessage);
			
			messagingTemplate.convertAndSendToUser(receivingUser.get().getUsername(), "/queue/messages", messageEntityToMessageDisplayData(newMessage));
			updateConversationData(conversation);
		},
		() -> {
			ConversationEntity newConversation = new ConversationEntity();
			newConversation.setUsers(Set.of(senderId, receiverId));
			newConversation.setId(conversationId);
			newConversation.setLastMessage(messageData.getContent());
			newConversation.setLastTimestamp(time);
			
			newMessage.setConversationId(newConversation.getId());
			
			conversationRepository.save(newConversation);
			messageRepository.save(newMessage);
			
			messagingTemplate.convertAndSendToUser(receivingUser.get().getUsername(), "/queue/messages",  messageEntityToMessageDisplayData(newMessage));
			updateConversationData(newConversation);
		});
	}
	
	public ResponseEntity<List<ConversationData>> getConversations(Principal principal) {
		String principalId = userRepository.findByUsername(principal.getName()).get().getId();
	    List<ConversationEntity> conversations = conversationRepository.findByUsersContaining(principalId);

	    List<ConversationData> conversationsData = new ArrayList<>();
	    for (ConversationEntity c : conversations) {
	        ConversationData cData = new ConversationData();
	        cData.setId(c.getId());
	        cData.setLastMessage(c.getLastMessage());
	        cData.setLastTimestamp(c.getLastTimestamp());
	        cData.setUsers(c.getUsers());

//	        long unread = messageRepository.countByConversationIdAndReceiverAndReadAtIsNull(c.getId(), principalId);
//	        cData.setUnreadCount((int) unread);
	        
	        boolean hasUnread = messageRepository.existsByConversationIdAndReceiverAndReadAtIsNull(c.getId(), principalId);
	        System.out.println(hasUnread);
	        cData.setHasUnread(hasUnread);
	        
	        Set<String> usernames = new HashSet<>();
	        for (String userId : c.getUsers()) {
	            userRepository.findById(userId)
	                .ifPresent(u -> usernames.add(u.getUsername()));
	        }
	        cData.setUsernames(usernames);

	        conversationsData.add(cData);
	    }
	    
	    return ResponseEntity.ok(conversationsData);
	}
	
	public ResponseEntity<String> getConversationId(String username, Principal principal) {
	    String me = userRepository.findByUsername(principal.getName()).get().getId();
	    String other = userRepository.findByUsername(username).map(u -> u.getId()).orElse(null);
	    if (other == null) return ResponseEntity.notFound().build();
	    
	    String conversationId = buildConversationId(me, other);
	    if (!conversationRepository.existsById(conversationId)) {
	        ConversationEntity conversationEntity = new ConversationEntity();
	        conversationEntity.setId(conversationId);
	        conversationEntity.setUsers(Set.of(me, other));
	        conversationEntity.setLastMessage("");
	        conversationEntity.setLastTimestamp(System.currentTimeMillis());
	        conversationRepository.save(conversationEntity);
		    updateConversationData(conversationEntity);
	    }
	    
	    return ResponseEntity.ok(conversationId);
	}
	
	public ResponseEntity<List<MessageDisplayData>> getMessages(@PathVariable String conversationId, Principal principal, int pageNumber) { 
		Pageable pageable = PageRequest.of(pageNumber, 50, Sort.by("timestamp").descending());
		
		List<MessageEntity> messages = messageRepository.findByConversationId(conversationId, pageable);
		HashMap<String, String> idToUsername = new HashMap<>();
		
		if (messages.isEmpty()) return ResponseEntity.ok(List.of());
		
		//TODO for groupchats: find username not just using the first message
		String id1 = messages.get(0).getReceiver();
		String id2 = messages.get(0).getSender();
		String username1 = userRepository.findById(id1).map(UserEntity::getUsername).orElse("Deleted user");
		String username2 = userRepository.findById(id2).map(UserEntity::getUsername).orElse("Deleted user");
		
		idToUsername.put(id1, username1);
		idToUsername.put(id2, username2);
		
		List<MessageDisplayData> messagesToDisplay = messages.stream().map(m -> {
			MessageDisplayData displayData = new MessageDisplayData();
			displayData.setId(m.getId());
			displayData.setContent(m.getContent());
			displayData.setTimestamp(m.getTimestamp());
			displayData.setConversationId(m.getConversationId());
			displayData.setReceiver(idToUsername.get(m.getReceiver()));
			displayData.setSender(idToUsername.get(m.getSender()));
			displayData.setReadAt(m.getReadAt());
			return displayData;
		}).collect(Collectors.toList());
	
	    return ResponseEntity.ok(messagesToDisplay);
	}
	
	public void typing(String receiverUsername, Principal principal, String conversationId) {
	    messagingTemplate.convertAndSendToUser(
	            receiverUsername,
	            "/queue/typing",
	            Map.of("sender", principal.getName(), "conversationId", conversationId)
	        );
	}
	
	public void markRead(String conversationId, Principal principal) {
			List<MessageEntity> unread = messageRepository.findByConversationIdAndReceiverAndReadAtIsNull(conversationId, userRepository.findByUsername(principal.getName()).get().getId());
		    
			if (unread.isEmpty()) return;
		    unread.forEach(m -> m.setReadAt(System.currentTimeMillis()));
		    messageRepository.saveAll(unread);

		    String lastReadId = unread.stream()
		            .reduce((first, second) -> second) 
		            .map(MessageEntity::getId)
		            .orElse(null);
		    
		    unread.stream()
		        .map(MessageEntity::getSender)
		        .distinct()
		        .forEach(senderId -> 
		            userRepository.findById(senderId).ifPresent(u ->
		                messagingTemplate.convertAndSendToUser(u.getUsername(), "/queue/read",
		                    Map.of("conversationId", conversationId))
		            )
		        );
	}
}
