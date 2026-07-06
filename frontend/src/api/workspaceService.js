import api from './axios'

export function getWorkspace(id) {
  return api.get(`/api/workspaces/${id}`).then((res) => res.data)
}

export function renameWorkspace(id, workspaceName) {
  return api.patch(`/api/workspaces/${id}/name`, { workspaceName }).then((res) => res.data)
}

export function updateWorkspaceDescription(id, description) {
  return api.patch(`/api/workspaces/${id}/description`, { description }).then((res) => res.data)
}

export function deleteWorkspace(id) {
  return api.delete(`/api/workspaces/${id}`)
}

export function getWorkspaceMembers(id) {
  return api.get(`/api/workspaces/${id}/members`).then((res) => res.data)
}

export function addWorkspaceMember(id, userId, role) {
  return api.post(`/api/workspaces/${id}/members`, { userId, role }).then((res) => res.data)
}

export function removeWorkspaceMember(id, userId) {
  return api.delete(`/api/workspaces/${id}/members/${userId}`)
}

export function updateWorkspaceMemberRole(id, userId, role) {
  return api
    .patch(`/api/workspaces/${id}/members/${userId}/role`, { role })
    .then((res) => res.data)
}

export function transferWorkspaceOwnership(id, userId) {
  return api.patch(`/api/workspaces/${id}/members/transfer-ownership`, { userId })
}

export async function getWorkspaceStats(id) {
  const [members, boardsRes] = await Promise.all([
    getWorkspaceMembers(id),
    api.get(`/api/boards/workspace/${id}`),
  ])
  return { memberCount: members.length, boardCount: boardsRes.data.length }
}
