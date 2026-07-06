import { useEffect, useLayoutEffect, useRef } from 'react'
import { pushEscapeHandler } from './escapeStack'

const FOCUSABLE_SELECTOR =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])'

// Traps Tab focus within the returned ref while `active`, moves focus into the
// container on activation, restores it to the previously-focused element on
// deactivation, and calls `onEscape` on the Escape key. Escape is routed
// through a shared stack so a nested layer (e.g. a popover opened inside a
// modal) dismisses first, rather than every active trap reacting at once.
export function useFocusTrap(active, onEscape) {
  const containerRef = useRef(null)
  // Callers routinely pass an inline `() => setX(false)`, a fresh reference
  // every render. Reading through a ref (instead of listing onEscape as a
  // dependency) keeps the effect's setup/teardown tied to `active` alone —
  // otherwise a parent re-render mid-open tears the trap down and its
  // cleanup restores focus to whatever was focused before that render,
  // fighting the focus-into-container logic below on every unrelated render.
  const onEscapeRef = useRef(onEscape)
  useLayoutEffect(() => {
    onEscapeRef.current = onEscape
  })

  useEffect(() => {
    if (!active) return

    const previouslyFocused = document.activeElement
    const container = containerRef.current
    const getFocusable = () =>
      Array.from(container?.querySelectorAll(FOCUSABLE_SELECTOR) || [])

    if (!container?.contains(document.activeElement)) {
      getFocusable()[0]?.focus()
    }

    const handleTabKey = (e) => {
      if (e.key !== 'Tab') return
      const items = getFocusable()
      if (items.length === 0) return
      const first = items[0]
      const last = items[items.length - 1]
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault()
        last.focus()
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleTabKey)
    const popEscapeHandler = pushEscapeHandler(() => onEscapeRef.current?.())
    return () => {
      document.removeEventListener('keydown', handleTabKey)
      popEscapeHandler()
      if (previouslyFocused instanceof HTMLElement) previouslyFocused.focus()
    }
  }, [active])

  return containerRef
}
