# Code Review: Periode CRUD

Review of the `com.siakad.periode` module (uncommitted changes), focused on the "single active periode per jenjang" invariant.

## Findings

### 1. `create()` never enforces the invariant
**File:** `src/main/java/com/siakad/periode/service/PeriodeService.java:31`

`create()` has an empty `if (request.status())` block that only contains a comment describing the "deactivate other active periode for the same jenjang" invariant, but never enacts it.

**Failure scenario:** Client creates a second `PeriodeEntity` with `status=true` for jenjang SD while one already exists active; both rows end up with `status=true` for the same jenjang, silently breaking the single-active-periode invariant.

### 2. `update()` invariant checks are no-ops
**File:** `src/main/java/com/siakad/periode/service/PeriodeService.java:56`

Same issue as above: setting `status=true` never deactivates other active periods of the same jenjang, and setting `status=false` never checks that at least one other active periode remains.

**Failure scenario:** Admin flips the only active SD periode to `status=false` via `PUT /api/periode/{id}`; the system ends up with zero active periods for jenjang SD and nothing prevents or warns about it, even though the comment states this must be guaranteed.

### 3. `delete()` lacks a last-active-periode guard
**File:** `src/main/java/com/siakad/periode/service/PeriodeService.java:67`

There's a dangling comment ("pastikan minimal ada 1 periode yang aktif...") with no corresponding check before `periodeRepository.delete(entity)`.

**Failure scenario:** Admin deletes the last active periode for jenjang SD; the repository call succeeds unconditionally, leaving jenjang SD with no active periode and no validation error raised.

### 4. `@NotNull` on `status` contradicts documented optionality
**File:** `src/main/java/com/siakad/periode/dto/PeriodeRequest.java:23`

Field `status` is annotated `@NotNull(message = "Status wajib diisi")`, contradicting the class Javadoc (lines 14-17) which says status is optional and defaults to `false` on create / is preserved on update.

**Failure scenario:** A client omits `status` in a `POST /api/periode` body as the README/Javadoc imply is supported; Bean Validation rejects the request with 400 "Status wajib diisi" before `PeriodeService.create` is ever reached, so the documented default-to-false behavior (only exercised in `PeriodeServiceTest`, which bypasses `@Valid`) is unreachable via the actual HTTP endpoint.

## Root cause

No repository query exists to support the "single active periode per jenjang" invariant at all — this confirms it's entirely unimplemented, not just uncalled.

## Suggested fix

- Add a repository query, e.g. `findByJenjangAndStatusTrue(jenjang)` (or similar), to `PeriodeRepository`.
- In `create()`/`update()`, when setting `status=true`, use that query to find and deactivate any other active periode for the same jenjang.
- In `update()`/`delete()`, when deactivating or removing an active periode, check whether it's the last active one for its jenjang and reject the operation (or require a replacement) if so.
- Relax `@NotNull` on `PeriodeRequest.status` (or change the Javadoc) so behavior matches across validation and service layers.
