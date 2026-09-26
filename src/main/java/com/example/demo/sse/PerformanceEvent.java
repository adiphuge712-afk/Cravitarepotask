package com.example.demo.sse;

/**
 * What gets pushed to a live-updating dashboard when a performance log is
 * created or updated. Deliberately thin - the frontend uses this only as a
 * signal to refetch from the (already role-scoped) REST endpoint it already
 * calls, not as the source of truth itself.
 */
public record PerformanceEvent(long logid, long athleteId, long workoutId, String completestatus) {
}
