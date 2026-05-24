import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from './auth'

const request = axios.create({
  baseURL: '/webapp',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    if (error.response && error.response.status === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/#/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
