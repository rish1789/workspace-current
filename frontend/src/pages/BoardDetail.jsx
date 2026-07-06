import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  pointerWithin,
  useDroppable,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  horizontalListSortingStrategy,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import api from '../api/axios'
import CardModal from '../components/CardModal'
import LabelsModal from '../components/LabelsModal'
import Modal from '../components/Modal'
import BoardSettingsModal from '../components/BoardSettingsModal'
import LaneSettingsModal from '../components/LaneSettingsModal'
import { getBoard, getCardsByLane, moveCard } from '../api/boardService'
import { getLanesByBoard, moveLane } from '../api/laneService'
import { readableTextColor } from '../utils/color'

// Neutral accent for cards with no label to color-code by.
const DEFAULT_CARD_ACCENT = '#cbd5e1'

// Card ids and lane ids share one DndContext, so sortable/droppable ids are
// namespaced ("card:1", "lane:1", "lanedrop:1") to stay unique across both.
const cardDndId = (cardId) => `card:${cardId}`
const laneDndId = (laneId) => `lane:${laneId}`
const laneDropDndId = (laneId) => `lanedrop:${laneId}`

// A lane's own sortable rect (the whole column, so the whole thing slides
// during reorder) physically overlaps its card-drop-zone rect (the area
// below the header). Left to plain closestCenter, that overlap makes `over`
// resolve to whichever rect is geometrically closer, not whichever makes
// sense for what's being dragged — so a card dropped into an empty lane can
// resolve to the lane's own "lane:" id instead of its "lanedrop:" id and
// silently no-op. Filtering candidates by drag type removes the ambiguity:
// dragging a card never considers "lane:" targets, dragging a lane only
// considers "lane:" targets.
function collisionDetectionStrategy(args) {
  const activeId = String(args.active.id)
  const isLaneDrag = activeId.startsWith('lane:')
  const filteredContainers = args.droppableContainers.filter((container) => {
    const id = String(container.id)
    // A sortable item is also registered as its own droppable target (so
    // siblings can detect "hovering over me" to reorder) — left in, it can
    // win the closest-match trivially and `over` collapses back to the
    // dragged item itself no matter where the pointer actually is.
    if (id === activeId) return false
    return isLaneDrag ? id.startsWith('lane:') : !id.startsWith('lane:')
  })
  const filteredArgs = { ...args, droppableContainers: filteredContainers }

  // closestCenter compares centroid-to-centroid, but a mostly-empty lane's
  // card-drop-zone stretches to fill the column's full height — its centroid
  // can sit far below where the pointer visually is, so it loses to a card
  // still sitting in the *source* lane. pointerWithin (literal pointer-inside-
  // rect containment) fixes that; closestCenter stays as the fallback for
  // gaps between lanes where nothing literally contains the pointer.
  const pointerCollisions = pointerWithin(filteredArgs)
  if (pointerCollisions.length > 0) {
    const cardHit = pointerCollisions.find((c) => String(c.id).startsWith('card:'))
    return cardHit ? [cardHit] : [pointerCollisions[0]]
  }
  return closestCenter(filteredArgs)
}

