# TODO — Remaining Changes

---

## 🔴 Critical (Fix First — Will Break in Production)

### 1. Add `@Transactional` to all multi-step service methods - Done
Every method that does more than one DB write needs `@Transactional` so a mid-way failure doesn't leave the DB in a broken state.

**Files to update:**
- `CardService.java`
  - `createCard` — saves card twice (initial + generateTicketId)
  - `moveCard` — updates positions across multiple cards then saves all
  - `deleteCard` — deletes across 3 tables then deletes card
- `LaneService.java`
  - `moveLane` — updates positions across multiple lanes then saves all
  - `deleteLane` — (once cascade is added, will touch multiple tables)
- `BoardService.java`
  - `deleteBoard` — deletes across lanes, cards, cardMembers, cardLabels, boardMembers, labels
  - `changeAdmin` — updates two BoardMember rows
- `WorkspaceService.java`
  - `deleteWorkspace` — deletes across entire tree of entities
  - `transferOwnerShip` — updates two WorkspaceMember rows

**How to fix — add `@Transactional` import and annotation:**
```java
import org.springframework.transaction.annotation.Transactional;

@Transactional
public void deleteBoard(Integer boardId) { ... }
```

---

### 2. `deleteLane` does not cascade delete cards (FK violation) - Done
`LaneService.deleteLane` calls `laneRepository.delete(lane)` directly.
If the lane has cards, the DB throws a foreign key constraint violation.

**File:** `LaneService.java`

**Missing dependencies to inject:**
- `CardRepository`
- `CardMemberRepository`
- `CardLabelRepository`
- `CommentRepository`
- `ActivityLogRepository`
- `ChecklistRepository`
- `ChecklistItemRepository`

**Fix — replace the current `deleteLane` body:**
```java
public void deleteLane(Integer laneId) {
    Lane lane = helper.resolveLane(laneId);
    AppUser current = helper.getCurrentUser();
    accessControl.requiredBoardMemberOrAbove(lane.getBoard(), current);

    List<Card> cards = cardRepository.findByLane(lane);
    for (Card card : cards) {
        activityLogRepository.deleteAll(activityLogRepository.findByCard(card));
        cardMemberRepository.deleteAll(cardMemberRepository.findByCard(card));
        cardLabelRepository.deleteAll(cardLabelRepository.findByCard(card));
        commentRepository.deleteAll(commentRepository.findByCard(card));
        List<Checklist> checklists = checklistRepository.findByCard(card);
        for (Checklist checklist : checklists) {
            checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklist));
        }
        checklistRepository.deleteAll(checklists);
    }
    cardRepository.deleteAll(cards);
    laneRepository.delete(lane);
}
```

---

### 3. `deleteCard` does not delete comments or checklists (FK violation) -- Done
`CardService.deleteCard` deletes activityLogs, cardMembers, cardLabels — but not `Comment` or `Checklist`/`ChecklistItem` rows. FK violation if those exist on the card.

**File:** `CardService.java`

**Missing dependencies to inject:**
- `CommentRepository`
- `ChecklistRepository`
- `ChecklistItemRepository`

**Fix — add to `deleteCard` before `cardRepository.delete(card)`:**
```java
commentRepository.deleteAll(commentRepository.findByCard(card));
List<Checklist> checklists = checklistRepository.findByCard(card);
for (Checklist checklist : checklists) {
    checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklist));
}
checklistRepository.deleteAll(checklists);
```

---

### 4. `deleteBoard` and `deleteWorkspace` don't delete comments or checklists - Done
Same as above — the card cascade in both `BoardService.deleteBoard` and `WorkspaceService.deleteWorkspace` deletes cardMembers and cardLabels but misses comments and checklists.

**Files:** `BoardService.java`, `WorkspaceService.java`

**Fix — inside the `for (Card card : cards)` loop in both methods, add:**
```java
commentRepository.deleteAll(commentRepository.findByCard(card));
List<Checklist> checklists = checklistRepository.findByCard(card);
for (Checklist checklist : checklists) {
    checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklist));
}
checklistRepository.deleteAll(checklists);
```

Both services also need the missing repos injected in constructor.

---

### 5. `changeAdmin` endpoint is missing in `BoardController`   - Done
`BoardService.changeAdmin()` is implemented but there is no controller mapping for it. The feature is completely unreachable from the API.

**File:** `BoardController.java`

