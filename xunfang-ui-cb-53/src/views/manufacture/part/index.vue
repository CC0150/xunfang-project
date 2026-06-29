<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="Part名称" prop="partName">
        <el-input v-model="queryParams.partName" placeholder="请输入Part名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="Part种类" prop="partType">
        <el-select v-model="queryParams.partType" placeholder="请选择" clearable style="width:100px">
          <el-option label="原材料" value="Ma" /><el-option label="半成品" value="Sfp" />
          <el-option label="成品" value="Pro" /><el-option label="耗材类" value="Rhy" />
          <el-option label="其它" value="Oth" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:90px">
          <el-option label="启用" value="Enable" /><el-option label="停用" value="Disable" />
        </el-select>
      </el-form-item>
      <el-form-item label="购制属性" prop="purchaseOrManufacture">
        <el-select v-model="queryParams.purchaseOrManufacture" placeholder="请选择" clearable style="width:90px">
          <el-option label="购买" value="Pur" /><el-option label="自制" value="Manu" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple || hasCheckedOutSelected" @click="handleBatchDelete">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="partList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- 红色小锁：检出状态标识 -->
      <el-table-column width="28" align="center">
        <template #default="{ row }">
          <el-icon v-if="wsCode(row) === 'CHECKED_OUT'" class="lock-red"><Lock /></el-icon>
        </template>
      </el-table-column>
      <!-- Part编码 -->
      <el-table-column label="Part编码" width="150" align="center">
        <template #default="{ row }">
          <el-tooltip :content="`PN${row.id || ''}`" placement="top" :show-after="150" effect="light" popper-class="clamp-tooltip">
            <span class="clamp2">PN{{ row.id }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <!-- 中文名称 -->
      <el-table-column label="中文名称" align="center" min-width="120">
        <template #default="{ row }">
          <el-tooltip :content="row.partName || '-'" placement="top" :show-after="150" effect="light" popper-class="clamp-tooltip">
            <span class="clamp2">{{ row.partName || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="英文名称" align="center" min-width="100">
        <template #default="{ row }">
          <el-tooltip :content="row.partNameEn || '-'" placement="top"><span class="clamp2">{{ row.partNameEn || '-' }}</span></el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="Part种类" align="center">
        <template #default="{ row }">{{ partTypeMap[row.partType] || row.partType || '-' }}</template>
      </el-table-column>
      <el-table-column label="规格型号" align="center" min-width="120">
        <template #default="{ row }">
          <el-tooltip :content="row.specificationsModel || '-'" placement="top" :show-after="150" effect="light" popper-class="clamp-tooltip">
            <span class="clamp2">{{ row.specificationsModel || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="单位" align="center" prop="unit" />
      <el-table-column label="Part版本" align="center" prop="displayVersion" width="80" />
      <el-table-column label="购制属性" align="center">
        <template #default="{ row }">{{ purchaseMap[row.purchaseOrManufacture] || row.purchaseOrManufacture || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'Enable' ? 'success' : 'warning'">
            {{ row.status === 'Enable' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 附件 -->
      <el-table-column label="附件" align="center" min-width="160">
        <template #default="{ row }">
          <template v-if="row.fileNameNoExt && row.fileDownloadUrl">
            <el-tooltip :content="row.fileNameNoExt" placement="top" :show-after="150" effect="light" popper-class="clamp-tooltip">
              <el-link type="primary" underline="always" @click="downloadFile(row)">
                <span class="clamp2 inline-block">{{ row.fileNameNoExt }}</span>
              </el-link>
            </el-tooltip>
          </template>
          <span v-else-if="row.fileId">-</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="Part说明" align="center" min-width="200">
        <template #default="{ row }">
          <el-tooltip :content="row.partDeclaration || '-'" placement="top" :show-after="150" effect="light" popper-class="clamp-tooltip">
            <span class="clamp2">{{ row.partDeclaration || '-' }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="创建日期" align="center" width="160">
        <template #default="{ row }">{{ parseTime(row.createTime, '{y}-{m}-{d} {h}:{i}') }}</template>
      </el-table-column>
      <!-- 操作 -->
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="wsCode(row) === 'CHECKED_IN'">
            <el-button link type="primary" icon="Upload" @click="confirmCheckout(row)">检出</el-button>
            <el-button link type="danger" icon="Delete" @click="handleSingleDelete(row)">删除</el-button>
          </template>
          <template v-else-if="wsCode(row) === 'CHECKED_OUT'">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="success" icon="Download" @click="confirmCheckin(row)">检入</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bottom">
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="partRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="中文名称" prop="partName">
              <el-input v-model="form.partName" placeholder="请输入中文名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名称" prop="partNameEn">
              <el-input v-model="form.partNameEn" placeholder="请输入英文名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Part种类" prop="partType">
              <el-select v-model="form.partType" placeholder="请选择Part种类">
                <el-option label="原材料" value="Ma" /><el-option label="半成品" value="Sfp" />
                <el-option label="成品" value="Pro" /><el-option label="包材" value="Rhy" />
                <el-option label="其它" value="Oth" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态">
                <el-option label="启用" value="Enable" /><el-option label="停用" value="Disable" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择单位" filterable remote reserve-keyword
                :remote-method="remoteMethodUnit" :loading="unitLoading">
                <el-option v-for="item in unitOptions" :key="item.id" :label="item.unitName" :value="item.unitName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购制属性" prop="purchaseOrManufacture">
              <el-select v-model="form.purchaseOrManufacture" placeholder="请选择购制属性">
                <el-option label="购买" value="Pur" /><el-option label="自制" value="Manu" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规格型号" prop="specificationsModel">
          <el-input v-model="form.specificationsModel" placeholder="请输入规格型号" />
        </el-form-item>
        <!-- 文件上传：类型限制 + 三态 -->
        <el-form-item label="产品文件">
          <el-upload ref="uploadRef" class="upload-one wide" :auto-upload="false" :multiple="false"
            list-type="text" :file-list="fileList" :before-upload="handleBeforeAdd"
            :on-change="onFileChange" :on-remove="onFileRemove"
            accept=".png,.jpg,.jpeg,.gif,.bmp,.webp,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt">
            <el-button type="primary">选择文件</el-button>
            <template #tip v-if="!fileList.length">
              <div class="el-upload__tip">请选择小于 5MB 的图片或文档（png/jpg/pdf/word/excel/ppt/txt）</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="Part说明" prop="partDeclaration">
          <el-input type="textarea" v-model="form.partDeclaration" placeholder="请输入说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Part">
import { getCurrentInstance, ref, reactive, toRefs } from "vue"
import { ElMessage } from "element-plus"
import { Lock } from '@element-plus/icons-vue'
import { listPart, getPart, delPart, addPart, updatePart, checkOut, checkIn } from "@/api/manufacture/part"
import { listUnit } from "@/api/manufacture/unit"
import { downloadByStream } from '@/utils/download'

const { proxy } = getCurrentInstance()
const partList = ref([])
const unitOptions = ref([])
const fileList = ref([])
let selectedRawFile = null
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)
const hasCheckedOutSelected = ref(false)
const unitLoading = ref(false)
const total = ref(0)
const title = ref("")
const uploadRef = ref(null)

const partTypeMap = { Ma: '原材料', Sfp: '半成品', Pro: '成品', Rhy: '耗材类', Oth: '其它' }
const purchaseMap = { Pur: '购买', Manu: '自制' }

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, partName: null, partType: null, status: null, purchaseOrManufacture: null },
  rules: {
    partType: [{ required: true, message: "请选择Part种类", trigger: "change" }],
    partName: [{ required: true, message: "Part名称不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

/** 统一工作状态显示为 CHECKED_IN / CHECKED_OUT */
const wsCode = (row) => {
  let v = row?.uiWorkingState || row?.workingStateAlias || row?.workingStateCode || row?.workingState
  if (!v) return 'CHECKED_IN'
  if (typeof v === 'string') {
    const s = v.trim()
    if (s.startsWith('{') && s.endsWith('}')) {
      try { const obj = JSON.parse(s); v = obj.alias || obj.code || '' } catch { v = s }
    } else { v = s }
  } else if (typeof v === 'object') { v = v?.alias || v?.code || '' }
  v = String(v).toUpperCase()
  if (v === 'INWORK') return 'CHECKED_OUT'
  if (v === 'CHECKED_IN') return 'CHECKED_IN'
  if (v === 'CHECKED_OUT') return 'CHECKED_OUT'
  return v
}

/** 列表 */
function getList() {
  loading.value = true
  listPart(queryParams.value).then(res => {
    partList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => loading.value = false)
}

/** 单位远程搜索 */
function remoteMethodUnit(keyword) {
  unitLoading.value = true
  listUnit({ pageNum: 1, pageSize: 100, ...(keyword ? { unitName: keyword } : {}) })
    .then(res => { unitOptions.value = res.rows || [] })
    .finally(() => { unitLoading.value = false })
}

/** 上传处理 */
function handleBeforeAdd() {
  uploadRef.value?.clearFiles?.(); fileList.value = []; selectedRawFile = null; return true
}
function onFileChange(file) {
  selectedRawFile = file?.raw || null
  fileList.value = selectedRawFile ? [file] : []
  form.value.clearFile = false
}
function onFileRemove() {
  selectedRawFile = null; fileList.value = []; form.value.clearFile = true
}

/** 取消/重置 */
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: null, partType: null, partName: null, partNameEn: null, specificationsModel: null, unit: null, purchaseOrManufacture: null, status: 'Enable', partDeclaration: null, clearFile: false }
  selectedRawFile = null; fileList.value = []
  proxy.resetForm("partRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSelectionChange(selection) {
  selectedRows.value = selection
  single.value = selection.length != 1
  multiple.value = !selection.length
  hasCheckedOutSelected.value = selection.some(r => wsCode(r) === 'CHECKED_OUT')
}

/** 新增 */
function handleAdd() {
  reset()
  open.value = true; title.value = "新增Part"
  remoteMethodUnit("")
}

/** 修改（仅检出态允许） */
function handleUpdate(row) {
  if (wsCode(row) !== 'CHECKED_OUT') { ElMessage.warning('请先检出，再进行修改'); return }
  reset()
  getPart(row.id).then(res => {
    const d = res.data || {}
    form.value = { ...d, clearFile: false }
    fileList.value = d.fileName ? [{ name: d.fileName, url: "", status: "success" }] : []
    open.value = true; title.value = "修改Part"
    remoteMethodUnit("")
  })
}

/** 提交（新增/修改） */
function submitForm() {
  proxy.$refs["partRef"].validate(async (valid) => {
    if (!valid) return
    if (!form.value.id) {
      await addPart(form.value, selectedRawFile)
      proxy.$modal.msgSuccess("新增成功")
    } else {
      await updatePart(form.value, selectedRawFile)
      proxy.$modal.msgSuccess("修改成功")
    }
    open.value = false; getList()
  })
}

/** 单个删除 */
function handleSingleDelete(row) {
  if (wsCode(row) === 'CHECKED_OUT') { ElMessage.warning('包含检出状态的数据，不能删除'); return }
  if (!row.masterId) { ElMessage.error('缺少 masterId'); return }
  proxy.$modal.confirm(`是否确认删除 PN${row.id || ''}？`).then(() => delPart(row.masterId)).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(() => {})
}

/** 批量删除 */
function handleBatchDelete() {
  const rows = selectedRows.value
  if (!rows.length) return ElMessage.warning('请先选择要删除的数据')
  if (rows.some(r => wsCode(r) === 'CHECKED_OUT')) { ElMessage.warning('包含检出状态的数据，不能删除'); return }
  const ids = rows.map(r => r.masterId).filter(Boolean)
  if (!ids.length) return ElMessage.error('所选数据缺少 masterId')
  proxy.$modal.confirm(`是否确认删除 ${ids.length} 条？`).then(() => delPart(ids.join(','))).then(() => {
    proxy.$modal.msgSuccess('删除成功'); getList()
  }).catch(() => {})
}

/** 下载文件 */
function downloadFile(row) {
  if (!row.fileDownloadUrl) return ElMessage.error('无下载地址')
  const filename = row.fileName || 'download'
  const url = row.fileDownloadUrl + (filename ? `&filename=${encodeURIComponent(filename)}` : '')
  downloadByStream(url, filename).catch(e => ElMessage.error(e?.message || '下载失败'))
}

/** 检出 */
function confirmCheckout(row) {
  if (!row.masterId) return ElMessage.error('缺少 masterId，无法检出')
  proxy.$modal.confirm(`是否确认检出Part编码为"PN${row.id || ''}"的数据项？`)
    .then(() => checkOut(row.masterId))
    .then(() => { proxy.$modal.msgSuccess('检出成功'); getList() })
    .catch(() => {})
}

/** 检入 */
function confirmCheckin(row) {
  if (!row.masterId) return ElMessage.error('缺少 masterId，无法检入')
  proxy.$modal.confirm(`是否确认检入Part编码为"PN${row.id || ''}"的数据项？`)
    .then(() => checkIn(row.masterId))
    .then(() => { proxy.$modal.msgSuccess('检入成功'); getList() })
    .catch(() => {})
}

getList()
</script>

<style scoped>
:deep(.el-table .cell) { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.clamp2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; white-space: normal !important; word-break: break-all; line-height: 20px; max-height: 40px; }
.inline-block { display: inline-block; max-width: 100%; }
.clamp-tooltip { max-width: 600px; white-space: normal; word-break: break-word; }
.lock-red { color: #F56C6C; font-size: 16px; vertical-align: middle; }
.upload-one.wide { width: 100%; }
.upload-one.wide .el-upload-list, .upload-one.wide .el-upload-list__item, .upload-one.wide .el-upload-list__item-name { max-width: none; width: 100%; }
.upload-one.wide .el-upload-list__item-name { white-space: normal; word-break: break-all; }
.upload-one.wide .el-upload__tip { margin-left: 8px; }
.pagination-bottom { padding: 10px 0 20px; display: flex; justify-content: flex-end; }
</style>
