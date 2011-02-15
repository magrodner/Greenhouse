package com.springsource.greenhouse.connect;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.security.encrypt.SearchableStringEncryptor;
import org.springframework.security.encrypt.StringEncryptor;
import org.springframework.social.facebook.FacebookOperations;
import org.springframework.social.twitter.TwitterOperations;

import com.springsource.greenhouse.account.AccountMapper;
import com.springsource.greenhouse.account.StubFileStorage;
import com.springsource.greenhouse.database.GreenhouseTestDatabaseBuilder;

public class JdbcServiceProviderFactoryTest {
	
	private EmbeddedDatabase db;

	private JdbcTemplate jdbcTemplate;

	private ServiceProviderFactory providerFactory;

	@Before
	public void setup() {
		db = new GreenhouseTestDatabaseBuilder().member().connectedAccount().testData(getClass()).getDatabase();
		jdbcTemplate = new JdbcTemplate(db);
		StringEncryptor encryptor = new SearchableStringEncryptor("secret", "5b8bd7612cdab5ed");
		AccountMapper accountMapper = new AccountMapper(new StubFileStorage(), "http://localhost:8080/members/{profileKey}");
		providerFactory = new JdbcServiceProviderFactory(jdbcTemplate, encryptor, accountMapper);
	}

	@After
	public void destroy() {
		if (db != null) {
			db.shutdown();
		}
	}

	@Test
	public void getAccountProvider() {
		ServiceProvider<TwitterOperations> twitterProvider = providerFactory.getServiceProvider("twitter", TwitterOperations.class);
		assertEquals("twitter", twitterProvider.getName());
		assertEquals("Twitter", twitterProvider.getDisplayName());
		assertEquals("123456789", twitterProvider.getApiKey());
		assertEquals("http://www.twitter.com/authorize?oauth_token=123456789", twitterProvider.buildAuthorizeUrl("123456789"));

		ServiceProvider<FacebookOperations> facebookProvider = providerFactory.getServiceProvider("facebook", FacebookOperations.class);
		assertEquals("facebook", facebookProvider.getName());
		assertEquals("Facebook", facebookProvider.getDisplayName());
		assertEquals("345678901", facebookProvider.getApiKey());
 	}
	
	@Test
	public void getAccountProviderByName() {
		ServiceProvider<TwitterOperations> twitterProvider = providerFactory.getServiceProvider("twitter", TwitterOperations.class);
		assertEquals("twitter", twitterProvider.getName());
		assertEquals("Twitter", twitterProvider.getDisplayName());
		assertEquals("123456789", twitterProvider.getApiKey());
		assertEquals("http://www.twitter.com/authorize?oauth_token=123456789", twitterProvider.buildAuthorizeUrl("123456789"));

		ServiceProvider<FacebookOperations> facebookProvider = providerFactory.getServiceProvider("facebook", FacebookOperations.class);
		assertEquals("facebook", facebookProvider.getName());
		assertEquals("Facebook", facebookProvider.getDisplayName());
		assertEquals("345678901", facebookProvider.getApiKey());
	}
}
