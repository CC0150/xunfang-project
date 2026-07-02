import request from '@/utils/request'

// 查询供应商信息列表
export function listSupplier(query) {
  return request({
    url: '/manufacture/supplier/list',
    method: 'get',
    params: query
  })
}

// 查询供应商信息详细
export function getSupplier(id) {
  return request({
    url: '/manufacture/supplier/' + id,
    method: 'get'
  })
}

// 新增供应商信息
export function addSupplier(data) {
  return request({
    url: '/manufacture/supplier',
    method: 'post',
    data: data
  })
}

// 修改供应商信息
export function updateSupplier(data) {
  return request({
    url: '/manufacture/supplier',
    method: 'put',
    data: data
  })
}

// 删除供应商信息
export function delSupplier(id) {
  return request({
    url: '/manufacture/supplier/' + id,
    method: 'delete'
  })
}
