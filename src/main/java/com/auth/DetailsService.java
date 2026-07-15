package com.auth;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.auth.user.UserEntity;
import com.auth.user.UserRepository;
import com.auth.user.role.RoleEntity;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DetailsService implements UserDetailsService {
	private final UserRepository repository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<UserEntity> authUser = repository.findByUsername(username.toLowerCase());
		if (!authUser.isPresent()) {
			throw new UsernameNotFoundException(username);
		} else {
			ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
			for (String role : authUser.get().getRoles()) {
				auths.add(new SimpleGrantedAuthority(role));
			}
			
			return User.builder()
					.username(authUser.get().getUsername())
					.password(authUser.get().getPassword())
					.authorities(auths)
					.disabled(!authUser.get().isActive())
					.build();
		}
	}
}
