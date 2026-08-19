# ParkEasy Product Requirements Document

**Version:** 0.5  
**Status:** Draft for product-owner review  
**Date:** 2026-08-15  
**Initial market:** Rajouri Garden–Tagore Garden, West Delhi  

## 1. Document purpose

This document defines the first testable version of ParkEasy. It describes the product problem, intended users, required behavior, boundaries, risks, and unresolved product decisions.

It intentionally does not prescribe the software architecture, database schema, Java classes, or API endpoints. Those decisions belong in the HLD, LLD, database design, and API specification after this PRD is reviewed.

## 2. Product summary

ParkEasy is a two-sided parking marketplace. It allows people or businesses with an unused, suitable parking space to publish its availability, while drivers can discover and instantly reserve that space for an hourly or daily period.

The MVP will test whether trusted private parking supply can be matched with drivers seeking predictable parking in the Rajouri Garden–Tagore Garden corridor. ParkEasy will validate listings before publication and will simulate payments without collecting money or financial credentials.

## 3. Problem statement

Drivers visiting busy localities may face uncertainty about:

- where legal and suitable parking is available;
- whether a space will still be available when they arrive;
- whether their vehicle will fit;
- the price and allowed parking period; and
- how to access the space.

At the same time, residents and businesses may possess suitable parking capacity that is unused during predictable periods but have no trusted, convenient way to offer it temporarily.

The initial product hypothesis is:

> If ParkEasy provides verified listings, explicit availability, transparent details, and dependable reservations, drivers will reserve nearby private parking and owners will make otherwise unused spaces available.

This hypothesis has not yet been validated with real users.

## 4. Product objectives

The MVP must:

1. Enable a legitimate owner to submit one independently bookable parking space for review.
2. Enable an administrator to validate that listing before it becomes discoverable.
3. Enable a driver to search for parking by locality and required time.
4. Give the driver enough information to judge suitability before booking.
5. Prevent overlapping confirmed bookings for the same space.
6. Confirm a booking instantly when the space is available and simulated payment succeeds.
7. Allow drivers and owners to view and manage relevant bookings.
8. Produce evidence that supports or rejects the initial marketplace hypothesis.

## 5. Non-goals

The following are explicitly outside MVP v0.1:

- monthly or annual parking rentals;
- owner-approved or request-to-book reservations;
- real payment gateway integration or movement of money;
- refunds, settlements, payouts, invoices, or tax processing;
- dynamic pricing;
- ratings and reviews;
- messaging between drivers and owners;
- multi-space capacity within one listing;
- recurring bookings;
- waitlists;
- auctions or bidding;
- AI-powered or semantic search;
- recommendations;
- Kafka or event-driven microservices;
- native mobile applications;
- nationwide or Delhi-wide launch;
- automated fraud detection; and
- fully automated listing approval.

Excluding these features does not imply that the future design should make them impossible. It means the MVP will not carry their operational and engineering cost before the core hypothesis is validated.

## 6. Initial market and validation boundary

The provisional launch area is the Rajouri Garden–Tagore Garden corridor in West Delhi. This was selected because the product owner can repeatedly access the area for interviews, listing checks, and early operational support.

Before treating the location as validated, the team should:

- interview at least five drivers who park in the area;
- interview at least five potential parking-space owners;
- observe parking behavior during at least two different busy periods;
- identify the main alternatives drivers currently use; and
- determine whether the dominant problem is capacity, discovery, predictability, distance, safety, or price.

These counts are discovery activities, not claims of statistical significance.

## 7. Personas and jobs to be done

### 7.1 Driver

The initial driver is a commuter or visitor who needs hourly or daily parking near a destination.

**Job to be done:** When I plan to drive to a busy locality, help me find and reserve a suitable parking space for the required time so that I do not arrive without a reliable parking option.

### 7.2 Owner

The initial owner is an individual or business that controls a suitable parking space and can make it available during known periods.

**Job to be done:** When my parking space is unused, help me offer it safely for specific periods so that I can earn from it without losing control of its availability.

### 7.3 Administrator

The initial administrator is a ParkEasy operations team member responsible for marketplace trust and exception handling.

**Job to be done:** Help me prevent unsuitable or fraudulent listings from entering the marketplace and give me a traceable way to handle operational problems.

## 8. MVP scope and priority

