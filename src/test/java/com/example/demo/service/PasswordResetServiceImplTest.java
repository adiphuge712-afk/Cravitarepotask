package com.example.demo.service;

import com.example.demo.entity.PasswordResetOtp;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.PasswordResetOtpRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Positive and negative coverage for the forgot-password flow, including the
 * properties that make it safe: no account enumeration, hashed codes, a
 * capped number of guesses, expiry, and single use.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetServiceImplTest {

	private static final String EMAIL = "athlete@example.com";

	@Mock private PasswordResetOtpRepo otpRepo;
	@Mock private AccountDirectory accounts;
	@Mock private OtpMailer mailer;

	@InjectMocks private PasswordResetServiceImpl service;

	private static String sha256(String v) throws Exception {
		return HexFormat.of().formatHex(
				MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8)));
	}

	/** A live, unredeemed code row. */
	private PasswordResetOtp liveOtp(String code) throws Exception {
		PasswordResetOtp o = new PasswordResetOtp();
		o.setEmail(EMAIL);
		o.setOtpHash(sha256(code));
		o.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
		o.setCreatedAt(Instant.now().minus(Duration.ofMinutes(5)));
		return o;
	}

	@BeforeEach
	void setUp() {
		when(otpRepo.findTopByEmailOrderByCreatedAtDesc(anyString())).thenReturn(Optional.empty());
		when(accounts.exists(anyString())).thenReturn(true);
		when(accounts.displayName(anyString())).thenReturn(Optional.of("Rohit"));
	}

	@Nested
	@DisplayName("requestOtp")
	class RequestOtp {

		@Test
		@DisplayName("positive: stores a hashed code and mails the plain one")
		void storesHashAndMails() {
			service.requestOtp(EMAIL);

			ArgumentCaptor<PasswordResetOtp> saved = ArgumentCaptor.forClass(PasswordResetOtp.class);
			verify(otpRepo).save(saved.capture());
			ArgumentCaptor<String> mailed = ArgumentCaptor.forClass(String.class);
			verify(mailer).sendOtp(eq(EMAIL), eq("Rohit"), mailed.capture(), anyInt());

			assertThat(mailed.getValue()).matches("\\d{6}");
			// what is persisted must NOT be the code itself
			assertThat(saved.getValue().getOtpHash())
					.isNotEqualTo(mailed.getValue())
					.hasSize(64);
			assertThat(saved.getValue().getExpiresAt()).isAfter(Instant.now());
		}

		@Test
		@DisplayName("positive: any outstanding code is invalidated first")
		void invalidatesPrevious() {
			service.requestOtp(EMAIL);
			verify(otpRepo).invalidateOutstanding(EMAIL);
		}

		@Test
		@DisplayName("positive: the email is normalised, so casing/spacing cannot fork the flow")
		void normalisesEmail() {
			service.requestOtp("  ATHLETE@Example.com  ");
			verify(mailer).sendOtp(eq(EMAIL), anyString(), anyString(), anyInt());
		}

		@Test
		@DisplayName("SECURITY: an unknown address succeeds silently and sends nothing")
		void unknownAddressDoesNotRevealItself() {
			when(accounts.exists(anyString())).thenReturn(false);

			assertThatCode(() -> service.requestOtp("nobody@example.com")).doesNotThrowAnyException();

			verifyNoInteractions(mailer);
			verify(otpRepo, never()).save(any());
		}

		@Test
		@DisplayName("negative: a second request inside the cooldown is refused")
		void cooldown() throws Exception {
			PasswordResetOtp recent = liveOtp("123456");
			recent.setCreatedAt(Instant.now().minusSeconds(5));
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(recent));

			assertThatThrownBy(() -> service.requestOtp(EMAIL))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("wait");

			verifyNoInteractions(mailer);
		}

		@Test
		@DisplayName("negative: a blank email is rejected")
		void blankEmail() {
			assertThatThrownBy(() -> service.requestOtp("  "))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("required");
		}
	}

	@Nested
	@DisplayName("verifyOtp")
	class VerifyOtp {

		@Test
		@DisplayName("positive: the right code yields a reset token and marks the row verified")
		void correctCode() throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(row));

			String token = service.verifyOtp(EMAIL, "123456");

			assertThat(token).isNotBlank().hasSizeGreaterThan(20);
			assertThat(row.isVerified()).isTrue();
			assertThat(row.getResetToken()).isEqualTo(token);
			assertThat(row.getResetTokenExpiresAt()).isAfter(Instant.now());
		}

		@Test
		@DisplayName("negative: a wrong code increments attempts and reports how many are left")
		void wrongCodeCountsDown() throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.verifyOtp(EMAIL, "000000"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("4 attempts remaining");

			assertThat(row.getAttempts()).isEqualTo(1);
			assertThat(row.isVerified()).isFalse();
		}

		@Test
		@DisplayName("SECURITY: the row is burned once the attempt cap is reached")
		void attemptCapBurnsTheRow() throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			row.setAttempts(PasswordResetServiceImpl.MAX_ATTEMPTS);
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.verifyOtp(EMAIL, "123456"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("Too many incorrect attempts");

			assertThat(row.isUsed()).isTrue();
		}

		@Test
		@DisplayName("negative: an expired code is rejected even when correct")
		void expiredCode() throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			row.setExpiresAt(Instant.now().minusSeconds(1));
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.verifyOtp(EMAIL, "123456"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("no longer valid");
		}

		@Test
		@DisplayName("SECURITY: an already-verified code cannot be verified twice")
		void noReplay() throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			row.setVerified(true);
			when(otpRepo.findTopByEmailOrderByCreatedAtDesc(EMAIL)).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.verifyOtp(EMAIL, "123456"))
					.isInstanceOf(ValidationException.class);
		}

		@Test
		@DisplayName("negative: verifying with no outstanding request is rejected")
		void noOutstandingRequest() {
			assertThatThrownBy(() -> service.verifyOtp(EMAIL, "123456"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("request a new one");
		}
	}

	@Nested
	@DisplayName("resetPassword")
	class ResetPassword {

		private PasswordResetOtp verifiedRow(String token) throws Exception {
			PasswordResetOtp row = liveOtp("123456");
			row.setVerified(true);
			row.setResetToken(token);
			row.setResetTokenExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
			return row;
		}

		@Test
		@DisplayName("positive: writes the new password and consumes the token")
		void happyPath() throws Exception {
			PasswordResetOtp row = verifiedRow("tok-123");
			when(otpRepo.findByEmailAndResetToken(EMAIL, "tok-123")).thenReturn(Optional.of(row));
			when(accounts.updatePassword(EMAIL, "brandNew1")).thenReturn(true);

			service.resetPassword(EMAIL, "tok-123", "brandNew1");

			verify(accounts).updatePassword(EMAIL, "brandNew1");
			assertThat(row.isUsed()).isTrue();
			assertThat(row.getResetToken()).isNull();
		}

		@Test
		@DisplayName("SECURITY: a token cannot be used twice")
		void singleUse() throws Exception {
			PasswordResetOtp row = verifiedRow("tok-123");
			row.setUsed(true);
			when(otpRepo.findByEmailAndResetToken(EMAIL, "tok-123")).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.resetPassword(EMAIL, "tok-123", "brandNew1"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("already used");

			verify(accounts, never()).updatePassword(anyString(), anyString());
		}

		@Test
		@DisplayName("negative: an expired reset token is refused")
		void expiredToken() throws Exception {
			PasswordResetOtp row = verifiedRow("tok-123");
			row.setResetTokenExpiresAt(Instant.now().minusSeconds(1));
			when(otpRepo.findByEmailAndResetToken(EMAIL, "tok-123")).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.resetPassword(EMAIL, "tok-123", "brandNew1"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("expired");
		}

		@Test
		@DisplayName("negative: an unknown token is refused")
		void unknownToken() {
			when(otpRepo.findByEmailAndResetToken(anyString(), anyString())).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.resetPassword(EMAIL, "made-up", "brandNew1"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("not valid");
		}

		@Test
		@DisplayName("negative: a too-short password is refused before anything is written")
		void shortPassword() {
			assertThatThrownBy(() -> service.resetPassword(EMAIL, "tok-123", "abc"))
					.isInstanceOf(ValidationException.class)
					.hasMessageContaining("at least 6");

			verify(accounts, never()).updatePassword(anyString(), anyString());
			verifyNoInteractions(otpRepo);
		}

		@Test
		@DisplayName("negative: a token whose OTP was never verified is refused")
		void neverVerified() throws Exception {
			PasswordResetOtp row = verifiedRow("tok-123");
			row.setVerified(false);
			when(otpRepo.findByEmailAndResetToken(EMAIL, "tok-123")).thenReturn(Optional.of(row));

			assertThatThrownBy(() -> service.resetPassword(EMAIL, "tok-123", "brandNew1"))
					.isInstanceOf(ValidationException.class);
		}
	}
}
