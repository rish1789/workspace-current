import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  deleteAccount,
  getCurrentUser,
  updateEmail,
  updatePassword,
  updateUsername,
  verifyPassword,
} from '../api/userService'

const EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
const PASSWORD_PATTERN =
  /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=[\]{}|;:,.<>?]).{8,20}$/
const PASSWORD_HINT =
  '8-20 characters, with an uppercase letter, a lowercase letter, a digit, and a special character.'

// Both the shared `api` instance (401/403 → forced logout) and the backend's
// GlobalExceptionHandler (returns the error as a raw string body, not
// {message}) make the usual `err.response?.data?.message` pattern useless
// here — this reads the real message so users see "Email is already in use"
// instead of a generic fallback.
function extractMessage(err, fallback) {
  return (typeof err.response?.data === 'string' && err.response.data) || fallback
}

function Account() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  const [usernameInput, setUsernameInput] = useState('')
  const [usernameSaving, setUsernameSaving] = useState(false)
  const [usernameError, setUsernameError] = useState('')
  const [usernameSuccess, setUsernameSuccess] = useState('')

  const [newEmail, setNewEmail] = useState('')
  const [emailPassword, setEmailPassword] = useState('')
  const [emailSaving, setEmailSaving] = useState(false)
  const [emailError, setEmailError] = useState('')
  const [emailSuccess, setEmailSuccess] = useState('')

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [passwordSaving, setPasswordSaving] = useState(false)
  const [passwordError, setPasswordError] = useState('')
  const [passwordSuccess, setPasswordSuccess] = useState('')

  const [showDeleteForm, setShowDeleteForm] = useState(false)
  const [deletePassword, setDeletePassword] = useState('')
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState('')

  const fetchProfile = async () => {
    setLoading(true)
    setLoadError('')
    try {
      const data = await getCurrentUser()
      setProfile(data)
      setUsernameInput(data.username || '')
    } catch (err) {
      setLoadError(extractMessage(err, 'Failed to load your profile'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchProfile()
  }, [])

  const handleUpdateUsername = async (e) => {
    e.preventDefault()
    if (!usernameInput.trim() || usernameInput === profile.username) return
    setUsernameSaving(true)
    setUsernameError('')
    setUsernameSuccess('')
    try {
      const updated = await updateUsername(profile.id, usernameInput.trim())
      setProfile(updated)
      setUsernameSuccess('Username updated.')
    } catch (err) {
      setUsernameError(extractMessage(err, 'Failed to update username'))
    } finally {
      setUsernameSaving(false)
    }
  }

  const handleUpdateEmail = async (e) => {
    e.preventDefault()
    setEmailError('')
    setEmailSuccess('')
    if (!EMAIL_PATTERN.test(newEmail)) {
      setEmailError('Enter a valid email address.')
      return
    }
    if (newEmail === profile.email) {
      setEmailError('That is already your current email.')
      return
    }
    if (!emailPassword) {
      setEmailError('Enter your current password to confirm this change.')
      return
    }
    setEmailSaving(true)
    try {
      // Verify identity first — updateEmail itself has no password check.
      await verifyPassword(profile.email, emailPassword)
    } catch {
      setEmailError('Current password is incorrect.')
      setEmailSaving(false)
      return
    }
    try {
      const updated = await updateEmail(profile.id, newEmail)
      // The JWT's subject is the email, so the old token is now orphaned —
      // mint a fresh one against the new email before anything else runs.
      const { token } = await verifyPassword(newEmail, emailPassword)
      localStorage.setItem('token', token)
      setProfile(updated)
      setNewEmail('')
      setEmailPassword('')
      setEmailSuccess('Email updated. Your session has been refreshed.')
    } catch (err) {
      setEmailError(extractMessage(err, 'Failed to update email'))
    } finally {
      setEmailSaving(false)
    }
  }

  const handleUpdatePassword = async (e) => {
    e.preventDefault()
    setPasswordError('')
    setPasswordSuccess('')
    if (!currentPassword) {
      setPasswordError('Enter your current password.')
      return
    }
    if (!PASSWORD_PATTERN.test(newPassword)) {
      setPasswordError(`New password doesn't meet the requirements. ${PASSWORD_HINT}`)
      return
    }
    if (newPassword !== confirmPassword) {
      setPasswordError('New password and confirmation do not match.')
      return
    }
    setPasswordSaving(true)
    try {
      await verifyPassword(profile.email, currentPassword)
    } catch {
      setPasswordError('Current password is incorrect.')
      setPasswordSaving(false)
      return
    }
    try {
      await updatePassword(profile.id, newPassword)
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setPasswordSuccess('Password updated. Your current session stays signed in.')
    } catch (err) {
      setPasswordError(extractMessage(err, 'Failed to update password'))
    } finally {
      setPasswordSaving(false)
    }
  }

  const handleDeleteAccount = async (e) => {
    e.preventDefault()
    setDeleteError('')
    if (!deletePassword) {
      setDeleteError('Enter your password to confirm account deletion.')
      return
    }
    setDeleting(true)
    try {
      await verifyPassword(profile.email, deletePassword)
    } catch {
      setDeleteError('Password is incorrect.')
      setDeleting(false)
      return
    }
    try {
      await deleteAccount(profile.id)
      localStorage.removeItem('token')
      navigate('/login')
    } catch (err) {
      setDeleteError(extractMessage(err, 'Failed to delete account'))
      setDeleting(false)
    }
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
            <h1 className="text-lg font-semibold text-white">Account settings</h1>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-2xl px-6 py-8">
        {loading ? (
          <p className="text-sm text-slate-600">Loading your profile...</p>
        ) : loadError ? (
          <div className="alert-error">{loadError}</div>
        ) : (
          <div className="space-y-6">
            <div className="surface-card p-5">
              <h2 className="text-base font-semibold text-ink">Profile</h2>
              <p className="mt-1 text-sm text-muted">Your account's basic identity.</p>
              <dl className="mt-4 grid grid-cols-2 gap-4">
                <div>
                  <dt className="text-xs text-muted">Username</dt>
                  <dd className="mt-0.5 text-sm font-medium text-ink">{profile.username}</dd>
                </div>
                <div>
                  <dt className="text-xs text-muted">Email</dt>
                  <dd className="mt-0.5 text-sm font-medium text-ink">{profile.email}</dd>
                </div>
              </dl>
            </div>

            <div className="surface-card p-5">
              <h2 className="text-base font-semibold text-ink">Security</h2>
              <p className="mt-1 text-sm text-muted">
                Update your username, email, or password.
              </p>

              <form onSubmit={handleUpdateUsername} className="mt-4 space-y-2 border-t border-slate-100 pt-4">
                <label className="field-label">Username</label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={usernameInput}
                    onChange={(e) => setUsernameInput(e.target.value)}
                    className="field-input"
                  />
                  <button
                    type="submit"
                    disabled={
                      usernameSaving || !usernameInput.trim() || usernameInput === profile.username
                    }
                    className="btn-secondary btn-sm shrink-0"
                  >
                    {usernameSaving ? 'Saving...' : 'Save'}
                  </button>
                </div>
                {usernameError && <p className="text-xs text-red-600">{usernameError}</p>}
                {usernameSuccess && <p className="text-xs text-green-700">{usernameSuccess}</p>}
              </form>

              <form onSubmit={handleUpdateEmail} className="mt-5 space-y-2 border-t border-slate-100 pt-4">
                <label className="field-label">Change email</label>
                <input
                  type="email"
                  placeholder="New email address"
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  className="field-input"
                />
                <input
                  type="password"
                  placeholder="Current password"
                  value={emailPassword}
                  onChange={(e) => setEmailPassword(e.target.value)}
                  className="field-input"
                />
                <button
                  type="submit"
                  disabled={emailSaving || !newEmail || !emailPassword}
                  className="btn-secondary btn-sm"
                >
                  {emailSaving ? 'Saving...' : 'Update email'}
                </button>
                {emailError && <p className="text-xs text-red-600">{emailError}</p>}
                {emailSuccess && <p className="text-xs text-green-700">{emailSuccess}</p>}
              </form>

              <form onSubmit={handleUpdatePassword} className="mt-5 space-y-2 border-t border-slate-100 pt-4">
                <label className="field-label">Change password</label>
                <input
                  type="password"
                  placeholder="Current password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="field-input"
                />
                <input
                  type="password"
                  placeholder="New password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="field-input"
                />
                <input
                  type="password"
                  placeholder="Confirm new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="field-input"
                />
                <p className="text-xs text-muted">{PASSWORD_HINT}</p>
                <button
                  type="submit"
                  disabled={passwordSaving || !currentPassword || !newPassword || !confirmPassword}
                  className="btn-secondary btn-sm"
                >
                  {passwordSaving ? 'Saving...' : 'Update password'}
                </button>
                {passwordError && <p className="text-xs text-red-600">{passwordError}</p>}
                {passwordSuccess && <p className="text-xs text-green-700">{passwordSuccess}</p>}
              </form>
            </div>

            <div className="mt-10 border-t border-slate-200 pt-6">
              {!showDeleteForm ? (
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <h2 className="text-sm font-medium text-slate-600">Danger zone</h2>
                    <p className="mt-0.5 text-xs text-slate-600">
                      Permanently delete your account. This cannot be undone.
                    </p>
                  </div>
                  <button
                    onClick={() => setShowDeleteForm(true)}
                    className="shrink-0 text-xs font-medium text-red-600 hover:text-red-700 hover:underline"
                  >
                    Delete account
                  </button>
                </div>
              ) : (
                <div className="rounded-lg border border-red-200 bg-red-50 p-4">
                  <h2 className="text-sm font-semibold text-red-700">Delete your account</h2>
                  <p className="mt-1 text-xs text-red-700/80">
                    This permanently deletes your account and everything in it. This cannot be
                    undone. Enter your password to confirm.
                  </p>
                  <form onSubmit={handleDeleteAccount} className="mt-3 space-y-2">
                    <input
                      type="password"
                      placeholder="Enter your password to confirm"
                      value={deletePassword}
                      onChange={(e) => setDeletePassword(e.target.value)}
                      className="field-input border-red-200 focus:border-red-500 focus:ring-red-500"
                      autoFocus
                    />
                    <div className="flex items-center gap-3">
                      <button
                        type="submit"
                        disabled={deleting || !deletePassword}
                        className="btn-danger-ghost btn-sm"
                      >
                        {deleting ? 'Deleting...' : 'Permanently delete my account'}
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setShowDeleteForm(false)
                          setDeletePassword('')
                          setDeleteError('')
                        }}
                        className="text-xs text-muted hover:text-ink"
                      >
                        Cancel
                      </button>
                    </div>
                    {deleteError && <p className="text-xs text-red-700">{deleteError}</p>}
                  </form>
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default Account
