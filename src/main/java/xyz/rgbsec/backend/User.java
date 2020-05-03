package xyz.rgbsec.backend;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configurable // to get it so it re attaches encoder spring bean on deserialization
public class User implements UserDetails {

	String getLineNumber() {
		String out = "";
		for (StackTraceElement n : new Throwable().getStackTrace()) {

			if (n.getClassName().contains("rgbsec")) {
				out += String.format("\n\t@ %s at function %s, line %d", n.getFileName(), n.getMethodName(),
						n.getLineNumber());
			}
		}
		return out;
	}

	private User() {
	}

	Logger logger = LoggerFactory.getLogger(User.class);

	private BCryptPasswordEncoder encoder; // TODO: this is you last night. for some reason this isnt being set properly

	private static final long serialVersionUID = 1L;
	private String password, username;

	@SuppressWarnings("unused")
	private boolean credentialsNonExpired = true, accountNonLocked = true, enabled = true, accountNonExpired = true;

	private User(UserBuilder builder) {
		this.setEncodedPassword(builder.password);
		this.username = builder.username;
		this.setEnabled(builder.accountEnabled);
		// we're leaving out the other fields since they're just there to preserve
		// Spring interface compliance
	}

	public static class UserBuilder {
		private String password, username;
		private boolean accountEnabled = true;

		public UserBuilder username(String username) {
			this.username = username;
			return this;
		}

		public UserBuilder password(String password) {
			this.password = password;
			return this;
		}

		public UserBuilder accountEnabled(boolean accountEnabled) {
			this.accountEnabled = accountEnabled;
			return this;
		}

		public User build() {
			if (this.password == null || this.username == null)
				throw new IllegalStateException(
						this.password == null ? "Password is required" : "Username is required");

			return new User(this);
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Deprecated
	/**
	 * We aren't using this method since it directly sets the password w/o encoding it. ONLY used for serialization/deserialization
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public void setEncodedPassword(String password) {
		if (this.encoder == null)
			this.encoder = new BCryptPasswordEncoder(15);

		this.password = this.encoder.encode(password);
		logger.debug("{} -> {}: setPassword(): Current password hash: {}", getLineNumber(), this.password);
	}

	public boolean checkPassword(String password) {
		if (this.encoder == null)
			this.encoder = new BCryptPasswordEncoder(15);
		return this.encoder.matches(password, this.password);
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}