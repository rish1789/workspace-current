# myapp — Progress Report

Generated: 2026-07-01
Package: `com.example.myapp` — a Trello-like task/board management backend (Workspaces → Boards → Lanes → Cards → Checklists/Comments/Labels).

---

## 1. Project Overview

### Tech Stack
- **Spring Boot** 4.0.6, **Java** 21, **Maven** (wrapper included)
- **Web**: `spring-boot-starter-web`
- **Persistence**: `spring-boot-starter-data-jpa` (Hibernate) + **PostgreSQL** (`org.postgresql:postgresql`)
- **Security**: `spring-boot-starter-security`, `spring-boot-starter-validation`
- **JWT**: `io.jsonwebtoken:jjwt-api/impl/jackson` v0.12.3
- **Docs**: `springdoc-openapi-starter-webmvc-ui` v2.5.0 (Swagger UI)
- **Testing**: H2 (test scope), `spring-boot-starter-test`, `spring-security-test` — an existing test suite covers entities (ActivityLog, Board, Card, Checklist(Item), Comment, Label, Lane, User, Workspace) and services (Board/Card/Checklist/Comment/Lane/User/Workspace)

**`application.properties`**: `jdbc:postgresql://localhost:5432/myapp_db`, `spring.jpa.hibernate.ddl-auto=create-drop` (drops schema on every shutdown — dev only), `show-sql=true`, `open-in-view=true`, JWT secret + 24h expiration **hardcoded in plaintext**.

### Database Schema

| Entity | Key fields | Relationships |
|---|---|---|
| `AppUser` | id, username, email (unique), password (BCrypt), createdAt | referenced by nearly everything as `createdBy`/`user` |
| `Workspace` | id, name, description, createdBy→AppUser, createdAt | 1→N Board, 1→N WorkspaceMember |
| `WorkspaceMember` | id, workspace, user, role, joinedAt | unique(workspace,user); role: OWNER/ADMIN/MEMBER |
| `Board` | id, workspace, name, description, visibility, createdBy, createdAt | 1→N Lane, 1→N BoardMember, 1→N Label |
| `BoardMember` | id, board, user, role, joinedAt | unique(board,user); role: ADMIN/MEMBER/OBSERVER |
| `Lane` | id, board, name, position, isArchived, createdAt | holds Cards only via `CardRepository.findByLane` (no `@OneToMany`) |
| `Card` | id, ticketId (unique, `TKT-%04d`), lane, title, description, position, dueDate, isArchived, createdBy, createdAt | 1→N Checklist, Comment, CardMember, CardLabel, ActivityLog |
| `CardMember` | id, card, user, assignedAt | unique(card,user); immutable join entity |
| `Label` | id, board, name, color (hex) | unique(board,name) |
| `CardLabel` | id, card, label | unique(card,label); immutable join entity |
| `Checklist` | id, card, title | 1→N ChecklistItem |
| `ChecklistItem` | id, checklist, content, isDone, position | |
| `Comment` | id, card, user, content, createdAt, updatedAt | |
| `ActivityLog` | id, card, user, action, createdAt | immutable |

All parent references are **unidirectional `@ManyToOne`** (with `@JsonIgnore`); there are no inverse `@OneToMany` collections anywhere — every "list children" operation goes through a repository derived query (`findByBoard`, `findByLane`, `findByCard`, etc.), not entity navigation.

Hierarchy: `AppUser` → `Workspace` → `Board` → `Lane` → `Card` → `{Checklist, Comment, CardMember, CardLabel, ActivityLog}` → `ChecklistItem`.

---

## 2. Completed Features

**Full Repository + Service + Controller** exist for: AppUser, Workspace, Board, Lane, Card, Comment, Checklist.
**Sub-resources without their own controller** (managed inside a parent service/controller): WorkspaceMember, BoardMember, Label, CardLabel, CardMember, ActivityLog, ChecklistItem — all exposed as nested endpoints under `BoardController`/`CardController`/`ChecklistController`.

- 14 repositories, all simple `JpaRepository<T, Integer>` with derived-query methods only (no custom JPQL/SQL).
- 7 `@Service @Transactional` classes.
- 9 controller classes (incl. `GlobalExceptionHandler`).