**Fix — add this endpoint and the missing import:**
```java
import com.example.myapp.dto.request.ChangeBoardAdmin;

@PatchMapping("/{id}/admin")
public ResponseEntity<Void> changeAdmin(
        @Positive(message = "Invalid ID") @PathVariable Integer id,
        @Valid @RequestBody ChangeBoardAdmin request) {
    boardService.changeAdmin(id, request);
    return ResponseEntity.noContent().build();
}
```

---

## 🟠 High (Security Gaps)

### 6. `AppUserController` has no ownership checks   - Done
Any authenticated user can call `PATCH /api/users/5/password` and change another user's password, or `DELETE /api/users/3` to delete another user's account.

**File:** `AppUserController.java`

**Fix — at the start of every update and delete method, verify the caller is the owner:**
```java
// example for updateUsername by id
AppUser current = helper.getCurrentUser(); // inject Helper into controller
if (!current.getId().equals(id)) {
    throw new IllegalArgumentException("You can only update your own account");
}
```
Or alternatively: remove all the `/{id}/...` and `/email/{email}/...` update endpoints and replace with a single `/me/...` endpoint that always operates on the currently authenticated user. This is the cleaner REST design.

---

### 7. `AppUser.checkPassword()` compares raw strings — wrong and dangerous  - Done
```java
// current — WRONG, DB holds BCrypt hash, this always returns false
public boolean checkPassword(String password) {
    return this.password.equals(password);
}
```
**File:** `AppUser.java`

**Fix — delete the method entirely** (auth is handled by Spring Security + BCrypt) or rewrite if needed:
```java
// only add this if you actually need it somewhere
public boolean checkPassword(String rawPassword, PasswordEncoder encoder) {
    return encoder.matches(rawPassword, this.password);
}
```

---

### 8. Generic exception handler leaks internal error details    - in development phase need to know type of error.
```java
// current — exposes DB errors, class names, SQL to the client
return ResponseEntity.status(500).body("Error: " + ex.getMessage());
```
**File:** `GlobalExceptionHandler.java`

**Fix:**
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleGenericException(Exception ex) {
    ex.printStackTrace(); // or use a logger
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body("An unexpected error occurred. Please try again later.");
}
```

---

### 9. No rate limiting on the login endpoint  - in development phase, will do limit in future
`POST /api/auth/login` has no throttling — brute force attacks are possible.

**Options (pick one):**
- Add `Bucket4j` library and an IP-based rate limiting filter
- Configure rate limiting at the reverse proxy (Nginx / API Gateway) level
- Add Spring Boot's built-in `spring-boot-starter-actuator` + circuit breaker

---

## 🟡 Medium (Correctness & Completeness)

### 10. `UpdateUserPassword` DTO has no validation rules  - it has checked it, check again if for surety
Unlike `CreateAppUserRequest` which enforces complexity (uppercase, digit, special char, 8–20 chars), `UpdateUserPassword` likely has none.

**File:** `UpdateUserPassword.java`

**Fix — add the same constraints as registration:**
```java
@NotBlank(message = "Password cannot be empty")
@Size(min = 8, max = 20, message = "Password must be 8-20 characters")
@Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,20}$",
    message = "Password must contain uppercase, lowercase, digit and special character")
private String password;
```

---

### 11. `addMember` to a board allows setting role directly to `ADMIN` - Done
`POST /api/boards/{id}/members` accepts any role in the body, so a caller can bypass `changeAdmin` and add someone directly as ADMIN.

**File:** `BoardService.java` → `addMember` method

**Fix — add a role check:**
```java
if (request.getRole() == Role.ADMIN) {
    throw new IllegalArgumentException("Use the change-admin endpoint to assign the admin role");
}
```

---

### 12. `archiveLane`, `unarchiveLane`, `transferOwnership` return wrong HTTP status - Done
These return `ResponseEntity.ok().build()` (HTTP 200 with empty body). When there is no body, the correct response is `204 No Content`.

**Files:** `LaneController.java`, `WorkspaceController.java`

**Fix:**
```java
// change this:
return ResponseEntity.ok().build();
// to this:
return ResponseEntity.noContent().build();
```
Affected endpoints:
- `PATCH /api/lanes/{id}/archive`
- `PATCH /api/lanes/{id}/unarchive`
- `PATCH /api/workspaces/{id}/members/transfer-ownership`

---

### 13. No pagination on list endpoints  - will add in future 
All list endpoints return every record with no limit. This will cause memory and timeout issues on any real dataset.

**Files:** All service and controller list methods.

**Fix — example for `getCardsByLaneId`:**
```java
// Controller
@GetMapping("/lane/{laneId}")
public ResponseEntity<Page<CardResponse>> getCardsByLaneId(
        @PathVariable Integer laneId,
        @PageableDefault(size = 20, sort = "position") Pageable pageable) {
    return ResponseEntity.ok(cardService.getCardsByLaneId(laneId, pageable));
}

