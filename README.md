# Cravita — Backend

Spring Boot 4 (Java 17) API for a sports academy platform: admins manage coaches,
coaches manage athletes' training plans and performance, athletes follow their
schedule and can request a coach. JWT authentication with server-side logout,
object-level authorization, an email-based forgot-password flow, and live
dashboard updates over Server-Sent Events.

## Email Config 

I am using the Render Free vertion for deployment thats why the Mail smtp not working on it but locally its working now i am start working on gmail api it will be work on free version of render
## Stack

- Java 17, Spring Boot 4.0.1, Maven
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL
- JWT (`io.jsonwebtoken`)
- Spring Mail (SMTP), for password-reset codes
- JUnit 5, Mockito, AssertJ

## Project layout

```
src/main/java/com/example/demo/
├── controller/    REST endpoints (Mycontroller, AdminController, CoachControler - one per role prefix)
├── service/       business logic, one interface + implementation per concern
├── repository/    Spring Data JPA repositories
├── entity/        JPA entities
├── dto/           request/response payloads - never the raw entities
├── security/      JWT filter/util, Spring Security config, principals, AccessGuard
├── sse/           live-update broadcasters (see "Live updates" below)
├── exception/     typed exceptions + a single @RestControllerAdvice
└── config/        CORS
```

Each of the three roles (Admin, Coach, Athlete) has its own controller under its
own URL prefix (`/admin/**`, `/coach/**`, `/athelet/**`), but they share one
service layer underneath — the split is only at the HTTP boundary. Each
controller now exposes *only* the endpoints that role actually needs (they
used to be near-identical copies of one another), which is what lets
`SecurityConfig` enforce strict prefix isolation with plain rules — an admin
token works on `/admin/**` and nowhere else, likewise coach and athlete, with
no cross-prefix exceptions.

## Running locally

Requires a local PostgreSQL instance.

Like the `prod` profile, `dev` (`application-dev.properties`, active by
default) now has **no inline fallback values either** — every variable in the
table below is required, sourced from `dev.env` at the repo root:

```bash
set -a && source dev.env && set +a
./mvnw spring-boot:run
```

(Or point your IDE's run configuration at `dev.env` as an env file instead of
sourcing it in a shell.) `dev.env` holds only non-secret local values — a
local Postgres at `localhost:5432/Aditya` (see `dev.env` for the actual
credentials), mail switched off, and a fixed local-only JWT signing key — so it's committed
in spirit but not in fact: it's listed in `.gitignore`/`.dockerignore`
precisely so nobody accidentally treats it as safe to commit later just
because today's values happen to be harmless. Password-reset email is **off**
by default: the 6-digit code is written to the application log instead of
being sent, so the flow is testable without a mail server. `./mvnw test` needs
`dev.env` sourced too, for the same reason — see "Running the tests" below.

The API listens on `http://localhost:8056`.

## Demo data / seed accounts