### JWT Security — Status: **Working, basic**
- `JwtUtil`: HMAC-signed token (`Keys.hmacShaKeyFor` from `jwt.secret`), subject = email, 24h expiry. `isTokenValid` swallows all parse exceptions into a single `false` (no distinction between expired/malformed/tampered).
- `JwtFilter` (`OncePerRequestFilter`): reads `Authorization: Bearer <token>`; if valid, loads the user via `AppUserDetailService` and populates `SecurityContextHolder`. Missing/invalid header simply falls through (anonymous), relying on `SecurityConfig`'s `anyRequest().authenticated()` to reject later.
- `AppUserDetailService`: every user gets a single hardcoded authority `"USER"` — Spring's own role-based mechanisms (`hasRole`, `@PreAuthorize`) are **not used anywhere**; all authorization is done imperatively in the service layer.
- `SecurityConfig`: CSRF disabled, stateless sessions, BCrypt, `DaoAuthenticationProvider`. Public paths: `/api/users/register`, `/api/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`. Everything else requires a valid JWT.
- No refresh tokens, no logout/blacklist, no JWT-embedded roles.

### Access Control Implementation — Status: **Implemented for 6/7 services, missing for AppUserService**
`HelperFiles/AccessControl.java` + `HelperFiles/Helper.java` are used consistently by `WorkspaceService`, `BoardService`, `LaneService`, `CardService`, `CommentService`, `ChecklistService`. **`AppUserService` is the one outlier — it has no access control at all** (see §5/§6).

---

## 3. API Endpoints

### AuthController — `/api/auth`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/auth/login` | Authenticate, return JWT + email | Public |

### AppUserController — `/api/users`
| Method | Path | Description | Access |
|---|---|---|---|
| GET | `/api/users` | List **all** users | ⚠️ None (any authenticated user) |
| GET | `/api/users/{id}` | Get user by id | ⚠️ None |
| GET | `/api/users/email/{email}` | Get user by email | ⚠️ None |
| POST | `/api/users/register` | Register new user | Public |
| PATCH | `/api/users/{id}/username` | Update username by id | ⚠️ None |
| PATCH | `/api/users/email/{email}/update-username` | Update username by email | ⚠️ None |
| PATCH | `/api/users/{id}/email` | Update email by id | ⚠️ None |
| PATCH | `/api/users/email/{email}/update` | Update email by email | ⚠️ None |
| PATCH | `/api/users/{id}/update-password` | Update password by id | ⚠️ None |
| PATCH | `/api/users/email/{email}/update-password` | Update password by email | ⚠️ None |
| DELETE | `/api/users/{id}` | Delete user by id | ⚠️ None |
| DELETE | `/api/users/email/{email}` | Delete user by email | ⚠️ None |

### WorkspaceController — `/api/workspaces`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/workspaces` | Create workspace (creator becomes OWNER) | Authenticated |
| GET | `/api/workspaces/all` | List current user's workspaces | Authenticated |
| GET | `/api/workspaces/{id}` | Get workspace | Workspace member |
| PATCH | `/api/workspaces/{id}/name` | Rename | Admin+ |
| PATCH | `/api/workspaces/{id}/description` | Update description | Admin+ |
| DELETE | `/api/workspaces/{id}` | Delete + cascade | Owner |
| POST | `/api/workspaces/{id}/members` | Add member | Owner if role=ADMIN, else Admin+ |
| DELETE | `/api/workspaces/{id}/members/{userId}` | Remove member | Owner if target=ADMIN, else Admin+; blocks self/OWNER removal |
| GET | `/api/workspaces/{id}/members` | List members | Member |
| GET | `/api/workspaces/{id}/members/{userId}/role` | Get member role | Member |
| PATCH | `/api/workspaces/{id}/members/{userId}/role` | Update member role | Owner if ADMIN involved, else Admin+; blocks OWNER role changes |
| PATCH | `/api/workspaces/{id}/members/transfer-ownership` | Transfer ownership | Owner |

