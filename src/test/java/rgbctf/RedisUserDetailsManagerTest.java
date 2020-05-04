package rgbctf;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import xyz.rgbsec.backend.RedisUserDetailsManager;
import xyz.rgbsec.backend.User;

@FixMethodOrder(MethodSorters.JVM)
public class RedisUserDetailsManagerTest {

	static private RedisUserDetailsManager redisUserDetailsManager;

	static String firstTestString = "abcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+-=;:/?.>,<`~ABCDEFGHIJKLMNOPQRSTUVWXYZ",
			secondTestString = "ZYXWVUTSRQPONMLKJIHGFEDCBA~`<,>.?/:;=-+_)(*&^%$#@!0987654321zyxwvutsrqponmlkjihgfedcba";

	static User testUser;
	
	static Logger logger = (Logger)LoggerFactory.getLogger(RedisUserDetailsManager.class);

	@BeforeAll
	public static void initalize() {

		testUser = new User.UserBuilder().username(firstTestString).password(firstTestString).accountEnabled(true)
				.build();
		redisUserDetailsManager = new RedisUserDetailsManager();
		assertNotNull(redisUserDetailsManager);
		final Logger lettuceLogger = (Logger)LoggerFactory.getLogger("io.lettuce");		
		lettuceLogger.setLevel(Level.OFF);
	}

	@Test
	public void testLoadUserByUsername() {
		System.out.println("-----------------------------testLoadUserByUsername()-----------------");

		redisUserDetailsManager.createUser(testUser);

		User loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		assertEquals(loadedUser.getUsername(), testUser.getUsername());
		assertTrue(redisUserDetailsManager.checkPassword(testUser.getUsername(), firstTestString));
		assertEquals(loadedUser.isEnabled(), testUser.isEnabled());

		redisUserDetailsManager.deleteUser(loadedUser.getUsername());
	}

	@Test
	public void testCreateUser() {
		System.out.println("-----------------------------testCreateUser()-----------------");

		redisUserDetailsManager.createUser(testUser);

		User loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		assertEquals(loadedUser.getUsername(), testUser.getUsername());
		assertTrue(redisUserDetailsManager.checkPassword(testUser.getUsername(), firstTestString));
		assertEquals(loadedUser.isEnabled(), testUser.isEnabled());

		redisUserDetailsManager.deleteUser(loadedUser.getUsername());
	}

	@Test
	public void testCheckPassword() {
		System.out.println("-----------------------------testCheckPassword()-----------------");

		redisUserDetailsManager.createUser(testUser);
		assertTrue(redisUserDetailsManager.checkPassword(testUser.getUsername(), firstTestString));
	}

	@Test
	public void testDeleteUser() {
		
		
		System.out.println("-----------------------------testDeleteUser()-----------------");
		redisUserDetailsManager.createUser(testUser);

		User loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		redisUserDetailsManager.deleteUser(loadedUser.getUsername());

		assertThrows(UsernameNotFoundException.class,
				() -> redisUserDetailsManager.loadUserByUsername(firstTestString));
	}

	@Test
	public void testUpdateUser() {
		System.out.println("-----------------------------testUpdateUser()-----------------");

		redisUserDetailsManager.createUser(testUser);

		User loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		loadedUser.updatePassword(secondTestString);
		loadedUser.setEnabled(false);

		redisUserDetailsManager.updateUser(loadedUser);

		loadedUser = null;

		loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		logger.debug("testUpdateUser(): Reloaded & updated user {}. Enabled {}", loadedUser.getUsername(), loadedUser.isEnabled());

		assertNotNull(loadedUser);
		assertTrue(loadedUser.checkPassword(secondTestString));

		assertFalse(loadedUser.isEnabled());
		redisUserDetailsManager.deleteUser(loadedUser.getUsername());

	}

	@Test
	public void testChangePassword() {
		System.out.println("-----------------------------testChangePassword()-----------------");

		redisUserDetailsManager.createUser(testUser);

		User loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		redisUserDetailsManager.changePassword(loadedUser.getUsername(), secondTestString);

		loadedUser = redisUserDetailsManager.loadUserByUsername(testUser.getUsername());

		assertTrue(redisUserDetailsManager.checkPassword(loadedUser.getUsername(), secondTestString));
		assertFalse(redisUserDetailsManager.checkPassword(loadedUser.getUsername(), firstTestString));

		redisUserDetailsManager.deleteUser(loadedUser.getUsername());
	}

	@Test
	public void testUserExists() {
		System.out.println("-----------------------------testUserExists()-----------------");

		redisUserDetailsManager.createUser(testUser);
		assertTrue(redisUserDetailsManager.userExists(testUser.getUsername()));
		redisUserDetailsManager.deleteUser(testUser.getUsername());
		assertFalse(redisUserDetailsManager.userExists(testUser.getUsername()));

	}

}
