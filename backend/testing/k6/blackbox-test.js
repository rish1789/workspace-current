/**
 * Black-box functional test for the myapp API.
 *
 * Treats the running Spring Boot app purely as an HTTP black box —
 * no internal knowledge, only what the REST contract documents.
 * Exercises the full Workspace -> Board -> Lane -> Card -> Checklist ->
 * Comment -> Label lifecycle, plus a handful of access-control checks.
 *
 * Run:
 *   k6 run testing/k6/blackbox-test.js
 *   k6 run -e BASE_URL=http://localhost:8080 testing/k6/blackbox-test.js
 *
 * This is written as a single-iteration functional smoke test (vus:1,
 * iterations:1). To turn it into a load test later, just change `options`
 * (e.g. stages / vus) — the flow itself doesn't need to change.
 *
 * Two checks below are expected to FAIL against the current codebase.
 * They assert the *correct* contract behavior on purpose, to surface
 * two known bugs discovered during black-box testing:
 *   1. POST /api/auth/login with a wrong password returns 500 instead of 401
 *      (AuthenticationException isn't caught by GlobalExceptionHandler).
 *   2. PATCH /api/comments/{id}/content by a non-author returns 400 instead
 *      of 403 (CommentService.editComment throws IllegalArgumentException
 *      instead of AccessDeniedException).
 */

import http from 'k6/http';
import { check, group } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    // Network-level failures only (k6 doesn't count 4xx/5xx as "failed" by default)
    http_req_failed: ['rate<0.01'],
  },
};

function headers(token) {
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return h;
}

