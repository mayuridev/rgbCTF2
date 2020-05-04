package xyz.rgbsec.backend;

import static xyz.rgbsec.util.TraceUtils.getLineNumber;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import xyz.rgbsec.backend.RedisUserDetailsManager.Authority;

/** A class representing a User. Implements {@link UserDetails} */
public class User implements UserDetails {

  Logger logger = LoggerFactory.getLogger(User.class);
  private BCryptPasswordEncoder encoder;

  private static final long serialVersionUID = 1L;

  private String password, username;
  @SuppressWarnings("unused")
  private boolean credentialsNonExpired = true,
      accountNonLocked = true,
      enabled = true,
      accountNonExpired = true;

  private HashSet<RedisUserDetailsManager.Authority> authorities =
      new HashSet<RedisUserDetailsManager.Authority>();

  // Suppresses default constructor, ensuring non-instantiability.
  private User() {}

  private User(UserBuilder builder) {
    this.updatePassword(builder.password);
    this.username = builder.username;
    this.setEnabled(builder.accountEnabled);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.authorities;
  }
  /**
   * Grants an authority to the user. Cannot return null.
   *
   * @param authority Authority to add to the user. If the user already has this authority, nothing
   *     happens
   * @throws IllegalArgumentException if the <code>authority</code> is <code>null</code>
   */
  public void grantAuthority(Authority authority) throws IllegalArgumentException {
    if (authority == null) throw new IllegalArgumentException("Authority cannot be null");
    authorities.add(authority);
  }

  /**
   * Revokes an authority the user. Cannot return null.
   *
   * @param authority Authority to revoke from the user. If the user doesn't have the authority,
   *     nothing happens
   * @throws IllegalArgumentException if the <code>authority</code> is <code>null</code>
   */
  public void revokeAuthority(Authority authority) {
    authorities.remove(authority);
  }

  /**
   * Returns the hashed password used to authenticate the user
   *
   * @return the hashed password
   */
  @Override
  public String getPassword() {
    return this.password;
  }

  @SuppressWarnings("unused")
  @Deprecated
  /**
   * We aren't using this method since it directly sets the password w/o encoding it. ONLY used for
   * serialization/deserialization & accessed through JUnit's reflection.
   *
   * @param password an encrypted password to be directly used to update <code>this.password</password>
   */
  private void setPassword(String password) {
    this.password = password;
  }

  /**
   * Takes <code>password</code> and encrypts it w/ BCrypt, and then sets it as the password used to
   * authenticate.
   *
   * @param password - unencrypted password to replace the current password
   */
  public void updatePassword(String password) {
    if (this.encoder == null) this.encoder = new BCryptPasswordEncoder(15);

    this.password = this.encoder.encode(password);
    logger.debug(
        "{} -> {}: setPassword(): Current password hash: {}", getLineNumber(), this.password);
  }

  /**
   * @param password - unencrypted password to validate
   * @return valid - a <strong>boolean</strong> representing if the supplied password matches the
   *     current one
   */
  public boolean checkPassword(String password) {
    if (this.encoder == null) this.encoder = new BCryptPasswordEncoder(15);
    return this.encoder.matches(password, this.password);
  }

  /** {@inheritDoc} */
  @Override
  public String getUsername() {
    return this.username;
  }
  /**
   * Sets the username used to authenticate. Cannot be null
   *
   * @param username (can't be null)
   * @throws IllegalArgumentException if <code>username</code> is <code>null</code>
   */
  public void setUsername(String username) throws IllegalArgumentException {
    if (username == "" || username == null)
      throw new IllegalArgumentException("Username cannot be null");
    this.username = username;
  }

  /**
   * Currently nonfunctional, and purely used for compatibility with the {@link UserDetails}
   * interface
   */
  @Deprecated
  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  /**
   * Currently nonfunctional, and purely used for compatibility with the {@link UserDetails}
   * interface
   */
  @Deprecated
  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  /**
   * Currently nonfunctional, and purely used for compatibility with the {@link UserDetails}
   * interface
   */
  @Deprecated
  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEnabled() {
    return this.enabled;
  }
  /**
   * Sets whether the user is enabled or disabled. A disabled user cannot be authenticated.
   *
   * @param enabled <code>true</code> if the user is enabled, <code>false</code> otherwise
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** Helper class to create valid {@link User} instances */
  public static class UserBuilder {
    private String password, username;
    private boolean accountEnabled = true;
    private HashSet<Authority> authorities = new HashSet<Authority>();

    /**
     * Sets the username used to authenticate. Cannot be null
     *
     * @param username (can't be null)
     * @throws IllegalArgumentException if <code>username</code> is <code>null</code>
     */
    public UserBuilder username(String username) throws IllegalArgumentException {
      if (username == null) throw new IllegalArgumentException("Username cannot be null");
      this.username = username;
      return this;
    }

    /**
     * Takes <code>password</code> and encrypts it w/ BCrypt, and then sets it as the password used
     * to authenticate.
     *
     * @param password - unencrypted password to replace the current password
     */
    public UserBuilder password(String password) {
      this.password = password;
      return this;
    }

    /**
     * Sets whether the user is enabled or disabled. A disabled user cannot be authenticated.
     *
     * @param enabled <code>true</code> if the user is enabled, <code>false</code> otherwise
     */
    public UserBuilder accountEnabled(boolean accountEnabled) {
      this.accountEnabled = accountEnabled;
      return this;
    }

    /**
     * Grants an authority to the user. Cannot return null.
     *
     * @param authority Authority to add to the user. If the user already has this authority,
     *     nothing happens
     * @throws IllegalArgumentException if the <code>authority</code> is <code>null</code>
     */
    public UserBuilder grantAuthority(Authority authority) throws IllegalArgumentException {
      if (authority == null) throw new IllegalArgumentException("Authority cannot be null");
      authorities.add(authority);
      return this;
    }

    /** Creates a {@link User} with the given information */
    public User build() {
      if (this.password == null || this.username == null)
        throw new IllegalStateException(
            this.password == null ? "Password is required" : "Username is required");

      return new User(this);
    }
  }
}
