import { useEffect, useState } from 'react'
import Modal from './Modal'
import { createBoardLabel, deleteBoardLabel, getBoardLabels } from '../api/boardService'
import { readableTextColor } from '../utils/color'

const PRESET_COLORS = [
  '#2563EB', '#16A34A', '#DC2626', '#D97706',
  '#7C3AED', '#DB2777', '#0891B2', '#4B5563',
  '#0D9488', '#EA580C', '#65A30D', '#9333EA',
]

function LabelsModal({ open, onClose, boardId, onLabelsChanged }) {
  const [labels, setLabels] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [name, setName] = useState('')
  const [color, setColor] = useState(PRESET_COLORS[0])
  const [creating, setCreating] = useState(false)

  const fetchLabels = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getBoardLabels(boardId)
      setLabels(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load labels')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (open && boardId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      fetchLabels()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, boardId])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!name.trim()) return
    setCreating(true)
    setError('')
    try {
      await createBoardLabel(boardId, { name, color })
      setName('')
      setColor(PRESET_COLORS[0])
      await fetchLabels()
      onLabelsChanged && onLabelsChanged()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create label')
    } finally {
      setCreating(false)
    }
  }

  const handleDelete = async (labelId) => {
    try {
      await deleteBoardLabel(boardId, labelId)
      setLabels((prev) => prev.filter((l) => l.labelId !== labelId))
      onLabelsChanged && onLabelsChanged()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete label')
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Manage labels" maxWidth="max-w-sm">
      <div className="space-y-5">
        {error && <div className="alert-error">{error}</div>}

        <form onSubmit={handleCreate} className="space-y-3">
          <div>
            <label className="field-label">Label name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Bug, Urgent, Design"
              className="field-input"
              required
            />
          </div>

          <div>
            <label className="field-label">Color</label>
            <div className="flex flex-wrap items-center gap-2">
              {PRESET_COLORS.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  className={`h-6 w-6 rounded-full transition ${
                    color.toLowerCase() === c.toLowerCase()
                      ? 'ring-2 ring-offset-2 ring-blue-500'
                      : ''
                  }`}
                  style={{ backgroundColor: c }}
                  aria-label={c}
                />
              ))}

              {/* Full-range picker — any color the backend's hex validator accepts */}
              <label
                className="relative flex h-6 w-6 cursor-pointer items-center justify-center rounded-full border border-dashed border-slate-300 text-[10px] text-muted"
                title="Custom color"
              >
                <input
                  type="color"
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  className="absolute inset-0 h-full w-full cursor-pointer opacity-0"
                />
                <span
                  className="h-4 w-4 rounded-full"
                  style={{
                    background:
                      'conic-gradient(red, yellow, lime, cyan, blue, magenta, red)',
                  }}
                />
              </label>

              <input
                type="text"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className="field-input w-24 py-1 text-xs uppercase"
                maxLength={7}
              />
            </div>
          </div>

          <button type="submit" disabled={creating} className="btn-primary btn-sm w-full">
            {creating ? 'Adding...' : '+ Add label'}
          </button>
        </form>

        <div className="border-t border-slate-100 pt-4">
          {loading ? (
            <p className="text-sm text-muted">Loading labels...</p>
          ) : labels.length === 0 ? (
            <p className="text-sm text-muted">No labels yet on this board.</p>
          ) : (
            <ul className="space-y-1.5">
              {labels.map((label) => (
                <li
                  key={label.labelId}
                  className="flex items-center justify-between rounded-lg px-1 py-1 transition hover:bg-slate-50"
                >
                  <span
                    className="label-pill"
                    style={{ backgroundColor: label.color, color: readableTextColor(label.color) }}
                  >
                    {label.labelName}
                  </span>
                  <button
                    onClick={() => handleDelete(label.labelId)}
                    className="btn-danger-ghost"
                  >
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </Modal>
  )
}

export default LabelsModal