function CardFaceContent({ card }) {
  const labels = card.labels || []
  // Backend's Card.getDueDate() returns the literal string "No Date" instead of null when unset.
  const hasDueDate = card.dueDate && card.dueDate !== 'No Date'
  const hasAssignees = Array.isArray(card.assignedUserIds) && card.assignedUserIds.length > 0
  // Not part of the card-list response today — rendered only if the data is
  // actually present, so this keeps working untouched if it's added later.
  const hasChecklistProgress =
    typeof card.checklistDone === 'number' && typeof card.checklistTotal === 'number'
  const hasCommentCount = typeof card.commentCount === 'number'
  const hasMetaRow = hasDueDate || hasChecklistProgress || hasCommentCount || hasAssignees
  // A card can carry several labels (CardLabel is a one-to-many join), so the
  // accent bar splits into one stripe per label rather than only showing the
  // first. Falls back to a single neutral segment when there's no label.
  const accentSegments = labels.length > 0 ? labels : [{ labelId: 'default', color: DEFAULT_CARD_ACCENT }]

  return (
    <>
      <div className="absolute inset-y-0 left-0 flex w-[3px] flex-col" aria-hidden="true">
        {accentSegments.map((l) => (
          <span key={l.labelId} className="flex-1" style={{ backgroundColor: l.color }} />
        ))}
      </div>

      <div className="py-3 pl-4 pr-3">
        <div className="flex items-center justify-between">
          <p className="text-[11px] font-medium text-muted">{card.fullId}</p>
          {card.archived && <span className="badge-gray">Archived</span>}
        </div>
        <p className="mt-2 text-sm font-medium leading-snug text-ink">{card.title}</p>

        {labels.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1">
            {labels.map((l) => (
              <span
                key={l.labelId}
                className="label-pill"
                style={{ backgroundColor: l.color, color: readableTextColor(l.color) }}
              >
                {l.labelName}
              </span>
            ))}
          </div>
        )}

        {hasMetaRow && (
          <div className="mt-3 flex items-center gap-2.5 text-[12px] text-muted">
            {hasDueDate && (
              <span className="inline-flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3M16 7V3M4 11h16M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {card.dueDate}
              </span>
            )}
            {hasChecklistProgress && (
              <span className="inline-flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {card.checklistDone}/{card.checklistTotal}
              </span>
            )}
            {hasCommentCount && (
              <span className="inline-flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                {card.commentCount}
              </span>
            )}
            {hasAssignees && (
              <span className="ml-auto flex -space-x-1.5">
                {card.assignedUserIds.slice(0, 3).map((uid) => (
                  <span
                    key={uid}
                    className="flex h-5 w-5 items-center justify-center rounded-full border-2 border-white bg-accent-soft text-[10px] font-semibold text-accent"
                  >
                    {String(uid).slice(-1)}
                  </span>
                ))}
              </span>
            )}
          </div>
        )}
      </div>
    </>
  )
}

function CardFace({ card, onClick }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: cardDndId(card.id),
  })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  return (
    <button
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      type="button"
      onClick={onClick}
      className={`group relative block w-full overflow-hidden rounded-lg border border-slate-200 bg-white text-left shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-1 ${
        card.archived ? 'opacity-60' : ''
      } ${isDragging ? 'opacity-40' : ''}`}
    >
      <CardFaceContent card={card} />
    </button>
  )
}

// Mirrors CardFace's shape so the swap to real cards doesn't shift layout.
function CardFaceSkeleton() {
  return (
    <div className="relative overflow-hidden rounded-lg border border-slate-200 bg-white py-3 pl-4 pr-3">
      <span className="absolute inset-y-0 left-0 w-[3px] bg-slate-200" aria-hidden="true" />
      <div className="h-2.5 w-14 animate-pulse rounded bg-slate-200" />
      <div className="mt-2.5 h-3.5 w-4/5 animate-pulse rounded bg-slate-200" />
      <div className="mt-1.5 h-3.5 w-1/2 animate-pulse rounded bg-slate-200" />
    </div>
  )
}

// Mirrors a loaded Lane column's shape for the board's first load.
function LaneSkeleton() {
  return (
    <div className="flex h-full w-[300px] flex-none flex-col gap-2.5">
      <div className="flex items-center justify-between border-b border-slate-300/70 px-0.5 pb-2.5">
        <div className="h-3.5 w-20 animate-pulse rounded bg-slate-200" />
        <div className="h-5 w-6 animate-pulse rounded-full bg-slate-200" />
      </div>
      <CardFaceSkeleton />
      <CardFaceSkeleton />
    </div>
  )
}