### 8.1 Must-have capabilities

- Account registration and sign-in.
- Driver, owner, and administrator permissions.
- Owner listing creation, submission, and review.
- Locality-and-time-based parking search.
- Listing details and suitability information.
- Recurring weekly availability with date-specific exceptions.
- Hourly and daily prices.
- Instant booking.
- Simulated payment outcomes.
- Booking history and booking details for drivers and owners.
- Driver cancellation using the approved grace-period and time-based fee policy.
- Administrative listing review and marketplace controls.
- Audit history for privileged administrative actions.

### 8.2 Should-have capability

- A consent-based **Near me** search that finds spaces within a distance of the driver's current location.

Near me may be included in the MVP if geolocation and map dependencies do not delay the complete locality-search-to-booking journey. Locality search remains the required launch path.

## 9. Functional requirements

### 9.1 Accounts and roles

| ID | Requirement |
|---|---|
| FR-A01 | A person must be able to register and sign in securely. |
| FR-A02 | The system must distinguish driver, owner, and administrator permissions. |
| FR-A03 | One person may act as both a driver and an owner without maintaining unrelated duplicate identities. |
| FR-A04 | Suspended users must not be able to perform restricted marketplace actions. |

The exact authentication mechanism is an architectural decision, not a PRD decision.

### 9.2 Owner verification and listings

| ID | Requirement |
|---|---|
| FR-O01 | An owner must be able to create and save a draft listing. |
| FR-O02 | A listing must contain an address or map position, photos, supported vehicle size or type, covered/uncovered status, access instructions, hourly and/or daily price, availability, and owner contact information. |
| FR-O03 | The owner must provide evidence of identity and evidence that they control or are authorized to offer the space. The accepted evidence remains an open compliance decision. |
| FR-O04 | Automated checks must identify missing required data and may flag implausible or duplicate submissions before manual review. |
| FR-O05 | A listing must not appear in customer search until an administrator approves it. |
| FR-O06 | An administrator must approve or reject a submission using a recorded reason. |
| FR-O07 | Rejected listings must remain editable and may be resubmitted. |
| FR-O08 | Changes to address, authorization evidence, or owner identity must trigger re-review. |
| FR-O09 | Routine pricing and availability changes must not require re-review unless flagged as suspicious. |
| FR-O10 | An owner must be able to pause an approved listing without deleting its history. |
| FR-O11 | One MVP listing must represent exactly one independently bookable parking space. |

### 9.3 Availability and pricing

| ID | Requirement |
|---|---|
| FR-V01 | An owner must be able to define recurring availability by day of week and time range. |
| FR-V02 | An owner must be able to add date-specific availability exceptions or closures. |
| FR-V03 | A search result must only show a space when the requested interval fits its effective availability. |
| FR-V04 | Existing confirmed bookings must make the overlapping interval unavailable. |
| FR-V05 | The system must support hourly and daily pricing. |
| FR-V06 | The full price used for the simulated transaction must be displayed before booking confirmation. |
| FR-V07 | The system may calculate and record an illustrative platform commission, but no fee is collected in the MVP. |
| FR-V08 | A fixed 10-minute turnover buffer must block the space after each booking's scheduled departure before another booking may begin. The driver is not charged for this buffer and is still required to leave by the scheduled departure time. |

Pricing precedence, partial-day pricing, taxes, minimum duration, and rounding rules remain open decisions.

### 9.4 Search and listing discovery

| ID | Requirement |
|---|---|
| FR-S01 | A driver must provide a locality, arrival time, and departure time to perform the required MVP search. |
| FR-S02 | Search must return approved, active, suitable, and available listings only. |
| FR-S03 | A driver must be able to filter results by price, distance when coordinates are available, covered/uncovered status, and supported vehicle size or type. |
| FR-S04 | Results must provide enough summary information to compare price, approximate location, suitability, and availability. |
| FR-S05 | A listing-detail view must show photos, features, access constraints, cancellation terms, price, and an approximate location before booking. |
| FR-S06 | The exact access location should be disclosed only when operationally necessary and according to the privacy rule agreed during PRD review. |
| FR-S07 | If Near me is implemented, the system must request location permission and remain usable through locality search when permission is denied. |

Search ranking order remains an open product decision. The MVP must not advertise an opaque “best” result without defined ranking rules.

