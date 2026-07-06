# Myapp - Sequence Diagrams

## 1. Full Workflow: From User Registration to Card Management

This diagram shows the complete happy-path flow of setting up a workspace and managing cards.

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    Note over C,DB: USER REGISTRATION
    C->>Ctrl: POST /api/users (username, email, password)
    Ctrl->>Svc: createUser(username, email, password)
    Svc->>Repo: userRepository.save(new User)
    Repo->>DB: INSERT INTO users
    DB-->>Repo: User id=1
    Repo-->>Svc: User
    Svc-->>Ctrl: User
    Ctrl-->>C: 201 Created userId, username, email

    Note over C,DB: CREATE WORKSPACE
    C->>Ctrl: POST /api/workspaces (name, createdById 1)
    Ctrl->>Svc: createWorkspace(name, createdById)
    Svc->>Repo: userRepository.findById(1)
    Repo->>DB: SELECT FROM users WHERE id=1
    DB-->>Repo: User
    Svc->>Repo: workspaceRepository.save(new Workspace)
    Repo->>DB: INSERT INTO workspaces
    DB-->>Repo: Workspace id=1
    Svc->>Repo: memberRepository.save(WorkspaceMember ADMIN)
    Repo->>DB: INSERT INTO workspace_members
    Note right of Svc: Creator auto-added as ADMIN
    Svc-->>Ctrl: Workspace
    Ctrl-->>C: 201 Created id, name, createdAt

    Note over C,DB: CREATE BOARD
    C->>Ctrl: POST /api/boards (workspaceId, name, visibility, createdById)
    Ctrl->>Svc: createBoard(workspaceId, name, visibility, createdById)
    Svc->>Repo: workspaceRepository.findById(1)
    Repo->>DB: SELECT FROM workspaces
    DB-->>Repo: Workspace
    Svc->>Repo: userRepository.findById(1)
    Repo->>DB: SELECT FROM users
    DB-->>Repo: User
    Svc->>Repo: boardRepository.save(new Board)
    Repo->>DB: INSERT INTO boards
    DB-->>Repo: Board id=1
    Svc->>Repo: boardMemberRepository.save(BoardMember ADMIN)
    Repo->>DB: INSERT INTO board_members
    Svc-->>Ctrl: Board
    Ctrl-->>C: 201 Created id, name, visibility

    Note over C,DB: CREATE LANE
    C->>Ctrl: POST /api/lanes (boardId 1, name To Do, position 0)
    Ctrl->>Svc: createLane(boardId, name, position)
    Svc->>Repo: boardRepository.findById(1)
    Repo->>DB: SELECT FROM boards
    DB-->>Repo: Board
    Svc->>Repo: laneRepository.save(new Lane)
    Repo->>DB: INSERT INTO lanes
    DB-->>Repo: Lane id=1
    Svc-->>Ctrl: Lane
    Ctrl-->>C: 201 Created id, name, position

    Note over C,DB: CREATE CARD
    C->>Ctrl: POST /api/cards (laneId 1, title, position 0, createdBy 1)
    Ctrl->>Svc: createCard(laneId, title, position, createdBy)
    Svc->>Repo: laneRepository.findById(1)
    Repo->>DB: SELECT FROM lanes
    DB-->>Repo: Lane
    Svc->>Repo: userRepository.findById(1)
    Repo->>DB: SELECT FROM users
    DB-->>Repo: User
    Svc->>Repo: cardRepository.save(new Card)
    Repo->>DB: INSERT INTO cards
    DB-->>Repo: Card id=1
    Svc->>Svc: card.generateTicketId() returns TKT-0001
    Svc->>Repo: cardRepository.save(card)
    Repo->>DB: UPDATE cards SET ticket_id=TKT-0001
    Svc-->>Ctrl: Card
    Ctrl-->>C: 201 Created id, ticketId, title, position
