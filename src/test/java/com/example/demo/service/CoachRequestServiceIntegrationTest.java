package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Requestforacoach;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.RequestRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Real-database coverage for the coach-request flow, run against the actual
 * configured datasource rather than mocked repositories.
 *
 * <p>The class is {@code @Transactional}: Spring wraps every {@code @Test}
 * method in one transaction and rolls it back once the method returns, no
 * matter what the code under test committed along the way. That is what
 * makes it safe to write real rows through real repositories here - nothing
 * this class does is still in the database after the test finishes, which
 * you can verify yourself by re-running the row-count query below before and
 * after this class runs.
 *
 * <p>This replaces mutating the live database by hand (curl / psql) to
 * "prove" a fix works. That approach leaves permanent junk behind - which is
 * exactly what happened with the demo rows from earlier manual verification
 * in this project - and it is not repeatable by anyone else. This is.
 *
 * <p><b>No test may send real email.</b> A {@code @SpringBootTest} builds the
 * whole application context, so it would pick up whatever mail settings are
 * live at the time - including a real Gmail account and app password. If
 * {@code MAIL_ENABLED=true} is exported in the shell (which it is while
 * testing delivery by hand), the real {@link SmtpOtpMailer} becomes the
 * active {@code OtpMailer} bean, and any test that touched the
 * forgot-password flow would send a genuine message from the owner's inbox.
 *
 * <p>The {@code @TestPropertySource} below closes that. Its inline properties
 * sit at the top of the test Environment's precedence order - above
 * {@code application-dev.properties} and above OS environment variables - so
 * {@code app.mail.enabled} is false here no matter what the shell exports.
 * That keeps {@link LoggingOtpMailer} as the active bean (it writes the OTP
 * to the log instead of sending it), and the blanked credentials mean the
 * real account details are never even loaded into a test context. The
 * unroutable host/port is the backstop: an accidental send fails immediately
 * against localhost rather than reaching an SMTP server.
 *
 * <p>Repeat this annotation on any new {@code @SpringBootTest}.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"app.mail.enabled=false",
		"spring.mail.host=localhost",
		"spring.mail.port=1",
		"spring.mail.username=",
		"spring.mail.password="
})
@Transactional
class CoachRequestServiceIntegrationTest {

	@Autowired
	private CoachRequestService coachRequestService;

	@Autowired
	private Atheletrepo atheletrepo;

	@Autowired
	private RequestRepo requestRepo;

	private Athelet persistTestAthlete() {
		Athelet athlete = new Athelet();
		athlete.setName("Integration Test Athlete");
		// Unique per run so a second test method (or a rollback that somehow
		// did not fire) can never collide with a leftover row from another.
		athlete.setEmail("itest-" + UUID.randomUUID() + "@example.com");
		athlete.setPassword("irrelevant");
		athlete.setAge(20);
		athlete.setSporttype("Chess");
		return atheletrepo.save(athlete);
	}

	@Test
	@DisplayName("REGRESSION (real DB): submitting a request twice updates one row, it does not create two")
	void secondRequestDoesNotDuplicateTheRow() {
		Athelet athlete = persistTestAthlete();

		Requestforacoach first = new Requestforacoach();
		first.setRequest("First message");
		coachRequestService.addrequest(first, athlete.getAthid());

		Requestforacoach second = new Requestforacoach();
		second.setRequest("Second message");
		coachRequestService.addrequest(second, athlete.getAthid());

		List<Requestforacoach> rows = requestRepo.findAll().stream()
				.filter(r -> r.getAthid() != null && r.getAthid().getAthid() == athlete.getAthid())
				.toList();

		assertThat(rows).hasSize(1);
		assertThat(rows.get(0).getRequest()).isEqualTo("Second message");
	}

	@Test
	@DisplayName("REGRESSION (real DB): checking status after two submissions no longer throws")
	void statusLookupSurvivesADoubleSubmission() {
		Athelet athlete = persistTestAthlete();

		coachRequestService.addrequest(requestFor("First message"), athlete.getAthid());
		coachRequestService.addrequest(requestFor("Second message"), athlete.getAthid());

		// Before the fix this threw NonUniqueResultException the moment a
		// second row existed for the athlete - proved separately against the
		// live app in this session. Here it must not throw at all.
		assertThatCode(() -> coachRequestService.viewrequestbyathelet(athlete.getAthid()))
				.doesNotThrowAnyException();

		assertThat(coachRequestService.viewrequestbyathelet(athlete.getAthid()))
				.get()
				.extracting(Requestforacoach::getRequest)
				.isEqualTo("Second message");
	}

	@Test
	@DisplayName("positive (real DB): a fresh athlete with no request yields an empty result, not an error")
	void freshAthleteHasNoRequestYet() {
		Athelet athlete = persistTestAthlete();

		assertThat(coachRequestService.viewrequestbyathelet(athlete.getAthid())).isEmpty();
	}

	private Requestforacoach requestFor(String message) {
		Requestforacoach req = new Requestforacoach();
		req.setRequest(message);
		return req;
	}
}
