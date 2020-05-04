package xyz.rgbsec.backend;

import java.io.IOException;
import java.util.ArrayList;

import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

@Component
public class RedisUserDetailsManager implements UserDetailsManager {

	public static enum Authority implements GrantedAuthority {

		USER {
			@Override
			public String getAuthority() {
				// TODO Auto-generated method stub
				return "USER";
			}
		},
		ADMIN {
			@Override
			public String getAuthority() {
				// TODO Auto-generated method stub
				return "USER";
			}
		};

	}

	int getLineNumber() {
		return new Throwable().getStackTrace()[0].getLineNumber();
	}

	private final RedisClient redisClient;
	private final ObjectMapper mapper;

	Logger logger = LoggerFactory.getLogger(RedisUserDetailsManager.class);

	StatefulRedisConnection<String, byte[]> connection;

	ArrayList<String> usernamesCache = new ArrayList<String>();

	public RedisUserDetailsManager() {
		redisClient = RedisClient.create("redis://localhost:6379/");
		connection = redisClient.connect(new StringByteCodec());
		mapper = new ObjectMapper(new MessagePackFactory());
		connection.sync().keys("*").forEach(usernamesCache::add);

		this.createUser(new User.UserBuilder().accountEnabled(true).password("admin").username("admin").build());
	}

	public void removeAuthority(String username, Authority authority) {
		if (!this.userExists(username))
			throw new UsernameNotFoundException("User doesn't exist");

		User user = this.loadUserByUsername(username);
	}

	public void addAuthority(String username, Authority authority) {
		if (!this.userExists(username))
			throw new UsernameNotFoundException("User doesn't exist");

		User user = this.loadUserByUsername(username);
		
	}

	@Override
	public void createUser(UserDetails user) {

		if (!(user instanceof User))
			throw new IllegalArgumentException();
		try {
			usernamesCache.add(user.getUsername());
			byte[] serializedUser = mapper.writeValueAsBytes((User) user);
			connection.sync().set(user.getUsername(), serializedUser);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public User loadUserByUsername(String username) {

		if (!this.userExists(username))
			throw new UsernameNotFoundException("Invalid username");

		byte[] serializedUser = connection.sync().get(username);
		User user;
		try {
			user = mapper.readValue(serializedUser, User.class);
			return user;

		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
			throw new UsernameNotFoundException("Invalid username");
		}

	}

	@Override
	public void updateUser(UserDetails user) {
		if (!(user instanceof User))
			throw new ClassCastException();
		if (!this.userExists(user.getUsername()))
			throw new UsernameNotFoundException("User doesn't exist");

		this.deleteUser(user.getUsername());
		this.createUser(user);

	}

	@Override
	public void deleteUser(String username) {
		usernamesCache.removeIf(username::equals);
		logger.debug("Line {}: deleteUser(): Deleting user {} (keys deleted: {})", getLineNumber(), username,
				connection.sync().del(username));
	}

	@Override

	public void changePassword(String username, String newPassword) {
		if (!this.userExists(username))
			throw new UsernameNotFoundException("User doesn't exist");

		User user = this.loadUserByUsername(username);
		user.updatePassword(newPassword);
		this.updateUser(user);

	}

	@Override
	public boolean userExists(String username) {
		return usernamesCache.contains(username);

	}

	public boolean checkPassword(String username, String password) {
		return this.loadUserByUsername(username).checkPassword(password);
	}

}
