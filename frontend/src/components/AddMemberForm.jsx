import { useState } from 'react'
import { searchUsers } from '../api/userService'

// Shared by WorkspaceSettingsModal and BoardSettingsModal — search-by-username
// then add, since neither scope exposes a full user directory to browse.
function AddMemberForm({ existingMemberIds, roleOptions, onAdd }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searched, setSearched] = useState(false)
  const [searching, setSearching] = useState(false)
  const [role, setRole] = useState(roleOptions[0])
  const [addingUserId, setAddingUserId] = useState(null)
  const [error, setError] = useState('')

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    setSearching(true)
    setSearched(true)
    setError('')
    try {
      const data = await searchUsers(query.trim())
      setResults(data.filter((u) => !existingMemberIds.includes(u.id)))
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed')
    } finally {
      setSearching(false)
    }
  }

  const handleAdd = async (user) => {
    setAddingUserId(user.id)
    setError('')
    try {
      await onAdd(user.id, role)
      setResults((prev) => prev.filter((u) => u.id !== user.id))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add member')
    } finally {
      setAddingUserId(null)
    }
  }

  return (
    <div>
      <form onSubmit={handleSearch} className="flex items-center gap-2">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by username..."
          className="field-input"
        />
        {roleOptions.length > 1 && (
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="field-input w-auto"
          >
            {roleOptions.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        )}
        <button
          type="submit"
          disabled={searching || !query.trim()}
          className="btn-secondary btn-sm"
        >
          {searching ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <p className="mt-2 text-xs text-red-600">{error}</p>}

      {searched && !searching && (
        <div className="mt-2">
          {results.length === 0 ? (
            <p className="text-sm text-muted">No matching users to add.</p>
          ) : (
            <ul className="space-y-1">
              {results.map((user) => (
                <li
                  key={user.id}
                  className="flex items-center justify-between rounded-md px-1 py-1 hover:bg-slate-50"
                >
                  <span className="flex items-center gap-2 text-sm text-ink">
                    <span className="flex h-6 w-6 items-center justify-center rounded-full bg-accent-soft text-[10px] font-semibold text-accent">
                      {user.username?.[0]?.toUpperCase() || '?'}
                    </span>
                    {user.username}
                  </span>
                  <button
                    onClick={() => handleAdd(user)}
                    disabled={addingUserId === user.id}
                    className="btn-secondary btn-sm"
                  >
                    {addingUserId === user.id ? 'Adding...' : `Add as ${role}`}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

export default AddMemberForm
