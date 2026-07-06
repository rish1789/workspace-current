import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios'
import Modal from '../components/Modal'
import { getWorkspaceMembers, getWorkspaceStats } from '../api/workspaceService'

function Workspaces() {
  const navigate = useNavigate()
  const [workspaces, setWorkspaces] = useState([])
  const [stats, setStats] = useState({})
  const [uniqueMemberCount, setUniqueMemberCount] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [workspaceName, setWorkspaceName] = useState('')
  const [creating, setCreating] = useState(false)
  const [formError, setFormError] = useState('')

  const fetchWorkspaces = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await api.get('/api/workspaces/all')
      setWorkspaces(response.data)

      response.data.forEach((workspace) => {
        getWorkspaceStats(workspace.id)
          .then((s) => setStats((prev) => ({ ...prev, [workspace.id]: s })))
          .catch(() => {})
      })

      const memberLists = await Promise.all(
        response.data.map((workspace) => getWorkspaceMembers(workspace.id).catch(() => []))
      )
      const uniqueIds = new Set(memberLists.flat().map((m) => m.userId))
      setUniqueMemberCount(uniqueIds.size)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load workspaces')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchWorkspaces()
  }, [])

  const closeForm = () => {
    setShowForm(false)
    setWorkspaceName('')
    setFormError('')
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError('')
    setCreating(true)
    try {
      await api.post('/api/workspaces', { workspaceName })
      closeForm()
      fetchWorkspaces()
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create workspace')
    } finally {
      setCreating(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    navigate('/login')
  }

  const totalBoards = Object.values(stats).reduce((sum, s) => sum + (s?.boardCount || 0), 0)

  const topWorkspaces = [...workspaces]
    .map((w) => ({ ...w, boardCount: stats[w.id]?.boardCount ?? -1 }))
    .filter((w) => w.boardCount >= 0)
    .sort((a, b) => b.boardCount - a.boardCount)
    .slice(0, 5)

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <div className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-white/15 text-sm font-bold text-white ring-1 ring-inset ring-white/25">
              W
            </div>
            <h1 className="text-lg font-semibold text-white">Workspaces</h1>
          </div>
          <div className="flex items-center gap-2">
            <button onClick={() => navigate('/account')} className="btn-header-glass btn-sm">
              Account
            </button>
            <button onClick={handleLogout} className="btn-header-glass btn-sm">
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto flex max-w-6xl gap-6 px-6 py-8">
        <div className="min-w-0 flex-1">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-ink">Your workspaces</h2>
              <p className="text-sm text-slate-600">
                Pick a workspace or create a new one to get started.
              </p>
            </div>
            <button onClick={() => setShowForm(true)} className="btn-primary">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              New Workspace
            </button>
          </div>

          {error && <div className="alert-error mb-4">{error}</div>}

          {loading ? (
            <p className="text-sm text-slate-600">Loading workspaces...</p>
          ) : workspaces.length === 0 ? (
            <div className="empty-state">No workspaces yet. Create one to get started.</div>
          ) : (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              {workspaces.map((workspace) => {
                const s = stats[workspace.id]
                return (
                  <div
                    key={workspace.id}
                    onClick={() => navigate(`/workspaces/${workspace.id}`)}
                    className="tile"
                  >
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent-soft text-sm font-semibold text-accent">
                      {workspace.name?.[0]?.toUpperCase() || 'W'}
                    </div>
                    <h3 className="mt-3 text-base font-semibold text-ink">{workspace.name}</h3>
                    <p className="mt-1 line-clamp-2 text-sm text-muted">
                      {workspace.description || 'No description'}
                    </p>
                    <div className="mt-3 flex items-center gap-3 border-t border-slate-100 pt-3 text-xs text-muted">
                      <span>
                        <span className="font-semibold text-ink">{s ? s.boardCount : '—'}</span>{' '}
                        boards
                      </span>
                      <span>
                        <span className="font-semibold text-ink">{s ? s.memberCount : '—'}</span>{' '}
                        members
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        {workspaces.length > 0 && (
          <aside className="hidden w-72 flex-none space-y-4 lg:block">
            <div className="surface-card p-4">
              <h3 className="mb-3 text-sm font-semibold text-ink">Overview</h3>
              <div className="grid grid-cols-3 gap-2 text-center">
                <div>
                  <p className="text-lg font-semibold text-ink">{workspaces.length}</p>
                  <p className="text-[11px] text-muted">Workspaces</p>
                </div>
                <div>
                  <p className="text-lg font-semibold text-ink">{totalBoards}</p>
                  <p className="text-[11px] text-muted">Boards</p>
                </div>
                <div>
                  <p className="text-lg font-semibold text-ink">
                    {uniqueMemberCount ?? '—'}
                  </p>
                  <p className="text-[11px] text-muted">People</p>
                </div>
              </div>
            </div>

            <div className="surface-card p-4">
              <h3 className="mb-3 text-sm font-semibold text-ink">Top workspaces</h3>
              {topWorkspaces.length === 0 ? (
                <p className="text-sm text-muted">Nothing to rank yet.</p>
              ) : (
                <ul className="space-y-2">
                  {topWorkspaces.map((w) => (
                    <li
                      key={w.id}
                      onClick={() => navigate(`/workspaces/${w.id}`)}
                      className="flex cursor-pointer items-center justify-between rounded-md px-2 py-1.5 text-sm transition hover:bg-slate-50"
                    >
                      <span className="flex items-center gap-2 truncate text-ink">
                        <span className="flex h-6 w-6 flex-none items-center justify-center rounded-md bg-accent-soft text-[10px] font-semibold text-accent">
                          {w.name?.[0]?.toUpperCase() || 'W'}
                        </span>
                        <span className="truncate">{w.name}</span>
                      </span>
                      <span className="flex-none text-xs text-muted">{w.boardCount} boards</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </aside>
        )}
      </main>

      <Modal
        open={showForm}
        onClose={closeForm}
        title="Create workspace"
        footer={
          <>
            <button onClick={closeForm} className="btn-secondary">
              Cancel
            </button>
            <button
              type="submit"
              form="create-workspace-form"
              disabled={creating}
              className="btn-primary"
            >
              {creating ? 'Creating...' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-workspace-form" onSubmit={handleCreate}>
          {formError && <div className="alert-error mb-3">{formError}</div>}
          <label className="field-label">Workspace name</label>
          <input
            type="text"
            value={workspaceName}
            onChange={(e) => setWorkspaceName(e.target.value)}
            required
            autoFocus
            className="field-input"
          />
        </form>
      </Modal>
    </div>
  )
}

export default Workspaces
