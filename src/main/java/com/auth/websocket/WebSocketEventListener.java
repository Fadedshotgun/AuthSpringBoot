package com.auth.websocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.auth.chat.conversation.ConversationRepository;
import com.auth.user.UserRepository;
import com.auth.user.status.StatusUpdate;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class WebSocketEventListener {
	 
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet(); // thread safe i guess. google told me to
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
 
    private void changeUserStatus(Message<byte[]> eventMessage, boolean online) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(eventMessage);
        if (accessor.getUser() == null) return;
        String username = accessor.getUser().getName();
 
        if (online) {
            onlineUsers.add(username);
        } else {
            onlineUsers.remove(username);
        }
 
        StatusUpdate status = new StatusUpdate(username, online);
 
        userRepository.findByUsername(username).ifPresent(user -> {
            List<String> relevantUsernames = conversationRepository
                .findByUsersContaining(user.getId())
                .stream()
                .flatMap(c -> c.getUsers().stream())
                .filter(id -> !id.equals(user.getId()))
                .distinct()
                .map(id -> userRepository.findById(id).map(u -> u.getUsername()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
 
            relevantUsernames.forEach(u -> messagingTemplate.convertAndSendToUser(u, "/queue/status", status)
            );
        });
    }
 
    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        changeUserStatus(event.getMessage(), true);
    }
 
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        changeUserStatus(event.getMessage(), false);
    }
 
    public boolean isOnline(String username) {
        return onlineUsers.contains(username);
    }
 
    public Set<String> getOnlineUsersFor(String username) {
        return userRepository.findByUsername(username).map(user ->
            conversationRepository.findByUsersContaining(user.getId())
                .stream()
                .flatMap(c -> c.getUsers().stream())
                .filter(id -> !id.equals(user.getId()))
                .distinct()
                .map(id -> userRepository.findById(id).map(u -> u.getUsername()).orElse(null))
                .filter(Objects::nonNull)
                .filter(onlineUsers::contains)
                .collect(Collectors.toSet())
        ).orElse(Set.of());
    }
}