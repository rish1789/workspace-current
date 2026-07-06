import api from './axios'

export function renameLane(laneId, laneName) {
  return api.patch(`/api/lanes/${laneId}/name`, { laneName }).then((res) => res.data)
}

export function moveLane(laneId, position) {
  return api.patch(`/api/lanes/${laneId}/position`, { position }).then((res) => res.data)
}

export function getLanesByBoard(boardId) {
  return api.get(`/api/lanes/board/${boardId}`).then((res) =>
    [...res.data].sort((a, b) => a.position - b.position)
  )
}

export function archiveLane(laneId) {
  return api.patch(`/api/lanes/${laneId}/archive`).then((res) => res.data)
}

export function unarchiveLane(laneId) {
  return api.patch(`/api/lanes/${laneId}/unarchive`).then((res) => res.data)
}

export function deleteLane(laneId) {
  return api.delete(`/api/lanes/${laneId}`)
}
