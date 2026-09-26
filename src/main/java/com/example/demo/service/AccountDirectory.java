package com.example.demo.service;

import java.util.Optional;

/**
 * Looks an account up by email across all three account tables.
 *
 * <p>Admins, coaches and athletes live in separate tables, so "who owns this
 * email?" is not a single query. Password reset needs exactly that question
 * answered - and needs to write a new password back to whichever table the
 * account came from - so it lives behind one interface instead of being
 * re-implemented per caller.
 */
public interface AccountDirectory {

	/** True when some account - of any role - uses this email. */
	boolean exists(String email);

	/** The account holder's name, for addressing the email. */
	Optional<String> displayName(String email);

	/**
	 * Overwrites the stored password for whichever account owns this email.
	 *
	 * @return true if an account was found and updated
	 */
	boolean updatePassword(String email, String newPassword);
}
