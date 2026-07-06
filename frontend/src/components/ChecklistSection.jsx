import { useEffect, useState } from 'react'
import {
  addChecklistItem,
  completeChecklistItem,
  deleteChecklist,
  deleteChecklistItem,
  getChecklistItems,
  renameChecklist,
  uncompleteChecklistItem,
  updateChecklistItemContent,
} from '../api/checklistService'

// Mirrors a loaded item row's shape (checkbox + text) for the first load.
function ChecklistItemSkeleton() {
  return (
    <div className="flex items-center gap-2 px-1 py-1">
      <div className="h-4 w-4 shrink-0 animate-pulse rounded bg-slate-200" />
      <div className="h-3.5 w-2/3 animate-pulse rounded bg-slate-200" />
    </div>
  )
}

function ChecklistSection({ checklist, onDeleted }) {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [title, setTitle] = useState(checklist.title)
  const [newItemContent, setNewItemContent] = useState('')
  const [addingItem, setAddingItem] = useState(false)

  const [editingItemId, setEditingItemId] = useState(null)
  const [editingContent, setEditingContent] = useState('')

  const fetchItems = async () => {
    setLoading(true)
    setError('')
    try {
      const data = await getChecklistItems(checklist.id)
      setItems(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load items')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchItems()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checklist.id])

  const handleRenameChecklist = async () => {
    if (!title.trim() || title === checklist.title) return
    try {
      await renameChecklist(checklist.id, title)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to rename checklist')
    }
  }

  const handleDeleteChecklist = async () => {
    try {
      await deleteChecklist(checklist.id)
      onDeleted && onDeleted(checklist.id)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete checklist')
    }
  }

  const handleToggleItem = async (item) => {
    setError('')
    setItems((prev) => prev.map((i) => (i.id === item.id ? { ...i, done: !i.done } : i)))
    try {
      const updated = item.done
        ? await uncompleteChecklistItem(item.id)
        : await completeChecklistItem(item.id)
      setItems((prev) => prev.map((i) => (i.id === item.id ? updated : i)))
    } catch (err) {
      setItems((prev) => prev.map((i) => (i.id === item.id ? item : i)))
      setError(err.response?.data?.message || 'Failed to update item')
    }
  }

  const handleAddItem = async (e) => {
    e.preventDefault()
    if (!newItemContent.trim()) return
    setAddingItem(true)
    setError('')
    try {
      const item = await addChecklistItem(checklist.id, newItemContent, items.length)
      setItems((prev) => [...prev, item])
      setNewItemContent('')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add item')
    } finally {
      setAddingItem(false)
    }
  }

  const startEditItem = (item) => {
    setEditingItemId(item.id)
    setEditingContent(item.content)
  }

  const saveEditItem = async (item) => {
    setEditingItemId(null)
    if (!editingContent.trim() || editingContent === item.content) return
    try {
      const updated = await updateChecklistItemContent(item.id, editingContent)
      setItems((prev) => prev.map((i) => (i.id === item.id ? updated : i)))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update item')
    }
  }

  const handleDeleteItem = async (itemId) => {
    setError('')
    const previous = items
    setItems((prev) => prev.filter((i) => i.id !== itemId))
    try {
      await deleteChecklistItem(itemId)
    } catch (err) {
      setItems(previous)
      setError(err.response?.data?.message || 'Failed to delete item')
    }
  }

  const doneCount = items.filter((i) => i.done).length
  const total = items.length
  const percent = total > 0 ? Math.round((doneCount / total) * 100) : 0

  return (
    <div className="rounded-lg border border-slate-200 p-3">
      <div className="flex items-center justify-between gap-2">
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          onBlur={handleRenameChecklist}
          className="flex-1 rounded-sm border-0 bg-transparent p-0 text-sm font-semibold text-ink focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-1"
        />
        <button
          onClick={handleDeleteChecklist}
          className="text-muted hover:text-red-600"
          title="Delete checklist"
          aria-label="Delete checklist"
        >
          &times;
        </button>
      </div>

      {total > 0 && (
        <div className="mt-2 flex items-center gap-2">
          <span className="text-xs text-muted">
            {doneCount}/{total}
          </span>
          <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-slate-100">
            <div
              className="h-full rounded-full bg-accent transition-all"
              style={{ width: `${percent}%` }}
            />
          </div>
        </div>
      )}

      {error && <p className="mt-2 text-xs text-red-600">{error}</p>}

      <div className="mt-2 space-y-1.5">
        {loading ? (
          <div role="status" aria-live="polite">
            <span className="sr-only">Loading items…</span>
            <ChecklistItemSkeleton />
            <ChecklistItemSkeleton />
          </div>
        ) : (
          items.map((item) => (
            <div
              key={item.id}
              className={`group flex items-center gap-2 rounded-md px-1 py-1 hover:bg-slate-50 ${
                item.done ? 'opacity-70' : ''
              }`}
            >
              <input
                type="checkbox"
                checked={item.done}
                onChange={() => handleToggleItem(item)}
                aria-label={item.content}
                className="h-4 w-4 shrink-0 accent-blue-600"
              />
              {editingItemId === item.id ? (
                <input
                  autoFocus
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  onBlur={() => saveEditItem(item)}
                  onKeyDown={(e) => e.key === 'Enter' && e.target.blur()}
                  className="flex-1 rounded-sm border-0 bg-transparent p-0 text-sm text-ink focus:outline-none focus:ring-2 focus:ring-accent/40 focus:ring-offset-1"
                />
              ) : (
                <button
                  type="button"
                  onClick={() => startEditItem(item)}
                  className={`flex-1 rounded-sm text-left text-sm cursor-text focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/40 focus-visible:ring-offset-1 ${
                    item.done ? 'text-muted line-through' : 'text-ink'
                  }`}
                >
                  {item.content}
                </button>
              )}
              <button
                onClick={() => handleDeleteItem(item.id)}
                className="text-muted opacity-0 transition hover:text-red-600 group-hover:opacity-100 focus-visible:opacity-100"
                title="Delete item"
                aria-label="Delete item"
              >
                &times;
              </button>
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleAddItem} className="mt-4 flex items-center gap-2">
        <input
          type="text"
          value={newItemContent}
          onChange={(e) => setNewItemContent(e.target.value)}
          placeholder="Add an item..."
          className="field-input flex-1 py-1.5 text-sm"
        />
        <button type="submit" disabled={addingItem} className="btn-secondary btn-sm">
          Add
        </button>
      </form>
    </div>
  )
}

export default ChecklistSection
