package com.example.demo.security.principal;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entity.Admin;

public class AdminPrinciple implements UserDetails {
		private Admin admin;
public AdminPrinciple(Admin admin) {
	this.admin=admin;
}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority("ROLE_"+admin.getRole().toUpperCase()));
	}

	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return admin.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return admin.getEmail();
	}


	/** The signed-in account itself, so callers can check ownership
	 * without another database round-trip. */
	public Admin getAdmin() {
		return admin;
	}
}
