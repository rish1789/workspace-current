# Myapp - Kanban Board Management System

## What is this project?

Myapp is a **Trello-like Kanban board** backend API built with Java. It lets teams organize work using workspaces, boards, lanes (columns), and cards (tickets). Users can collaborate by adding members, leaving comments, creating checklists, and tagging cards with labels.

## Tech Stack

| Layer        | Technology                    |
|-------------|-------------------------------|
| Language     | Java 21                       |
| Framework    | Spring Boot 4.0.6             |
| ORM          | Spring Data JPA / Hibernate   |
| Database     | PostgreSQL                    |
| Build Tool   | Maven (with Maven Wrapper)    |
| API Style    | REST (JSON over HTTP)         |

## How It Works - The Domain Model

The application follows a clear hierarchy:

```
User
 └── Workspace          (top-level container, like a Trello workspace)
      └── Board          (a Kanban board inside a workspace)
           ├── Lane      (a column on the board: "To Do", "In Progress", "Done")
           │    └── Card (a ticket/task within a lane)
           │         ├── Comment        (discussion on a card)
           │         ├── Checklist      (task list on a card)
           │         │    └── ChecklistItem  (individual to-do)
           │         ├── CardMember     (users assigned to a card)
           │         ├── CardLabel      (labels/tags attached to a card)
           │         └── ActivityLog    (immutable audit trail)
           ├── Label     (board-scoped color-coded tags, e.g. "Bug", "Feature")
           └── BoardMember  (users with roles on a board)
```

### Key Concepts