### 9.5 Booking

| ID | Requirement |
|---|---|
| FR-B01 | A driver must be able to request an instant booking for an available hourly or daily interval. |
| FR-B02 | A booking must identify one driver, one listing, one time interval, and the agreed price snapshot. |
| FR-B03 | The system must re-check availability during booking and must never confirm overlapping bookings for one listing. |
| FR-B04 | A successful simulated payment must lead to one confirmed booking. |
| FR-B05 | A failed simulated payment must not create a confirmed booking or permanently consume availability. |
| FR-B06 | Repeated submission of the same booking operation must not create multiple confirmed bookings or multiple payment attempts unintentionally. |
| FR-B07 | Drivers and owners must be able to view booking status and relevant booking details. |
| FR-B08 | The system must retain historical bookings rather than physically deleting them through normal product operations. |
| FR-B09 | A driver must be able to request cancellation according to the agreed cancellation policy. |
| FR-B10 | Owners must not be able to silently cancel or alter an accepted booking without a recorded operational outcome. |
| FR-B11 | If an owner cancels a confirmed booking, the driver must receive a full simulated refund and be offered suitable available replacement listings when any exist. |
| FR-B12 | Owner-caused cancellation must record a strike and a simulated owner penalty balance unless an administrator grants a reasoned waiver. |
| FR-B13 | Repeated owner-caused cancellations must be eligible for listing pause and administrative review under thresholds defined before pilot launch. |

The concurrency mechanism, transaction boundary, locking strategy, and idempotency implementation belong in later design documents.

#### 9.5.1 Driver cancellation policy

Cancellation uses the following ordered rules. The first matching rule determines the outcome:

1. If the driver cancels within 10 minutes of booking confirmation **and before the scheduled arrival time**, the outcome is a full refund.
2. Otherwise, if the driver cancels at least 24 hours before arrival, the outcome is a full refund.
3. Otherwise, if the driver cancels between 2 and 24 hours before arrival, the cancellation fee is 50% of the booking price.
4. Otherwise, if the driver cancels less than 2 hours before arrival or after arrival but before the scheduled departure time, the cancellation fee is 100% of the booking price.
5. After the scheduled departure time, cancellation is forbidden. The driver may instead use the support or dispute workflow.

For the MVP, refunds and fees are simulated accounting outcomes; no money moves. Every cancellation must record the booking, actor, decision time, applicable rule, fee/refund outcome, and reason when supplied. Cancellation must not delete booking history.

The cancelled interval must become available immediately after a successful cancellation. If another booking is confirmed for that interval concurrently, normal booking-consistency rules apply.

Repeated use of the 10-minute grace period is a potential temporary-hold abuse pattern. The MVP must make this behavior measurable. Automated penalties or rate limits are deferred until evidence justifies them.

#### 9.5.2 Owner cancellation principle

When an owner cancels a confirmed booking:

1. The driver receives a full simulated refund.
2. ParkEasy searches for currently available alternatives matching the required time and vehicle suitability.
3. The driver may choose from the offered alternatives; ParkEasy must not silently create a replacement booking.
4. ParkEasy issues a bounded, non-withdrawable, non-transferable simulated booking credit even when owner recovery is not immediately possible.
5. An equivalent simulated penalty balance and cancellation strike are recorded against the owner.
6. A future real-payment system may recover the penalty from pending or future owner payouts. Customer compensation must not wait for that recovery.
7. If recovery never occurs, ParkEasy bears the bounded compensation cost.
8. Repeated cancellation strikes pause the listing and trigger administrative review.
9. An administrator may waive a strike or penalty for verified exceptional circumstances, with an audit record and reason.

The compensation formula is provisionally expressed as:

`minimum(percentage of original booking price, fixed monetary cap)`

The percentage, cap, strike window, and escalation thresholds must be decided after local pricing and field research. The MVP simulates these amounts and must not imply that real money or credit has been issued.

### 9.6 Simulated payments