function Lane({ lane, cards, loading, error, onCardClick, onOpenSettings, onCardCreated }) {
  const [showArchivedCards, setShowArchivedCards] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [cardTitle, setCardTitle] = useState('')
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  const { setNodeRef, attributes, listeners, transform, transition, isDragging } = useSortable({
    id: laneDndId(lane.id),
  })
  const laneStyle = {
    transform: CSS.Transform.toString(transform),
    transition,
  }

  const { setNodeRef: setDropRef } = useDroppable({ id: laneDropDndId(lane.id) })

  const archivedCards = cards.filter((c) => c.archived)
  const visibleCards = showArchivedCards ? cards : cards.filter((c) => !c.archived)

  const handleCreateCard = async (e) => {
    e.preventDefault()
    setCreating(true)
    setCreateError('')
    try {
      await api.post('/api/cards', {
        laneId: lane.id,
        title: cardTitle,
        position: cards.length,
      })
      setCardTitle('')
      setShowForm(false)
      onCardCreated(lane.id)
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create card')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div
      ref={setNodeRef}
      style={laneStyle}
      className={`flex h-full w-[300px] flex-none flex-col ${lane.archived ? 'opacity-60' : ''} ${
        isDragging ? 'opacity-50' : ''
      }`}
    >
      <div className="mb-3 flex flex-none items-center justify-between border-b border-slate-300/70 px-0.5 pb-2.5">
        <div className="flex items-center gap-1.5">
          <button
            {...attributes}
            {...listeners}
            aria-label={`Reorder ${lane.name} lane`}
            className="flex h-6 w-5 flex-none cursor-grab items-center justify-center rounded text-slate-400 transition hover:bg-slate-200/70 hover:text-slate-600 active:cursor-grabbing"
          >
            <svg className="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 16 16">
              <circle cx="5" cy="4" r="1.3" />
              <circle cx="11" cy="4" r="1.3" />
              <circle cx="5" cy="8" r="1.3" />
              <circle cx="11" cy="8" r="1.3" />
              <circle cx="5" cy="12" r="1.3" />
              <circle cx="11" cy="12" r="1.3" />
            </svg>
          </button>
          <h3 className="text-sm font-semibold text-ink">
            {lane.name}
          </h3>
          {lane.archived && <span className="badge-gray">Archived</span>}
        </div>
        <div className="flex items-center gap-1.5">
          {archivedCards.length > 0 && (
            <button
              onClick={() => setShowArchivedCards((v) => !v)}
              className="text-[11px] font-medium text-slate-600 hover:text-ink"
              title={`${archivedCards.length} archived card(s)`}
            >
              {showArchivedCards ? 'hide' : 'show'} archived ({archivedCards.length})
            </button>
          )}
          <span className="inline-flex h-5 min-w-[20px] items-center justify-center rounded-full border border-slate-300 px-1.5 text-[11px] font-medium text-slate-600">
            {visibleCards.length}
          </span>
          <div className="h-4 w-px bg-slate-300/70" />
          <button
            onClick={() => onOpenSettings(lane)}
            className="flex h-7 w-7 items-center justify-center rounded-md text-slate-400 transition hover:bg-slate-200/70 hover:text-ink"
            title="Lane settings"
            aria-label="Lane settings"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </button>
        </div>
      </div>

      <div ref={setDropRef} className="min-h-0 flex-1 space-y-2.5 overflow-y-auto px-0.5 pb-1">
        {loading ? (
          <div role="status" aria-live="polite" className="space-y-2.5">
            <span className="sr-only">Loading cards…</span>
            <CardFaceSkeleton />
            <CardFaceSkeleton />
          </div>
        ) : (
          <>
            <SortableContext
              items={visibleCards.map((c) => cardDndId(c.id))}
              strategy={verticalListSortingStrategy}
            >
              {visibleCards.length === 0 ? (
                <div className="rounded-lg border border-dashed border-slate-300 py-7 text-center text-xs text-slate-600">
                  No cards yet
                </div>
              ) : (
                visibleCards.map((card) => (
                  <CardFace key={card.id} card={card} onClick={() => onCardClick(card.id)} />
                ))
              )}
            </SortableContext>

            {(error || createError) && (
              <p className="px-1 text-sm text-red-600">{error || createError}</p>
            )}

            {showForm ? (
              <form onSubmit={handleCreateCard}>
                <input
                  type="text"
                  placeholder="Card title"
                  value={cardTitle}
                  onChange={(e) => setCardTitle(e.target.value)}
                  required
                  autoFocus
                  className="field-input"
                />
                <div className="mt-2 flex gap-2">
                  <button type="submit" disabled={creating} className="btn-primary btn-sm">
                    {creating ? 'Adding...' : 'Add card'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowForm(false)}
                    className="btn-ghost btn-sm"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <button
                onClick={() => setShowForm(true)}
                className="flex w-full items-center gap-1.5 px-1.5 py-1.5 text-left text-sm text-slate-600 transition hover:text-ink"
              >
                <span className="text-base leading-none">+</span> Add a card
              </button>
            )}
          </>
        )}
      </div>
    </div>
  )
}

function BoardDetail() {
  const { id } = useParams()
  const navigate = useNavigate()

  const [board, setBoard] = useState(null)
  const [lanes, setLanes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [cardsByLane, setCardsByLane] = useState({})
  const [cardsLoading, setCardsLoading] = useState(true)
  const [cardsError, setCardsError] = useState('')
  // Only the very first load shows per-lane skeletons; background refetches
  // (e.g. after closing a card's modal) keep existing cards mounted so open
  // dropdowns/keyboard focus on a card aren't yanked away mid-session.
  const hasLoadedCardsOnce = useRef(false)

  const [showLaneForm, setShowLaneForm] = useState(false)
  const [laneName, setLaneName] = useState('')
  const [creatingLane, setCreatingLane] = useState(false)

  const [selectedCardId, setSelectedCardId] = useState(null)
  const [showLabelsModal, setShowLabelsModal] = useState(false)
  const [showBoardSettings, setShowBoardSettings] = useState(false)
  const [settingsLane, setSettingsLane] = useState(null)
  const [showArchivedLanes, setShowArchivedLanes] = useState(false)
  const [boardRefreshKey, setBoardRefreshKey] = useState(0)
  const [activeDragItem, setActiveDragItem] = useState(null)

  const archivedLanes = lanes.filter((lane) => lane.archived)
  const visibleLanes = showArchivedLanes ? lanes : lanes.filter((lane) => !lane.archived)

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  )

  const fetchData = async () => {
    setLoading(true)
    setError('')
    try {
      const [boardData, sortedLanes] = await Promise.all([
        getBoard(id),
        getLanesByBoard(id),
      ])
      setBoard(boardData)
      setLanes(sortedLanes)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load lanes')
    } finally {
      setLoading(false)
    }
  }

  const fetchAllCards = async (laneList) => {
    if (!hasLoadedCardsOnce.current) setCardsLoading(true)
    setCardsError('')
    try {
      const entries = await Promise.all(
        laneList.map(async (lane) => [lane.id, await getCardsByLane(lane.id)])
      )
      setCardsByLane(Object.fromEntries(entries))
    } catch (err) {
      setCardsError(err.response?.data?.message || 'Failed to load cards')
    } finally {
      setCardsLoading(false)
      hasLoadedCardsOnce.current = true
    }
  }

  const refreshLaneCards = async (laneId) => {
    try {
      const laneCards = await getCardsByLane(laneId)
      setCardsByLane((prev) => ({ ...prev, [laneId]: laneCards }))
    } catch {
      // A stale list here just means the next full refresh catches it up —
      // not worth a second error banner alongside the create-card one.
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchData()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    // Guarded on `loading` so this doesn't fire once with the initial empty
    // `lanes` array before fetchData's real result lands — that would mark
    // hasLoadedCardsOnce true early and skip the skeleton on the real first load.
    if (loading) return
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchAllCards(lanes)
  }, [loading, lanes, boardRefreshKey])

  const closeLaneForm = () => {
    setShowLaneForm(false)
    setLaneName('')
  }

  const handleCreateLane = async (e) => {
    e.preventDefault()
    setCreatingLane(true)
    try {
      await api.post('/api/lanes', { boardId: id, laneName, position: lanes.length })
      closeLaneForm()
      fetchData()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create lane')
    } finally {
      setCreatingLane(false)
    }
  }

  const handleCardModalClose = () => {
    setSelectedCardId(null)
    setBoardRefreshKey((v) => v + 1)
  }

  const handleDragStart = (event) => {
    const activeId = String(event.active.id)
    if (activeId.startsWith('lane:')) {
      const laneId = Number(activeId.slice(5))
      const lane = lanes.find((l) => l.id === laneId)
      setActiveDragItem(lane ? { type: 'lane', lane } : null)
    } else if (activeId.startsWith('card:')) {
      const cardId = Number(activeId.slice(5))
      for (const laneCards of Object.values(cardsByLane)) {
        const card = laneCards.find((c) => c.id === cardId)
        if (card) {
          setActiveDragItem({ type: 'card', card })
          break
        }
      }
    }
  }

  const handleDragEnd = (event) => {
    setActiveDragItem(null)
    const { active, over } = event
    if (!over) return
    const activeId = String(active.id)
    const overId = String(over.id)
    if (activeId === overId) return

    if (activeId.startsWith('lane:')) {
      if (!overId.startsWith('lane:')) return
      const activeLaneId = Number(activeId.slice(5))
      const overLaneId = Number(overId.slice(5))
      const oldIndex = visibleLanes.findIndex((l) => l.id === activeLaneId)
      const newIndex = visibleLanes.findIndex((l) => l.id === overLaneId)
      if (oldIndex === -1 || newIndex === -1 || oldIndex === newIndex) return

      const previousLanes = lanes
      const reorderedVisible = arrayMove(visibleLanes, oldIndex, newIndex)
      // Archived lanes are hidden from this reorder — splice the reordered
      // visible lanes back into their existing slots rather than the archived ones.
      let cursor = 0
      const nextLanes = lanes.map((lane) => (lane.archived ? lane : reorderedVisible[cursor++]))
      setLanes(nextLanes)
      moveLane(activeLaneId, newIndex).catch(() => setLanes(previousLanes))
      return
    }

    if (activeId.startsWith('card:')) {
      const cardId = Number(activeId.slice(5))
      let sourceLaneId = null
      for (const [laneId, laneCards] of Object.entries(cardsByLane)) {
        if (laneCards.some((c) => c.id === cardId)) {
          sourceLaneId = Number(laneId)
          break
        }
      }
      if (sourceLaneId === null) return

      let targetLaneId = null
      let targetIndex = 0
      if (overId.startsWith('card:')) {
        const overCardId = Number(overId.slice(5))
        for (const [laneId, laneCards] of Object.entries(cardsByLane)) {
          const idx = laneCards.findIndex((c) => c.id === overCardId)
          if (idx !== -1) {
            targetLaneId = Number(laneId)
            targetIndex = idx
            break
          }
        }
      } else if (overId.startsWith('lanedrop:')) {
        targetLaneId = Number(overId.slice(9))
        targetIndex = (cardsByLane[targetLaneId] || []).length
      } else {
        return
      }
      if (targetLaneId === null) return

      const sourceCards = cardsByLane[sourceLaneId] || []
      const targetCards = cardsByLane[targetLaneId] || []

      if (sourceLaneId === targetLaneId) {
        const oldIndex = sourceCards.findIndex((c) => c.id === cardId)
        if (oldIndex === -1 || oldIndex === targetIndex) return
        const reordered = arrayMove(sourceCards, oldIndex, targetIndex)
        setCardsByLane((prev) => ({ ...prev, [sourceLaneId]: reordered }))
        moveCard(cardId, targetLaneId, targetIndex).catch(() => {
          setCardsByLane((prev) => ({ ...prev, [sourceLaneId]: sourceCards }))
        })
      } else {
        const movingCard = sourceCards.find((c) => c.id === cardId)
        if (!movingCard) return
        const nextSource = sourceCards.filter((c) => c.id !== cardId)
        const nextTarget = [...targetCards]
        nextTarget.splice(targetIndex, 0, movingCard)
        setCardsByLane((prev) => ({
          ...prev,
          [sourceLaneId]: nextSource,
          [targetLaneId]: nextTarget,
        }))
        moveCard(cardId, targetLaneId, targetIndex).catch(() => {
          setCardsByLane((prev) => ({
            ...prev,
            [sourceLaneId]: sourceCards,
            [targetLaneId]: targetCards,
          }))
        })
      }
    }
  }

  return (
    <div className="flex h-screen flex-col overflow-hidden bg-app-bg">
      <header className="app-header flex-none">
        <div className="app-header-inner h-auto min-h-[68px] flex-wrap gap-y-2 px-3 py-2 sm:flex-nowrap sm:px-6 sm:py-0">
          <div className="flex min-w-0 items-center gap-2 sm:gap-3">
            <button
              onClick={() => navigate(board ? `/workspaces/${board.workspaceId}` : -1)}
              aria-label="Back"
              className="btn-header-ghost btn-sm -ml-2 flex-none"
            >
              &larr; <span className="sr-only sm:not-sr-only">Back</span>
            </button>
            <div className="hidden h-5 w-px flex-none bg-white/25 sm:block" />
            <h1 className="min-w-0 truncate text-lg font-semibold text-white">
              {board ? board.boardName : 'Board'}
            </h1>
            {board && (
              <span className="badge-header hidden flex-none sm:inline-flex">{board.visibility}</span>
            )}
          </div>
          <div className="flex flex-none items-center gap-1.5 sm:gap-2.5">
            <button
              onClick={() => setShowBoardSettings(true)}
              aria-label="Settings"
              className="btn-header-glass btn-sm"
            >
              <svg className="h-4 w-4 flex-none" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span className="sr-only sm:not-sr-only">Settings</span>
            </button>
            <button
              onClick={() => setShowLabelsModal(true)}
              aria-label="Labels"
              className="btn-header-glass btn-sm"
            >
              <svg className="h-4 w-4 flex-none" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M7 7h.01M7 3h5.586a1 1 0 01.707.293l6.414 6.414a1 1 0 010 1.414l-8.586 8.586a1 1 0 01-1.414 0L3.293 13.293A1 1 0 013 12.586V7a4 4 0 014-4z" />
              </svg>
              <span className="sr-only sm:not-sr-only">Labels</span>
            </button>
            {archivedLanes.length > 0 && (
              <button
                onClick={() => setShowArchivedLanes((v) => !v)}
                aria-label={`${showArchivedLanes ? 'Hide' : 'Show'} archived lanes (${archivedLanes.length})`}
                className="btn-header-ghost btn-sm"
              >
                <svg className="h-4 w-4 flex-none" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
                </svg>
                <span className="sr-only sm:not-sr-only">
                  {showArchivedLanes ? 'Hide' : 'Show'} archived{' '}
                </span>
                ({archivedLanes.length})
              </button>
            )}
            <button onClick={() => setShowLaneForm(true)} className="btn-header-cta btn-sm">
              <svg className="h-4 w-4 flex-none" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
              </svg>
              <span className="sr-only sm:not-sr-only">New </span>Lane
            </button>
          </div>
        </div>
      </header>

      <DndContext
        sensors={sensors}
        collisionDetection={collisionDetectionStrategy}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <main className="min-h-0 flex-1 overflow-x-auto overflow-y-hidden bg-board-bg px-6 py-5">
          {error && <div className="alert-error mb-4 max-w-md">{error}</div>}

          {loading ? (
            <div role="status" aria-live="polite" className="flex h-full flex-nowrap items-start gap-5">
              <span className="sr-only">Loading board…</span>
              <LaneSkeleton />
              <LaneSkeleton />
              <LaneSkeleton />
            </div>
          ) : visibleLanes.length === 0 ? (
            <div className="empty-state max-w-md">No lanes yet. Create one to get started.</div>
          ) : (
            <SortableContext
              items={visibleLanes.map((l) => laneDndId(l.id))}
              strategy={horizontalListSortingStrategy}
            >
              <div className="flex h-full flex-nowrap items-start gap-5">
                {visibleLanes.map((lane) => (
                  <Lane
                    key={lane.id}
                    lane={lane}
                    cards={cardsByLane[lane.id] || []}
                    loading={cardsLoading}
                    error={cardsError}
                    onCardClick={setSelectedCardId}
                    onOpenSettings={setSettingsLane}
                    onCardCreated={refreshLaneCards}
                  />
                ))}
              </div>
            </SortableContext>
          )}
        </main>

        <DragOverlay>
          {activeDragItem?.type === 'card' && (
            <div className="relative w-[284px] overflow-hidden rounded-lg border border-slate-300 bg-white text-left shadow-xl">
              <CardFaceContent card={activeDragItem.card} />
            </div>
          )}
          {activeDragItem?.type === 'lane' && (
            <div className="w-[300px] rounded-lg border border-slate-300 bg-white/95 px-3 py-2 text-sm font-semibold text-ink shadow-xl">
              {activeDragItem.lane.name}
            </div>
          )}
        </DragOverlay>
      </DndContext>

      {selectedCardId && (
        <CardModal
          cardId={selectedCardId}
          boardId={board?.boardId}
          onClose={handleCardModalClose}
        />
      )}

      <LabelsModal
        open={showLabelsModal}
        onClose={() => setShowLabelsModal(false)}
        boardId={board?.boardId}
        onLabelsChanged={() => setBoardRefreshKey((v) => v + 1)}
      />

      <Modal
        open={showLaneForm}
        onClose={closeLaneForm}
        title="Create lane"
        maxWidth="max-w-sm"
        footer={
          <>
            <button onClick={closeLaneForm} className="btn-secondary">
              Cancel
            </button>
            <button
              type="submit"
              form="create-lane-form"
              disabled={creatingLane}
              className="btn-primary"
            >
              {creatingLane ? 'Creating...' : 'Create'}
            </button>
          </>
        }
      >
        <form id="create-lane-form" onSubmit={handleCreateLane}>
          <label className="field-label">Lane name</label>
          <input
            type="text"
            placeholder="e.g. To Do"
            value={laneName}
            onChange={(e) => setLaneName(e.target.value)}
            required
            autoFocus
            className="field-input"
          />
        </form>
      </Modal>

      <BoardSettingsModal
        open={showBoardSettings}
        onClose={() => setShowBoardSettings(false)}
        boardId={board?.boardId}
        laneCount={visibleLanes.length}
        onUpdated={(updated) => setBoard(updated)}
        onDeleted={() => navigate(board ? `/workspaces/${board.workspaceId}` : '/workspaces')}
      />

      <LaneSettingsModal
        key={settingsLane?.id ?? 'none'}
        open={!!settingsLane}
        onClose={() => setSettingsLane(null)}
        lane={settingsLane}
        laneCount={lanes.length}
        onUpdated={() => {
          fetchData()
          setSettingsLane(null)
        }}
        onDeleted={fetchData}
      />
    </div>
  )
}

export default BoardDetail