```

## 2. Card Operations: Move, Comment, Checklist

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    Note over C,DB: MOVE CARD - Same Lane
    C->>Ctrl: PATCH /api/cards/1/move (laneId 1, position 2)
    Ctrl->>Svc: moveCard(cardId=1, targetLaneId=1, newPosition=2)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT FROM cards WHERE id=1
    DB-->>Repo: Card position=0, lane=1
    Svc->>Repo: laneRepository.findById(1)
    Repo->>DB: SELECT FROM lanes WHERE id=1
    DB-->>Repo: Lane
    Svc->>Repo: cardRepository.findByLane(lane)
    Repo->>DB: SELECT FROM cards WHERE lane_id=1
    DB-->>Repo: List of Cards
    Note right of Svc: Shift cards between pos 0 and 2 backward
    Svc->>Repo: cardRepository.saveAll(shiftedCards)
    Repo->>DB: UPDATE cards SET position=... batch
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK

    Note over C,DB: MOVE CARD - Cross Lane
    C->>Ctrl: PATCH /api/cards/1/move (laneId 2, position 0)
    Ctrl->>Svc: moveCard(cardId=1, targetLaneId=2, newPosition=0)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT card
    DB-->>Repo: Card lane=1, position=2
    Svc->>Repo: laneRepository.findById(2)
    Repo->>DB: SELECT lane
    DB-->>Repo: Target Lane - board must match
    Svc->>Svc: Validate same board
    Svc->>Repo: cardRepository.findByLane(sourceLane)
    Repo->>DB: SELECT cards in source lane
    Svc->>Repo: cardRepository.findByLane(targetLane)
    Repo->>DB: SELECT cards in target lane
    Note right of Svc: Shift source cards down, target cards up
    Svc->>Repo: cardRepository.saveAll(sourceCards)
    Svc->>Repo: cardRepository.saveAll(targetCards)
    Svc->>Repo: cardRepository.save(card) lane=2, pos=0
    Repo->>DB: UPDATE cards batch
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK

    Note over C,DB: ADD COMMENT
    C->>Ctrl: POST /api/comments (cardId 1, userId 1, content)
    Ctrl->>Svc: addComment(cardId, userId, content)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT FROM cards
    DB-->>Repo: Card
    Svc->>Repo: userRepository.findById(1)
    Repo->>DB: SELECT FROM users
    DB-->>Repo: User
    Svc->>Repo: commentRepository.save(new Comment)
    Repo->>DB: INSERT INTO comments
    DB-->>Repo: Comment id=1, createdAt, updatedAt
    Svc-->>Ctrl: Comment
    Ctrl-->>C: 201 Created id, content, createdAt

    Note over C,DB: CREATE CHECKLIST AND ITEM
    C->>Ctrl: POST /api/checklists (cardId 1, title Tasks)
    Ctrl->>Svc: createChecklist(cardId, title)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT FROM cards
    DB-->>Repo: Card
    Svc->>Repo: checklistRepository.save(new Checklist)
    Repo->>DB: INSERT INTO checklists
    DB-->>Repo: Checklist id=1
    Svc-->>Ctrl: Checklist
    Ctrl-->>C: 201 Created id, title

    C->>Ctrl: POST /api/checklists/1/items (content Write tests, position 0)
    Ctrl->>Svc: addItem(checklistId=1, content, position)
    Svc->>Repo: checklistRepository.findById(1)
    Repo->>DB: SELECT FROM checklists
    DB-->>Repo: Checklist
    Svc->>Repo: itemRepository.save(new ChecklistItem)
    Repo->>DB: INSERT INTO checklist_items isDone=false
    DB-->>Repo: ChecklistItem id=1
    Svc-->>Ctrl: ChecklistItem
    Ctrl-->>C: 201 Created id, content, isDone false

    C->>Ctrl: PATCH /api/checklists/items/1/complete
    Ctrl->>Svc: completeItem(itemId=1)
    Svc->>Repo: itemRepository.findById(1)
    Repo->>DB: SELECT FROM checklist_items
    DB-->>Repo: ChecklistItem
    Svc->>Svc: item.markDone()
    Svc->>Repo: itemRepository.save(item)
    Repo->>DB: UPDATE checklist_items SET is_done=true
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK
```