| ID | Requirement |
|---|---|
| FR-P01 | The MVP must use an internal simulated payment provider and must not integrate a real payment gateway. |
| FR-P02 | The simulator must never request or store real card, bank-account, or UPI credentials. |
| FR-P03 | The simulator must support deterministic success and failure scenarios for testing. |
| FR-P04 | Payment attempts must record amount, status, booking association, and timestamps. |
| FR-P05 | The design must allow a real gateway sandbox implementation to replace the simulator later without rewriting booking policy. |
| FR-P06 | The simulator must record the refund and cancellation-fee outcome that would apply under the approved policy without moving money. |
| FR-P07 | The simulator must separately record driver compensation, owner penalty balances, recovered amounts, and hypothetical platform-funded loss. |

Timeout, delayed-confirmation, webhook, reconciliation, refund, and chargeback behavior will be designed when an external payment provider is introduced.

### 9.7 Administration

| ID | Requirement |
|---|---|
| FR-M01 | An administrator must be able to view pending listing submissions and their verification evidence. |
| FR-M02 | An administrator must be able to approve or reject a listing with a reason. |
| FR-M03 | An administrator must be able to suspend and reactivate users and listings with a reason. |
| FR-M04 | An administrator must be able to inspect users, listings, and bookings needed for support. |
| FR-M05 | An administrator must be able to record the outcome of a cancellation or dispute. |
| FR-M06 | Privileged actions must create an audit record containing the actor, action, target, reason, and time. |
| FR-M07 | Normal administrative workflows must not physically delete booking or audit history. |
| FR-M08 | Administrators must have a queue for pending listing reviews and unresolved disputes. |

Administrative impersonation, bulk operations, refunds, and advanced fraud tooling are outside the MVP.

## 10. Product state definitions

These states describe observable product behavior, not implementation classes.

### 10.1 Listing lifecycle

`DRAFT -> PENDING_REVIEW -> APPROVED | REJECTED`

An approved listing may additionally become `PAUSED` or `SUSPENDED`. Security-sensitive edits return it to `PENDING_REVIEW` before republication.

### 10.2 Booking lifecycle

At minimum, the product must distinguish:

- a booking attempt that has not completed;
- a confirmed booking;
- a cancelled booking;
- a completed booking; and
- a failed booking attempt.

Exact state names and transition ownership will be defined during LLD.

### 10.3 Simulated payment lifecycle

At minimum, the product must distinguish a created attempt, successful attempt, and failed attempt. Additional external-provider states are deferred.

## 11. Core user journeys

### 11.1 Owner publishes a space

1. Owner registers or signs in.
2. Owner creates a draft and supplies listing and verification information.
3. System validates required information and flags obvious issues.
4. Owner submits the listing.
5. Administrator reviews it.
6. Owner receives approval or actionable rejection reasons.
7. An approved, active listing becomes eligible for search.

### 11.2 Driver reserves a space

1. Driver enters locality, arrival time, and departure time.
2. Driver compares available listings and applies optional filters.
3. Driver reviews listing details, price, and terms.
4. Driver initiates an instant booking.
5. System re-checks availability and executes the simulated payment.
6. On success, the system confirms the booking and exposes necessary access details.
7. Driver and owner can view the confirmed booking.

### 11.3 Administrator handles a marketplace exception

1. Administrator opens the relevant review or dispute queue.
2. Administrator examines the minimum information necessary.
3. Administrator records a decision and reason.
4. The system applies the allowed state change and creates an audit record.

## 12. Non-functional requirements

### 12.1 Correctness and consistency

- A single parking space must never have overlapping confirmed bookings.
- A retry must not accidentally create duplicate bookings or payment attempts.
- Price and important listing terms agreed at booking time must remain historically explainable even if the listing later changes.
- Booking and audit history must remain traceable.

### 12.2 Security and privacy

- Passwords, if used, must never be stored in plaintext.
- Authorization must be enforced by the server, not only hidden in the user interface.
- Owners must access only their own private listing and booking information.
- Drivers must access only their own private booking information.
- Administrator access must follow least privilege and be auditable.
- Sensitive identity and authorization evidence must be minimized, protected, and retained only under an explicit policy.
- Real financial credentials must never enter the simulated-payment system.
- Deployed network traffic carrying credentials or private information must use HTTPS.
- Exact parking addresses must not be exposed more widely or earlier than necessary.

### 12.3 Reliability and failure behavior

- Failures must produce an understandable user outcome rather than ambiguous confirmation.
- A simulated payment failure must release any temporary booking claim.
- The system must be restartable without losing committed booking history.
- Health checks, structured logs, and operational metrics will be required before production deployment.

