import api from './axios'

export function getCommentsByCard(cardId) {
  return api.get(`/api/comments/card/${cardId}`).then((res) => res.data)
}

export function addComment(cardId, content) {
  return api.post('/api/comments', { cardId, content }).then((res) => res.data)
}

export function editComment(commentId, content) {
  return api.patch(`/api/comments/${commentId}/content`, { content }).then((res) => res.data)
}

export function deleteComment(commentId) {
  return api.delete(`/api/comments/${commentId}`)
}