`../reset_and_seed_demo_data.sql` (one level up from this repo, alongside the
frontend repo — it isn't specific to either one) resets the local `Aditya`
database to a clean, known state: it **truncates every table** and creates
exactly 3 login accounts, all with the password `123456`:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@cravita.com` | `123456` |
| Coach | `coach@cravita.com` | `123456` |
| Athlete | `athlete@cravita.com` | `123456` |

It also adds a realistic amount of demo data around that coach — 2 training
plans, 8 work drills across them, and a second athlete (`priya@cravita.com`
/ `123456`, same coach) so multi-athlete views (filters, per-athlete stats,
roster tables) have more than one row to show. Between the two athletes
there are 11 performance logs in a genuine mix of statuses, each athlete
logged against a different subset of the 8 drills so their numbers are
visibly distinct, and 4 feedback entries — so all three dashboards have
something worth looking at immediately after logging in, instead of one
trivial row that reads as 100% complete by accident. It does **not** add
more than the 3 login accounts above; Priya is demo data under the same
coach, not a fourth account of a different kind.

**This is destructive** — it deletes every row in every table with no undo
short of a prior backup. Run it yourself, deliberately, in a query tool
(`psql -h localhost -U postgres -d Aditya -f ../reset_and_seed_demo_data.sql`
from this repo, or paste it into pgAdmin/DBeaver) — it is not run
automatically by anything in this project. The passwords in it are a real
BCrypt hash generated with this project's own `BCryptPasswordEncoder` and
verified to match `123456` before being pasted in, so they work exactly like
any password created through the app's own registration flow.

`../add_athlete_priya_sharma.sql` is the **non-destructive** counterpart —
it adds just Priya (and her logs/feedback) to whatever's already in your
database, without truncating anything. Useful if you already have a
database you don't want to reset but still want a second athlete to test
with. Safe to run more than once: every insert in it is guarded against
duplicates.

## Running the tests

```bash
set -a && source dev.env && set +a
./mvnw test
```

Same requirement as running the app: the `dev` profile has no inline
fallbacks, so `./mvnw test` on its own fails fast with "Could not resolve
placeholder '${DB_URL}'" (etc.) — there's deliberately no test-only copy of
these values baked into the jar, so nothing in `src/test/resources` can drift
out of sync with `dev.env` or leak a stale value onto the runtime classpath.

The suite is almost entirely Mockito-based unit tests — no database required.
The few tests that exercise a real repository against the actual configured
database (`*IntegrationTest`) are `@Transactional`: Spring wraps each test
method in a transaction and rolls it back once the method returns, so nothing
they write is ever left behind, even though they hit the real database
connection. Never verify a fix by running mutating requests against a real
database by hand (curl/psql) instead of a test like this — it leaves permanent
data behind with no way to clean it up automatically.

**Be aware:** any `@SpringBootTest` (this includes `TaskOfCravitaApplicationTests`
and the `*IntegrationTest` classes) boots the *real* application context against
whichever database the active profile points at, including every
`ApplicationRunner` bean — `LegacyPasswordMigrationRunner` among them. Running
`./mvnw test` locally will genuinely re-hash any still-plaintext password row
in your dev database. That's harmless (the same password keeps working, see
the security notes below) and idempotent, but it's a real side effect on a
real table, not something confined to a test transaction — worth knowing
before assuming "it's just tests."

**No test can send real email**, even with `MAIL_ENABLED=true` exported in
your shell (which you do while testing delivery by hand). Every
`@SpringBootTest` pins `app.mail.enabled=false` via `@TestPropertySource`,
which outranks both `application-dev.properties` and the environment — so the
real `SmtpOtpMailer` bean never exists in a test context, only the
log-writing one. `TaskOfCravitaApplicationTests` asserts this directly
(`testsCanNeverSendRealEmail`) rather than just relying on it by convention.
Copy that annotation onto any new `@SpringBootTest` you add.

## Configuration

All configuration is externalized via environment variables, and **neither
profile has an inline fallback anymore** — `dev` and `prod` both fail loudly
at boot ("Could not resolve placeholder '...'") if a variable is missing,
rather than silently falling back to something that might be wrong. The only
difference between them is where the values come from:

| Variable | Purpose | dev value (`dev.env`) | prod |
|---|---|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://host:5432/dbname` | local Postgres | required |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials | see `dev.env` | required |
| `FRONTEND_URL` | Exact origin allowed by CORS (no trailing slash) | `http://localhost:5173` | required |
| `JWT_SECRET_KEY` | HS256 signing key, 32+ random bytes | fixed local-only placeholder | required |
| `PORT` | HTTP port | `8056` | required |
| `MAIL_ENABLED` | `true` to send real email, `false` to log the code instead | `false` | required |
| `MAIL_FROM` | From address for password-reset email | placeholder | required |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server | `smtp.gmail.com` / `587` | required |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP auth (an app password for Gmail, not your login password) | blank (unused while `MAIL_ENABLED=false`) | required |
| `MAIL_SMTP_AUTH` / `MAIL_SMTP_STARTTLS` | Usually `true` / `true` | `true` / `true` | required |

`dev.env` (repo root) and `prod.env` (same, for running the Docker image
locally — see the Docker section below) hold these as plain files, both
gitignored/dockerignored. For a real prod deployment the variables come from
wherever your hosting platform injects environment variables (they should
never live in a committed file at all). Run with the prod profile via
`SPRING_PROFILES_ACTIVE=prod`.

**Never commit real credentials into any `application-*.properties`, `dev.env`,
or `prod.env` file** — not even as a placeholder that happens to be safe
today. `dev.env`'s values are safe now because they're a fixed local
placeholder key and a throwaway local DB password, not because the file is
exempt from the rule.

## Docker

The `Dockerfile` is a multi-stage build: a JDK stage compiles the jar with
the Maven wrapper (so `docker build .` alone is reproducible, with no
pre-built jar required), then a slim JRE-alpine stage runs it as a
non-root user. It exposes `/actuator/health` (only "health", nothing else
from Actuator) for the built-in `HEALTHCHECK` and for any orchestrator's own
liveness/readiness probe — this endpoint is intentionally unauthenticated
(see `SecurityConfig`) and reports only up/down, never any app internals.

```bash
docker build -t cravita-backend .

docker run -d --name cravita-backend \
  --add-host=host.docker.internal:host-gateway \
  -p 8056:8056 \
  --env-file prod.env \
  cravita-backend

curl http://localhost:8056/actuator/health   # {"status":"UP"}
```

`prod.env` (repo root, gitignored/dockerignored) holds the variables from the
Configuration table above for a local test run — including
`DB_URL=jdbc:postgresql://host.docker.internal:5432/...`, since `localhost`
inside the container means the container itself, not the host machine running
Postgres. `--add-host` is what makes `host.docker.internal` resolve on Linux;
Docker Desktop (Windows/Mac) already provides it. For an actual production
deployment, replace `--env-file prod.env` with whatever your hosting platform
uses to inject environment variables — a file like this should never exist
outside a local machine.

## Live updates (Server-Sent Events)

Dashboards refresh themselves when the underlying data changes elsewhere,
instead of needing a manual page reload — a coach recording a result shows up
on that athlete's dashboard immediately, a new drill shows up on the schedule
it belongs to, an admin sees a new coach-request the moment it's submitted.

Three broadcasters in `sse/`, each scoped exactly the way the matching REST
read already is (mirroring `AccessGuard` on purpose — a broadcast channel that
ignored those boundaries would leak one athlete's data to every other
signed-in athlete):

- `PerformanceEventBroadcaster` — performance-log changes
- `WorkoutEventBroadcaster` — new work drills, scoped by coach id
- `CoachRequestEventBroadcaster` — coach-request submissions and assignments

Each is called from the service method that actually writes the change
(`PerformanceServiceImpl`, `WorkoutServiceImpl`, `CoachRequestServiceImpl`,
`AthleteServiceImpl`), not from the controller — so it fires no matter which
endpoint triggered the write.

**One connection per page, not one per topic.** A page that needs several
event types (e.g. the admin dashboard needs performance + schedule +
coach-request) subscribes to one combined endpoint —
`/admin/sse/events`, `/coach/sse/events`, `/athelet/sse/events` — which opens
a single `SseEmitter` and registers it with every broadcaster that page
cares about, instead of opening one connection per topic. This matters
because browsers cap concurrent HTTP/1.1 connections to a single origin at
6; several long-lived SSE streams per tab, multiplied across several open
tabs, can exhaust that budget and stall the page's own ordinary data-fetching
requests — which looks exactly like the page hanging on its loading spinner.
Single-topic endpoints (`/coach/sse/performance`, `/coach/sse/schedule`, …)
still exist for the one page that only ever needs one topic at a time
(`TraningSchedule.jsx`, `RequestCoach.jsx`).

The shared bookkeeping (subscribe, clean up on disconnect, send-and-drop-on
failure) lives once in `EmitterRegistry`, composed by each broadcaster rather
than duplicated three times.

**What this cannot do**: change data a signed-in browser has already baked
into its JWT. An athlete's own `hasCoach`/`coachid` are decoded from the
token issued at login — a snapshot. When an admin assigns them a coach, the
live event tells that athlete's dashboard to prompt for a refresh; it cannot
silently rewrite what the token itself says.

## Security notes (current state, being worked through incrementally)

- **Logout actually revokes the token**, server-side. `TokenBlacklistService`
  stores the SHA-256 hash of every logged-out token (never the token itself)
  with its expiry, and `JwtFilter` checks it on every request. Without this, a
  JWT is stateless and would keep working for anyone holding a copy of it
  until its natural 24-hour expiry, "logout" or not. Expired rows are swept
  lazily rather than needing a scheduled job.
- **Object-level authorization**, not just role checks. `AccessGuard` answers
  "may *this specific caller* touch *this specific record*" — role checks
  alone let any signed-in athlete read or write every other athlete's data
  just by changing the id in the URL, since almost every endpoint takes its
  target id straight from the path. Every controller method that takes an id
  calls the matching `requireAccessTo*` (or reads `current*Id()` off the
  signed-in principal instead of trusting a path value at all) before doing
  anything else.
- **Strict prefix isolation** in `SecurityConfig`: `/admin/**` requires
  `ROLE_ADMIN`, `/coach/**` requires `ROLE_COACH`, `/athelet/**` requires
  `ROLE_ATHELET`, with no cross-prefix exceptions. This only works because
  each controller now exposes solely its own role's endpoints — when the
  three controllers were near-identical copies of one another, this exact
  rule would have let an admin token reach athlete and coach endpoints too.
- Sign-in is one endpoint (`POST /auth/login` in `AuthController`) for all
  three roles, not three copies of the same login method on three
  controllers — the role travels in the request body, not the URL.
- Passwords are hashed with `BCryptPasswordEncoder` — nowhere in the codebase
  compares a password with `String.equals` or stores one as typed. Every write
  path (registration in `RegistrationServiceImpl`, reset in
  `AccountDirectoryImpl`) encodes through the shared `PasswordEncoder` bean
  (`PasswordEncoderConfig`), and every check (`AuthenticationServiceImpl`, and
  Spring Security's own `DaoAuthenticationProvider` for the initial
  `authenticate()` call) goes through `PasswordEncoder#matches`.
  `LegacyPasswordMigrationRunner` runs once at every boot, finds any row whose
  password does not already look like a BCrypt hash, and re-hashes it in
  place from the plaintext value that was there — so an account created
  before this change keeps working with the exact same password, it just
  stops being stored in the clear. The check is idempotent, so this component
  is meant to stay in the codebase rather than be deleted after first use.
- Registration and profile-edit endpoints bind to purpose-built DTOs, not the
  raw JPA entities — a client can never set fields like `role` or `password`
  through them, because those DTOs have no such field to begin with. If you
  add a new "edit X" endpoint, follow the same pattern: a DTO with only the
  fields that endpoint is meant to change.
- The forgot-password flow never reveals whether an email is registered, hashes
  the OTP (never stores it in the clear), caps verification attempts, and
  every code/token is single-use with its own expiry.
- All errors go through one `GlobalExceptionHandler` returning a consistent
  `{status, error, message, path, timestamp}` body with the correct HTTP
  status — there should be no bare try/catch swallowing an exception into a
  misleading response anywhere in a controller.
