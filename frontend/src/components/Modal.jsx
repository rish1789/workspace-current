import { useEffect, useId, useState } from 'react'
import { useFocusTrap } from '../hooks/useFocusTrap'

function Modal({
  open,
  onClose,
  title,
  children,
  footer,
  maxWidth = 'max-w-md',
  // Lets a caller with a dynamic (non-heading) title — e.g. CardModal's
  // ticket-id + status badge — supply the dialog's accessible name directly,
  // instead of aria-labelledby pointing at that same non-descriptive content.
  ariaLabel,
  // Most callers just want a right-aligned action row; CardModal needs a
  // split layout (archive/delete on the left, close on the right).
  footerClassName = 'flex justify-end gap-2.5 border-t border-slate-100 bg-slate-50/60 px-6 py-4',
}) {
  const [mounted, setMounted] = useState(open)
  const [visible, setVisible] = useState(false)
  const titleId = useId()
  const panelRef = useFocusTrap(open, onClose)

  // Mount and the start-of-close-transition are pure functions of `open`, so
  // they're set here during render rather than in the effect below — only
  // the parts that genuinely need a timer (waiting a frame to trigger the
  // CSS transition-in, waiting out the transition-out before unmounting)
  // belong in an effect.
  if (open && !mounted) {
    setMounted(true)
  }
  if (!open && visible) {
    setVisible(false)
  }

  useEffect(() => {
    if (open) {
      const raf = requestAnimationFrame(() => setVisible(true))
      return () => cancelAnimationFrame(raf)
    }
    const timeout = setTimeout(() => setMounted(false), 200)
    return () => clearTimeout(timeout)
  }, [open])

  if (!mounted) return null

  return (
    <div
      className={`modal-backdrop ${visible ? 'opacity-100' : 'opacity-0'}`}
      onClick={onClose}
    >
      <div
        ref={panelRef}
        role="dialog"
        aria-modal="true"
        {...(ariaLabel ? { 'aria-label': ariaLabel } : { 'aria-labelledby': titleId })}
        className={`modal-panel ${maxWidth} flex max-h-[85vh] flex-col ${
          visible ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-2 scale-95 opacity-0'
        }`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-accent-bar shrink-0" />

        <div className="flex shrink-0 items-center justify-between px-6 py-4">
          <h2 id={titleId} className="text-lg font-semibold text-ink">{title}</h2>
          <button
            onClick={onClose}
            aria-label="Close"
            className="flex h-9 w-9 items-center justify-center rounded-full text-muted transition hover:bg-slate-100 hover:text-slate-600"
          >
            &times;
          </button>
        </div>

        <div className="flex-1 overflow-y-auto border-t border-slate-100 px-6 py-5">{children}</div>

        {footer && <div className={`shrink-0 ${footerClassName}`}>{footer}</div>}
      </div>
    </div>
  )
}

export default Modal
