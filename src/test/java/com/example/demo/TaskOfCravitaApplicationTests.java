package com.example.demo;

import com.example.demo.service.LoggingOtpMailer;
import com.example.demo.service.OtpMailer;
import com.example.demo.service.SmtpOtpMailer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Context-level smoke test, and the guard that keeps the test suite from ever
 * sending real email.
 *
 * <p>The mail properties below are pinned off - see
 * CoachRequestServiceIntegrationTest for the full reasoning on why every
 * SpringBootTest in this project carries them.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"app.mail.enabled=false",
		"spring.mail.host=localhost",
		"spring.mail.port=1",
		"spring.mail.username=",
		"spring.mail.password="
})
class TaskOfCravitaApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
	}

	/**
	 * Asserts the safeguard instead of trusting it. A comment saying "tests
	 * don't send mail" is worth nothing once someone adds a SpringBootTest
	 * that exercises the forgot-password flow while MAIL_ENABLED=true is
	 * exported; this fails the build in that situation.
	 */
	@Test
	@DisplayName("SECURITY: the active OtpMailer under test is never the real SMTP sender")
	void testsCanNeverSendRealEmail() {
		OtpMailer active = context.getBean(OtpMailer.class);

		assertThat(active)
				.as("a test context must not wire the real SMTP mailer - it would send "
						+ "genuine email from the configured account")
				.isNotInstanceOf(SmtpOtpMailer.class)
				.isInstanceOf(LoggingOtpMailer.class);

		assertThat(context.getBeanNamesForType(SmtpOtpMailer.class))
				.as("SmtpOtpMailer must not exist as a bean at all during tests")
				.isEmpty();
	}
}
