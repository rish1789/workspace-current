// Backend timestamps are formatted as "dd-MM-yyyy HH:mm:ss", which the
// native Date constructor can't parse directly.
export function parseBackendDate(str) {
  if (!str) return null
  const [datePart, timePart] = str.split(' ')
  const [day, month, year] = datePart.split('-').map(Number)
  const [hour = 0, minute = 0, second = 0] = (timePart || '').split(':').map(Number)
  return new Date(year, month - 1, day, hour, minute, second)
}

export function timeAgo(str) {
  const date = parseBackendDate(str)
  if (!date || Number.isNaN(date.getTime())) return ''

  const diffSec = Math.floor((Date.now() - date.getTime()) / 1000)
  if (diffSec < 5) return 'just now'
  if (diffSec < 60) return `${diffSec}s ago`

  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin}m ago`

  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}h ago`

  const diffDay = Math.floor(diffHour / 24)
  if (diffDay < 30) return `${diffDay}d ago`

  const diffMonth = Math.floor(diffDay / 30)
  if (diffMonth < 12) return `${diffMonth}mo ago`

  const diffYear = Math.floor(diffMonth / 12)
  return `${diffYear}y ago`
}
