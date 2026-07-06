# Kanban App — Frontend

React + Vite + Tailwind CSS + Axios frontend for the Spring Boot Kanban backend.

## Prerequisites

- Node.js (frontend)
- Java 21 + Maven wrapper, already in the backend repo (`../myapp`)
- PostgreSQL running locally on port 5432, with a `myapp_db` database
  (backend uses `spring.jpa.hibernate.ddl-auto=create-drop`, so tables are
  created on startup and dropped on shutdown — no manual migrations needed)

## Running both apps

**1. Backend** (from `myapp/`):

```bash
./mvnw.cmd spring-boot:run
```

Runs on `http://localhost:8080`. Wait for `Tomcat started on port 8080` in the
log before starting the frontend.

**2. Frontend** (from `myapp-frontend/`):

```bash
npm run dev
```

Runs on `http://localhost:5173`.

> CORS: the backend only allows requests from `http://localhost:5173`
> (see `myapp/src/main/java/com/example/myapp/security/CorsConfig.java`).
> If you change the frontend's dev port, update that origin too.

## Page map (all pages are linked — none are dead ends)

```
/                    → redirects to /login
/login               → email + password → POST /api/auth/login
                        on success: token saved to localStorage, → /workspaces
                        link → /register
/register            → username + email + password → POST /api/users/register
                        on success: → /login
                        link → /login
/workspaces          → protected. GET /api/workspaces/all
                        + New Workspace → POST /api/workspaces
                        click a workspace card → /workspaces/:id
                        Logout → clears token → /login
/workspaces/:id       → protected. GET /api/boards/workspace/:id
                        + New Board (name + visibility) → POST /api/boards
                        click a board card → /boards/:id
                        ← Back → /workspaces
/boards/:id           → protected. GET /api/lanes/board/:id, then per lane
                        GET /api/cards/lane/:laneId
                        + New Lane → POST /api/lanes
                        + Add a card (per lane) → POST /api/cards
                        click a card → opens CardModal (overlay, not a route)
                        ← Back → browser back
CardModal (overlay)  → GET /api/cards/:id, /api/comments/card/:id,
                        /api/checklists/card/:id
                        edit title/description (blur to save) → PATCH
                        set due date → PATCH
                        add comment → POST /api/comments
                        Close → dismisses overlay, underlying board stays
```

"Protected" pages redirect to `/login` if `localStorage.token` is missing
(`src/components/ProtectedRoute.jsx`), and any API call that gets a 401/403
clears the token and redirects to `/login` (`src/api/axios.js` interceptor).

## Manual test walkthrough

1. Open `http://localhost:5173` → should land on `/login`.
2. Click "Register" → fill username/email/password
   (password needs upper + lower + digit + special char, 8–20 chars) → submit
   → should redirect to `/login`.
3. Log in with the same email/password → should redirect to `/workspaces`.
4. Click "+ New Workspace", name it, create → card appears.
5. Click the workspace card → lands on `/workspaces/:id`.
6. Click "+ New Board", name it, pick a visibility, create → card appears.
7. Click the board card → lands on `/boards/:id`.
8. Click "+ New Lane", name it (e.g. "To Do") → column appears.
9. Inside the lane, click "+ Add a card", give it a title → card appears
   with a ticket id like `TKT-0001`.
10. Click the card → modal opens. Edit the description (click away to save),
    set a due date, post a comment → all should persist without a page reload.
11. Close the modal, go back to `/workspaces`, click "Logout" → token cleared,
    redirected to `/login`.
12. Try visiting `/workspaces` directly without logging in → should bounce
    straight to `/login`.

## Known field-name quirks (frontend already accounts for these)

The backend's response DTOs don't always mirror the request field names —
worth knowing if you touch the API layer:

| Entity    | Request field(s)           | Response field    |
|-----------|-----------------------------|--------------------|
| Workspace | `workspaceName`             | `name`             |
| Board     | `boardName`                 | `boardName`, but id is `boardId` (not `id`) |
| Lane      | `laneName`                  | `name`              |
| Card      | `title`                     | `title`, ticket number is `fullId` (not `ticketId`) |