### 12.4 Performance and scale

No resume or production performance figures are claimed in this PRD.

During HLD and performance-test planning, the team must define a reproducible workload and provisional latency, throughput, concurrency, and resource targets. Baseline measurements must be captured before optimizations are introduced.

### 12.5 Accessibility and usability

- Critical journeys must work on common mobile-sized screens, even if the MVP is a web application.
- Forms must explain validation failures and preserve recoverable user input.
- Location permission denial must not block locality-based search.
- Listing rejection reasons must tell the owner what can be corrected.

## 13. Business model hypothesis

The provisional business model is a percentage commission on completed bookings. During the MVP:

- the commission may be calculated and stored for analysis;
- no commission is collected;
- no owner payout is performed; and
- the commission percentage remains undecided.

The purpose is to preserve price transparency and test whether the marketplace can support a commission model without prematurely building financial operations.

## 14. Product metrics

The MVP should instrument the following without inventing targets before discovery:

### Supply

- owners who start and complete listing submission;
- listing approval and rejection counts;
- median and tail listing-review time;
- approved listings with bookable availability; and
- reasons for owner abandonment or listing rejection.

### Demand

- completed searches;
- searches returning zero results;
- listing-detail views;
- booking attempts and confirmed bookings; and
- search-to-detail and detail-to-booking conversion.

### Marketplace quality

- booking conflicts or double bookings, whose required count is zero;
- cancellations and their reasons;
- owner-caused cancellations, strikes, waivers, and replacement options offered;
- no-shows and access failures, once those outcomes are defined;
- disputes and resolution time; and
- repeat drivers and repeat owners.

The MVP must also report simulated compensation issued, simulated owner recovery, and the amount ParkEasy would have absorbed. These figures must be clearly labelled as simulated rather than real financial results.

### Technical performance

- search and booking latency distributions;
- request throughput and error rate;
- database query latency;
- resource use under a defined workload; and
- booking results under concurrent attempts.

Metrics must distinguish real-user measurements from synthetic tests.

## 15. Release acceptance criteria

The MVP is eligible for a controlled pilot only when:

1. At least one approved listing can complete the full owner workflow.
2. A driver can search, inspect, and successfully reserve that listing.
3. The same interval cannot be confirmed twice under concurrent booking attempts.
4. Failed simulated payments do not leave confirmed bookings or permanently blocked availability.
5. Repeated booking submissions do not produce unintended duplicates.
6. Role boundaries have automated tests for important unauthorized actions.
7. Administrative listing decisions and suspensions produce audit records.
8. The critical workflows have integration tests against a real PostgreSQL instance in the test environment.
9. Known limitations and pilot support procedures are documented.
10. No real financial credentials or money are handled.

The number of pilot listings and users will be chosen after field discovery; it is not a software-release criterion yet.

## 16. Assumptions

- Initial users have web access on a mobile or desktop browser.
- The first pilot can be supported manually by a small ParkEasy operations team.
- Owners can describe deterministic availability in advance.
- One listing represents one identifiable space during the MVP.
- Drivers accept instant booking without prior owner approval when the listing is active and available.
- Locality search is sufficient for the required path; device geolocation is optional.
- Notifications can initially use an in-product mechanism or another simple channel chosen later.
- The legality and permissions required to commercially offer different types of private parking spaces still require validation.

## 17. Risks and mitigations

