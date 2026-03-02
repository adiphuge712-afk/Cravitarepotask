package com.example.demo.Principals;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.Coach;

public class CoachPrinciple implements UserDetails {
	private Coach Coach;
	public CoachPrinciple(Coach Coach) {
		this.Coach=Coach;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority("ROLE_"+Coach.getRole().toUpperCase()));
	}

	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return Coach.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return Coach.getEmail();
	}

}
