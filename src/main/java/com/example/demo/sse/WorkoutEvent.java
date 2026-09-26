package com.example.demo.sse;

/**
 * Pushed when a coach adds a work drill. Thin, like every other event here -
 * the frontend uses it only as a signal to reload the schedule it already
 * fetches over REST, which stays correctly scoped by coach id there.
 */
public record WorkoutEvent(long workid, long coachId, String workname) {
}
