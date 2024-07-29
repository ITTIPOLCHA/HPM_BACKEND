package com.gj.hpm.config.security.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.gj.hpm.entity.User;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private String id;
	private String email;
	private String name;
	private String password;
	private String lineId;
	private Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(String id, String email,String name, String password,String lineId,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.password = password;
		this.lineId = lineId;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(User user) {
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toList());

		return new UserDetailsImpl(
				user.getId(),
				user.getEmail(),
				user.getFirstName() + " " + user.getLastName(),
				user.getPassword(),
				user.getLineId(),
				authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	public String getEmail() {
		return email;
	}

	public String getLineId() {
		return lineId;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