## 3. Membership & Label Management

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    Note over C,DB: ADD WORKSPACE MEMBER
    C->>Ctrl: POST /api/workspaces/1/members (userId 2, role MEMBER)
    Ctrl->>Svc: addMember(workspaceId=1, userId=2, MEMBER)
    Svc->>Repo: workspaceRepository.findById(1)
    Repo->>DB: SELECT FROM workspaces
    DB-->>Repo: Workspace
    Svc->>Repo: userRepository.findById(2)
    Repo->>DB: SELECT FROM users
    DB-->>Repo: User
    Svc->>Repo: memberRepository.existsByWorkspaceAndUser(ws, user)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: false
    Svc->>Repo: memberRepository.save(new WorkspaceMember)
    Repo->>DB: INSERT INTO workspace_members
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK

    Note over C,DB: CREATE LABEL
    C->>Ctrl: POST /api/boards/1/labels (name Bug, color red)
    Ctrl->>Svc: createLabel(boardId=1, name, color)
    Svc->>Repo: boardRepository.findById(1)
    Repo->>DB: SELECT FROM boards
    DB-->>Repo: Board
    Svc->>Repo: labelRepository.save(new Label)
    Repo->>DB: INSERT INTO labels
    DB-->>Repo: Label id=1
    Svc-->>Ctrl: Label
    Ctrl-->>C: 201 Created id, name, color

    Note over C,DB: ATTACH LABEL TO CARD
    C->>Ctrl: POST /api/cards/1/labels (labelId 1)
    Ctrl->>Svc: attachLabel(cardId=1, labelId=1)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT FROM cards
    DB-->>Repo: Card
    Svc->>Repo: labelRepository.findById(1)
    Repo->>DB: SELECT FROM labels
    DB-->>Repo: Label
    Svc->>Repo: cardLabelRepository.existsByCardAndLabel(card, label)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: false
    Svc->>Repo: cardLabelRepository.save(new CardLabel)
    Repo->>DB: INSERT INTO card_labels
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK

    Note over C,DB: ASSIGN MEMBER TO CARD
    C->>Ctrl: POST /api/cards/1/members (userId 2)
    Ctrl->>Svc: assignMember(cardId=1, userId=2)
    Svc->>Repo: cardRepository.findById(1)
    Repo->>DB: SELECT FROM cards
    DB-->>Repo: Card
    Svc->>Repo: userRepository.findById(2)
    Repo->>DB: SELECT FROM users
    DB-->>Repo: User
    Svc->>Repo: cardMemberRepository.existsByCardAndUser(card, user)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: false
    Svc->>Repo: cardMemberRepository.save(new CardMember)
    Repo->>DB: INSERT INTO card_members
    Svc-->>Ctrl: void
    Ctrl-->>C: 200 OK
