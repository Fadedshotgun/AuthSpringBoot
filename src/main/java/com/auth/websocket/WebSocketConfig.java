package com.auth.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		//WebSocketMessageBrokerConfigurer.super.registerStompEndpoints(registry);
		registry.addEndpoint("/chat").setAllowedOrigins("http://localhost:5173", "https://auth-gray-seven.vercel.app").withSockJS();//.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		//WebSocketMessageBrokerConfigurer.super.configureMessageBroker(registry);
		registry.enableSimpleBroker("/queue");
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}
	
}
