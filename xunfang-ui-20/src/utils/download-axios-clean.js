// utils/download-axios-clean.js
import axios from 'axios'

const clean = axios.create({
  baseURL: '/dev-api',
  responseType: 'arraybuffer',
  transformResponse: [d => d], // 不要自动转 JSON
})
// 不加任何 request/response 拦截器！

export async function downloadWithAxios(url, filename) {
  const res = await clean.get(url, { withCredentials: true })

  // 尝试识别 JSON 错误体
  const ct = String(res.headers['content-type'] || '').toLowerCase()
  if (ct.includes('application/json') || ct.includes('text/json')) {
    const text = new TextDecoder().decode(new Uint8Array(res.data || []))
    try {
      const obj = JSON.parse(text || '{}')
      throw new Error(obj?.msg || obj?.message || '下载失败')
    } catch {
      throw new Error('下载失败')
    }
  }

  // 转 Blob
  const blob = new Blob([res.data], { type: ct || 'application/octet-stream' })

  // 文件名
  let name = filename
  if (!name) {
    const cd = res.headers['content-disposition'] || ''
    name = parseFilenameFromContentDisposition(cd)
  }
  if (!name) name = 'download.bin'

  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = URL.createObjectURL(blob)
  a.download = name
  document.body.appendChild(a)
  a.click()
  URL.revokeObjectURL(a.href)
  document.body.removeChild(a)
}
