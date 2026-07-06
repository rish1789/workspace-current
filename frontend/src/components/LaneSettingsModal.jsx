import { useState } from 'react'
import Modal from './Modal'
import { archiveLane, deleteLane, moveLane, renameLane, unarchiveLane } from '../api/laneService'

// The parent keys this component by lane id (see BoardDetail.jsx), so a
// fresh instance — and fresh initial state below — is created every time a
// different lane's settings are opened. No reset effect needed.
function LaneSettingsModal({ open, onClose, lane, laneCount, onUpdated, onDeleted }) {
  const [name, setName] = useState(lane?.name || '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [targetPosition, setTargetPosition] = useState(String(lane?.position ?? ''))

  if (!lane) return null

  const maxPosition = Math.max((laneCount || 1) - 1, 0)

  const handleSaveName = async () => {
    if (!name.trim() || name === lane.name) return
    setSaving(true)
    setError('')
    try {
      await renameLane(lane.id, name)
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to rename lane')
    } finally {
      setSaving(false)
    }
  }

  const handleMove = async () => {
    const position = Number(targetPosition)
    if (!Number.isInteger(position) || position < 0) {
      setError('Position must be a whole number, 0 or higher')
      return
    }
    if (position === lane.position) return
    setSaving(true)
    setError('')
    try {
      await moveLane(lane.id, position)
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to move lane — check the position is valid')
    } finally {
      setSaving(false)
    }
  }

  const handleToggleArchive = async () => {
    setSaving(true)
    setError('')
    try {
      if (lane.archived) {
        await unarchiveLane(lane.id)
      } else {
        await archiveLane(lane.id)
      }
      onUpdated && onUpdated()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update lane')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async () => {
    setSaving(true)
    setError('')
    try {
      await deleteLane(lane.id)
      onDeleted && onDeleted()
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete lane')
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Lane settings" maxWidth="max-w-sm">
      <div className="space-y-6">
        {error && <div className="alert-error">{error}</div>}

        <div className="space-y-2">
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
                disabled={saving || !name.trim() || name === lane.name}
                className="btn-secondary btn-sm"
              >
                Save
              </button>
            </div>
          </div>
          <p className="text-xs text-muted">
            Position {lane.position} · Created {lane.createdAt?.slice(0, 10) || '—'}
          </p>
        </div>

        <div className="border-t border-slate-200 pt-5">
          <label className="field-label">Move to position (0–{maxPosition})</label>
          <div className="flex gap-2">
            <input
              type="number"
              min={0}
              max={maxPosition}
              value={targetPosition}
              onChange={(e) => setTargetPosition(e.target.value)}
              className="field-input"
            />
            <button
              onClick={handleMove}
              disabled={saving || targetPosition === '' || Number(targetPosition) === lane.position}
              className="btn-secondary btn-sm"
            >
              Move
            </button>
          </div>
        </div>

        <div className="space-y-3 border-t border-slate-200 pt-5">
          <p className="text-xs text-muted">
            Lanes don't have their own description or member list — access is controlled by who's a
            member of the board.
          </p>

          <div className="flex items-center justify-between">
            <button onClick={handleToggleArchive} disabled={saving} className="btn-secondary btn-sm">
              {lane.archived ? 'Unarchive lane' : 'Archive lane'}
            </button>

            {confirmingDelete ? (
              <div className="flex items-center gap-2">
                <span className="text-xs text-muted">Delete this lane and all its cards?</span>
                <button onClick={handleDelete} disabled={saving} className="btn-danger-ghost btn-sm">
                  Confirm delete
                </button>
                <button
                  onClick={() => setConfirmingDelete(false)}
                  className="text-xs text-muted hover:text-ink"
                >
                  Cancel
                </button>
              </div>
            ) : (
              <button
                onClick={() => setConfirmingDelete(true)}
                disabled={saving}
                className="btn-danger-ghost btn-sm"
              >
                Delete lane
              </button>
            )}
          </div>
        </div>
      </div>
    </Modal>
  )
}

export default LaneSettingsModal
