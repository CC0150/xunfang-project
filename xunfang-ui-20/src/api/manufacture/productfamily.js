import request from '@/utils/request'

export function listFamily(query) {
  return request({ url: '/manufacture/family/list', method: 'get', params: query })
}
export function getFamily(id) {
  return request({ url: '/manufacture/family/' + id, method: 'get' })
}
export function addFamily(data) {
  return request({ url: '/manufacture/family', method: 'post', data })
}
export function updateFamily(data) {
  return request({ url: '/manufacture/family', method: 'put', data })
}
/** 单个删除 */
export function delFamily(id) {
  return request({ url: '/manufacture/family/delete/' + id, method: 'delete' })
}
/** 批量删除 */
export function batchDelFamily(ids) {
  if (!Array.isArray(ids)) ids = [ids]
  const safe = ids.map(i => (i === null || i === undefined ? '' : String(i).trim())).filter(Boolean)
  if (safe.length === 0) return Promise.reject(new Error('没有可删除的 id'))
  return request({ url: '/manufacture/family/' + safe.join(','), method: 'delete' })
}
