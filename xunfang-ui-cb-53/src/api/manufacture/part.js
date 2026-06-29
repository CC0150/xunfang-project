import request from '@/utils/request'

// 查询Part列表
export function listPart(query) {
  return request({ url: '/manufacture/part/list', method: 'get', params: query })
}

// 查询Part详细
export function getPart(id) {
  return request({ url: '/manufacture/part/' + id, method: 'get' })
}

// 新增Part（multipart: data JSON + file 可选，不手动设 Content-Type 让浏览器自动加 boundary）
export function addPart(data, file) {
  const fd = new FormData()
  fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (file) fd.append('file', file, file.name)
  return request({ url: '/manufacture/part', method: 'post', data: fd, timeout: 60000 })
}

// 修改Part（multipart: 附件三态 — 替换/删除/保持）
export function updatePart(data, file) {
  const fd = new FormData()
  fd.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (file) fd.append('file', file, file.name)
  return request({ url: '/manufacture/part', method: 'put', data: fd, timeout: 60000 })
}

// 删除Part（masterId，支持逗号分隔批量）
export function delPart(masterIds) {
  return request({ url: '/manufacture/part/' + masterIds, method: 'delete' })
}

// 检出
export function checkOut(masterId) {
  return request({
    url: '/manufacture/part/checkout',
    method: 'put',
    data: { masterId, workCopyType: 'BOTH' }
  })
}

// 检入
export function checkIn(masterId) {
  return request({
    url: '/manufacture/part/checkin',
    method: 'put',
    data: { masterId }
  })
}
