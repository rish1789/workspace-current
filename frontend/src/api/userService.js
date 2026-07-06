import axios from 'axios'
import api from './axios'

export function getCurrentUser() {
  return api.get('/api/users/me').then((res) => res.data)
}

export function getUserById(userId) {
  return api.get(`/api/users/find/${userId}`).then((res) => res.data)
}

export function searchUsers(username) {
  return api.get('/api/users/find', { params: { username } }).then((res) => res.data)
}

export function updateUsername(userId, username) {
  return api.patch(`/api/users/${userId}/username`, { username }).then((res) => res.data)
}

export function updateEmail(userId, email) {
  return api.patch(`/api/users/${userId}/email`, { email }).then((res) => res.data)
}

export function updatePassword(userId, password) {
  return api.patch(`/api/users/${userId}/update-password`, { password }).then((res) => res.data)
}

export function deleteAccount(userId) {
  return api.delete(`/api/users/${userId}`).then((res) => res.data)
}

// Deliberately bypasses the shared `api` instance: its response interceptor
// clears the token and redirects to /login on any 401/403, which would log
// the user out the moment they mistype their current password here. This is
// also how we get a fresh, valid JWT after an email change, since the token's
// subject is the email and a changed email invalidates the old token.
export function verifyPassword(email, password) {
  return axios
    .post('http://localhost:8080/api/auth/login', { email, password })
    .then((res) => res.data)
}