// Service
public Page<CardResponse> getCardsByLaneId(Integer laneId, Pageable pageable) {
    Lane lane = helper.resolveLane(laneId);
    ...
    return cardRepository.findByLane(lane, pageable).map(this::toCardResponse);
}

// Repository
Page<Card> findByLane(Lane lane, Pageable pageable);
```
Apply to: `getCardsByLaneId`, `getLanesByBoardId`, `getCommentsByCardId`, `getMembers` (workspace), `getMembersByBoardId`, `getLabelsByBoardId`, `getAllAppUsers`.

---

## 🟢 Low (Code Quality)

### 14. Dead code — `ValidateLoginRequest.java` is never used  - Done
The file `dto/request/ValidateLoginRequest.java` exists but is not imported or used anywhere. `LoginRequest.java` is the one actually used in `AuthController`.

**Fix:** Delete `ValidateLoginRequest.java`.

---
 
### 15. Inconsistent DTO naming — `Respond` vs `Response` - Done but didn't change folder name
Some DTOs use the suffix `Respond` and others use `Response`. Pick one and rename.

| Current name | Should be |
|---|---|
| `AppUserRespond` | `AppUserResponse` |
| `WorkspaceRespond` | `WorkspaceResponse` |
| `WorkspaceMemberRespond` | `WorkspaceMemberResponse` |
| `BoardRespond` | `BoardResponse` |
| `BoardMemberRespond` | `BoardMemberResponse` |
| `CommentRespond` | `CommentResponse` |

Remember to update all controller return types and service method signatures after renaming.

---

### 16. `Lane` entity fields missing `@Column` constraints - Done
These three fields have no `@Column` annotation so the DB schema won't enforce NOT NULL:

**File:** `Lane.java`

**Fix:**
```java
@Column(nullable = false)
private Integer position;

@Column(name = "is_archived", nullable = false)
private boolean isArchived;

@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;
```

---

### 17. Add Swagger / OpenAPI documentation  - not added , add for me
No API documentation exists. This is expected on any backend portfolio project.

**Fix — add to `pom.xml`:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

Then permit the Swagger UI in `SecurityConfig.java`:
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

Access at: `http://localhost:8080/swagger-ui/index.html`

---

### 18. Add integration tests - create test for me 
Unit tests exist for entities and some services, but there are no integration tests covering the full HTTP stack.

**What to add:**
- `AuthControllerTest` — register → login → receive token
- `WorkspaceControllerTest` — create workspace, add member, remove member
- `BoardControllerTest` — create board, update visibility, delete board
- `CardControllerTest` — create card, move card, archive card

**Setup:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // rolls back DB after each test
class WorkspaceControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void createWorkspace_returns201() throws Exception {
        mockMvc.perform(post("/api/workspaces")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workspaceName\": \"My WS\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("My WS"));
    }
}
```

---

## Implementation Order Recommendation

```
Week 1 — Critical
  [ ] @Transactional on all multi-step methods
  [ ] deleteLane cascade
  [ ] deleteCard cascade (comments + checklists)
  [ ] deleteBoard + deleteWorkspace cascade (comments + checklists)
  [ ] changeAdmin controller endpoint

Week 2 — Security
  [ ] AppUserController ownership checks (or refactor to /me endpoints)
  [ ] Delete or fix AppUser.checkPassword()
  [ ] Fix generic exception handler
  [ ] UpdateUserPassword DTO validation
  [ ] Block ADMIN role in addMember

Week 3 — Quality
  [ ] Fix HTTP 200 → 204 on void operations
  [ ] Delete ValidateLoginRequest.java
  [ ] Fix Lane @Column annotations
  [ ] Rename Respond → Response DTOs

Week 4 — Features
  [ ] Add Swagger/OpenAPI
  [ ] Add pagination to list endpoints
  [ ] Add integration tests
  [ ] Rate limiting on login endpoint
```
