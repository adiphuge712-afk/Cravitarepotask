package com.example.demo.security.principal;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entity.Athelet;

public class AtheletPrincilpals implements UserDetails {
	private Athelet Athelet;
	public AtheletPrincilpals(Athelet Athelet) {
		this.Athelet=Athelet;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return List.of(new SimpleGrantedAuthority("ROLE_"+Athelet.getRole().toUpperCase()));
	}

	@Override
	public @Nullable String getPassword() {
		// TODO Auto-generated method stub
		return Athelet.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return Athelet.getEmail();
	}


	/** The signed-in account itself, so callers can check ownership
	 * without another database round-trip. */
	public Athelet getAthelet() {
		return Athelet;
	}
}
