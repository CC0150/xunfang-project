// utils/download.js
import { getToken } from '@/utils/auth'   //  取 token

export async function downloadByStream(path, filename) {
  if (!path || !path.startsWith('/')) {
    throw new Error('无效的下载地址（必须是以 / 开头的相对路径）')
  }

  const finalUrl = `/dev-api${path}`

  // 平台登录令牌
  const headers = {}
  const token = getToken && getToken()
  if (token) headers['Authorization'] = 'Bearer ' + token

  const res = await fetch(finalUrl, {
    method: 'GET',
    credentials: 'include',
    headers               // <<<<<<<< 关键
  })

  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`)

  const ct = (res.headers.get('content-type') || '').toLowerCase()
  if (ct.includes('application/json') || ct.includes('text/json')) {
    const text = await res.text().catch(() => '')
    try {
      const data = JSON.parse(text || '{}')
      throw new Error(data?.msg || data?.message || '下载失败（返回 JSON）')
    } catch {
      throw new Error('下载失败（返回 JSON 且无法解析）')
    }
  }

  const blob = await res.blob()

  let name = (filename && filename.trim()) ? filename.trim() : ''
  if (!name) {
    const cd = res.headers.get('content-disposition') || ''
    name = parseFilenameFromContentDisposition(cd) || 'download'
  }

  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = URL.createObjectURL(blob)
  a.download = name
  document.body.appendChild(a)
  a.click()
  URL.revokeObjectURL(a.href)
  document.body.removeChild(a)
}

function parseFilenameFromContentDisposition(cd) {
  if (!cd) return ''
  const star = /filename\*\s*=\s*[^']*''([^;]+)/i.exec(cd)
  if (star && star[1]) {
    try { return decodeURIComponent(star[1].replace(/"/g, '').trim()) }
    catch { return star[1].replace(/"/g, '').trim() }
  }
  const normal = /filename\s*=\s*("?)([^";]+)\1/i.exec(cd)
  if (normal && normal[2]) return normal[2].trim()
  return ''
}
