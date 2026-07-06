import { useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import Modal from './Modal'
import Popover from './Popover'
import {
  archiveCard,
  assignCardMember,
  attachCardLabel,
  deleteCard,
  detachCardLabel,
  getBoardLabels,
  getBoardMembers,
  getCardActivity,
  getCardLabels,
  getCardMembers,
  moveCard,
  removeCardMember,
  unarchiveCard,
} from '../api/boardService'
import { getLanesByBoard } from '../api/laneService'
import { createChecklist, getChecklistsByCard } from '../api/checklistService'
import {
  addComment,
  deleteComment,
  editComment,
  getCommentsByCard,
} from '../api/commentService'
import { getCurrentUser } from '../api/userService'
import { readableTextColor } from '../utils/color'
import ChecklistSection from './ChecklistSection'

// Mirrors the loaded card body's field layout (title, labels, members,
// description, due date) so the swap to real content doesn't shift layout.
function CardDetailSkeleton() {
  return (
    <div className="space-y-4" role="status" aria-live="polite">
      <span className="sr-only">Loading card…</span>
      <div className="h-5 w-3/4 animate-pulse rounded bg-slate-200" />
      <div>
        <div className="h-3 w-12 animate-pulse rounded bg-slate-200" />
        <div className="mt-2 flex gap-1.5">
          <div className="h-5 w-16 animate-pulse rounded-full bg-slate-200" />
          <div className="h-5 w-14 animate-pulse rounded-full bg-slate-200" />
        </div>
      </div>
      <div>
        <div className="h-3 w-16 animate-pulse rounded bg-slate-200" />
        <div className="mt-2 flex gap-1.5">
          <div className="h-5 w-24 animate-pulse rounded-full bg-slate-200" />
        </div>
      </div>
      <div>
        <div className="h-3 w-20 animate-pulse rounded bg-slate-200" />
        <div className="mt-2 h-16 w-full animate-pulse rounded-md bg-slate-200" />
      </div>
      <div>
        <div className="h-3 w-16 animate-pulse rounded bg-slate-200" />
        <div className="mt-2 h-9 w-36 animate-pulse rounded-md bg-slate-200" />
      </div>
    </div>
  )
}

function CardModal({ cardId, boardId, onClose, onUpdated }) {
  const [card, setCard] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [dueDate, setDueDate] = useState('')

  const [comments, setComments] = useState([])
  const [newComment, setNewComment] = useState('')
  const [commentError, setCommentError] = useState('')
  const [editingCommentId, setEditingCommentId] = useState(null)
  const [editingCommentContent, setEditingCommentContent] = useState('')

  const [activity, setActivity] = useState([])
  const [showActivity, setShowActivity] = useState(false)

  const [currentUserId, setCurrentUserId] = useState(null)
  const [confirmingDeleteCard, setConfirmingDeleteCard] = useState(false)
  const [deletingCard, setDeletingCard] = useState(false)

  const [checklists, setChecklists] = useState([])

  const [cardLabels, setCardLabels] = useState([])
  const [boardLabels, setBoardLabels] = useState([])
  const [showLabelPicker, setShowLabelPicker] = useState(false)
  const labelAddBtnRef = useRef(null)

  const [boardLanes, setBoardLanes] = useState([])
  const [targetLaneId, setTargetLaneId] = useState('')
  const [targetPosition, setTargetPosition] = useState('')
  const [moving, setMoving] = useState(false)
  const [showMoveControl, setShowMoveControl] = useState(false)
  const [archiving, setArchiving] = useState(false)

  const [showAddChecklist, setShowAddChecklist] = useState(false)
  const [newChecklistTitle, setNewChecklistTitle] = useState('')
  const [addingChecklist, setAddingChecklist] = useState(false)

  const [cardMembers, setCardMembers] = useState([])
  const [boardMembers, setBoardMembers] = useState([])
  const [showMemberPicker, setShowMemberPicker] = useState(false)
  const memberAddBtnRef = useRef(null)

  const fetchCard = async () => {
    setLoading(true)
    setError('')
    try {
      const [cardRes, commentsData, checklistsRes, cardLabelsRes, cardMembersRes, user] =
        await Promise.all([
          api.get(`/api/cards/${cardId}`),
          getCommentsByCard(cardId),
          getChecklistsByCard(cardId),
          getCardLabels(cardId),
          getCardMembers(cardId),
          getCurrentUser(),
        ])
      setCard(cardRes.data)
      setTitle(cardRes.data.title || '')
      setDescription(cardRes.data.description || '')
      const rawDueDate = cardRes.data.dueDate
      // Backend's Card.getDueDate() returns the literal string "No Date" instead of null when unset.
      setDueDate(rawDueDate && rawDueDate !== 'No Date' ? rawDueDate.slice(0, 10) : '')
      setComments(commentsData)
      setChecklists(checklistsRes)
      setCardLabels(cardLabelsRes)
      // Backend embeds username on each member now — no per-member lookup.
      setCardMembers(cardMembersRes)
      setCurrentUserId(user.id)
      setTargetLaneId(String(cardRes.data.laneId))
      setTargetPosition(String(cardRes.data.position))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load card')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchCard()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cardId])

  useEffect(() => {
    if (boardId) {
      getBoardLabels(boardId)
        .then(setBoardLabels)
        .catch(() => {})
      getLanesByBoard(boardId)
        .then(setBoardLanes)
        .catch(() => {})
      // Backend embeds username on each member now — no per-member lookup.
      getBoardMembers(boardId)
        .then(setBoardMembers)
        .catch(() => {})
    }
  }, [boardId])

  const handleTitleBlur = async () => {
    if (!card || title === card.title) return
    try {
      await api.patch(`/api/cards/${cardId}/title`, { title })
      setCard((prev) => ({ ...prev, title }))
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update title')
    }
  }

  const handleDescriptionBlur = async () => {
    if (!card || description === card.description) return
    try {
      await api.patch(`/api/cards/${cardId}/description`, { description })
      setCard((prev) => ({ ...prev, description }))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update description')
    }
  }

  const handleDueDateChange = async (e) => {
    const value = e.target.value
    setDueDate(value)
    try {
      await api.patch(`/api/cards/${cardId}/due-date`, { date: value })
      setCard((prev) => ({ ...prev, dueDate: value }))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update due date')
    }
  }

  const handleAddComment = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return
    setCommentError('')
    try {
      const comment = await addComment(cardId, newComment)
      setComments((prev) => [...prev, comment])
      setNewComment('')
    } catch (err) {
      setCommentError(err.response?.data?.message || 'Failed to add comment')
    }
  }

  const startEditComment = (comment) => {
    setEditingCommentId(comment.id)
    setEditingCommentContent(comment.content)
  }

  const saveEditComment = async (comment) => {
    setEditingCommentId(null)
    if (!editingCommentContent.trim() || editingCommentContent === comment.content) return
    setCommentError('')
    try {
      const updated = await editComment(comment.id, editingCommentContent)
      setComments((prev) => prev.map((c) => (c.id === comment.id ? updated : c)))
    } catch (err) {
      setCommentError(err.response?.data?.message || 'Failed to update comment')
    }
  }

  const handleDeleteComment = async (commentId) => {
    setCommentError('')
    const previous = comments
    setComments((prev) => prev.filter((c) => c.id !== commentId))
    try {
      await deleteComment(commentId)
    } catch (err) {
      setComments(previous)
      setCommentError(err.response?.data?.message || 'Failed to delete comment')
    }
  }

  const handleShowActivity = async () => {
    const next = !showActivity
    setShowActivity(next)
    if (next && activity.length === 0) {
      try {
        const data = await getCardActivity(cardId)
        setActivity(data)
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load activity')
      }
    }
  }

  const handleDeleteCard = async () => {
    setDeletingCard(true)
    setError('')
    try {
      await deleteCard(cardId)
      onUpdated && onUpdated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete card')
      setDeletingCard(false)
    }
  }

  const handleAttachLabel = async (labelId) => {
    try {
      await attachCardLabel(cardId, labelId)
      const updated = await getCardLabels(cardId)
      setCardLabels(updated)
      setShowLabelPicker(false)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to attach label')
    }
  }

  const handleDetachLabel = async (labelId) => {
    try {
      await detachCardLabel(cardId, labelId)
      setCardLabels((prev) => prev.filter((l) => l.labelId !== labelId))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove label')
    }
  }

  const handleMove = async () => {
    const position = Number(targetPosition)
    const laneId = Number(targetLaneId)
    if (!Number.isInteger(position) || position < 0) {
      setError('Position must be a whole number, 0 or higher')
      return
    }
    if (laneId === card.laneId && position === card.position) return
    setMoving(true)
    setError('')
    try {
      await moveCard(cardId, laneId, position)
      onUpdated && onUpdated()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to move card — check the position is valid')
      setMoving(false)
    }
  }

  const handleToggleArchive = async () => {
    setArchiving(true)
    setError('')
    try {
      const updated = card.archived ? await unarchiveCard(cardId) : await archiveCard(cardId)
      setCard(updated)
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update card')
    } finally {
      setArchiving(false)
    }
  }

  const handleCreateChecklist = async (e) => {
    e.preventDefault()
    if (!newChecklistTitle.trim()) return
    setAddingChecklist(true)
    setError('')
    try {
      const checklist = await createChecklist(cardId, newChecklistTitle)
      setChecklists((prev) => [...prev, checklist])
      setNewChecklistTitle('')
      setShowAddChecklist(false)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create checklist')
    } finally {
      setAddingChecklist(false)
    }
  }

  const handleChecklistDeleted = (checklistId) => {
    setChecklists((prev) => prev.filter((c) => c.id !== checklistId))
  }

  const handleAssignMember = async (userId) => {
    try {
      await assignCardMember(cardId, userId)
      // Backend embeds username on each member now — no per-member lookup.
      const updated = await getCardMembers(cardId)
      setCardMembers(updated)
      setShowMemberPicker(false)
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign member')
    }
  }

  const handleRemoveMember = async (userId) => {
    try {
      await removeCardMember(cardId, userId)
      setCardMembers((prev) => prev.filter((m) => m.userId !== userId))
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove member')
    }
  }

  const availableLabels = boardLabels.filter(
    (bl) => !cardLabels.some((cl) => cl.labelId === bl.labelId)
  )

  const availableMembers = boardMembers.filter(
    (bm) => !cardMembers.some((cm) => cm.userId === bm.userId)
  )

  const isBoardAdmin = boardMembers.some((m) => m.userId === currentUserId && m.role === 'ADMIN')
  const getCommentAuthorName = (comment) => {
    if (comment.userId === currentUserId) return 'You'
    return boardMembers.find((m) => m.userId === comment.userId)?.username || 'Unknown user'
  }

  const dialogLabel = card ? `Card ${card.fullId}: ${card.title}` : 'Card details'

  return (
    <Modal
      open={true}
      onClose={onClose}
      ariaLabel={dialogLabel}
      maxWidth="max-w-xl"
      footerClassName="flex items-center justify-between border-t border-slate-100 bg-slate-50/60 px-6 py-4"
      title={
        <span className="flex items-center gap-2">
          <span className="text-xs font-medium text-accent">{card ? card.fullId : '...'}</span>
          {card && (
            <span className={card.archived ? 'badge-gray' : 'badge-green'}>
              {card.archived ? 'Archived' : 'Active'}
            </span>
          )}
        </span>
      }
      footer={
        <>
          {card && (
            <div className="flex items-center gap-2">
              <button
                onClick={handleToggleArchive}
                disabled={archiving}
                className="btn-ghost btn-sm"
              >
                {archiving
                  ? 'Working...'
                  : card.archived
                  ? 'Unarchive card'
                  : 'Archive card'}
              </button>

              <div className="h-4 w-px bg-slate-200" />

              {confirmingDeleteCard ? (
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted">Delete permanently?</span>
                  <button
                    onClick={handleDeleteCard}
                    disabled={deletingCard}
                    className="btn-danger-ghost btn-sm"
                  >
                    {deletingCard ? 'Deleting...' : 'Confirm'}
                  </button>
                  <button
                    onClick={() => setConfirmingDeleteCard(false)}
                    className="text-xs text-muted hover:text-ink"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setConfirmingDeleteCard(true)}
                  className="btn-danger-ghost btn-sm"
                >
                  Delete card
                </button>
              )}
            </div>
          )}
          <button onClick={onClose} className="btn-secondary btn-sm">
            Close
          </button>
        </>
      }
    >
      {error && <div className="alert-error mb-3">{error}</div>}

      {loading ? (
        <CardDetailSkeleton />
      ) : (
        card && (
          <div className="space-y-6">
                <input
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  onBlur={handleTitleBlur}
                  className="w-full rounded-md border-0 px-0 text-lg font-semibold leading-snug text-ink focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-1"
                  placeholder="Card title"
                />

                <div className="space-y-3">
                <div>
                  <label className="field-label">Labels</label>
                  <div className="flex flex-wrap items-center gap-1.5">
                    {cardLabels.map((label) => (
                      <span
                        key={label.labelId}
                        className="label-pill"
                        style={{ backgroundColor: label.color, color: readableTextColor(label.color) }}
                      >
                        {label.labelName}
                        <button
                          onClick={() => handleDetachLabel(label.labelId)}
                          aria-label={`Remove label ${label.labelName}`}
                          className="ml-0.5 leading-none opacity-80 hover:opacity-100"
                        >
                          &times;
                        </button>
                      </span>
                    ))}

                    <div className="relative">
                      <button
                        ref={labelAddBtnRef}
                        type="button"
                        onClick={() => setShowLabelPicker((prev) => !prev)}
                        aria-label="Add label"
                        className="rounded-full border border-dashed border-slate-300 px-2.5 py-0.5 text-xs font-medium text-muted hover:bg-slate-50"
                      >
                        + Add
                      </button>
                      <Popover
                        open={showLabelPicker}
                        onClose={() => setShowLabelPicker(false)}
                        anchorRef={labelAddBtnRef}
                        className="w-44 rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg"
                      >
                        {availableLabels.length === 0 ? (
                          <p className="px-2 py-1 text-xs text-muted">No more labels</p>
                        ) : (
                          availableLabels.map((label) => (
                            <button
                              key={label.labelId}
                              onClick={() => handleAttachLabel(label.labelId)}
                              className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-xs hover:bg-slate-50"
                            >
                              <span
                                className="h-3 w-3 rounded-full"
                                style={{ backgroundColor: label.color }}
                              />
                              {label.labelName}
                            </button>
                          ))
                        )}
                      </Popover>
                    </div>
                  </div>
                </div>

                <div>
                  <label className="field-label">Members</label>
                  <div className="flex flex-wrap items-center gap-1.5">
                    {cardMembers.map((member) => (
                      <span
                        key={member.userId}
                        className="flex items-center gap-1.5 rounded-full bg-slate-100 py-0.5 pl-0.5 pr-2 text-xs font-medium text-ink"
                      >
                        <span className="flex h-5 w-5 items-center justify-center rounded-full bg-accent-soft text-[10px] font-semibold text-accent">
                          {member.username?.[0]?.toUpperCase() || '?'}
                        </span>
                        {member.username}
                        <button
                          onClick={() => handleRemoveMember(member.userId)}
                          aria-label={`Remove member ${member.username}`}
                          className="leading-none opacity-60 hover:opacity-100"
                        >
                          &times;
                        </button>
                      </span>
                    ))}

                    <div className="relative">
                      <button
                        ref={memberAddBtnRef}
                        type="button"
                        onClick={() => setShowMemberPicker((prev) => !prev)}
                        aria-label="Add member"
                        className="rounded-full border border-dashed border-slate-300 px-2.5 py-0.5 text-xs font-medium text-muted hover:bg-slate-50"
                      >
                        + Add
                      </button>
                      <Popover
                        open={showMemberPicker}
                        onClose={() => setShowMemberPicker(false)}
                        anchorRef={memberAddBtnRef}
                        className="w-48 rounded-lg border border-slate-200 bg-white p-1.5 shadow-lg"
                      >
                        {availableMembers.length === 0 ? (
                          <p className="px-2 py-1 text-xs text-muted">
                            No more board members
                          </p>
                        ) : (
                          availableMembers.map((member) => (
                            <button
                              key={member.userId}
                              onClick={() => handleAssignMember(member.userId)}
                              className="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-xs hover:bg-slate-50"
                            >
                              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-accent-soft text-[10px] font-semibold text-accent">
                                {member.username?.[0]?.toUpperCase() || '?'}
                              </span>
                              {member.username}
                            </button>
                          ))
                        )}
                      </Popover>
                    </div>
                  </div>
                </div>
                </div>

                <div className="space-y-3">
                  <div>
                    <label className="field-label">Description</label>
                    <textarea
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      onBlur={handleDescriptionBlur}
                      rows={3}
                      placeholder="Add a more detailed description..."
                      className="field-input resize-none"
                    />
                  </div>

                  <div>
                    <label className="field-label">Due date</label>
                    <input
                      type="date"
                      value={dueDate}
                      onChange={handleDueDateChange}
                      className="field-input w-auto"
                    />
                  </div>
                </div>

                <div className="border-t border-slate-200 pt-5">
                  {showMoveControl ? (
                    <>
                      <label className="field-label">Move card</label>
                      <div className="flex gap-2">
                        <select
                          value={targetLaneId}
                          onChange={(e) => setTargetLaneId(e.target.value)}
                          className="field-input"
                        >
                          {boardLanes.map((l) => (
                            <option key={l.id} value={l.id}>
                              {l.name}
                            </option>
                          ))}
                        </select>
                        <input
                          type="number"
                          min={0}
                          value={targetPosition}
                          onChange={(e) => setTargetPosition(e.target.value)}
                          className="field-input w-20"
                          title="Position within the lane, 0 = top"
                        />
                        <button
                          onClick={handleMove}
                          disabled={
                            moving ||
                            targetLaneId === '' ||
                            targetPosition === '' ||
                            (Number(targetLaneId) === card.laneId &&
                              Number(targetPosition) === card.position)
                          }
                          className="btn-secondary btn-sm"
                        >
                          {moving ? 'Moving...' : 'Move'}
                        </button>
                        <button
                          type="button"
                          onClick={() => setShowMoveControl(false)}
                          className="btn-ghost btn-sm"
                        >
                          Cancel
                        </button>
                      </div>
                      <p className="mt-1.5 text-xs text-muted">Position 0 is the top of the lane.</p>
                    </>
                  ) : (
                    <button
                      type="button"
                      onClick={() => setShowMoveControl(true)}
                      className="flex w-full items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-left text-sm font-medium text-ink transition hover:bg-slate-50"
                    >
                      <svg className="h-4 w-4 text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M8 7l4-4m0 0l4 4m-4-4v18m8-6l-4 4m0 0l-4-4m4 4V3" />
                      </svg>
                      Move card
                      <span className="ml-auto text-xs font-normal text-muted">
                        {boardLanes.find((l) => l.id === card.laneId)?.name || 'this lane'}
                      </span>
                    </button>
                  )}
                </div>

                <div className="space-y-5 border-t border-slate-200 pt-5">
                <div>
                  <div className="mb-2 flex items-center justify-between">
                    <h3 className="text-sm font-semibold text-ink">
                      Checklists
                      {checklists.length > 0 && (
                        <span className="ml-1.5 font-normal text-muted">
                          ({checklists.length})
                        </span>
                      )}
                    </h3>
                    {!showAddChecklist && (
                      <button
                        onClick={() => setShowAddChecklist(true)}
                        className="text-xs font-medium text-accent hover:text-accent-hover"
                      >
                        + Add checklist
                      </button>
                    )}
                  </div>

                  {showAddChecklist && (
                    <form onSubmit={handleCreateChecklist} className="mb-3 flex gap-2">
                      <input
                        type="text"
                        autoFocus
                        placeholder="Checklist title"
                        value={newChecklistTitle}
                        onChange={(e) => setNewChecklistTitle(e.target.value)}
                        className="field-input py-1.5 text-sm"
                      />
                      <button
                        type="submit"
                        disabled={addingChecklist}
                        className="btn-primary btn-sm"
                      >
                        Add
                      </button>
                      <button
                        type="button"
                        onClick={() => setShowAddChecklist(false)}
                        className="btn-ghost btn-sm"
                      >
                        Cancel
                      </button>
                    </form>
                  )}

                  {checklists.length === 0 ? (
                    <p className="text-sm text-muted">No checklists yet.</p>
                  ) : (
                    <div className="space-y-3">
                      {checklists.map((checklist) => (
                        <ChecklistSection
                          key={checklist.id}
                          checklist={checklist}
                          onDeleted={handleChecklistDeleted}
                        />
                      ))}
                    </div>
                  )}
                </div>

                <div>
                  <h3 className="mb-2 text-sm font-semibold text-ink">
                    Comments
                    {comments.length > 0 && (
                      <span className="ml-1.5 font-normal text-muted">({comments.length})</span>
                    )}
                  </h3>
                  {comments.length > 0 && (
                    <ul className="mb-3 space-y-2">
                      {comments.map((comment) => {
                        const canEdit = comment.userId === currentUserId
                        const canDelete = canEdit || isBoardAdmin
                        return (
                          <li
                            key={comment.id}
                            className="group rounded-md bg-slate-50 px-3 py-2 text-sm text-ink"
                          >
                            <div className="mb-0.5 flex items-center justify-between gap-2">
                              <span className="text-xs font-medium text-muted">
                                {getCommentAuthorName(comment)}
                              </span>
                              {canDelete && editingCommentId !== comment.id && (
                                <button
                                  onClick={() => handleDeleteComment(comment.id)}
                                  className="text-muted opacity-0 transition hover:text-red-600 group-hover:opacity-100 focus-visible:opacity-100"
                                  title="Delete comment"
                                  aria-label="Delete comment"
                                >
                                  &times;
                                </button>
                              )}
                            </div>
                            {editingCommentId === comment.id ? (
                              <input
                                autoFocus
                                value={editingCommentContent}
                                onChange={(e) => setEditingCommentContent(e.target.value)}
                                onBlur={() => saveEditComment(comment)}
                                onKeyDown={(e) => e.key === 'Enter' && e.target.blur()}
                                className="w-full rounded-sm border-0 bg-transparent p-0 text-sm text-ink focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-1"
                              />
                            ) : canEdit ? (
                              <button
                                type="button"
                                onClick={() => startEditComment(comment)}
                                className="cursor-text text-left text-sm text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/40 focus-visible:ring-offset-1"
                              >
                                {comment.content}
                              </button>
                            ) : (
                              <p>{comment.content}</p>
                            )}
                          </li>
                        )
                      })}
                    </ul>
                  )}

                  <form onSubmit={handleAddComment} className="flex gap-2">
                    <input
                      type="text"
                      placeholder="Add a comment..."
                      value={newComment}
                      onChange={(e) => setNewComment(e.target.value)}
                      className="field-input flex-1"
                    />
                    <button type="submit" className="btn-primary btn-sm">
                      Post
                    </button>
                  </form>
                  {commentError && <p className="mt-2 text-sm text-red-600">{commentError}</p>}
                </div>

                <div>
                  <button
                    type="button"
                    onClick={handleShowActivity}
                    className="text-sm font-medium text-muted hover:text-ink"
                  >
                    Activity {showActivity ? '▾' : '▸'}
                  </button>
                  {showActivity && (
                    activity.length === 0 ? (
                      <p className="mt-2 text-sm text-muted">No activity recorded yet.</p>
                    ) : (
                      <ul className="mt-2 space-y-1.5">
                        {activity.map((log) => (
                          <li key={log.id} className="text-xs text-muted">
                            {log.action}
                            <span className="ml-1.5 text-muted">{log.createdAt}</span>
                          </li>
                        ))}
                      </ul>
                    )
                  )}
                </div>
                </div>
              </div>
            )
          )}
    </Modal>
  )
}

export default CardModal
