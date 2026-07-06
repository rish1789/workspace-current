import api from './axios'
import { getLanesByBoard } from './laneService'

export function getBoard(boardId) {
  return api.get(`/api/boards/${boardId}`).then((res) => res.data)
}

export function renameBoard(boardId, boardName) {
  return api.patch(`/api/boards/${boardId}/name`, { boardName }).then((res) => res.data)
}

export function updateBoardDescription(boardId, description) {
  return api.patch(`/api/boards/${boardId}/description`, { description }).then((res) => res.data)
}

export function updateBoardVisibility(boardId, visibility) {
  return api.patch(`/api/boards/${boardId}/visibility`, { visibility }).then((res) => res.data)
}

export function deleteBoard(boardId) {
  return api.delete(`/api/boards/${boardId}`)
}

export function getBoardMembers(boardId) {
  return api.get(`/api/boards/${boardId}/members`).then((res) => res.data)
}

export function addBoardMember(boardId, userId, role) {
  return api.post(`/api/boards/${boardId}/members`, { userId, role }).then((res) => res.data)
}

export function removeBoardMember(boardId, userId) {
  return api.delete(`/api/boards/${boardId}/members/${userId}`)
}

export function updateBoardMemberRole(boardId, userId, role) {
  return api
    .patch(`/api/boards/${boardId}/members/${userId}/role`, { role })
    .then((res) => res.data)
}

export function changeBoardAdmin(boardId, userId) {
  return api.patch(`/api/boards/${boardId}/change-admin`, { userId })
}

export function getBoardLabels(boardId) {
  return api.get(`/api/boards/${boardId}/labels`).then((res) => res.data)
}

export function createBoardLabel(boardId, { name, color }) {
  return api.post(`/api/boards/${boardId}/labels`, { name, color }).then((res) => res.data)
}

export function deleteBoardLabel(boardId, labelId) {
  return api.delete(`/api/boards/${boardId}/labels/${labelId}`)
}

export function getCardLabels(cardId) {
  return api.get(`/api/cards/${cardId}/labels`).then((res) => res.data)
}

export function attachCardLabel(cardId, labelId) {
  return api.post(`/api/cards/${cardId}/labels`, { labelId }).then((res) => res.data)
}

export function detachCardLabel(cardId, labelId) {
  return api.delete(`/api/cards/${cardId}/labels/${labelId}`)
}

export function moveCard(cardId, laneId, position) {
  return api.patch(`/api/cards/${cardId}/move`, { laneId, position }).then((res) => res.data)
}

export function archiveCard(cardId) {
  return api.patch(`/api/cards/${cardId}/archive`).then((res) => res.data)
}

export function unarchiveCard(cardId) {
  return api.patch(`/api/cards/${cardId}/unarchive`).then((res) => res.data)
}

export function deleteCard(cardId) {
  return api.delete(`/api/cards/${cardId}`)
}

export function getCardActivity(cardId) {
  return api.get(`/api/cards/${cardId}/activity`).then((res) => res.data)
}

export function getCardMembers(cardId) {
  return api.get(`/api/cards/${cardId}/members`).then((res) => res.data)
}

export function assignCardMember(cardId, userId) {
  return api.post(`/api/cards/${cardId}/members`, { userId }).then((res) => res.data)
}

export function removeCardMember(cardId, userId) {
  return api.delete(`/api/cards/${cardId}/members/${userId}`)
}

export function getCardsByLane(laneId) {
  return api.get(`/api/cards/lane/${laneId}`).then((res) =>
    [...res.data].sort((a, b) => a.position - b.position)
  )
}

export async function getBoardStats(boardId) {
  const [members, lanes] = await Promise.all([
    getBoardMembers(boardId),
    getLanesByBoard(boardId),
  ])
  const cardLists = await Promise.all(lanes.map((lane) => getCardsByLane(lane.id)))
  const cardCount = cardLists.reduce((sum, cards) => sum + cards.length, 0)
  return { memberCount: members.length, laneCount: lanes.length, cardCount }
}
