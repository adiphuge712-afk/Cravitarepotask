package com.example.demo.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Shared bookkeeping for a live-event broadcaster: keep a list of currently
 * connected {@link SseEmitter}s per id, clean each one up once it closes, and
 * send to whichever ones are still open. Every broadcaster in this package
 * needs exactly this - it was first written inline in
 * {@code PerformanceEventBroadcasterImpl}, and extracted here once a second
 * and third broadcaster were about to copy the same cleanup wiring
 * (subscribe / onCompletion / onTimeout / onError / send-and-drop-on-failure)
 * verbatim. One copy of this logic means one place to get it right.
 *
 * <p>Package-private: broadcasters compose this, they do not expose it -
 * each one still defines its own domain-specific {@code publish(...)}
 * method and {@code subscribeX(...)} names, so the public API stays
 * meaningful rather than a generic pub/sub bag.
 */
final class EmitterRegistry {

	// Shared so a combined multi-topic endpoint (see register()) can build
	// its one emitter with the same lifetime every single-topic one gets.
	static final long DEFAULT_TIMEOUT_MS = 30L * 60 * 1000;

	private final long timeoutMs;
	private final Map<Long, List<SseEmitter>> byId = new ConcurrentHashMap<>();

	EmitterRegistry(long timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	SseEmitter subscribe(long id) {
		SseEmitter emitter = new SseEmitter(timeoutMs);
		register(id, emitter);
		return emitter;
	}

	/**
	 * Adopts an emitter created elsewhere into this bucket, instead of
	 * minting a new one. This is what lets one HTTP connection carry several
	 * broadcasters' events at once: a combined endpoint creates a single
	 * {@code SseEmitter} and registers it with each broadcaster it wants to
	 * hear from, rather than the caller opening one connection per topic.
	 * Registering the same emitter with several registries is safe - each
	 * just adds its own removal callback, and all of them fire when the
	 * connection actually closes.
	 */
	void register(long id, SseEmitter emitter) {
		List<SseEmitter> bucket = byId.computeIfAbsent(id, key -> new CopyOnWriteArrayList<>());
		bucket.add(emitter);

		Runnable remove = () -> bucket.remove(emitter);
		emitter.onCompletion(remove);
		emitter.onTimeout(remove);
		emitter.onError(ex -> remove.run());
	}

	void sendTo(long id, String eventName, Object payload) {
		for (SseEmitter emitter : byId.getOrDefault(id, List.of())) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(payload));
			} catch (IOException | IllegalStateException e) {
				// The other end is gone; the onCompletion/onError callbacks
				// registered in subscribe() remove it from the bucket - this
				// just stops a dead connection's send from being retried.
				emitter.completeWithError(e);
			}
		}
	}

	/** Package-private: for tests, to verify a closed emitter is actually removed, not just skipped. */
	int subscriberCount(long id) {
		return byId.getOrDefault(id, List.of()).size();
	}

	/** Package-private: for tests that need the live emitter itself (e.g. to call .complete() on it). */
	List<SseEmitter> subscribersFor(long id) {
		return byId.getOrDefault(id, List.of());
	}
}
