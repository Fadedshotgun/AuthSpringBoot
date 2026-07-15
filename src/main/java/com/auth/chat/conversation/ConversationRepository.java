package com.auth.chat.conversation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<ConversationEntity, String> {
	Optional<ConversationEntity>findById(String id);
	List<ConversationEntity>findByUsersContaining(String id);
}
