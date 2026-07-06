const ROLE_BADGE = {
  OWNER: 'badge-accent',
  ADMIN: 'badge-green',
  MEMBER: 'badge-gray',
  OBSERVER: 'badge-gray',
}

// Backend embeds username on each member now — no per-member lookup.
// renderActions is optional: (member) => node, for callers that let admins
// manage members (remove, change role, reassign admin/owner).
function MemberList({ members, currentUserId, renderActions }) {
  if (members.length === 0) return <p className="text-sm text-muted">No members found.</p>

  return (
    <ul className="divide-y divide-slate-100">
      {members.map((m) => (
        <li key={m.userId} className="flex items-center justify-between gap-3 py-2">
          <div className="flex min-w-0 items-center gap-2.5">
            <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-accent-soft text-xs font-semibold text-accent">
              {m.username?.[0]?.toUpperCase() || '?'}
            </span>
            <div className="min-w-0">
              <p className="truncate text-sm font-medium text-ink">
                {m.username}
                {m.userId === currentUserId && (
                  <span className="ml-1.5 text-xs font-normal text-muted">(you)</span>
                )}
              </p>
              <p className="text-xs text-muted">Joined {m.joinedAt}</p>
            </div>
          </div>
          <div className="flex shrink-0 items-center gap-2">
            <span className={ROLE_BADGE[m.role] || 'badge-gray'}>{m.role}</span>
            {renderActions && renderActions(m)}
          </div>
        </li>
      ))}
    </ul>
  )
}

export default MemberList
