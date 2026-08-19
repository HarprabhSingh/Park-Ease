# ParkEasy HTTP API Specification

**Version:** 0.1  
**Status:** Approved  
**Date:** 2026-08-17  
**Base path:** `/api/v1`  
**Related documents:** [PRD](./PRD.md), [HLD](./HLD.md), [LLD](./LLD.md), [Database design](./DATABASE.md)  

## 1. Purpose

This document defines the browser-to-backend HTTP contract for the ParkEasy MVP: request intent, authentication, authorization, payloads, responses, errors, pagination, idempotency, and privacy boundaries.

It does not define Spring controllers or annotations. Implementation begins after this contract is reviewed.

## 2. Terminology

| Term | Meaning |
|---|---|
| Endpoint | One HTTP method and path, such as `POST /api/v1/bookings`. |
| Resource | A product concept exposed by the API, such as a listing or booking. |
| Header | Request/response metadata, such as content type or idempotency identity. |
| Request body | Untrusted JSON through which the client expresses intent. |
| Status code | Numeric summary of the outcome. |
| Authentication | Establishing who the caller is. |
| Authorization | Deciding whether that caller may perform the operation. |
| Idempotency | Retrying one logical command without creating another business result. |
| Pagination | Returning a bounded page rather than an entire collection. |

## 3. General conventions

### 3.1 Transport and versioning

- Pilot and production traffic uses HTTPS.
- JSON requests use `Content-Type: application/json`.
- The MVP uses the major-version prefix `/api/v1`.
- Compatible additions may remain in v1; breaking semantic or structural changes require migration planning.

### 3.2 Identifiers

- API identifiers are UUID strings.
- Clients treat identifiers as opaque values.
- Knowing an identifier never grants authorization.

### 3.3 Time

- Business instants use ISO 8601 timestamps with an explicit UTC offset or `Z`.
- Timestamps without an offset are rejected where an instant is required.
- A listing's IANA timezone interprets recurring local schedules.

Examples:

```text
2026-08-20T14:00:00+05:30
2026-08-20T08:30:00Z
```

### 3.4 Money

Money uses integer minor units plus currency:

```json
{
  "amountMinor": 15000,
  "currency": "INR"
}
```

For INR, `15000` means ₹150.00 in paise. Binary floating-point values are not authoritative monetary representations.

### 3.5 Unknown request fields

Command endpoints initially reject unknown fields. This catches misspellings and unintended client input instead of silently ignoring it. Compatibility policy must be revisited before supporting third-party clients.

## 4. Authentication and browser security

### 4.1 Session cookie

Login creates a server-side authenticated session and returns an opaque cookie. Deployed cookie properties are:

- `HttpOnly`;
- `Secure`;
- an explicit `SameSite` policy; and
- an intentionally bounded lifetime.

The browser automatically sends the cookie on subsequent same-origin requests. Request bodies do not contain authoritative `driverUserId`, `ownerUserId`, or `adminUserId` fields.

### 4.2 CSRF protection

Cookie-authenticated state-changing requests require a CSRF token. The frontend returns the server-issued value in a header such as:

```http
X-CSRF-TOKEN: <token>
```

The exact token delivery mechanism will be configured and tested with Spring Security. CORS is not a replacement for CSRF protection.

### 4.3 Authentication failures

- Missing or invalid authentication returns `401 Unauthorized`.
- An authenticated caller without permission returns `403 Forbidden`.
- Suspended accounts cannot perform protected marketplace actions even when an older session exists.

## 5. Authorization summary

| Capability | Driver | Owner | Administrator |
|---|---:|---:|---:|
| Search approved listings | Yes | Yes | Yes |
| Create booking | Yes | Yes, acting as driver | Only through supported workflow |
| View own driver bookings | Yes | Yes | Yes |
| Create/edit owned listing | No | Yes | Audited admin workflow only |
| View owned-listing bookings | No | Yes | Yes |
| Approve/reject listings | No | No | Yes |
| Suspend users/listings | No | No | Yes |
| View verification evidence | No | Own submission when permitted | Least-privilege access |

Administrator access still goes through audited application operations rather than arbitrary database mutation.

## 6. Error contract

Errors use a consistent problem body:

