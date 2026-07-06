---
1. Package Names Are Non-Standard

Your code:
com.example.myapp.ErrorException   ← ErrorException package
com.example.myapp.HelperFiles      ← HelperFiles package

The problem: Java packages must be all lowercase by convention. ErrorException and HelperFiles look like class names, not package names. Any senior reading this immediately notices it.

What it should be:
com.example.myapp.exception    ← standard
com.example.myapp.util         ← standard (or .helper is also acceptable)

This is the same convention every major Java project follows — Spring itself uses org.springframework.web.bind.annotation, never org.springframework.WebAnnotations.

---
2. Exception Class Name Is Wrong

Your code:
// ErrorException/DuplicateResponseException.java
public class DuplicateResponseException extends RuntimeException {

The problem: "DuplicateResponse" makes no sense as an exception name. A response is what the server sends back. The exception is about a duplicate resource — a duplicate email, a duplicate workspace name.

What it should be:
// exception/DuplicateResourceException.java
public class DuplicateResourceException extends RuntimeException {

Or even more specifically: ConflictException, which maps clearly to HTTP 409 Conflict. Names should say what happened, not what you sent back.

---
3. Entities Return Formatted Strings — Big Anti-Pattern

This is the most important quality issue in your entire codebase.

Your code in AppUser.java, Workspace.java, Board.java, Card.java, Lane.java:
// every entity has this
private static final DateTimeFormatter FORMATTER =
                     DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

public String getCreatedAt() {
    return createdAt.format(FORMATTER);  // ← returns a String, not LocalDateTime
}

Because of this, your response DTOs are forced to use String too:
// WorkspaceRespond.java
private String createdAt;  // ← forced to be String because entity returns String

// CardResponse.java
private String createdAt;  // ← same problem
private String dueDate;    // ← same problem

The problems this causes:
- The entity (data model) is now responsible for display formatting — that's the job of the API layer
- If a frontend tomorrow wants ISO format instead of dd-MM-yyyy, you must change all 5 entity classes
- You cannot do any date comparison, sorting, or arithmetic on a String date in Java
- Jackson (Spring's JSON library) already knows how to serialize LocalDateTime cleanly — you're duplicating that work manually
- Tests become fragile because they assert on a specific formatted string

What it should look like:

// Entity — raw data only, no formatting
public LocalDateTime getCreatedAt() {
    return createdAt;   // just return the value
}

// Response DTO — also raw
private LocalDateTime createdAt;

// application.properties — configure the format once, globally
spring.jackson.date-format=dd-MM-yyyy HH:mm:ss
spring.jackson.serialization.write-dates-as-timestamps=false

Or on the DTO field if you want per-field control:
@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
private LocalDateTime createdAt;

One place. Done. Every response serialized correctly without touching a single entity.

---
4. BoardService Constructor Takes Parameters It Never Uses

Your code (BoardService.java lines 76–96):
public BoardService(BoardRepository boardRepository,
                    BoardMemberRepository boardMemberRepository,
                    LabelRepository labelRepository,
                    AppUserRepository userRepository,       // ← injected
                    WorkspaceRepository workspaceRepository, // ← injected
                    LaneRepository laneRepository,
                    ...
                    Helper helper, AccessControl accessControl) {
    this.boardRepository       = boardRepository;
    this.boardMemberRepository = boardMemberRepository;
    this.labelRepository       = labelRepository;
    // userRepository is NEVER assigned to a field
    // workspaceRepository is NEVER assigned to a field
    this.laneRepository        = laneRepository;
    ...
}

The problem: Spring injects AppUserRepository and WorkspaceRepository into this constructor, they get passed in, and then they're immediately thrown away. The fields don't exist for them. This means Spring is doing useless work on every startup, and anyone reading this code thinks those repos are being used somewhere.

What happened: You probably had them early in development, moved their responsibility to Helper, but forgot to remove the parameters.

What it should be: Remove both from the constructor entirely. Helper already handles user and workspace resolution.

public BoardService(BoardRepository boardRepository,
                    BoardMemberRepository boardMemberRepository,
                    LabelRepository labelRepository,
                    LaneRepository laneRepository,
                    CardRepository cardRepository,
                    CardMemberRepository cardMemberRepository,
                    CardLabelRepository cardLabelRepository,
                    Helper helper,
                    AccessControl accessControl) {
    // every parameter gets assigned to a field
}

---
5. Stale TODO Comments That Are Already Done

Your code (CardService.java lines 123–125, 136–138, 151–153):
/**
 * Updates the card title.
 * TODO: log activity after Phase 5 JWT — user will come from SecurityContext
 */
public CardResponse updateTitle(...) {
    ...
    activityLogRepository.save(...); // ← it's already done!
}

/**
 * Updates the card description.
 * TODO: log activity after Phase 5 JWT    ← same, also already done
 */

The problem: JWT is implemented. Activity logging is implemented. These TODO comments are lies — they describe work that is already complete. A new developer reading this code would waste time looking for the "missing" logging, only to find it's there.

The rule: When you finish a TODO, delete the comment. A comment that describes what the code already does is worse than no comment at all — it actively misleads.

What it should look like: Just no comment. The code is self-explanatory:
public CardResponse updateTitle(Integer cardId, UpdateCardTitleRequest request) {
    Card card = helper.resolveCard(cardId);
    AppUser user = helper.getCurrentUser();
    accessControl.requiredBoardMemberOrAbove(card.getLane().getBoard(), user);
    card.setTitle(request.getTitle());
    Card updated = cardRepository.save(card);
    activityLogRepository.save(new ActivityLog(updated, user, "Title updated to: " + request.getTitle()));
    return toCardResponse(updated);
}

---
6. Dead Class That Was Never Cleaned Up

Your code (ValidateLoginRequest.java):
public class ValidateLoginRequest {
    private String email;
    private String password;
    // getters and setters...
}

The problem: This class is never imported anywhere. AuthController uses LoginRequest, not this. It's an artefact of an early draft that never got deleted. Dead code accumulates confusion — developers ask "what uses this? is it needed? can I change it?"

What it should be: Deleted. If it's not used, it doesn't exist from the codebase's point of view. Check with Find Usages in IntelliJ (right-click the class name → Find Usages) before deleting anything you're unsure about.

---
7. Inconsistent Naming Across the Same Package

Your code in dto/respond/:
AppUserRespond.java        ← "Respond"
WorkspaceRespond.java      ← "Respond"
WorkspaceMemberRespond.java ← "Respond"
BoardRespond.java          ← "Respond"
CommentRespond.java        ← "Respond"
CardResponse.java          ← "Response"   ← different!
LaneResponse.java          ← "Response"   ← different!
ChecklistResponse.java     ← "Response"   ← different!
LabelResponse.java         ← "Response"   ← different!

The problem: The package is split between two naming conventions. There's no technical difference — it's purely inconsistency. When you onboard a teammate or come back to this in 6 months, you have to remember which classes use which suffix. It wastes cognitive effort.

What it should be: Pick one and apply it everywhere. The standard in the Java ecosystem is Response:
AppUserResponse.java
WorkspaceResponse.java
WorkspaceMemberResponse.java
BoardResponse.java
CommentResponse.java
CardResponse.java
LaneResponse.java
ChecklistResponse.java
LabelResponse.java

The same inconsistency exists in method names — some services have toRespond() and others have toResponse(). When it's the same thing, it should look the same.

---
8. Response DTOs Have Setters They Don't Need

Your code (AppUserRespond.java):
public class AppUserRespond {
    private Integer id;
    private String username;
    private String email;

    public AppUserRespond() {}  // no-arg constructor

    // getters...

    public void setId(Integer id)             { this.id = id;         }  // ← why?
    public void setUsername(String username)  { this.username = username; } // ← why?
    public void setEmail(String email)        { this.email = email;   }  // ← why?
}

The problem: Response objects are built once by the service layer and sent to the client. Nothing ever needs to modify them after construction. Setters on a response DTO are an open invitation to accidentally mutate data that should be read-only. They also add noise — 6 extra lines per DTO that don't serve any purpose.

Jackson (Spring's JSON serializer) only needs getters to serialize to JSON — it does not need setters on response objects.

What it should be:
public class AppUserResponse {
    private final Integer id;
    private final String  username;
    private final String  email;

    public AppUserResponse(Integer id, String username, String email) {
        this.id       = id;
        this.username = username;
        this.email    = email;
    }

    public Integer getId()       { return id;       }
    public String  getUsername() { return username; }
    public String  getEmail()    { return email;    }
    // no setters
}

Or, even cleaner with Java 16+ records (which you can use since you're on Java 21):
public record AppUserResponse(Integer id, String username, String email) {}

One line. Immutable. Getters auto-generated. Jackson serializes it perfectly.

---
9. CardResponse Getters Don't Follow Java Convention

Your code (CardResponse.java lines 44–45):
public Integer createdBy() { return createdBy; }  // ← missing "get"
public String  createdAt() { return createdAt; }  // ← missing "get"

The problem: Java getters must start with get. Jackson looks for get* methods when serializing to JSON. These two fields (createdBy and createdAt) will be silently missing from every API response because Jackson can't find their getters.

Everything else in the class follows the convention:
public Integer getId()       { return id; }       // ← correct
public String  getTitle()    { return title; }    // ← correct
public Integer createdBy()   { return createdBy; } // ← WRONG, missing from JSON

What it should be:
public Integer getCreatedBy() { return createdBy; }
public String  getCreatedAt() { return createdAt; }

---
10. GlobalExceptionHandler Returns Raw FieldError.toString()

Your code (GlobalExceptionHandler.java line 38):
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<String> handleValidationError(MethodArgumentNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                         .body("Field Error " + ex.getFieldError());  // ← raw toString()
}

The problem: ex.getFieldError().toString() produces something like:
Field Error Field error in object 'createAppUserRequest' on field 'email':
rejected value [null]; codes [NotBlank.createAppUserRequest.email,...];
default message [Email cannot be empty]

This is an internal Spring validation object dump — unreadable by any client. It also exposes your class names (createAppUserRequest) which is information leakage.

What it should be:
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<String> handleValidationError(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult()
                       .getFieldErrors()
                       .stream()
                       .map(e -> e.getField() + ": " + e.getDefaultMessage())
                       .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
}

This gives the client a clean, readable response:
email: Email cannot be empty, password: Password must be 8-20 characters

---
Summary Table

┌────────────────────┬─────────────────────────────┬───────────────────────────────────┬──────────────────────────────────────┐
│       Issue        │            Where            │          What Was Wrong           │          What to Do Instead          │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Package names      │ ErrorException/,            │ PascalCase on packages            │ rename to exception/, util/          │
│                    │ HelperFiles/                │                                   │                                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Exception name     │ DuplicateResponseException  │ "Response" makes no sense here    │ DuplicateResourceException           │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Entity date        │ All 5 entities              │ Entity formats strings — wrong    │ Return LocalDateTime, format via     │
│ formatting         │                             │ layer                             │ @JsonFormat                          │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Unused constructor │ BoardService                │ 2 repos injected but never used   │ Remove them from constructor         │
│  params            │                             │                                   │                                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Stale TODOs        │ CardService (3 methods)     │ Describes work that's already     │ Delete the comments                  │
│                    │                             │ done                              │                                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Dead class         │ ValidateLoginRequest        │ Never imported or used            │ Delete the file                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Naming split       │ dto/respond/                │ Half Respond, half Response       │ Pick one, rename all to Response     │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Setters on DTOs    │ All Respond classes         │ Response objects should be        │ Remove setters, use records if on    │
│                    │                             │ immutable                         │ Java 16+                             │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Wrong getter names │ CardResponse                │ createdBy() / createdAt() —       │ getCreatedBy() / getCreatedAt()      │
│                    │                             │ Jackson can't find them           │                                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Naming split       │ dto/respond/                │ Half Respond, half Response       │ Pick one, rename all to Response     │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Setters on DTOs    │ All Respond classes         │ Response objects should be        │ Remove setters, use records if on    │
│                    │                             │ immutable                         │ Java 16+                             │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Wrong getter names │ CardResponse                │ createdBy() / createdAt() —       │ getCreatedBy() / getCreatedAt()      │
│                    │                             │ Jackson can't find them           │                                      │
├────────────────────┼─────────────────────────────┼───────────────────────────────────┼──────────────────────────────────────┤
│ Raw validation     │ GlobalExceptionHandler      │ Dumps internal Spring object to   │ Extract field + message cleanly      │
│ error              │                             │ client                            │                                      │
└────────────────────┴─────────────────────────────┴───────────────────────────────────┴──────────────────────────────────────┘

The most impactful ones to fix first are #9 (broken getters, fields missing from responses right now), #3 (entity formatting, affects every response in the app), and #8 (stale TODOs mislead anyone reading the code).
