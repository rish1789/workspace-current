// Coordinates Escape between nested layers (e.g. a popover open inside a
// modal): only the most-recently-opened layer's handler runs, so Escape
// dismisses the popover first and the modal only on a second press.
const stack = []

if (typeof document !== 'undefined') {
  document.addEventListener('keydown', (e) => {
    if (e.key !== 'Escape') return
    const top = stack[stack.length - 1]
    top?.()
  })
}

export function pushEscapeHandler(onEscape) {
  stack.push(onEscape)
  return () => {
    const index = stack.lastIndexOf(onEscape)
    if (index !== -1) stack.splice(index, 1)
  }
}