```json
{
  "type": "https://parkeasy.example/problems/booking-interval-unavailable",
  "title": "Booking interval unavailable",
  "status": 409,
  "code": "BOOKING_INTERVAL_UNAVAILABLE",
  "detail": "The parking space is no longer available for the requested interval.",
  "instance": "/api/v1/bookings",
  "correlationId": "request-correlation-id",
  "fieldErrors": []
}
```

- `code` is stable and machine-readable.
- `detail` is safe for users and contains no stack trace, SQL, or secrets.
- `correlationId` connects the response with protected logs.
- `fieldErrors` describes validation failures.

| Status | ParkEasy meaning |
|---:|---|
| `200 OK` | Successful read or operation returning a body |
| `201 Created` | Resource successfully created |
| `204 No Content` | Successful operation without a body |
| `400 Bad Request` | Malformed JSON, invalid syntax, or missing required protocol metadata |
| `401 Unauthorized` | Caller is not authenticated |
| `403 Forbidden` | Caller is authenticated but not permitted |
| `404 Not Found` | Resource is absent or intentionally hidden from this caller |
| `409 Conflict` | Request conflicts with current resource, price, concurrency, or idempotency state |
| `422 Unprocessable Content` | Structurally valid input violates semantic validation or expected simulated processing |
| `429 Too Many Requests` | Rate limit exceeded after rate limiting exists |
| `500 Internal Server Error` | Unexpected backend defect or failure |
| `503 Service Unavailable` | Required infrastructure is temporarily unavailable |

## 7. Pagination

Initial collection endpoints use bounded page-number pagination:

```http
GET /api/v1/me/bookings?page=0&size=20&sort=confirmedAt,desc
```

- page numbering begins at zero;
- default size is 20;
- maximum size is 50;
- sort fields are allow-listed; and
- ordering uses a deterministic identifier tie-breaker.

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Page-number pagination is selected for MVP simplicity. Query plans and large-offset behavior will be measured before cursor pagination is considered.

## 8. Account and session endpoints

### 8.1 Register

```http
POST /api/v1/auth/registrations
```

```json
{
  "email": "driver@example.com",
  "password": "user-supplied-secret",
  "displayName": "Example Driver"
}
```

Response: `201 Created`. Passwords are never returned or logged. Password policy and email verification remain open security decisions.

### 8.2 Login

```http
POST /api/v1/auth/sessions
```

```json
{
  "email": "driver@example.com",
  "password": "user-supplied-secret"
}
```

Response: `200 OK` plus the session cookie:

```json
{
  "userId": "UUID",
  "displayName": "Example Driver",
  "roles": ["DRIVER"]
}
```

Failure returns a generic `401` that does not reveal whether the email exists.

### 8.3 Current session

```http
GET /api/v1/auth/session
```

Returns the current authenticated profile or `401`.

### 8.4 Logout

```http
DELETE /api/v1/auth/session
```

Requires CSRF protection. Returns `204 No Content`, invalidates the server session, and expires the cookie.

## 9. Discovery endpoints

### 9.1 Search listings

```http
GET /api/v1/parking-listings?locality=Rajouri%20Garden&arrivalAt=...&departureAt=...&vehicleType=CAR&page=0&size=20
```

Required query parameters:

- locality;
- arrival and departure instants; and
- vehicle type.

Optional filters include covered status and approved price fields. Coordinates/radius are added with Near me.

Only approved, active, offered, and apparently unoccupied listings are returned. Search availability is advisory; booking rechecks it transactionally.

### 9.2 View public listing

```http
GET /api/v1/parking-listings/{listingId}
```

Before booking, this returns approximate location, public characteristics, access constraints, and calculated price—but not protected evidence or unnecessarily precise private access information.

## 10. Owner listing endpoints

| Operation | Endpoint | Result |
|---|---|---|
| Create draft | `POST /api/v1/owner/listings` | `201`; caller becomes owner |
| View owned listing | `GET /api/v1/owner/listings/{listingId}` | Private owner representation |
| Edit allowed fields | `PATCH /api/v1/owner/listings/{listingId}` | Updated representation |
| Replace weekly schedule | `PUT /api/v1/owner/listings/{listingId}/weekly-availability` | Complete schedule replacement |
| Add exception | `POST /api/v1/owner/listings/{listingId}/availability-exceptions` | `201` |
| Remove exception | `DELETE /api/v1/owner/listings/{listingId}/availability-exceptions/{exceptionId}` | `204` |
| Submit for review | `POST /api/v1/owner/listings/{listingId}/review-submissions` | `201` |
| Pause listing | `POST /api/v1/owner/listings/{listingId}/pauses` | Auditable pause outcome |