```

## 4. Cascade Delete: Workspace Deletion

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    C->>Ctrl: DELETE /api/workspaces/1
    Ctrl->>Svc: deleteWorkspace(workspaceId=1)
    Svc->>Repo: workspaceRepository.findById(1)
    Repo->>DB: SELECT FROM workspaces WHERE id=1
    DB-->>Repo: Workspace

    Note over Svc,DB: Find all boards in workspace
    Svc->>Repo: boardRepository.findByWorkspace(workspace)
    Repo->>DB: SELECT FROM boards WHERE workspace_id=1
    DB-->>Repo: List of Boards

    Note over Svc,DB: For each board delete children
    loop For each Board
        Svc->>Repo: laneRepository.findByBoard(board)
        Repo->>DB: SELECT FROM lanes WHERE board_id=?
        Svc->>Repo: laneRepository.deleteAll(lanes)
        Repo->>DB: DELETE FROM lanes

        Svc->>Repo: boardMemberRepository.findByBoard(board)
        Repo->>DB: SELECT FROM board_members WHERE board_id=?
        Svc->>Repo: boardMemberRepository.deleteAll(members)
        Repo->>DB: DELETE FROM board_members

        Svc->>Repo: labelRepository.findByBoard(board)
        Repo->>DB: SELECT FROM labels WHERE board_id=?
        Svc->>Repo: labelRepository.deleteAll(labels)
        Repo->>DB: DELETE FROM labels
    end

    Svc->>Repo: boardRepository.deleteAll(boards)
    Repo->>DB: DELETE FROM boards WHERE workspace_id=1

    Svc->>Repo: memberRepository.findByWorkspace(workspace)
    Repo->>DB: SELECT FROM workspace_members WHERE workspace_id=1
    Svc->>Repo: memberRepository.deleteAll(members)
    Repo->>DB: DELETE FROM workspace_members

    Svc->>Repo: workspaceRepository.delete(workspace)
    Repo->>DB: DELETE FROM workspaces WHERE id=1

    Svc-->>Ctrl: void
    Ctrl-->>C: 204 No Content
```

## 5. Error Flow: Validation Failure

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as Controller
    participant Svc as Service
    participant Repo as Repository
    participant DB as PostgreSQL

    Note over C,DB: Entity Not Found
    C->>Ctrl: GET /api/cards/999
    Ctrl->>Svc: getCardById(999)
    Svc->>Repo: cardRepository.findById(999)
    Repo->>DB: SELECT FROM cards WHERE id=999
    DB-->>Repo: Empty Optional
    Svc--xCtrl: throws IllegalArgumentException - Card not found
    Ctrl--xC: 500 Internal Server Error

    Note over C,DB: Duplicate Member
    C->>Ctrl: POST /api/workspaces/1/members (userId 2, role MEMBER)
    Ctrl->>Svc: addMember(1, 2, MEMBER)
    Svc->>Repo: workspaceRepository.findById(1)
    DB-->>Repo: Workspace
    Svc->>Repo: userRepository.findById(2)
    DB-->>Repo: User
    Svc->>Repo: memberRepository.existsByWorkspaceAndUser(ws, user)
    Repo->>DB: SELECT EXISTS
    DB-->>Repo: true - already a member
    Svc--xCtrl: throws IllegalArgumentException - User is already a member
    Ctrl--xC: 500 Internal Server Error

    Note over C,DB: Invalid Input - Entity Validation
    C->>Ctrl: POST /api/cards (laneId 1, title empty, position 0, createdBy 1)
    Ctrl->>Svc: createCard(1, empty, 0, 1)
    Svc->>Repo: laneRepository.findById(1)
    DB-->>Repo: Lane
    Svc->>Repo: userRepository.findById(1)
    DB-->>Repo: User
    Svc->>Svc: new Card with empty title
    Note right of Svc: Card constructor validates title
    Svc--xCtrl: throws IllegalArgumentException - Title cannot be empty
    Ctrl--xC: 500 Internal Server Error
```

## Architecture Layer Diagram

```mermaid
flowchart TD
    Client[Client / HTTP] -->|JSON Request| Controller

    subgraph Spring Boot Application
        Controller[Controller Layer]
        Service[Service Layer]
        Repository[Repository Layer]
        Entity[Entity Layer]
        DTO[DTO Layer]
    end

    Controller -->|delegates to| Service
    Controller -->|reads from| DTO
    Service -->|uses| Repository
    Service -->|creates/modifies| Entity
    Repository -->|persists| Entity
    Repository -->|SQL via Hibernate| DB[(PostgreSQL)]

    Controller -->|HTTP Response| Client
```
