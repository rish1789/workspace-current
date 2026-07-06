import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/axios'

function Register() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/api/users/register', { username, email, password })
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen">
      <div className="auth-panel relative hidden w-1/2 flex-col justify-between overflow-hidden p-12 text-white lg:flex">
        <div className="auth-panel-grid pointer-events-none absolute inset-0" />
        <div className="relative flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-white/15 text-base font-bold ring-1 ring-inset ring-white/25">
            W
          </div>
          <span className="text-lg font-semibold">Workspace</span>
        </div>
        <div className="relative">
          <h2 className="text-3xl font-bold leading-tight">
            Join your team's
            <br />
            workspace today.
          </h2>
          <p className="mt-3 max-w-sm text-sm text-blue-100">
            Create boards, track tickets, and collaborate with your team — all in one place.
          </p>
        </div>
        <p className="relative text-xs text-blue-200">© {new Date().getFullYear()} Workspace</p>
      </div>

      <div className="flex w-full items-center justify-center bg-app-bg px-4 py-12 lg:w-1/2">
        <div className="w-full max-w-sm">
          <div className="mb-8 flex flex-col items-center lg:hidden">
            <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg bg-accent text-lg font-bold text-white">
              W
            </div>
          </div>
          <h1 className="text-xl font-semibold text-ink">Create an account</h1>
          <p className="mt-1 text-sm text-slate-600">Start organizing your work on boards</p>

          <div className="surface-card mt-6 p-6">
            {error && <div className="alert-error mb-4">{error}</div>}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="field-label">Username</label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  autoFocus
                  className="field-input"
                />
              </div>

              <div>
                <label className="field-label">Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="field-input"
                />
              </div>

              <div>
                <label className="field-label">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="field-input"
                />
              </div>

              <button type="submit" disabled={loading} className="btn-primary w-full">
                {loading ? 'Creating account...' : 'Register'}
              </button>
            </form>
          </div>

          <p className="mt-5 text-center text-sm text-slate-600">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-accent hover:text-accent-hover">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Register