`PUT` replaces the complete weekly schedule because repeating the same representation should produce the same result. Sensitive listing edits trigger re-review under the PRD.

## 11. Booking endpoints

### 11.1 Create instant booking

```http
POST /api/v1/bookings
Idempotency-Key: <client-generated-unique-value>
X-CSRF-TOKEN: <csrf-token>
Content-Type: application/json
```

Authentication is carried by the session cookie.

```json
{
  "listingId": "UUID",
  "arrivalAt": "2026-08-20T14:00:00+05:30",
  "departureAt": "2026-08-20T16:00:00+05:30",
  "vehicleType": "CAR",
  "expectedPrice": {
    "amountMinor": 15000,
    "currency": "INR"
  }
}
```

Field ownership:

- client supplies booking intent and the price it was shown;
- authenticated session supplies the driver;
- listing supplies the owner;
- backend calculates authoritative price;
- backend determines status; and
- backend selects and snapshots the internal turnover buffer.

`expectedPrice` is not authoritative. It prevents ParkEasy from silently confirming at a price different from the one displayed.

Successful response: `201 Created`

```http
Location: /api/v1/bookings/{bookingId}
```

```json
{
  "bookingId": "UUID",
  "status": "CONFIRMED",
  "listingId": "UUID",
  "arrivalAt": "2026-08-20T14:00:00+05:30",
  "departureAt": "2026-08-20T16:00:00+05:30",
  "price": {
    "amountMinor": 15000,
    "currency": "INR"
  },
  "confirmedAt": "2026-08-17T12:00:00+05:30"
}
```

The internal blocked interval and buffer are omitted because the contractual departure time remains authoritative for the driver.

| Condition | Status | Code |
|---|---:|---|
| Missing/invalid idempotency key | `400` | `IDEMPOTENCY_KEY_REQUIRED` or `IDEMPOTENCY_KEY_INVALID` |
| Same key with different request | `409` | `IDEMPOTENCY_KEY_REUSED` |
| Matching request still processing | `409` | `BOOKING_ATTEMPT_IN_PROGRESS` |
| Listing not bookable | `409` | `LISTING_NOT_BOOKABLE` |
| Interval outside owner schedule | `409` | `INTERVAL_NOT_OFFERED` |
| Another booking wins the interval | `409` | `BOOKING_INTERVAL_UNAVAILABLE` |
| Displayed price is stale | `409` | `PRICE_CHANGED` |
| Invalid interval/vehicle | `422` | relevant validation code |
| Expected simulator failure | `422` | `SIMULATED_PAYMENT_FAILED` |

A matching successful retry returns the original booking rather than creating another. Exact replay response metadata will be proven and documented during implementation.

### 11.2 View booking

```http
GET /api/v1/bookings/{bookingId}
```

Accessible to the driver, listing owner, or authorized administrator. The representation is shaped by role and does not leak unnecessary information. A confirmed driver may receive the exact access details needed to use the space.

### 11.3 List current driver's bookings

```http
GET /api/v1/me/bookings?page=0&size=20&sort=confirmedAt,desc
```

No user-ID query parameter is accepted; identity comes from the session.

### 11.4 List owned-listing bookings

```http
GET /api/v1/owner/bookings?page=0&size=20&sort=arrivalAt,asc
```

The backend restricts results to listings owned by the authenticated user.

### 11.5 Cancel booking

```http
POST /api/v1/bookings/{bookingId}/cancellations
Idempotency-Key: <client-generated-unique-value>
X-CSRF-TOKEN: <csrf-token>
```

```json
{
  "reasonCode": "PLAN_CHANGED",
  "note": "Optional explanation"
}
```

The authenticated identity determines whether the actor is driver or owner. The client cannot choose the actor type. Response is `201 Created` with cancellation and clearly labelled simulated financial outcomes.

Conflicts include an already-cancelled booking and cancellation attempted at or after scheduled departure.

## 12. Administrative endpoints

All require administrator authorization and audit recording.

