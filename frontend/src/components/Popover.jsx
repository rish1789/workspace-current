import { useEffect, useLayoutEffect, useState } from 'react'
import { createPortal } from 'react-dom'
import { useFocusTrap } from '../hooks/useFocusTrap'

// Portals `children` to document.body and fixed-positions them against
// `anchorRef`, so a dropdown escapes any scrolling/overflow ancestor (e.g. a
// modal body) instead of being clipped by it. Reuses useFocusTrap for its own
// panel so Tab stays scoped to the popover and Escape dismisses it first,
// ahead of any modal it's nested inside. Also closes on outside click.
//
// Position starts at a default (0, 0) and is corrected in a layout effect —
// that runs before paint, so there's no visible flash at the wrong spot.
// Deliberately not `visibility: hidden` in the meantime: useFocusTrap's own
// effect tries to focus the panel's first item as soon as it mounts, and a
// hidden element can't receive focus, which would make that call a silent
// no-op.
function Popover({ open, onClose, anchorRef, className = '', children }) {
  const panelRef = useFocusTrap(open, onClose)
  const [style, setStyle] = useState({ top: 0, left: 0 })

  useLayoutEffect(() => {
    if (!open || !anchorRef.current) return
    const updatePosition = () => {
      const rect = anchorRef.current.getBoundingClientRect()
      setStyle({ top: rect.bottom + 4, left: rect.left })
    }
    updatePosition()
    window.addEventListener('resize', updatePosition)
    window.addEventListener('scroll', updatePosition, true)
    return () => {
      window.removeEventListener('resize', updatePosition)
      window.removeEventListener('scroll', updatePosition, true)
    }
  }, [open, anchorRef])

  useEffect(() => {
    if (!open) return
    const handlePointerDown = (e) => {
      if (panelRef.current?.contains(e.target) || anchorRef.current?.contains(e.target)) return
      onClose()
    }
    document.addEventListener('mousedown', handlePointerDown)
    return () => document.removeEventListener('mousedown', handlePointerDown)
  }, [open, onClose, anchorRef, panelRef])

  if (!open) return null

  return createPortal(
    <div
      ref={panelRef}
      style={{ position: 'fixed', top: style.top, left: style.left }}
      className={`z-[var(--z-popover)] ${className}`}
    >
      {children}
    </div>,
    document.body
  )
}

export default Popover