### BoardController — `/api/boards`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/boards` | Create board (creator → board ADMIN) | Workspace Admin+ |
| GET | `/api/boards/{id}` | Get board | Visibility-gated |
| GET | `/api/boards/workspace/{workspaceId}` | List boards in workspace | Workspace member |
| PATCH | `/api/boards/{id}/name` | Rename | Board Admin |
| PATCH | `/api/boards/{id}/description` | Update description | Board Admin |
| PATCH | `/api/boards/{id}/visibility` | Change visibility | Board Admin |
| DELETE | `/api/boards/{id}` | Delete + cascade | Board Admin |
| POST | `/api/boards/{id}/members` | Add board member (not ADMIN role) | Board Admin |
| DELETE | `/api/boards/{id}/members/{userId}` | Remove board member (no self-removal) | Board Admin |
| GET | `/api/boards/{id}/members` | List board members | Visibility-gated |
| GET | `/api/boards/{id}/members/{userId}/role` | Get member role | Visibility-gated |
| PATCH | `/api/boards/{id}/members/{userId}/role` | Update member role (not self, not ADMIN) | Board Admin |
| POST | `/api/boards/{id}/labels` | Create label | Board Member+ |
| GET | `/api/boards/labels/{labelId}` | Get label | Visibility-gated |
| GET | `/api/boards/{id}/labels` | List labels | Visibility-gated |
| DELETE | `/api/boards/labels/{labelId}` | Delete label | ⚠️ Board Member+ (weaker than expected) |
| PATCH | `/api/boards/{id}/change-admin` | Reassign sole board-admin | Board Admin |

### LaneController — `/api/lanes`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/lanes` | Create lane | Board Member+ |
| GET | `/api/lanes/{id}` | Get lane | Visibility-gated |
| GET | `/api/lanes/board/{boardId}` | List lanes | Visibility-gated |
| PATCH | `/api/lanes/{id}/name` | Rename | Board Member+ |
| PATCH | `/api/lanes/{id}/position` | Reorder | Board Member+ |
| PATCH | `/api/lanes/{id}/archive` | Archive | Board Member+ |
| PATCH | `/api/lanes/{id}/unarchive` | Unarchive | Board Member+ |
| DELETE | `/api/lanes/{id}` | Delete + cascade all cards/children | ⚠️ Board Member+ (destructive, same as rename) |

### CardController — `/api/cards`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/cards` | Create card (+ activity log) | Board Member+ |
| GET | `/api/cards/{id}` | Get card | Visibility-gated |
| GET | `/api/cards/lane/{laneId}` | List cards in lane | Visibility-gated |
| PATCH | `/api/cards/{id}/title` | Update title | Board Member+ |
| PATCH | `/api/cards/{id}/description` | Update description | Board Member+ |
| PATCH | `/api/cards/{id}/due-date` | Set/clear due date | Board Member+ |
| PATCH | `/api/cards/{id}/move` | Move card (lane/position) | Board Member+ |
| PATCH | `/api/cards/{id}/archive` | Archive | Board Member+ |
| PATCH | `/api/cards/{id}/unarchive` | Unarchive | Board Member+ |
| DELETE | `/api/cards/{id}` | Delete + cascade children | Board Member+ |
| POST | `/api/cards/{id}/members` | Assign user to card | ⚠️ Board Member+ (no check target is a board member) |
| DELETE | `/api/cards/{id}/members/{userId}` | Unassign | Board Member+ |
| GET | `/api/cards/{id}/members` | List assignees | Visibility-gated |
| POST | `/api/cards/{id}/labels` | Attach label | Board Member+ |
| DELETE | `/api/cards/{id}/labels/{labelId}` | Detach label | Board Member+ |
| GET | `/api/cards/{id}/labels` | List labels | Visibility-gated |
| GET | `/api/cards/{id}/activity` | List activity log | Visibility-gated |

### CommentController — `/api/comments`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/comments` | Add comment | ⚠️ Visibility-gated only (not "member+") |
| GET | `/api/comments/{id}` | Get comment | Visibility-gated |
| GET | `/api/comments/card/{cardId}` | List comments | Visibility-gated |
| PATCH | `/api/comments/{id}/content` | Edit comment | Author only (400 on mismatch, not 403) |
| DELETE | `/api/comments/{id}` | Delete comment | Author OR board admin (403 on mismatch) |

