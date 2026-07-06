import { useEffect, useState } from 'react'
import Modal from './Modal'
import MemberList from './MemberList'
import AddMemberForm from './AddMemberForm'
import {
  addBoardMember,
  changeBoardAdmin,
  deleteBoard,
  getBoard,
  getBoardMembers,
  removeBoardMember,
  renameBoard,
  updateBoardDescription,
  updateBoardMemberRole,
  updateBoardVisibility,
} from '../api/boardService'
import { getCurrentUser } from '../api/userService'

const VISIBILITY_OPTIONS = ['PUBLIC', 'WORKSPACE', 'PRIVATE']

function BoardSettingsModal({ open, onClose, boardId, laneCount, onUpdated, onDeleted }) {
  const [board, setBoard] = useState(null)
  const [members, setMembers] = useState([])
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [visibility, setVisibility] = useState('WORKSPACE')
  const [savingName, setSavingName] = useState(false)
  const [savingDescription, setSavingDescription] = useState(false)
  const [savingVisibility, setSavingVisibility] = useState(false)

  const [memberActionUserId, setMemberActionUserId] = useState(null)
  const [confirmingRemoveUserId, setConfirmingRemoveUserId] = useState(null)
  const [confirmingDeleteBoard, setConfirmingDeleteBoard] = useState(false)
  const [deletingBoard, setDeletingBoard] = useState(false)

  const fetchAll = async () => {
    setLoading(true)
    setError('')
    try {
      const [boardData, membersData, user] = await Promise.all([
        getBoard(boardId),
        getBoardMembers(boardId),
        getCurrentUser(),
      ])
      setBoard(boardData)
      setName(boardData.boardName || '')
      setDescription(boardData.description || '')
      setVisibility(boardData.visibility || 'WORKSPACE')
      setMembers(membersData)
      setCurrentUser(user)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load board settings')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (open && boardId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      fetchAll()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, boardId])

  const myMembership = members.find((m) => m.userId === currentUser?.id)
  const canAddMembers = myMembership?.role === 'ADMIN'

  const handleAddMember = async (userId, role) => {
    await addBoardMember(boardId, userId, role)
    await fetchAll()
  }

  const handleRemoveMember = async (userId) => {
    setMemberActionUserId(userId)
    setError('')
    try {
      await removeBoardMember(boardId, userId)
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
      await updateBoardMemberRole(boardId, userId, role)
      await fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update role')
    } finally {
      setMemberActionUserId(null)
    }
  }

  const handleMakeAdmin = async (userId) => {
    setMemberActionUserId(userId)
    setError('')
    try {
      await changeBoardAdmin(boardId, userId)
      await fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reassign admin')
    } finally {
      setMemberActionUserId(null)
    }
  }

  const handleDeleteBoard = async () => {
    setDeletingBoard(true)
    setError('')
    try {
      await deleteBoard(boardId)
      onDeleted && onDeleted()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete board')
      setDeletingBoard(false)
    }
  }

  const handleSaveName = async () => {
    if (!name.trim() || name === board?.boardName) return
    setSavingName(true)
    setError('')
    try {
      const updated = await renameBoard(boardId, name)
      setBoard(updated)
      onUpdated && onUpdated(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to rename board')
    } finally {
      setSavingName(false)
    }
  }

  const handleSaveDescription = async () => {
    if (description === (board?.description || '')) return
    setSavingDescription(true)
    setError('')
    try {
      const updated = await updateBoardDescription(boardId, description)
      setBoard(updated)
      onUpdated && onUpdated(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update description')
    } finally {
      setSavingDescription(false)
    }
  }

  const handleVisibilityChange = async (e) => {
    const value = e.target.value
    setVisibility(value)
    setSavingVisibility(true)
    setError('')
    try {
      const updated = await updateBoardVisibility(boardId, value)
      setBoard(updated)
      onUpdated && onUpdated(updated)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update visibility')
    } finally {
      setSavingVisibility(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Board settings" maxWidth="max-w-lg">
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
                disabled={savingName || !name.trim() || name === board?.boardName}
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
                placeholder="What is this board for?"
                className="field-input resize-none"
              />
              <button
                onClick={handleSaveDescription}
                disabled={savingDescription || description === (board?.description || '')}
                className="btn-secondary btn-sm self-start"
              >
                {savingDescription ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>

          <div>
            <label className="field-label">Visibility</label>
            <select
              value={visibility}
              onChange={handleVisibilityChange}
              disabled={savingVisibility}
              className="field-input"
            >
              {VISIBILITY_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-3 gap-3 rounded-lg bg-slate-50 p-3 text-center">
            <div>
              <p className="text-xs text-muted">Your role</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">
                {myMembership?.role || '—'}
              </p>
            </div>
            <div>
              <p className="text-xs text-muted">Lanes</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">{laneCount ?? '—'}</p>
            </div>
            <div>
              <p className="text-xs text-muted">Created</p>
              <p className="mt-0.5 text-sm font-semibold text-ink">
                {board?.createdAt?.slice(0, 10) || '—'}
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
                  roleOptions={['MEMBER', 'OBSERVER']}
                  onAdd={handleAddMember}
                />
              </div>
            )}
            <MemberList
              members={members}
              currentUserId={currentUser?.id}
              renderActions={
                canAddMembers
                  ? (m) => {
                      if (m.userId === currentUser?.id) return null
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
                          {m.role !== 'ADMIN' && (
                            <>
                              <select
                                value={m.role}
                                onChange={(e) => handleRoleChange(m.userId, e.target.value)}
                                disabled={busy}
                                className="field-input w-auto py-1 text-xs"
                              >
                                <option value="MEMBER">MEMBER</option>
                                <option value="OBSERVER">OBSERVER</option>
                              </select>
                              <button
                                onClick={() => handleMakeAdmin(m.userId)}
                                disabled={busy}
                                className="btn-ghost btn-sm"
                              >
                                Make admin
                              </button>
                            </>
                          )}
                          <button
                            onClick={() => setConfirmingRemoveUserId(m.userId)}
                            disabled={busy}
                            className="text-muted hover:text-red-600"
                            title="Remove member"
                            aria-label={`Remove member ${m.username}`}
                          >
                            &times;
                          </button>
                        </div>
                      )
                    }
                  : undefined
              }
            />
          </div>

          {canAddMembers && (
            <div className="border-t border-slate-100 pt-4">
              {confirmingDeleteBoard ? (
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted">
                    Delete this board and everything in it — lanes, cards, comments, checklists?
                    This can't be undone.
                  </span>
                  <button
                    onClick={handleDeleteBoard}
                    disabled={deletingBoard}
                    className="btn-danger-ghost shrink-0"
                  >
                    {deletingBoard ? 'Deleting...' : 'Confirm delete'}
                  </button>
                  <button
                    onClick={() => setConfirmingDeleteBoard(false)}
                    className="shrink-0 text-xs text-muted hover:text-ink"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setConfirmingDeleteBoard(true)}
                  className="btn-danger-ghost"
                >
                  Delete board
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </Modal>
  )
}

export default BoardSettingsModal
