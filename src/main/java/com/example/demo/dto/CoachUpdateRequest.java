package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inbound payload for editing a coach's profile.
 *
 * <p>Deliberately has no password field: an "edit profile" screen is not a
 * password-change screen, and a client should never be able to overwrite a
 * coach's credentials by editing their name. Password changes go through the
 * forgot-password flow (a code sent to the account's own email), the same
 * way {@code role} was removed from the registration DTOs so a client has no
 * field through which to grant itself privileges it should not have.
 *
 * <p>{@code ignoreUnknown = true}: a coach row fetched from the API (and
 * echoed back into an edit form) carries fields this DTO does not need -
 * {@code coachid}, {@code role}, the nested admin object - and none of them
 * should cause the request to fail just because a caller sent the whole row
 * back instead of hand-picking fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoachUpdateRequest {

	private String name;
	private String email;
	private long age;
	private String specialization;
	private long experience;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public long getAge() { return age; }
	public void setAge(long age) { this.age = age; }

	public String getSpecialization() { return specialization; }
	public void setSpecialization(String specialization) { this.specialization = specialization; }

	public long getExperience() { return experience; }
	public void setExperience(long experience) { this.experience = experience; }
}
