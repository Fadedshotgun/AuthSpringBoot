package com.auth.chat.message;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<MessageEntity, String> {
    List<MessageEntity> findByConversationId(String conversationId, Pageable pageable);
    List<MessageEntity> findByConversationIdAndReceiverAndReadAtIsNull(String conversationId, String receiver);
    long countByConversationIdAndReceiverAndReadAtIsNull(String conversationId, String receiver);
    boolean existsByConversationIdAndReceiverAndReadAtIsNull(String conversationId, String receiver);
}