import request from '@/utils/request'

/** 生命周期模板列表 */
export function listLifecycleTemplates(params) {
  return request({ url: '/manufacture/lifecycle/lifecycleTemplateList', method: 'get', params })
}

/** 获取业务操作（create/edit/checkout/checkin场景） */
export function getLifecycleBusiness(templateId, operation = 'create', stateId = '') {
  return request({
    url: '/manufacture/lifecycle/lifeBusinessList',
    method: 'get',
    params: { templateId, operation, stateId }
  })
}

/** 获取目标生命周期状态 */
export function getLifecycleStates(templateId, businessOperationId, stateId = '', operation) {
  return request({
    url: '/manufacture/lifecycle/lifeState',
    method: 'get',
    params: { templateId, businessOperationId, stateId, operation }
  })
}
