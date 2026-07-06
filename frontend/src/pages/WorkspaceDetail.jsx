import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/axios'
import Modal from '../components/Modal'
import WorkspaceSettingsModal from '../components/WorkspaceSettingsModal'
import MemberList from '../components/MemberList'
import { getWorkspaceMembers } from '../api/workspaceService'
import { getBoardStats } from '../api/boardService'
import { getCurrentUser, getUserById } from '../api/userService'
import { parseBackendDate, timeAgo } from '../utils/timeAgo'

const VISIBILITY_OPTIONS = ['PUBLIC', 'WORKSPACE', 'PRIVATE']

function WorkspaceDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [workspace, setWorkspace] = useState(null)
  const [boards, setBoards] = useState([])
  const [members, setMembers] = useState([])
  const [currentUser, setCurrentUser] = useState(null)
  const [boardStats, setBoardStats] = useState({})
  const [activity, setActivity] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [showForm, setShowForm] = useState(false)
  const [boardName, setBoardName] = useState('')
  const [visibility, setVisibility] = useState('WORKSPACE')
  const [creating, setCreating] = useState(false)
  const [formError, setFormError] = useState('')

  const [showSettings, setShowSettings] = useState(false)

  const fetchData = async () => {
    setLoading(true)
    setError('')
    try {
      const [workspaceRes, boardsRes, membersRes, user] = await Promise.all([
        api.get(`/api/workspaces/${id}`),
        api.get(`/api/boards/workspace/${id}`),
        getWorkspaceMembers(id),
        getCurrentUser(),
      ])
      setWorkspace(workspaceRes.data)
      setBoards(boardsRes.data)
      setMembers(membersRes)
      setCurrentUser(user)
      boardsRes.data.forEach((board) => {
        getBoardStats(board.boardId)
          .then((s) => setBoardStats((prev) => ({ ...prev, [board.boardId]: s })))
          .catch(() => {})
      })

      const sortedByRecency = [...boardsRes.data].sort(
        (a, b) => parseBackendDate(b.createdAt) - parseBackendDate(a.createdAt)
      )
      Promise.all(
        sortedByRecency.slice(0, 5).map(async (board) => {
          try {
            const creator = await getUserById(board.createdBy)
            return { ...board, creatorName: creator.username }
          } catch {
            return { ...board, creatorName: `User #${board.createdBy}` }
          }
        })
      ).then(setActivity)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load boards')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  const closeForm = () => {
    setShowForm(false)
    setBoardName('')
    setVisibility('WORKSPACE')
    setFormError('')
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    setFormError('')
    setCreating(true)
    try {
      await api.post('/api/boards', { workspaceId: id, boardName, visibility })
      closeForm()
      fetchData()
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create board')
    } finally {
      setCreating(false)
    }
  }

  const visibilityBadge = (v) => {
    if (v === 'PUBLIC') return 'badge-green'
    if (v === 'PRIVATE') return 'badge-gray'
    return 'badge-accent'
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-header-inner">
          <div className="flex items-center gap-3">
            <button onClick={() => navigate('/workspaces')} className="btn-header-ghost btn-sm -ml-2">
              &larr; Back
            </button>
            <div className="h-5 w-px bg-white/25" />
            <h1 className="text-lg font-semibold text-white">
              {workspace ? workspace.name : 'Workspace'}
            </h1>
          </div>
          <button onClick={() => setShowSettings(true)} className="btn-header-glass btn-sm">
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            Settings
          </button>
        </div>
      </header>

      <main className="mx-auto flex max-w-6xl gap-6 px-6 py-8">
        <div className="min-w-0 flex-1">
          <div className="mb-6 flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-ink">Boards</h2>
              <p className="text-sm text-slate-600">
                {workspace?.description || 'Boards in this workspace'}
              </p>
            </div>
            <button onClick={() => setShowForm(true)} className="btn-primary">
              <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              New Board
            </button>
          </div>

          {error && <div className="alert-error mb-4">{error}</div>}

          {loading ? (
            <p className="text-sm text-slate-600">Loading boards...</p>
          ) : boards.length === 0 ? (
            <div className="empty-state">No boards yet. Create one to get started.</div>
          ) : (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              {boards.map((board) => {
                const s = boardStats[board.boardId]
                return (
                  <div
                    key={board.boardId}
                    onClick={() => navigate(`/boards/${board.boardId}`)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        navigate(`/boards/${board.boardId}`)
                      }
                    }}
                    role="button"
                    tabIndex={0}
                    className="tile focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2"
                  >
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent-soft text-sm font-semibold text-accent">
                      {board.boardName?.[0]?.toUpperCase() || 'B'}
                    </div>
                    <h3 className="mt-3 text-base font-semibold text-ink">{board.boardName}</h3>
                    <span className={`mt-3 inline-block ${visibilityBadge(board.visibility)}`}>
                      {board.visibility}
                    </span>
                    <div className="mt-3 flex items-center gap-3 border-t border-slate-100 pt-3 text-xs text-muted">
                      <span>
                        <span className="font-semibold text-ink">{s ? s.memberCount : '—'}</span>{' '}
                        members
                      </span>
                      <span>
                        <span className="font-semibold text-ink">{s ? s.laneCount : '—'}</span>{' '}
                        lists
                      </span>
                      <span>
                        <span className="font-semibold text-ink">{s ? s.cardCount : '—'}</span>{' '}
                        tasks
                      </span>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </div>

        <aside className="hidden w-72 flex-none space-y-4 lg:block">
          <div className="surface-card sticky top-24 p-4">
            <h3 className="mb-3 text-sm font-semibold text-ink">Recent activity</h3>
            {activity.length === 0 ? (
              <p className="text-sm text-muted">No boards created yet.</p>
            ) : (
              <ul className="space-y-3">
                {activity.map((board) => (
                  <li key={board.boardId} className="flex gap-2.5">
                    <span className="mt-0.5 flex h-6 w-6 flex-none items-center justify-center rounded-full bg-accent-soft text-[10px] font-semibold text-accent">
                      {board.creatorName?.[0]?.toUpperCase() || '?'}
                    </span>
                    <p className="text-xs text-muted">
                      <span className="font-medium text-ink">{board.creatorName}</span> created{' '}
                      <span className="font-medium text-ink">{board.boardName}</span>
                      <br />
                      {timeAgo(board.createdAt)}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="surface-card p-4">
            <h3 className="mb-3 text-sm font-semibold text-ink">
              Members
              <span className="ml-1.5 font-normal text-muted">({members.length})</span>
            </h3>
            <MemberList members={members} currentUserId={currentUser?.id} />
          </div>
        </aside>
      </main>

      <Modal
        open={showForm}
        onClose={closeForm}
        title="Create board"
        footer={
          <>
            <button onClick={closeForm} className="btn-secondary">
              Cancel
            </button>
            <button
              type="submit"
              form="create-board-form"
              disabled={creating}
              className="btn-primary"
            >
              {creating ? 'Creating...' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-board-form" onSubmit={handleCreate} className="space-y-3">
          {formError && <div className="alert-error">{formError}</div>}
          <div>
            <label className="field-label">Board name</label>
            <input
              type="text"
              value={boardName}
              onChange={(e) => setBoardName(e.target.value)}
              required
              autoFocus
              className="field-input"
            />
          </div>
          <div>
            <label className="field-label">Visibility</label>
            <select
              value={visibility}
              onChange={(e) => setVisibility(e.target.value)}
              className="field-input"
            >
              {VISIBILITY_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
        </form>
      </Modal>

      <WorkspaceSettingsModal
        open={showSettings}
        onClose={() => setShowSettings(false)}
        workspaceId={id}
        boardCount={boards.length}
        onUpdated={(updated) => setWorkspace(updated)}
        onDeleted={() => navigate('/workspaces')}
      />
    </div>
  )
}

export default WorkspaceDetail
