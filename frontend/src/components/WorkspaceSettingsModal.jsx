import { useEffect, useState } from 'react'
import Modal from './Modal'
import MemberList from './MemberList'
import AddMemberForm from './AddMemberForm'
import {
  addWorkspaceMember,
  deleteWorkspace,
  getWorkspace,
  getWorkspaceMembers,
  removeWorkspaceMember,
  renameWorkspace,
  transferWorkspaceOwnership,
  updateWorkspaceDescription,
  updateWorkspaceMemberRole,
} from '../api/workspaceService'
import { getCurrentUser } from '../api/userService'

function WorkspaceSettingsModal({ open, onClose, workspaceId, boardCount, onUpdated, onDeleted }) {
  const [workspace, setWorkspace] = useState(null)
  const [members, setMembers] = useState([])
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [savingName, setSavingName] = useState(false)
  const [savingDescription, setSavingDescription] = useState(false)

  const [memberActionUserId, setMemberActionUserId] = useState(null)
  const [confirmingRemoveUserId, setConfirmingRemoveUserId] = useState(null)
  const [confirmingDeleteWorkspace, setConfirmingDeleteWorkspace] = useState(false)
  const [deletingWorkspace, setDeletingWorkspace] = useState(false)

  const fetchAll = async () => {
    setLoading(true)
    setError('')
    try {
      const [workspaceData, membersData, user] = await Promise.all([
        getWorkspace(workspaceId),
        getWorkspaceMembers(workspaceId),
        getCurrentUser(),
      ])
      setWorkspace(workspaceData)
      setName(workspaceData.name || '')
      setDescription(workspaceData.description || '')
      setMembers(membersData)
      setCurrentUser(user)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load workspace settings')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (open && workspaceId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      fetchAll()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, workspaceId])

  const myMembership = members.find((m) => m.userId === currentUser?.id)
  const canAddMembers = myMembership?.role === 'OWNER' || myMembership?.role === 'ADMIN'
  const addableRoles = myMembership?.role === 'OWNER' ? ['MEMBER', 'ADMIN'] : ['MEMBER']

  const handleAddMember = async (userId, role) => {
    await addWorkspaceMember(workspaceId, userId, role)
    await fetchAll()
  }

  const handleRemoveMember = async (userId) => {
    setMemberActionUserId(userId)
    setError('')
    try {
      await removeWorkspaceMember(workspaceId, userId)
      setConfirmingRemoveUserId(null)
      await fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove member')
    } finally {
      setMemberActionUserId(null)
    }
  }

  const handleRoleChange = async (userId, role) => {
    setMemberActionUserId(userId)
    setError('')
    try {
      await updateWorkspaceMemberRole(workspaceId, userId, role)
      await fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update role')
    } finally {
      setMemberActionUserId(null)
    }
  }

  const handleTransferOwnership = async (userId) => {
    setMemberActionUserId(userId)
    setError('')
    try {
      await transferWorkspaceOwnership(workspaceId, userId)
      await fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to transfer ownership')
    } finally {
      setMemberActionUserId(null)
    }
  }

  const handleDeleteWorkspace = async () => {
    setDeletingWorkspace(true)
    setError('')
    try {
      await deleteWorkspace(workspaceId)
      onDeleted && onDeleted()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete workspace')
      setDeletingWorkspace(false)
    }
  }

  const handleSaveName = async () => {
    if (!name.trim() || name === workspace?.name) return
    setSavingName(true)
    setError('')
    try {
      const updated = await renameWorkspace(workspaceId, name)
      setWorkspace(updated)
      onUpdated && onUpdated(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to rename workspace')
    } finally {
      setSavingName(false)
    }
  }

  const handleSaveDescription = async () => {
    if (description === (workspace?.description || '')) return
    setSavingDescription(true)
    setError('')
    try {
      const updated = await updateWorkspaceDescription(workspaceId, description)
      setWorkspace(updated)
      onUpdated && onUpdated(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update description')
    } finally {
      setSavingDescription(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Workspace settings" maxWidth="max-w-lg">
      {loading ? (
        <p className="py-6 text-center text-sm text-muted">Loading...</p>
      ) : (
        <div className="space-y-5">
          {error && <div className="alert-error">{error}</div>}

          <div>
            <label className="field-label">Name</label>
            <div className="flex gap-2">
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="field-input"
              />
              <button
                onClick={handleSaveName}
                disabled={savingName || !name.trim() || name === workspace?.name}
                className="btn-secondary btn-sm"
              >
                {savingName ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>

          <div>
            <label className="field-label">Description</label>
            <div className="flex gap-2">
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={2}
                placeholder="What is this workspace for?"
                className="field-input resize-none"
              />
              <button
                onClick={handleSaveDescription}
                disabled={savingDescription || description === (workspace?.description || '')}
                className="btn-secondary btn-sm self-start"
              >
                {savingDescription ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3 rounded-lg bg-slate-50 p-3 text-center">
            <div>
              <p className="text-xs text-muted">Your role</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">
                {myMembership?.role || '—'}
              </p>
            </div>
            <div>
              <p className="text-xs text-muted">Boards</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">{boardCount ?? '—'}</p>
            </div>
            <div>
              <p className="text-xs text-muted">Created</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">
                {workspace?.createdAt?.slice(0, 10) || '—'}
              </p>
            </div>
          </div>

          <div className="border-t border-slate-100 pt-4">
            <h3 className="mb-2 text-sm font-semibold text-ink">
              Members
              <span className="ml-1.5 font-normal text-muted">({members.length})</span>
            </h3>
            {canAddMembers && (
              <div className="mb-3">
                <AddMemberForm
                  existingMemberIds={members.map((m) => m.userId)}
                  roleOptions={addableRoles}
                  onAdd={handleAddMember}
                />
              </div>
            )}
            <MemberList
              members={members}
              currentUserId={currentUser?.id}
              renderActions={(m) => {
                if (m.userId === currentUser?.id || m.role === 'OWNER') return null
                const isOwner = myMembership?.role === 'OWNER'
                const isAdmin = myMembership?.role === 'ADMIN'
                const canRemove = isOwner || (isAdmin && m.role !== 'ADMIN')
                if (!canRemove && !isOwner) return null
                const busy = memberActionUserId === m.userId

                if (confirmingRemoveUserId === m.userId) {
                  return (
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs text-muted">Remove?</span>
                      <button
                        onClick={() => handleRemoveMember(m.userId)}
                        disabled={busy}
                        className="btn-danger-ghost"
                      >
                        Confirm
                      </button>
                      <button
                        onClick={() => setConfirmingRemoveUserId(null)}
                        className="text-xs text-muted hover:text-ink"
                      >
                        Cancel
                      </button>
                    </div>
                  )
                }

                return (
                  <div className="flex items-center gap-1.5">
                    {isOwner && (
                      <>
                        <select
                          value={m.role}
                          onChange={(e) => handleRoleChange(m.userId, e.target.value)}
                          disabled={busy}
                          className="field-input w-auto py-1 text-xs"
                        >
                          <option value="MEMBER">MEMBER</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                        {m.role === 'ADMIN' && (
                          <button
                            onClick={() => handleTransferOwnership(m.userId)}
                            disabled={busy}
                            className="btn-ghost btn-sm"
                          >
                            Transfer ownership
                          </button>
                        )}
                      </>
                    )}
                    {canRemove && (
                      <button
                        onClick={() => setConfirmingRemoveUserId(m.userId)}
                        disabled={busy}
                        className="text-muted hover:text-red-600"
                        title="Remove member"
                        aria-label={`Remove member ${m.username}`}
                      >
                        &times;
                      </button>
                    )}
                  </div>
                )
              }}
            />
          </div>

          {myMembership?.role === 'OWNER' && (
            <div className="border-t border-slate-100 pt-4">
              {confirmingDeleteWorkspace ? (
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted">
                    Delete this workspace and every board, lane, and card inside it? This can't be
                    undone.
                  </span>
                  <button
                    onClick={handleDeleteWorkspace}
                    disabled={deletingWorkspace}
                    className="btn-danger-ghost shrink-0"
                  >
                    {deletingWorkspace ? 'Deleting...' : 'Confirm delete'}
                  </button>
                  <button
                    onClick={() => setConfirmingDeleteWorkspace(false)}
                    className="shrink-0 text-xs text-muted hover:text-ink"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setConfirmingDeleteWorkspace(true)}
                  className="btn-danger-ghost"
                >
                  Delete workspace
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </Modal>
  )
}

export default WorkspaceSettingsModal
