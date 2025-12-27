package com.example.demo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="coach")
public class Coach {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private long coachid;
	@Column
private String name;
	@Column(unique=true)
private String email;
	@Column
private String password;
	@Column
private String specialization;
	@Column
private long experience;
public long getCoachid() {
	return coachid;
}
public void setCoachid(long coachid) {
	this.coachid = coachid;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getSpecialization() {
	return specialization;
}
public void setSpecialization(String specialization) {
	this.specialization = specialization;
}
public long getExperience() {
	return experience;
}
public void setExperience(long experience) {
	this.experience = experience;
}
public Admin getAdid() {
	return adid;
}
public void setAdid(Admin adid) {
	this.adid = adid;
}
@ManyToOne
@JoinColumn(name = "adminid")
private Admin adid;
}