### ChecklistController — `/api/checklists`
| Method | Path | Description | Access |
|---|---|---|---|
| POST | `/api/checklists` | Create checklist | Board Member+ |
| GET | `/api/checklists/{id}` | Get checklist | Visibility-gated |
| GET | `/api/checklists/card/{cardId}` | List checklists | Visibility-gated |
| PATCH | `/api/checklists/{id}/title` | Rename | Board Member+ |
| DELETE | `/api/checklists/{id}` | Delete + items | Board Member+ |
| POST | `/api/checklists/{id}/items` | Add item | Board Member+ |
| GET | `/api/checklists/items/{itemId}` | Get item | Visibility-gated |
| GET | `/api/checklists/{id}/items` | List items | Visibility-gated |
| PATCH | `/api/checklists/items/{id}/content` | Update item text | Board Member+ |
| PATCH | `/api/checklists/items/{id}/complete` | Mark done | Board Member+ |
| PATCH | `/api/checklists/items/{id}/uncomplete` | Mark not done | Board Member+ |
| DELETE | `/api/checklists/items/{id}` | Delete item | Board Member+ |

`GlobalExceptionHandler`: `ResourceNotFoundException`→404, `DuplicateResponseException`→409, `AccessDeniedException`→403, `IllegalArgumentException`→400, `MethodArgumentNotValidException`→400, generic `Exception`→500 (prints stack trace to stdout, leaks `ex.getMessage()` to the client).

---

## 4. Access Control Summary

### Workspace roles (`WorkspaceMember.Role`)
- **OWNER** — everything ADMIN can do, plus: delete workspace, add/remove/promote ADMIN-level members, transfer ownership.
- **ADMIN** — create boards, rename/describe workspace, add/remove/manage MEMBER-level members, view everything. Cannot delete the workspace or touch OWNER/other-ADMIN membership.
- **MEMBER** — view workspace, list boards, view own role only. No workspace-management rights. (Board-level rights come from separate `BoardMember` rows.)
- Workspace ADMIN/OWNER **implicitly get full board rights on every board in that workspace**, even boards they're not explicitly a board-member of (`isWorkspaceAdminOrOwner` bypass baked into every board-level check).

### Board roles (`BoardMember.Role`)
- **ADMIN** — manage board settings (name/description/visibility/delete), manage board membership, reassign the admin role, plus everything MEMBER can do.
- **MEMBER** — create/edit/move/archive/delete cards, lanes, checklists, comments, labels. Cannot manage board settings or membership.
- **OBSERVER** — view-only; blocked from every mutation gated by `requiredBoardMemberOrAbove`.

### Visibility (`Board.Visibility`) — gates viewing only, not mutation
- **PUBLIC** — any authenticated user can view, regardless of workspace/board membership.
- **WORKSPACE** — must be a member of the parent workspace (board membership not required).
- **PRIVATE** — must be an explicit `BoardMember`.
- Workspace ADMIN/OWNER bypass visibility checks entirely.

---

## 5. What Is Missing or Incomplete

### TODOs found (and their actual status)
`CardService.java` lines 140, 154, 168, 182, 196 — each says `TODO: log activity after Phase 5 JWT — user will come from SecurityContext`. **These are stale**: the very next lines in each method already call `helper.getCurrentUser()` and save an `ActivityLog` — the described work is done. Safe to delete the comments.

### Methods/endpoints missing access control
- **Entire `AppUserService`/`AppUserController`** — no access control whatsoever (see §6, this is the top priority gap).
- `CommentService.addComment` — gated by view-access only, not `requiredBoardMemberOrAbove` like every other mutation.
- `BoardService.deleteLabel` — only requires Board Member+, not Admin, despite being a destructive board-wide operation.
- `LaneService.deleteLane` — cascades deletion of all cards/children in the lane but only requires Board Member+, unlike `deleteBoard`/`deleteWorkspace` which require Admin/Owner.
- `CardService.assignMember` — never verifies the target `userId` is actually a member of the board before assigning them to a card.

### Services not using Helper/AccessControl
- `AppUserService` is the sole outlier — it talks directly to `AppUserRepository` and never injects `Helper` or `AccessControl`, while all six other services (`WorkspaceService`, `BoardService`, `LaneService`, `CardService`, `CommentService`, `ChecklistService`) consistently use both.
- `AccessControl.UserAccessAuthentication(AppUser user)` exists specifically to solve this (compares current user to target, throws `AccessDeniedException` on mismatch) but **is never called anywhere in the codebase** — confirmed by grep, it's dead code that was never wired into `AppUserService`.

---

## 6. Known Issues

