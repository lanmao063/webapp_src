const TOKEN_KEY = 'parcel_locker_token'
const USER_KEY = 'parcel_locker_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token, rememberMe) {
  if (rememberMe) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
  }
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(USER_KEY)
}

export function getUserInfo() {
  const user = localStorage.getItem(USER_KEY) || sessionStorage.getItem(USER_KEY)
  if (user) {
    try {
      return JSON.parse(user)
    } catch {
      return null
    }
  }
  return null
}

export function setUserInfo(user, rememberMe) {
  const data = JSON.stringify(user)
  if (rememberMe) {
    localStorage.setItem(USER_KEY, data)
  } else {
    sessionStorage.setItem(USER_KEY, data)
  }
}
