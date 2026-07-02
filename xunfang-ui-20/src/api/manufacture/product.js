import request from '@/utils/request'

const isFormData = (v) => typeof FormData !== 'undefined' && v instanceof FormData

/** 列表（latest=true） */
export function listProduct(query) {
  return request({ url: '/manufacture/product/list', method: 'get', params: query })
}

/** 详情 */
export function getProduct(id) {
  return request({ url: '/manufacture/product/' + id, method: 'get' })
}

/**
 * 新增 — 支持两种用法：
 * 1) addProduct(formData)               // 直接传 FormData
 * 2) addProduct(jsonData, rawFile)      // 传对象 + 文件，自动拼 FormData
 */
export function addProduct(data, file) {
  return sendWithOptionalFile({ url: '/manufacture/product', method: 'post', data, file, timeout: 60000 })
}

/**
 * 修改 — 同上
 */
export function updateProduct(data, file) {
  return sendWithOptionalFile({ url: '/manufacture/product', method: 'put', data, file, timeout: 60000 })
}

/** 单个删除（masterId） */
export function delProduct(masterId) {
  return request({ url: '/manufacture/product/delete/' + masterId, method: 'delete' })
}

/** 批量删除（masterIds 逗号分隔） */
export function delProductBatch(masterIds) {
  return request({ url: '/manufacture/product/batch/' + masterIds, method: 'delete' })
}

/** 检出 */
export function checkOut(data) {
  return request({ url: '/manufacture/product/checkout', method: 'put', data })
}

/** 检入 */
export function checkIn(data) {
  return request({ url: '/manufacture/product/checkin', method: 'put', data })
}

/** 更新生命周期状态 */
export function updateStatus(data) {
  return request({ url: '/manufacture/product/updateStatus', method: 'put', data })
}

/** 统一：有文件或已是 FormData 走 multipart；否则 JSON */
function sendWithOptionalFile({ url, method, data, file, timeout }) {
  if (isFormData(data)) {
    return request({ url, method, data, timeout })
  }
  if (file) {
    const fd = new FormData()
    fd.append('data', new Blob([JSON.stringify(data || {})], { type: 'application/json' }))
    fd.append('file', file, file.name)
    return request({ url, method, data: fd, timeout })
  }
  return request({ url, method, data, timeout })
}
