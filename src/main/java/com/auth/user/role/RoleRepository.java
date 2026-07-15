package com.auth.user.role;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<RoleEntity, Long> {
	Optional<RoleEntity>findByName(String name);
}