| Risk | Why it matters | Initial mitigation |
|---|---|---|
| Insufficient owner supply | Search has no useful results even if drivers are interested. | Interview owners before building and pilot in one compact corridor. |
| Existing parking is already adequate | The product does not solve a painful problem. | Observe behavior and ask drivers about predictability, walking distance, safety, and price rather than assuming capacity shortage. |
| Fraudulent or unauthorized listings | Drivers, owners, and ParkEasy face safety and legal harm. | Manual first-listing review, identity and authorization evidence, audit records, and suspension controls. |
| Manual review creates friction | Owners abandon submission or wait too long. | Validate fields before submission, provide reasoned rejections, track review time, and define an operating target before pilot. |
| Double booking | The core promise becomes unreliable. | Treat non-overlap as an invariant and test concurrent booking attempts. |
| Grace-period abuse | Repeated short bookings could temporarily hide desirable availability from genuine drivers. | Preserve cancellation history, measure repeated grace cancellations, and add limits only when evidence justifies them. |
| Owner-compensation exposure | ParkEasy may owe customer credit that it cannot recover from an owner who leaves the platform. | Bound compensation with a cap, measure simulated exposure, and introduce reserves or withholding only if later evidence and compliance review justify them. |
| Compensation collusion | A driver and owner could coordinate cancellations to manufacture credits. | Keep credits non-withdrawable and non-transferable, retain linked audit history, and study patterns before automating real compensation. |
| Owner availability is stale | Instant booking fails operationally even if technically valid. | Make availability easy to pause and edit; measure owner-caused cancellations and access failures. |
| Exact-address exposure | Owners face privacy and security concerns. | Show approximate location before booking and disclose exact details only when necessary. |
| Regulatory or society restrictions | Some spaces may not legally or contractually be rentable. | Obtain local legal/compliance guidance and define acceptable evidence before public launch. |
| Scope expansion | The learning project never reaches real users. | Keep monthly rentals, real payments, AI, and distributed infrastructure outside the MVP. |

## 18. Dependencies requiring later selection

- map, geocoding, and distance-calculation provider;
- notification channel;
- accepted owner identity and authorization evidence;
- privacy and evidence-retention policy;
- eventual payment gateway;
- hosting environment; and
- legal/compliance guidance for the pilot area.

Technology selection must follow the relevant requirement and must not be added solely for resume value.

## 19. Open product decisions

The PRD review must resolve or explicitly defer the following:

1. What is the minimum and maximum booking duration?
2. How is an hourly price converted when a booking includes partial hours?
3. When does daily pricing apply, and what defines a “day”?
4. Does field evidence support the provisional 10-minute turnover buffer, or should it change before the pilot?
5. How far into the future may drivers book?
6. What compensation percentage and monetary cap apply to an owner-caused cancellation, and what rolling strike thresholds trigger warning, pause, and suspension?
7. How are no-shows, overstays, and inaccessible spaces recorded and handled?
8. Which vehicle categories and dimensions are supported initially?
9. What evidence proves that an owner may offer a space?
10. When is the exact address revealed to a driver?
11. What default search order is used?
12. What illustrative commission percentage is shown?
13. Which notification channel is required for the pilot?
14. What information may an administrator view, and how long is it retained?
15. What local permissions, property rules, insurance, or liability terms apply?

## 20. Post-MVP directions

Candidate future increments include:

1. owner-selectable request-to-book with expiring holds;
2. real payment-provider sandbox integration, followed later by live payments;
3. monthly and annual parking products with their own renewal and cancellation policies;
4. multiple interchangeable spaces per listing;
5. ratings, reviews, and owner/driver reputation;
6. event-driven notifications and retry handling;
7. measured caching and search improvements;
8. broader Delhi coverage; and
9. natural-language parking search after structured search is proven.

Each increment requires its own problem statement, evidence, design alternatives, failure analysis, and measurable acceptance criteria.

## 21. Approval record

The product owner approved the following direction on 2026-08-15:

- West Delhi as the initial geography;
- Rajouri Garden–Tagore Garden as the provisional corridor;
- hourly and daily instant booking for the MVP;
- monthly and annual rentals as post-MVP;
- manual first-listing verification supported by automated checks;
- locality search as required and Near me as an enhancement;
- an internal simulated payment provider with no gateway or real money;
- a provisional commission business model; and
- the minimal administrative scope defined in this document.

The product owner approved the driver cancellation policy in Section 9.5.1 on 2026-08-15.

The product owner approved the owner-cancellation and independent-compensation principle in Section 9.5.2 on 2026-08-15. The monetary values and escalation thresholds remain unresolved.

The product owner approved the minimal persisted booking state and post-departure cancellation rule on 2026-08-15: bookings persist as `CONFIRMED` or `CANCELLED`; upcoming, in-progress, and ended views are derived from time; cancellation is forbidden after scheduled departure.

The product owner approved a provisional fixed 10-minute turnover buffer on 2026-08-15. The customer is charged only for the requested interval, must leave by scheduled departure, and the next booking may begin only after the buffer. This duration requires validation with owners.

Approval of this direction does not yet constitute approval of every unresolved rule in Section 19.