export default function () {
  const stamp = Date.now();
  const userA = { username: `userA_${stamp}`, email: `usera_${stamp}@test.com`, password: 'Passw0rd!23' };
  const userB = { username: `userB_${stamp}`, email: `userb_${stamp}@test.com`, password: 'Passw0rd!23' };

  let tokenA, tokenB, userBId;
  let workspaceId, boardId, laneId, cardId, checklistId, itemId, commentId, labelId;

  group('Auth: register + login', function () {
    let res = http.post(`${BASE_URL}/api/users/register`, JSON.stringify(userA), { headers: headers() });
    check(res, { 'register userA -> 201': (r) => r.status === 201 });

    res = http.post(`${BASE_URL}/api/users/register`, JSON.stringify(userB), { headers: headers() });
    check(res, { 'register userB -> 201': (r) => r.status === 201 });
    userBId = res.json('id');

    res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({ email: userA.email, password: userA.password }), { headers: headers() });
    check(res, {
      'login userA -> 200': (r) => r.status === 200,
      'login userA returns a token': (r) => !!r.json('token'),
    });
    tokenA = res.json('token');

    res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({ email: userB.email, password: userB.password }), { headers: headers() });
    check(res, { 'login userB -> 200': (r) => r.status === 200 });
    tokenB = res.json('token');

    res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({ email: userA.email, password: 'WrongPassword1!' }), { headers: headers() });
    check(res, {
      'login with wrong password -> 401 (KNOWN BUG: currently 500)': (r) => r.status === 401,
    });
  });

  group('Access control: missing token', function () {
    const res = http.get(`${BASE_URL}/api/workspaces/all`, { headers: headers() });
    check(res, { 'no token on protected endpoint -> 401/403': (r) => r.status === 401 || r.status === 403 });
  });

  group('Workspace lifecycle', function () {
    let res = http.post(`${BASE_URL}/api/workspaces`, JSON.stringify({ workspaceName: `Workspace_${stamp}` }), { headers: headers(tokenA) });
    check(res, { 'create workspace -> 201': (r) => r.status === 201 });
    workspaceId = res.json('id');

    res = http.get(`${BASE_URL}/api/workspaces/${workspaceId}`, { headers: headers(tokenA) });
    check(res, { 'owner gets workspace -> 200': (r) => r.status === 200 });

    // userB isn't a member yet
    res = http.get(`${BASE_URL}/api/workspaces/${workspaceId}`, { headers: headers(tokenB) });
    check(res, { 'non-member gets workspace -> 403/404': (r) => r.status === 403 || r.status === 404 });

    res = http.post(
      `${BASE_URL}/api/workspaces/${workspaceId}/members`,
      JSON.stringify({ userId: userBId, role: 'MEMBER' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'add userB as workspace member -> 200': (r) => r.status === 200 });
  });

  group('Board lifecycle + visibility', function () {
    let res = http.post(
      `${BASE_URL}/api/boards`,
      JSON.stringify({ workspaceId, boardName: `Board_${stamp}`, visibility: 'PRIVATE' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'create private board -> 201': (r) => r.status === 201 });
    boardId = res.json('boardId'); // BoardResponse serializes the id as "boardId", not "id"

    // userB is a workspace member but not a board member -> PRIVATE board should reject
    res = http.get(`${BASE_URL}/api/boards/${boardId}`, { headers: headers(tokenB) });
    check(res, { 'workspace member (not board member) views PRIVATE board -> 403': (r) => r.status === 403 });

    res = http.post(
      `${BASE_URL}/api/boards/${boardId}/members`,
      JSON.stringify({ userId: userBId, role: 'MEMBER' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'add userB as board member -> 200': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/api/boards/${boardId}`, { headers: headers(tokenB) });
    check(res, { 'board member views PRIVATE board -> 200': (r) => r.status === 200 });

    // userB is only a board MEMBER, not ADMIN -> should not be able to delete the board
    res = http.del(`${BASE_URL}/api/boards/${boardId}`, null, { headers: headers(tokenB) });
    check(res, { 'non-admin board member deletes board -> 403': (r) => r.status === 403 });
  });

  group('Lane + Card lifecycle', function () {
    let res = http.post(
      `${BASE_URL}/api/lanes`,
      JSON.stringify({ boardId, laneName: 'To Do', position: 0 }),
      { headers: headers(tokenA) }
    );
    check(res, { 'create lane -> 201': (r) => r.status === 201 });
    laneId = res.json('id');

    res = http.post(
      `${BASE_URL}/api/cards`,
      JSON.stringify({ laneId, title: 'Test Card', position: 0 }),
      { headers: headers(tokenA) }
    );
    check(res, {
      'create card -> 201': (r) => r.status === 201,
      'card has a ticket id': (r) => !!r.json('fullId'),
    });
    cardId = res.json('id');

    res = http.patch(
      `${BASE_URL}/api/cards/${cardId}/title`,
      JSON.stringify({ title: 'Updated Card Title' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'update card title -> 200': (r) => r.status === 200 });

    res = http.patch(`${BASE_URL}/api/cards/${cardId}/archive`, null, { headers: headers(tokenA) });
    check(res, {
      'archive card -> 200': (r) => r.status === 200,
      'archived card reflects isArchived=true': (r) => r.json('archived') === true,
    });

    res = http.patch(`${BASE_URL}/api/cards/${cardId}/unarchive`, null, { headers: headers(tokenA) });
    check(res, { 'unarchive card -> 200': (r) => r.status === 200 });
  });

  group('Checklist lifecycle', function () {
    let res = http.post(
      `${BASE_URL}/api/checklists`,
      JSON.stringify({ cardId, title: 'Checklist 1' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'create checklist -> 201': (r) => r.status === 201 });
    checklistId = res.json('id');

    res = http.post(
      `${BASE_URL}/api/checklists/${checklistId}/items`,
      JSON.stringify({ content: 'First item', position: 0 }),
      { headers: headers(tokenA) }
    );
    check(res, { 'add checklist item -> 201': (r) => r.status === 201 });
    itemId = res.json('id');

    res = http.patch(`${BASE_URL}/api/checklists/items/${itemId}/complete`, null, { headers: headers(tokenA) });
    check(res, { 'complete checklist item -> 200': (r) => r.status === 200 });
  });

  group('Comment lifecycle + ownership check', function () {
    let res = http.post(
      `${BASE_URL}/api/comments`,
      JSON.stringify({ cardId, content: 'Nice card!' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'add comment -> 201': (r) => r.status === 201 });
    commentId = res.json('id');

    // userB did not author this comment
    res = http.patch(
      `${BASE_URL}/api/comments/${commentId}/content`,
      JSON.stringify({ content: 'edited by someone else' }),
      { headers: headers(tokenB) }
    );
    check(res, {
      'non-author edits comment -> 403 (KNOWN BUG: currently 400)': (r) => r.status === 403,
    });
  });

  group('Label lifecycle', function () {
    let res = http.post(
      `${BASE_URL}/api/boards/${boardId}/labels`,
      JSON.stringify({ name: 'Urgent', color: '#FF0000' }),
      { headers: headers(tokenA) }
    );
    check(res, { 'create label -> 201': (r) => r.status === 201 });
    labelId = res.json('labelId'); // LabelResponse serializes the id as "labelId", not "id"

    res = http.post(
      `${BASE_URL}/api/cards/${cardId}/labels`,
      JSON.stringify({ labelId }),
      { headers: headers(tokenA) }
    );
    check(res, { 'attach label to card -> 200': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/api/cards/${cardId}/labels`, { headers: headers(tokenA) });
    check(res, {
      'get card labels -> 200': (r) => r.status === 200,
      'label list contains attached label': (r) => r.json().some((l) => l.labelId === labelId),
    });
  });

  group('Cleanup (cascade delete)', function () {
    let res = http.del(`${BASE_URL}/api/cards/${cardId}`, null, { headers: headers(tokenA) });
    check(res, { 'delete card -> 204': (r) => r.status === 204 });

    res = http.del(`${BASE_URL}/api/lanes/${laneId}`, null, { headers: headers(tokenA) });
    check(res, { 'delete lane -> 204': (r) => r.status === 204 });

    res = http.del(`${BASE_URL}/api/boards/${boardId}`, null, { headers: headers(tokenA) });
    check(res, { 'delete board -> 204': (r) => r.status === 204 });

    res = http.del(`${BASE_URL}/api/workspaces/${workspaceId}`, null, { headers: headers(tokenA) });
    check(res, { 'delete workspace -> 204': (r) => r.status === 204 });

    // workspace is gone — fetching it should now 404
    res = http.get(`${BASE_URL}/api/workspaces/${workspaceId}`, { headers: headers(tokenA) });
    check(res, { 'deleted workspace -> 404': (r) => r.status === 404 });
  });
}