- **User**: Registered account with username, email, and password. Passwords require uppercase, lowercase, digit, and special character (8-20 chars).
- **Workspace**: Top-level container created by a user. Has a name (max 100 chars) and optional description (max 500 chars). Members have ADMIN or MEMBER roles.
- **Board**: A Kanban board inside a workspace. Has visibility settings (PRIVATE, WORKSPACE, PUBLIC). Members have ADMIN, MEMBER, or OBSERVER roles.
- **Lane**: A column on a board (e.g., "To Do", "In Progress"). Has a zero-based position for ordering. Can be archived/unarchived.
- **Card**: A ticket/task inside a lane. Gets an auto-generated ticket ID (e.g., `TKT-0001`). Has title, optional description, optional due date, and a position within its lane. Can be moved between lanes and archived.
- **Label**: Color-coded tag scoped to a board. Uses CSS hex colors (#RGB or #RRGGBB). Can be attached to multiple cards.
- **Comment**: Text comment on a card by a user. Tracks created and updated timestamps. Max 500 chars.
- **Checklist / ChecklistItem**: Task lists on a card. Items can be marked done/undone and have positions for ordering.
- **ActivityLog**: Immutable audit log entries recording actions on cards.

## Project Structure

```
src/main/java/com/example/myapp/
├── MyappApplication.java           # Spring Boot entry point
├── controller/                     # REST API endpoints
│   ├── WorkspaceController.java    # /api/workspaces
│   ├── BoardController.java        # /api/boards
│   ├── LaneController.java         # /api/lanes
│   ├── CardController.java         # /api/cards
│   ├── CommentController.java      # /api/comments
│   └── ChecklistController.java    # /api/checklists
├── service/                        # Business logic
│   ├── UserService.java
│   ├── WorkspaceService.java
│   ├── BoardService.java
│   ├── LaneService.java
│   ├── CardService.java
│   ├── CommentService.java
│   └── ChecklistService.java
├── repository/                     # Spring Data JPA repositories (database access)
│   ├── UserRepository.java
│   ├── WorkspaceRepository.java
│   ├── WorkspaceMemberRepository.java
│   ├── BoardRepository.java
│   ├── BoardMemberRepository.java
│   ├── LaneRepository.java
│   ├── CardRepository.java
│   ├── CardMemberRepository.java
│   ├── CardLabelRepository.java
│   ├── LabelRepository.java
│   ├── CommentRepository.java
│   ├── ChecklistRepository.java
│   ├── ChecklistItemRepository.java
│   └── ActivityLogRepository.java
├── entity/                         # JPA entities (database table mappings)
│   ├── User.java
│   ├── Workspace.java
│   ├── WorkspaceMember.java
│   ├── Board.java
│   ├── BoardMember.java
│   ├── Lane.java
│   ├── Card.java
│   ├── CardMember.java
│   ├── CardLabel.java
│   ├── Label.java
│   ├── Comment.java
│   ├── Checklist.java
│   ├── ChecklistItem.java
│   └── ActivityLog.java
└── dto/request/                    # Request DTOs for API input validation
    ├── CreateUserRequest.java
    ├── ValidateLoginRequest.java
    ├── CreateWorkspaceRequest.java
    ├── CreateBoardRequest.java
    ├── CreateLaneRequest.java
    ├── CreateCardRequest.java
    ├── CreateChecklistRequest.java
    ├── ... (update/move/rename request DTOs)
```

## API Endpoints Summary

### Workspaces (`/api/workspaces`)
| Method  | Endpoint                                  | Description              |
|---------|------------------------------------------|--------------------------|
| POST    | `/api/workspaces`                         | Create workspace         |
| GET     | `/api/workspaces/{id}`                    | Get workspace            |
| PATCH   | `/api/workspaces/{id}/name`               | Update name              |
| PATCH   | `/api/workspaces/{id}/description`        | Update description       |
| DELETE  | `/api/workspaces/{id}`                    | Delete workspace         |
| POST    | `/api/workspaces/{id}/members`            | Add member               |
| DELETE  | `/api/workspaces/{id}/members/{userId}`   | Remove member            |
| GET     | `/api/workspaces/{id}/members`            | List members             |
| GET     | `/api/workspaces/{id}/members/{userId}/role` | Get member role       |
| PATCH   | `/api/workspaces/{id}/members/{userId}/role` | Update member role    |

### Boards (`/api/boards`)
| Method  | Endpoint                                  | Description              |
|---------|------------------------------------------|--------------------------|
| POST    | `/api/boards`                             | Create board             |
| GET     | `/api/boards/{id}`                        | Get board                |
| GET     | `/api/boards/workspace/{workspaceId}`     | List boards in workspace |
| PATCH   | `/api/boards/{id}/name`                   | Update name              |
| PATCH   | `/api/boards/{id}/description`            | Update description       |
| PATCH   | `/api/boards/{id}/visibility`             | Update visibility        |
| DELETE  | `/api/boards/{id}`                        | Delete board             |
| POST    | `/api/boards/{id}/members`                | Add member               |
| DELETE  | `/api/boards/{id}/members/{userId}`       | Remove member            |
| GET     | `/api/boards/{id}/members`                | List members             |
| GET     | `/api/boards/{id}/members/{userId}/role`  | Get member role          |
| PATCH   | `/api/boards/{id}/members/{userId}/role`  | Update member role       |
| POST    | `/api/boards/{id}/labels`                 | Create label             |
| GET     | `/api/boards/{id}/labels`                 | List labels              |
| GET     | `/api/boards/labels/{labelId}`            | Get label                |
| DELETE  | `/api/boards/labels/{labelId}`            | Delete label             |

### Lanes (`/api/lanes`)
| Method  | Endpoint                        | Description        |
|---------|--------------------------------|--------------------|
| POST    | `/api/lanes`                    | Create lane        |
| GET     | `/api/lanes/{id}`               | Get lane           |
| GET     | `/api/lanes/board/{boardId}`    | List lanes on board|
| PATCH   | `/api/lanes/{id}/name`          | Rename lane        |
| PATCH   | `/api/lanes/{id}/position`      | Move lane          |
| PATCH   | `/api/lanes/{id}/archive`       | Archive lane       |
| PATCH   | `/api/lanes/{id}/unarchive`     | Unarchive lane     |
| DELETE  | `/api/lanes/{id}`               | Delete lane        |

### Cards (`/api/cards`)
| Method  | Endpoint                               | Description         |
|---------|---------------------------------------|---------------------|
| POST    | `/api/cards`                           | Create card         |
| GET     | `/api/cards/{id}`                      | Get card            |
| GET     | `/api/cards/lane/{laneId}`             | List cards in lane  |
| PATCH   | `/api/cards/{id}/title`                | Update title        |
| PATCH   | `/api/cards/{id}/description`          | Update description  |
| PATCH   | `/api/cards/{id}/due-date`             | Set due date        |
| PATCH   | `/api/cards/{id}/move`                 | Move card           |
| PATCH   | `/api/cards/{id}/archive`              | Archive card        |
| PATCH   | `/api/cards/{id}/unarchive`            | Unarchive card      |
| DELETE  | `/api/cards/{id}`                      | Delete card         |
| POST    | `/api/cards/{id}/members`              | Assign member       |
| DELETE  | `/api/cards/{id}/members/{userId}`     | Remove member       |
| GET     | `/api/cards/{id}/members`              | List card members   |
| POST    | `/api/cards/{id}/labels`               | Attach label        |
| DELETE  | `/api/cards/{id}/labels/{labelId}`     | Detach label        |
| GET     | `/api/cards/{id}/labels`               | List card labels    |

### Comments (`/api/comments`)
| Method  | Endpoint                          | Description       |
|---------|----------------------------------|-------------------|
| POST    | `/api/comments`                   | Add comment       |
| GET     | `/api/comments/{id}`              | Get comment       |
| GET     | `/api/comments/card/{cardId}`     | List by card      |
| PATCH   | `/api/comments/{id}/content`      | Edit comment      |
| DELETE  | `/api/comments/{id}`              | Delete comment    |

### Checklists (`/api/checklists`)
| Method  | Endpoint                                     | Description         |
|---------|---------------------------------------------|---------------------|
| POST    | `/api/checklists`                             | Create checklist    |
| GET     | `/api/checklists/{id}`                        | Get checklist       |
| GET     | `/api/checklists/card/{cardId}`               | List by card        |
| PATCH   | `/api/checklists/{id}/title`                  | Rename checklist    |
| DELETE  | `/api/checklists/{id}`                        | Delete checklist    |
| POST    | `/api/checklists/{id}/items`                  | Add item            |
| GET     | `/api/checklists/items/{itemId}`              | Get item            |
| GET     | `/api/checklists/{id}/items`                  | List items          |
| PATCH   | `/api/checklists/items/{id}/content`          | Update item content |
| PATCH   | `/api/checklists/items/{id}/complete`         | Mark item done      |
| PATCH   | `/api/checklists/items/{id}/uncomplete`       | Mark item undone    |
| DELETE  | `/api/checklists/items/{id}`                  | Delete item         |

## Architecture Pattern

The project follows a **3-layer architecture**:

```
Client (HTTP) --> Controller --> Service --> Repository --> PostgreSQL
                  (routing)     (logic)    (data access)
```

- **Controller**: Handles HTTP requests/responses, delegates to services.
- **Service**: Contains business logic, validation, and orchestration.
- **Repository**: Spring Data JPA interfaces for database operations.
- **Entity**: JPA-annotated POJOs mapped to database tables. Each entity validates its own fields in constructors and setters.
- **DTO (Request)**: Simple data carriers for incoming API requests.

## Database

- **Engine**: PostgreSQL (runs on `localhost:5432`, database: `myapp_db`)
- **Schema management**: Hibernate `create-drop` (auto-creates tables on startup, drops on shutdown - development mode)
- **Tables**: `users`, `workspaces`, `workspace_members`, `boards`, `board_members`, `lanes`, `cards`, `card_members`, `card_labels`, `labels`, `comments`, `checklists`, `checklist_items`, `activity_logs`

## How to Run

1. Make sure PostgreSQL is running on `localhost:5432` with a database named `myapp_db`
2. From the project root:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The API will be available at `http://localhost:8080`

## Current Status

- This is a **backend-only** REST API (no frontend).
- **No authentication/authorization** middleware - endpoints are open (user identity is passed in request bodies).
- **No global exception handling** - entity-level validation throws `IllegalArgumentException`.
- Development mode (`create-drop`) - database resets on every restart.
