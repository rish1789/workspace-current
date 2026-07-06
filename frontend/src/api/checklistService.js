import api from './axios'

export function getChecklistsByCard(cardId) {
  return api.get(`/api/checklists/card/${cardId}`).then((res) => res.data)
}

export function createChecklist(cardId, title) {
  return api.post('/api/checklists', { cardId, title }).then((res) => res.data)
}

export function renameChecklist(checklistId, title) {
  return api.patch(`/api/checklists/${checklistId}/title`, { title }).then((res) => res.data)
}

export function deleteChecklist(checklistId) {
  return api.delete(`/api/checklists/${checklistId}`)
}

export function getChecklistItems(checklistId) {
  return api.get(`/api/checklists/${checklistId}/items`).then((res) =>
    [...res.data].sort((a, b) => a.position - b.position)
  )
}

export function addChecklistItem(checklistId, content, position) {
  return api
    .post(`/api/checklists/${checklistId}/items`, { content, position })
    .then((res) => res.data)
}

export function updateChecklistItemContent(itemId, content) {
  return api.patch(`/api/checklists/items/${itemId}/content`, { content }).then((res) => res.data)
}

export function completeChecklistItem(itemId) {
  return api.patch(`/api/checklists/items/${itemId}/complete`).then((res) => res.data)
}

export function uncompleteChecklistItem(itemId) {
  return api.patch(`/api/checklists/items/${itemId}/uncomplete`).then((res) => res.data)
}

export function deleteChecklistItem(itemId) {
  return api.delete(`/api/checklists/items/${itemId}`)
}
