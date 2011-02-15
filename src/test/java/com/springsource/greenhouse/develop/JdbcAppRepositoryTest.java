package com.springsource.greenhouse.develop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.security.encrypt.SearchableStringEncryptor;
import org.springframework.test.transaction.TransactionalMethodRule;
import org.springframework.transaction.annotation.Transactional;

import com.springsource.greenhouse.connect.NoSuchAccountConnectionException;
import com.springsource.greenhouse.database.GreenhouseTestDatabaseBuilder;

public class JdbcAppRepositoryTest {

	private EmbeddedDatabase db;

	private JdbcAppRepository appRepository;

	private JdbcTemplate jdbcTemplate;

	@Before
	public void setup() {
		db = new GreenhouseTestDatabaseBuilder().member().connectedApp().testData(getClass()).getDatabase();
		jdbcTemplate = new JdbcTemplate(db);
		appRepository = new JdbcAppRepository(jdbcTemplate, new SearchableStringEncryptor("secret", "5b8bd7612cdab5ed"));
	}

	@After
	public void destroy() {
		if (db != null) {
			db.shutdown();
		}
	}

	@Test
	public void findAppSummaries() {
		List<AppSummary> summaries = appRepository.findAppSummaries(2L);
		assertEquals(1, summaries.size());
		AppSummary summary = summaries.get(0);
		assertEquals("Greenhouse for Facebook", summary.getName());
		assertEquals("Awesome", summary.getDescription());
		assertEquals("http://images.greenhouse.springsource.org/apps/icon-default-app.png", summary.getIconUrl());
		assertEquals("greenhouse-for-facebook", summary.getSlug());

		summaries = appRepository.findAppSummaries(3L);
		assertEquals(2, summaries.size());
		summary = summaries.get(0);
		assertEquals("Greenhouse for the iPhone", summary.getName());
		summary = summaries.get(1);
		assertEquals("Greenhouse for the Android", summary.getName());
	}

	@Test
	public void noAppSummaries() {
		assertEquals(0, appRepository.findAppSummaries(1L).size());
	}

	@Test
	public void findAppBySlug() {
		App app = appRepository.findAppBySlug(3L, "greenhouse-for-the-iphone");
		assertExpectedApp(app);
	}
	
	@Test
	public void findAppByApiKey() throws InvalidApiKeyException {
		App app = appRepository.findAppByApiKey("123456789");
		assertExpectedApp(app);
	}
	
	@Test(expected=InvalidApiKeyException.class)
	public void findByAppKeyInvalidKey() throws InvalidApiKeyException {
		appRepository.findAppByApiKey("invalid");
	}

	@Test
	@Transactional
	public void createApp() {
		AppForm form = new AppForm();
		form.setName("My App");
		form.setDescription("My App Description");
		String slug = appRepository.createApp(1L, form);
		assertEquals("my-app", slug);

		App app = appRepository.findAppBySlug(1L, slug);
		assertEquals("My App", app.getSummary().getName());
		assertNotNull(app.getApiKey());
		assertNotNull(app.getSecret());
		assertEquals(null, app.getCallbackUrl());
	}

	@Test
	public void updateApp() {
		AppForm form = appRepository.getAppForm(2L, "greenhouse-for-facebook");
		form.setName("Greenhouse for Twitter");
		form.setWebsite("http://www.twitter.com");
		String slug = appRepository.updateApp(2L, "greenhouse-for-facebook", form);
		assertEquals("greenhouse-for-twitter", slug);
		form = appRepository.getAppForm(2L, "greenhouse-for-twitter");
		assertEquals("Greenhouse for Twitter", form.getName());
		assertEquals("http://www.twitter.com", form.getWebsite());
	}

	@Test
	public void deleteApp() {
		appRepository.deleteApp(2L, "greenhouse-for-facebook");
		assertEquals(0, appRepository.findAppSummaries(2L).size());
	}

	@Test
	@Transactional
	public void connectApp() throws InvalidApiKeyException, NoSuchAccountConnectionException {
		AppConnection connection = appRepository.connectApp(1L, "123456789");
		assertEquals((Long) 1L, connection.getAccountId());
		assertEquals("123456789", connection.getApiKey());
		assertNotNull(connection.getAccessToken());
		assertNotNull(connection.getSecret());

		AppConnection connection2 = appRepository.findAppConnection(connection.getAccessToken());
		assertEquals(connection.getAccountId(), connection2.getAccountId());
		assertEquals(connection.getApiKey(), connection2.getApiKey());
		assertEquals(connection.getAccessToken(), connection2.getAccessToken());
		assertEquals(connection.getSecret(), connection2.getSecret());

	}

	@Test(expected = InvalidApiKeyException.class)
	public void connectAppInvalidApiKey() throws InvalidApiKeyException {
		appRepository.connectApp(1L, "invalidApiKey");
	}

	@Test
	public void findAppConnection() throws NoSuchAccountConnectionException {
		AppConnection connection = appRepository.findAppConnection("234567890");
		assertEquals((Long) 1L, connection.getAccountId());
		assertEquals("123456789", connection.getApiKey());
		assertEquals("234567890", connection.getAccessToken());
		assertEquals("345678901", connection.getSecret());
	}

	@Test
	public void disconnectApp() throws InvalidApiKeyException {
		AppConnection app = appRepository.connectApp(1L, "123456789");
		appRepository.disconnectApp(1L, app.getAccessToken());
		try {
			appRepository.findAppConnection("123456789");
			fail("Should have failed");
		} catch (NoSuchAccountConnectionException e) {
		}
	}

	private void assertExpectedApp(App app) {
		assertEquals("Greenhouse for the iPhone", app.getSummary().getName());
		assertEquals("123456789", app.getApiKey());
		assertNotNull("987654321", app.getSecret());
		assertEquals("x-com-springsource-greenhouse://oauth-response", app.getCallbackUrl());
	}
	
	@Rule
	public TransactionalMethodRule transactional = new TransactionalMethodRule();

}