| Operation | Endpoint |
|---|---|
| List pending submissions | `GET /api/v1/admin/listing-review-submissions?status=PENDING&page=0&size=20` |
| Record review decision | `POST /api/v1/admin/listing-review-submissions/{submissionId}/decisions` |
| Suspend listing | `POST /api/v1/admin/listings/{listingId}/suspensions` |
| Suspend user | `POST /api/v1/admin/users/{userId}/suspensions` |
| Inspect support booking | `GET /api/v1/admin/bookings/{bookingId}` |

Decisions and suspensions require reason codes. Reactivation is an explicit audited operation rather than deletion of history.

## 13. Idempotency contract

### Client responsibilities

- Generate one unique key for one logical command.
- Reuse it only when retrying the identical command after timeout or ambiguous response.
- Generate a new key for a genuinely new intent.

### Server responsibilities

- Scope the key to authenticated user and operation.
- Fingerprint canonical material fields.
- Claim the key through PostgreSQL uniqueness.
- Return the original terminal result for a matching retry.
- Reject the same key with a different fingerprint.
- Never expose another user's result because keys collide.

The create-booking fingerprint includes authenticated driver, operation, listing, canonical arrival/departure instants, vehicle type, expected amount, and currency. It fingerprints typed canonical values rather than raw JSON, because harmless property ordering or timestamp formatting must not change logical identity.

## 14. Privacy and logging

- Public listing responses expose approximate location only.
- Confirmed drivers receive only required access details.
- Owners do not receive unnecessary driver credentials or evidence.
- Verification evidence is protected separately from public media.
- Passwords, session IDs, CSRF tokens, internal stack traces, and internal buffer values are never returned or logged.
- Errors return a correlation ID; structured logs contain only safe operational context.
- Idempotency keys are redacted or hashed in general logs while protected records remain diagnosable.

## 15. Contract testing requirements

Before an endpoint is complete:

- request/response examples are exercised by API integration tests;
- authentication and authorization failures are tested;
- malformed, unknown, and semantically invalid fields are tested;
- error codes and status mappings are asserted;
- idempotent replay and mismatched-key reuse are tested;
- booking concurrency is tested against PostgreSQL;
- exact-address and internal-buffer leakage are tested; and
- generated OpenAPI output, if introduced, agrees with this contract.

## 16. Deferred capabilities

- real payment callbacks, refunds, and payouts;
- JWT/OAuth client flows;
- mobile-specific authentication;
- monthly/annual rentals and request-to-book holds;
- ratings, messaging, and Kafka event contracts;
- Near me until geolocation/provider/privacy choices are approved; and
- third-party API clients.

## 17. Open decisions resolved feature-by-feature

1. Password policy and pilot email verification.
2. Session lifetime, renewal, concurrent-session, and invalidation rules.
3. Exact CSRF token delivery mechanism.
4. Whether registration automatically creates a session.
5. Minimum/maximum booking duration and future horizon.
6. Hourly rounding and daily-price precedence.
7. Vehicle codes and whether saved vehicles are introduced.
8. Successful idempotent-replay response metadata.
9. In-progress idempotency retry guidance.
10. Search sort order.
11. Detailed listing/photo/evidence payloads.
12. Owner role activation workflow.

These do not block repository foundation; they are resolved before implementing their affected endpoint.

## 18. Review questions

The engineer should be able to explain:

- why driver and owner IDs are not authoritative request fields;
- why expected price differs from authoritative price;
- why a booking race returns `409` rather than `500`;
- why the internal buffer is absent from customer responses;
- why session cookies require CSRF protection;
- why `401` and `403` differ;
- why `PUT` replaces the weekly schedule;
- how one idempotency key maps to one logical operation;
- why raw JSON is a poor fingerprint; and
- why UUIDs do not replace authorization.

## 19. Approval record

Before drafting v0.1, the product owner approved:

- backend-derived authenticated and owner identity;
- backend-controlled authoritative price, status, and turnover buffer;
- client-supplied listing, interval, vehicle type, and displayed-price expectation;
- idempotency metadata for booking commands;
- `201 Created` for successful booking creation;
- conflict semantics for concurrency loss;
- omission of internal buffer values from customer responses; and
- secure server-side session authentication with CSRF protection rather than JWT for the browser MVP.

API v0.1 was reviewed and approved by the product owner on 2026-08-18.
