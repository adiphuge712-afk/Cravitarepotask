package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Traningplanrepo;
import com.example.demo.security.principal.AdminPrinciple;
import com.example.demo.security.principal.AtheletPrincilpals;
import com.example.demo.security.principal.CoachPrinciple;

@Component
public class AccessGuardImpl implements AccessGuard {

	private static final String DENIED = "You do not have access to that record";

	private final Atheletrepo atheletrepo;
	private final Traningplanrepo traningplanrepo;

	public AccessGuardImpl(Atheletrepo atheletrepo, Traningplanrepo traningplanrepo) {
		this.atheletrepo = atheletrepo;
		this.traningplanrepo = traningplanrepo;
	}

	private Object principal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
			// No authenticated caller at all - deny rather than assume. Reaching
			// here means a guarded path was invoked outside a secured request.
			throw new AccessDeniedException(DENIED);
		}
		return auth.getPrincipal();
	}

	@Override
	public void requireAccessToAthlete(long athleteId) {
		Object who = principal();

		if (who instanceof AdminPrinciple) {
			return;
		}

		if (who instanceof AtheletPrincilpals athlete) {
			if (athlete.getAthelet().getAthid() != athleteId) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		if (who instanceof CoachPrinciple coach) {
			// A coach may only reach athletes on their own squad.
			Athelet target = atheletrepo.findById(athleteId)
					.orElseThrow(() -> new AccessDeniedException(DENIED));
			Coach assigned = target.getCoachid();
			if (assigned == null || assigned.getCoachid() != coach.getCoach().getCoachid()) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		throw new AccessDeniedException(DENIED);
	}

	@Override
	public void requireAccessToCoach(long coachId) {
		Object who = principal();

		if (who instanceof AdminPrinciple) {
			return;
		}

		if (who instanceof CoachPrinciple coach) {
			if (coach.getCoach().getCoachid() != coachId) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		if (who instanceof AtheletPrincilpals athlete) {
			// An athlete reads their own coach's schedule - and only theirs.
			Coach assigned = athlete.getAthelet().getCoachid();
			if (assigned == null || assigned.getCoachid() != coachId) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		throw new AccessDeniedException(DENIED);
	}

	@Override
	public void requireAccessToPlan(long planId) {
		Object who = principal();

		if (who instanceof AdminPrinciple) {
			return;
		}

		if (who instanceof CoachPrinciple coach) {
			Traningplan plan = traningplanrepo.findById(planId)
					.orElseThrow(() -> new AccessDeniedException(DENIED));
			Coach owner = plan.getCoachid();
			if (owner == null || owner.getCoachid() != coach.getCoach().getCoachid()) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		throw new AccessDeniedException(DENIED);
	}

	@Override
	public void requireAccessToAdmin(long adminId) {
		Object who = principal();

		if (who instanceof AdminPrinciple admin) {
			if (admin.getAdmin().getAdminid() != adminId) {
				throw new AccessDeniedException(DENIED);
			}
			return;
		}

		throw new AccessDeniedException(DENIED);
	}

	@Override
	public long currentAthleteId() {
		Object who = principal();
		if (who instanceof AtheletPrincilpals athlete) {
			return athlete.getAthelet().getAthid();
		}
		throw new AccessDeniedException(DENIED);
	}

	@Override
	public long currentCoachId() {
		Object who = principal();
		if (who instanceof CoachPrinciple coach) {
			return coach.getCoach().getCoachid();
		}
		throw new AccessDeniedException(DENIED);
	}

	@Override
	public long currentAssignedCoachId() {
		Object who = principal();
		if (who instanceof AtheletPrincilpals athlete) {
			Coach assigned = athlete.getAthelet().getCoachid();
			if (assigned != null) {
				return assigned.getCoachid();
			}
		}
		throw new AccessDeniedException(DENIED);
	}
}
