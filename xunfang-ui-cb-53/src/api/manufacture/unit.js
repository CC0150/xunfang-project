import request from '@/utils/request'

// 查询单位列表
export function listUnit(query) {
  return request({ url: '/manufacture/unit/list', method: 'get', params: query })
}

// 查询单位详细
export function getUnit(id) {
  return request({ url: '/manufacture/unit/' + id, method: 'get' })
}

// 新增单位（批量：传数组）
export function addUnit(dataList) {
  return request({ url: '/manufacture/unit', method: 'post', data: dataList })
}

// 修改单位
export function updateUnit(data) {
  return request({ url: '/manufacture/unit', method: 'put', data: data })
}

// 删除单位
export function delUnit(id) {
  return request({ url: '/manufacture/unit/' + id, method: 'delete' })
}