1. **Critical: any authenticated user can read/modify/delete any other user's account.** `GET /api/users` dumps every user; `PATCH .../{id}/update-password`, `PATCH .../{id}/email`, `DELETE /api/users/{id}` etc. accept any id/email with no ownership check.
2. **`AccessControl.UserAccessAuthentication` is dead code** — written but never invoked.
3. **Inconsistent HTTP status for "not your resource"**: `CommentService.editComment` throws `IllegalArgumentException` (400) for a non-author edit, while `CommentService.deleteComment` throws `AccessDeniedException` (403) for the same class of violation — and every `AccessControl` check elsewhere also uses 403. Edit should likely be 403 too.
4. **"Not a member" returns 404, not 403**: `AccessControl.resolveWorkspaceMembership`/`resolveBoardMember` throw `ResourceNotFoundException` when the caller simply isn't a member — worth a deliberate decision (hide existence vs. signal forbidden) rather than leaving it as an accident.
5. **`Card.java` javadoc is inaccurate** — claims `ticketId` is generated via `@PostPersist`, but there's no such annotation; it's a plain method (`generateTicketId()`) called manually in `CardService.createCard`. Functionally fine, but misleading documentation.
6. **Duplicated cascade-delete logic** — the same "activity logs → card members → card labels → comments → checklist items → checklists → cards" deletion sequence is copy-pasted in `BoardService.deleteBoard`, `CardService.deleteCard`, `LaneService.deleteLane`, and `WorkspaceService.deleteWorkspace`. A future change to cascade order has to be made in 4 places.
7. **REST path inconsistency** — label endpoints mix `/api/boards/{id}/labels` (create/list) with `/api/boards/labels/{labelId}` (get/delete), not nested consistently.
8. **Inconsistent response bodies for symmetric operations** — `LaneController.archiveLane/unarchiveLane` return `204 No Content`, while `CardService.archiveCard/unarchiveCard` return the updated resource with `200 OK`, despite being the same kind of operation on sibling entities.
9. **`AppUser.setPassword` has no validation** (accepts `null`/blank), unlike every other setter on the entity — relies entirely on the service layer having already encoded the value.
10. **`GlobalExceptionHandler`** uses `ex.printStackTrace()` instead of a logger, and returns raw `ex.getMessage()` to clients on 500s — fine for local dev, a concern before any real deployment.
11. **JWT secret is a plaintext, hardcoded string in `application.properties`** committed to the project — should move to an environment variable / secrets manager before any deployment.
12. **`Helper`'s constructor takes `WorkspaceMemberRepository` and `BoardMemberRepository` but never uses them** — dead parameters, harmless but worth cleaning up.

---

## 7. Next Steps Recommended (prioritized)

1. **Fix the AppUserService access-control hole** — wire `AccessControl.UserAccessAuthentication` (or a similar current-user check) into every `AppUserController` mutation endpoint (username/email/password update, delete), and restrict `GET /api/users` to admin-only or remove it / paginate + scope it down. This is the most serious real-world security gap in the app and the natural "next lesson" given every other service already follows this pattern.
2. **Delete the 5 stale TODO comments in `CardService`** — the work they describe is already done.
3. **Normalize the "not your resource" exception**: make `CommentService.editComment` throw `AccessDeniedException` like `deleteComment` does, for consistent 403 semantics app-wide.
4. **Decide and standardize** whether "not a member" should be 404 or 403, and apply it consistently.
5. **Raise the bar on destructive operations**: require `requiredBoardAdmin` for `LaneService.deleteLane` and `BoardService.deleteLabel`, matching the precedent set by `deleteBoard`/`deleteWorkspace`.
6. **Validate assignee membership** in `CardService.assignMember` (must be a board member before being assignable to a card).
7. **Extract the duplicated cascade-delete logic** (Board/Lane/Card/Workspace deletion) into one shared helper method to remove the 4x duplication and reduce future-bug risk.
8. **Move the JWT secret out of `application.properties`** into an environment variable, and swap `ddl-auto=create-drop` for `validate`/Flyway-managed migrations before treating this as anything beyond local dev.
9. **Polish**: fix the `Card.java` javadoc, remove unused `Helper` constructor params, replace `printStackTrace()` with a real logger, unify REST path nesting for labels, and align archive/unarchive response conventions between `Lane` and `Card`.
10. Once the above are addressed, this project is in solid shape to demonstrate as a portfolio piece — the JWT flow, layered access-control model (workspace/board roles + visibility), and cascade-aware deletes are all real, working, non-trivial backend patterns.
