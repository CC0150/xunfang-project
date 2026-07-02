<template>
  <div class="app-container">
    <!-- 搜索 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="产品族名称" prop="productFamily">
        <el-select v-model="queryParams.productFamily" placeholder="请选择产品族" filterable remote clearable reserve-keyword
          :remote-method="remoteMethodFamily" :loading="familyLoading" style="width: 220px">
          <el-option v-for="f in familyOptions" :key="f.id" :label="f.productFamilyNameCn" :value="f.productFamilyNameCn" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品类别" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择" clearable style="width: 120px">
          <el-option v-for="op in categoryDict" :key="op.value" :label="op.label" :value="op.value" />
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
    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column width="28" align="center">
        <template #default="{ row }">
          <el-icon v-if="wsCode(row)==='CHECKED_OUT'" class="lock-red"><Lock /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="产品编码" width="160" align="center">
        <template #default="{ row }">PO{{ row.id }}</template>
      </el-table-column>
      <el-table-column label="产品名称" width="220" align="center" prop="productName" show-overflow-tooltip />
      <el-table-column label="产品族" width="180" align="center" prop="productFamily" show-overflow-tooltip />
      <el-table-column label="产品类别" width="110" align="center">
        <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column label="产品版本" width="100" align="center" prop="displayVersion" />
      <el-table-column label="产品规格" prop="specificationModels" width="200" align="center" show-overflow-tooltip />
      <el-table-column label="生命周期状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="lcTag(lcName(row))">{{ lcName(row) || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产品文件" width="140" align="center">
        <template #default="{ row }">
          <el-link v-if="row.fileName && row.fileDownloadUrl" type="primary" underline="always" @click="downloadFile(row)">{{ row.fileName }}</el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="产品描述" prop="productDescribe" width="220" align="center" show-overflow-tooltip />
      <el-table-column label="创建日期" align="center" prop="createTime" width="180">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="360" fixed="right">
        <template #default="{ row }">
          <template v-if="wsCode(row)==='CHECKED_IN'">
            <el-button link type="primary" icon="Upload" @click="openFlowDlg('checkout', row)">检出</el-button>
            <el-button link type="danger" icon="Delete" @click="handleSingleDelete(row)">删除</el-button>
          </template>
          <template v-else-if="wsCode(row)==='CHECKED_OUT'">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="success" icon="Download" @click="openFlowDlg('checkin', row)">检入</el-button>
          </template>
          <el-button link type="warning" icon="Edit" @click="openStatusDlg(row)">更新状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog :title="title" v-model="open" width="760px" append-to-body>
      <el-form ref="productRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品族" prop="productFamily">
              <el-select v-model="form.productFamily" placeholder="请选择产品族" filterable remote clearable reserve-keyword
                :remote-method="remoteMethodFamily" :loading="familyLoading">
                <el-option v-for="f in familyOptions" :key="f.id" :label="f.productFamilyNameCn" :value="f.productFamilyNameCn" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择产品类别">
                <el-option v-for="op in categoryDict" :key="op.value" :label="op.label" :value="op.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品规格" prop="specificationModels">
              <el-input v-model="form.specificationModels" placeholder="请输入产品规格" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 新增时显示生命周期联动区 -->
        <template v-if="!form.id">
          <el-row :gutter="20">
            <el-col :span="24">
              <el-form-item label="生命周期模板" prop="lifecycleTemplateId" required>
                <el-select v-model="form.lifecycleTemplateId" placeholder="请选择生命周期模板" filterable clearable
                  :loading="lifeTplLoading" @visible-change="v => v && loadLifecycleTemplates()" @change="onTemplateChange" style="width:100%">
                  <el-option v-for="tpl in lifecycleTemplateOptions" :key="tpl.id" :label="tpl.name || tpl.id" :value="tpl.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="业务操作" prop="operation" required>
                <el-select v-model="form.operation" placeholder="请选择业务操作" :disabled="!form.lifecycleTemplateId"
                  :loading="lifeOpLoading" @change="onOperationChange">
                  <el-option v-for="op in createBizOptions" :key="op.id || op.operation" :label="op.name || '立项'" :value="op.operation || 'create'" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="目标生命周期状态" prop="lifecycleStateId" required>
                <el-select v-model="form.lifecycleStateId" placeholder="请选择状态" :disabled="!form.operation"
                  :loading="lifeStateLoading" clearable>
                  <el-option v-for="st in lifecycleStateOptions" :key="st.id" :label="st.name || st.internalName || st.businessCode" :value="st.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        <!-- 文件上传 -->
        <el-form-item label="产品文件">
          <el-upload ref="uploadRef" class="upload-one wide" :auto-upload="false" :multiple="false"
            list-type="text" :file-list="fileList" :before-upload="handleBeforeAdd" :on-change="onFileChange" :on-remove="onFileRemove"
            accept=".png,.jpg,.jpeg,.gif,.bmp,.webp,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,image/*">
            <el-button type="primary">选择文件</el-button>
            <template #tip v-if="!fileList.length">
              <div class="el-upload__tip">请选择小于 5MB 的图片或文档（png/jpg/pdf/word/excel/ppt/txt）</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="产品描述" prop="productDescribe">
          <el-input type="textarea" v-model="form.productDescribe" placeholder="请输入产品描述" :autosize="{ minRows: 3, maxRows: 8 }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 检出/检入弹窗 -->
    <el-dialog :title="flowDlg.mode==='checkout' ? '检出' : '检入'" v-model="flowDlg.open" width="560px" append-to-body>
      <el-form ref="flowFormRef" :model="flowDlg.form" :rules="flowRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="生命周期模板">
              <el-select v-model="flowDlg.form.lifecycleTemplateId" disabled style="width:100%">
                <el-option v-for="tpl in flowDlg.tplOptions" :key="tpl.id" :label="tpl.name || tpl.id" :value="tpl.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务操作" prop="businessOperationId" required>
              <el-select v-model="flowDlg.form.businessOperationId" placeholder="请选择业务操作"
                :loading="flowDlg.loading.business" @change="onFlowBizChange" style="width:100%">
                <el-option v-for="bo in flowDlg.bizOptions" :key="bo.id" :label="bo.name || bo.id" :value="bo.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标生命周期状态" prop="lifecycleStateId" required>
              <el-select v-model="flowDlg.form.lifecycleStateId" placeholder="请选择状态"
                :loading="flowDlg.loading.states" style="width:100%">
                <el-option v-for="st in flowDlg.stateOptions" :key="st.id" :label="st.name || st.internalName || st.businessCode" :value="st.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmFlowDlg">确 定</el-button>
        <el-button @click="flowDlg.open=false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 更新状态弹窗 -->
    <el-dialog title="更新状态" v-model="statusDlg.open" width="560px" append-to-body>
      <el-form ref="statusFormRef" :model="statusDlg.form" :rules="statusRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="生命周期模板">
              <el-select v-model="statusDlg.form.lifecycleTemplateId" disabled style="width:100%">
                <el-option v-for="tpl in statusDlg.tplOptions" :key="tpl.id" :label="tpl.name || tpl.id" :value="tpl.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="业务操作" prop="businessOperationId" required>
              <el-select v-model="statusDlg.form.businessOperationId" placeholder="请选择"
                :loading="statusDlg.loading.business" @change="onStatusBizChange" style="width:100%">
                <el-option v-for="bo in statusDlg.bizOptions" :key="bo.id" :label="bo.name || bo.id" :value="bo.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标生命周期状态" prop="lifecycleStateId" required>
              <el-select v-model="statusDlg.form.lifecycleStateId" placeholder="请选择"
                :disabled="!statusDlg.form.businessOperationId" :loading="statusDlg.loading.states" style="width:100%">
                <el-option v-for="st in statusDlg.stateOptions" :key="st.id" :label="st.name || st.internalName || st.businessCode" :value="st.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmStatusDlg">确 定</el-button>
        <el-button @click="statusDlg.open=false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Product">
import { getCurrentInstance, ref, reactive, toRefs, computed } from "vue"
import { ElMessage } from "element-plus"
import { Lock } from '@element-plus/icons-vue'
import { listProduct, getProduct, addProduct, updateProduct, delProduct, delProductBatch, checkOut, checkIn, updateStatus } from "@/api/manufacture/product"
import { listFamily } from "@/api/manufacture/productfamily"
import { listLifecycleTemplates, getLifecycleBusiness, getLifecycleStates } from "@/api/manufacture/lifecycle"
import { downloadByStream } from '@/utils/download'

const { proxy } = getCurrentInstance()

// ====== 列表/查询 ======
const productList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)
const hasCheckedOutSelected = ref(false)

const categoryDict = [
  { label: "定制", value: "CU" },
  { label: "标准", value: "ST" }
]

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, productName: null, productFamily: null, category: null },
  form: {},
  rules: {
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    productFamily: [{ required: true, message: "请选择产品族", trigger: "change" }],
    category: [{ required: true, message: "请选择产品类别", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// ====== 工具 ======
const categoryLabel = (val) => {
  if (!val) return '-'
  if (typeof val === 'object') {
    const code = val.alias || val.code
    const hit = categoryDict.find(x => x.value === code)
    return hit ? hit.label : (val.cnName || code || '-')
  }
  const hit = categoryDict.find(x => x.value === val)
  return hit ? hit.label : (val || '-')
}
const wsCode = (row) => {
  let v = row?.uiWorkingState || row?.workingState
  if (!v) return 'CHECKED_IN'
  if (typeof v === 'object') v = v.alias || v.code || ''
  v = String(v).toUpperCase()
  if (v === 'INWORK') return 'CHECKED_OUT'
  return v
}
const lcName = (row) => {
  if (row.lifecycleStateName) return row.lifecycleStateName
  const s = row?.lifecycleState
  if (s && typeof s === 'object') return s.name || s.internalName || s.nameEn || s.businessCode || '-'
  return s || '-'
}
const lcTag = (name) => {
  const k = (name || '').toLowerCase()
  if (k.includes('开始') || k.includes('start')) return undefined
  if (k.includes('待开发') || k.includes('tobedeveloped')) return 'info'
  if (k.includes('开发中') || k.includes('development')) return 'warning'
  if (k.includes('试产') || k.includes('trial')) return 'warning'
  if (k.includes('量产') || k.includes('volume')) return 'success'
  if (k.includes('停产') || k.includes('cease')) return 'danger'
  if (k.includes('结束') || k.includes('end')) return 'info'
  return undefined
}

// ====== 产品族远程下拉 ======
const familyOptions = ref([])
const familyLoading = ref(false)
function remoteMethodFamily(keyword) {
  familyLoading.value = true
  listFamily({ pageNum: 1, pageSize: 50, productFamilyNameCn: keyword || '' })
    .then(res => { familyOptions.value = res?.rows || [] })
    .finally(() => { familyLoading.value = false })
}

// ====== 列表 ======
function getList() {
  loading.value = true
  listProduct(queryParams.value).then(res => {
    productList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => loading.value = false)
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(selection) {
  selectedRows.value = selection
  single.value = selection.length != 1
  multiple.value = !selection.length
  hasCheckedOutSelected.value = selection.some(r => wsCode(r) === 'CHECKED_OUT')
}

// ====== 下载 ======
function downloadFile(row) {
  if (!row.fileDownloadUrl) { ElMessage.error("无下载地址"); return }
  const filename = row.fileName || "download"
  const sep = row.fileDownloadUrl.includes("?") ? "&" : "?"
  const url = row.fileDownloadUrl + (filename ? `${sep}filename=${encodeURIComponent(filename)}` : "")
  downloadByStream(url, filename).catch(e => ElMessage.error(e?.message || "下载失败"))
}

// ====== 弹窗：新增/修改 ======
const open = ref(false)
const title = ref("")
const uploadRef = ref(null)
const fileList = ref([])
let selectedRawFile = null

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加产品信息"
  remoteMethodFamily("")
}
function handleUpdate(row) {
  if (wsCode(row) !== 'CHECKED_OUT') { ElMessage.warning('请先检出，再进行修改'); return }
  reset()
  const _id = row.id
  getProduct(_id).then(res => {
    const d = res.data || {}
    form.value = { ...d }
    form.value.category = (typeof d.category === 'object') ? (d.category.alias || d.category.code) : d.category
    fileList.value = d.fileName ? [{ name: d.fileName, url: "", status: "success" }] : []
    form.value.clearFile = false
    selectedRawFile = null
    open.value = true
    title.value = "修改产品信息"
    remoteMethodFamily(form.value.productFamily || "")
  })
}
function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: null, productName: null, productFamily: null, category: null, specificationModels: null,
    productDescribe: null, lifecycleTemplateId: null, operation: null, lifecycleStateId: null, clearFile: false }
  uploadRef.value?.clearFiles?.()
  selectedRawFile = null; fileList.value = []
  lifecycleTemplateOptions.value = []
  lifecycleStateOptions.value = []
  createBizOptions.value = []
  proxy.resetForm("productRef")
}

// ====== 文件上传 ======
const MAX_SIZE = 5 * 1024 * 1024
const ALLOWED_EXTS = ['png','jpg','jpeg','gif','bmp','webp','pdf','doc','docx','xls','xlsx','ppt','pptx','txt']
function validateFile(file) {
  const ext = (file.name || '').split('.').pop().toLowerCase()
  if (!ALLOWED_EXTS.includes(ext)) { ElMessage.error("仅支持图片/文档：png/jpg/pdf/word/excel/ppt/txt"); return false }
  if (file.size > MAX_SIZE) { ElMessage.error("文件大小不能超过 5MB"); return false }
  return true
}
function handleBeforeAdd(file) {
  if (fileList.value.length) { uploadRef.value?.clearFiles?.(); fileList.value = []; selectedRawFile = null }
  return validateFile(file)
}
function onFileChange(file) {
  if (!file?.raw) return
  if (!validateFile(file.raw)) { uploadRef.value?.clearFiles?.(); fileList.value = []; selectedRawFile = null; return }
  selectedRawFile = file.raw; fileList.value = [file]
  form.value.clearFile = false
}
function onFileRemove() { selectedRawFile = null; fileList.value = []; form.value.clearFile = true }

// ====== 提交 ======
async function submitForm() {
  const ok = await proxy.$refs["productRef"].validate().catch(() => false)
  if (!ok) return
  const isAdd = !form.value.id
  const payload = { ...form.value }
  payload.category = (typeof payload.category === 'object') ? (payload.category.alias || payload.category.code) : payload.category
  const fd = new FormData()
  fd.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }))
  if (selectedRawFile) fd.append("file", selectedRawFile)
  try {
    if (isAdd) { await addProduct(fd); proxy.$modal.msgSuccess("新增成功") }
    else { await updateProduct(fd); proxy.$modal.msgSuccess("修改成功") }
    open.value = false; getList()
  } catch (e) { ElMessage.error(e?.msg || e?.message || "提交失败") }
}

// ====== 删除 ======
function handleSingleDelete(row) {
  if (wsCode(row) === 'CHECKED_OUT') { ElMessage.warning('检出状态不可删除，请先检入'); return }
  if (!row?.masterId) { ElMessage.error('缺少 masterId，无法删除'); return }
  proxy.$modal.confirm(`是否确认删除（PO${row.id || ''}）？`)
    .then(() => delProduct(row.masterId))
    .then(() => { proxy.$modal.msgSuccess('删除成功'); getList() })
    .catch(() => {})
}
function handleBatchDelete() {
  const rows = selectedRows.value
  if (!rows.length) return ElMessage.warning('请先选择要删除的数据')
  if (rows.some(r => wsCode(r) === 'CHECKED_OUT')) { ElMessage.warning('包含检出状态的数据，不能删除'); return }
  const ids = rows.map(r => r.masterId).filter(Boolean)
  if (!ids.length) return ElMessage.error('所选数据缺少 masterId')
  proxy.$modal.confirm(`是否确认批量删除 ${ids.length} 条？`)
    .then(() => delProductBatch(ids.join(',')))
    .then(() => { proxy.$modal.msgSuccess('批量删除成功'); getList() })
    .catch(() => {})
}

// ====== 生命周期：新增create联动 ======
const lifecycleTemplateOptions = ref([])
const lifecycleStateOptions = ref([])
const createBizOptions = ref([])
const lifeTplLoading = ref(false)
const lifeOpLoading = ref(false)
const lifeStateLoading = ref(false)

function loadLifecycleTemplates() {
  if (lifeTplLoading.value) return
  lifeTplLoading.value = true
  listLifecycleTemplates({ "master.businessCode": "LCT00000009", latest: true, pageNum: 1, pageSize: 50 })
    .then(res => { lifecycleTemplateOptions.value = (res?.rows || res?.data || []) })
    .finally(() => lifeTplLoading.value = false)
}
function onTemplateChange() {
  form.value.operation = null
  form.value.lifecycleStateId = null
  lifecycleStateOptions.value = []
  createBizOptions.value = []
  refreshBusinessAndStates()
}
function onOperationChange() {
  form.value.lifecycleStateId = null
  lifecycleStateOptions.value = []
  refreshStatesOnly()
}
function refreshBusinessAndStates() {
  const tplId = form.value.lifecycleTemplateId
  const op = form.value.operation || "create"
  if (!tplId) return
  lifeOpLoading.value = true
  getLifecycleBusiness(tplId, op)
    .then(res => {
      const list = Array.isArray(res?.data) ? res.data : (res?.data ? [res.data] : [])
      createBizOptions.value = list.length ? list : [{ operation: 'create', name: '立项' }]
      if (!form.value.operation && createBizOptions.value.length) {
        form.value.operation = createBizOptions.value[0].operation || 'create'
      }
      return refreshStatesOnly()
    })
    .finally(() => lifeOpLoading.value = false)
}
function refreshStatesOnly() {
  const tplId = form.value.lifecycleTemplateId
  const chosen = createBizOptions.value.find(op => (op.operation || 'create') === (form.value.operation || 'create'))
  const businessOperationId = chosen?.id
  if (!tplId || !businessOperationId) return
  lifeStateLoading.value = true
  return getLifecycleStates(tplId, businessOperationId, '', 'create')
    .then(res => {
      const arr = res?.data || res?.rows || []
      lifecycleStateOptions.value = Array.isArray(arr) ? arr : (arr ? [arr] : [])
      form.value.lifecycleStateId = lifecycleStateOptions.value.length ? lifecycleStateOptions.value[0].id : null
    })
    .finally(() => lifeStateLoading.value = false)
}

// ====== 检出/检入 ======
const flowDlg = reactive({
  open: false, mode: 'checkout', row: null,
  form: { masterId: '', operation: 'checkout', lifecycleTemplateId: '', businessOperationId: '', lifecycleStateId: '' },
  tplOptions: [], bizOptions: [], stateOptions: [],
  loading: { business: false, states: false }
})
const flowFormRef = ref(null)
const flowRules = {
  businessOperationId: [{ required: true, message: '请选择业务操作', trigger: 'change' }],
  lifecycleStateId: [{ required: true, message: '请选择目标生命周期状态', trigger: 'change' }]
}

function openFlowDlg(mode, row) {
  flowDlg.mode = mode; flowDlg.open = true; flowDlg.row = row
  flowDlg.form.operation = mode
  flowDlg.form.masterId = row.masterId || row.id
  const tplId = row?.lifecycleTemplate?.id || row?.lifecycleTemplateId || ''
  flowDlg.form.lifecycleTemplateId = tplId
  flowDlg.tplOptions = tplId ? [{ id: tplId, name: row?.lifecycleTemplate?.name || '' }] : []
  flowDlg.form.businessOperationId = ''; flowDlg.form.lifecycleStateId = ''
  flowDlg.bizOptions = []; flowDlg.stateOptions = []
  loadFlowBusiness()
}
async function loadFlowBusiness() {
  const tplId = flowDlg.form.lifecycleTemplateId
  if (!tplId) return
  flowDlg.loading.business = true
  try {
    const res = await getLifecycleBusiness(tplId, flowDlg.form.operation, '')
    const list = (res?.rows || res?.data || [])
    flowDlg.bizOptions = Array.isArray(list) ? list : (list ? [list] : [])
  } finally { flowDlg.loading.business = false }
}
function onFlowBizChange() { flowDlg.form.lifecycleStateId = ''; loadFlowStates() }
async function loadFlowStates() {
  const tplId = flowDlg.form.lifecycleTemplateId
  const boId = flowDlg.form.businessOperationId
  if (!tplId || !boId) { flowDlg.stateOptions = []; return }
  flowDlg.loading.states = true
  try {
    const res = await getLifecycleStates(tplId, boId, '', flowDlg.form.operation)
    const arr = res?.data || res?.rows || []
    flowDlg.stateOptions = Array.isArray(arr) ? arr : (arr ? [arr] : [])
    flowDlg.form.lifecycleStateId = flowDlg.stateOptions.length ? flowDlg.stateOptions[0].id : ''
  } finally { flowDlg.loading.states = false }
}
async function confirmFlowDlg() {
  const ok = await flowFormRef.value?.validate().catch(() => false)
  if (!ok) return
  const { masterId, lifecycleTemplateId, lifecycleStateId } = flowDlg.form
  const payload = { masterId, lifecycleTemplate: { id: lifecycleTemplateId }, lifecycleState: { id: lifecycleStateId } }
  try {
    if (flowDlg.mode === 'checkout') {
      await checkOut({ ...payload, workCopyType: 'BOTH' })
      proxy.$modal.msgSuccess('检出成功')
    } else {
      await checkIn({ ...payload, viewNo: '' })
      proxy.$modal.msgSuccess('检入成功')
    }
    flowDlg.open = false; getList()
  } catch (e) { ElMessage.error(e?.msg || e?.message || '操作失败') }
}

// ====== 更新状态（edit） ======
const statusDlg = reactive({
  open: false, row: null,
  form: { id: '', lifecycleTemplateId: '', businessOperationId: '', lifecycleStateId: '', workingState: '' },
  tplOptions: [], bizOptions: [], stateOptions: [],
  loading: { business: false, states: false }
})
const statusFormRef = ref(null)
const statusRules = {
  businessOperationId: [{ required: true, message: '请选择业务操作', trigger: 'change' }],
  lifecycleStateId: [{ required: true, message: '请选择目标生命周期状态', trigger: 'change' }]
}

function openStatusDlg(row) {
  statusDlg.open = true; statusDlg.row = row
  statusDlg.form.id = row.id || row.masterId || ''
  statusDlg.form.workingState = wsCode(row) || ''
  const tplId = row?.lifecycleTemplate?.id || row?.lifecycleTemplateId || ''
  statusDlg.form.lifecycleTemplateId = tplId
  statusDlg.tplOptions = tplId ? [{ id: tplId, name: row?.lifecycleTemplate?.name || '' }] : []
  statusDlg.form.businessOperationId = ''; statusDlg.form.lifecycleStateId = ''
  statusDlg.bizOptions = []; statusDlg.stateOptions = []
  loadStatusBusiness()
}
async function loadStatusBusiness() {
  const tplId = statusDlg.form.lifecycleTemplateId
  if (!tplId) { statusDlg.bizOptions = []; return }
  statusDlg.loading.business = true
  try {
    const res = await getLifecycleBusiness(tplId, 'edit', '')
    const list = (res?.rows || res?.data || [])
    statusDlg.bizOptions = Array.isArray(list) ? list : (list ? [list] : [])
  } finally { statusDlg.loading.business = false }
}
function onStatusBizChange() { statusDlg.form.lifecycleStateId = ''; loadStatusStates() }
async function loadStatusStates() {
  const tplId = statusDlg.form.lifecycleTemplateId
  const boId = statusDlg.form.businessOperationId
  if (!tplId || !boId) { statusDlg.stateOptions = []; return }
  statusDlg.loading.states = true
  try {
    const res = await getLifecycleStates(tplId, boId, '', 'edit')
    const arr = res?.data || res?.rows || []
    statusDlg.stateOptions = Array.isArray(arr) ? arr : (arr ? [arr] : [])
  } finally { statusDlg.loading.states = false }
}
async function confirmStatusDlg() {
  const ok = await statusFormRef.value?.validate().catch(() => false)
  if (!ok) return
  const { id, lifecycleTemplateId, lifecycleStateId, workingState } = statusDlg.form
  await updateStatus({ id, workingState, lifecycleTemplate: { id: lifecycleTemplateId }, lifecycleState: { id: lifecycleStateId } })
  proxy.$modal.msgSuccess('更新成功')
  statusDlg.open = false; getList()
}

getList()
</script>

<style scoped>
:deep(.el-table .cell) { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%; }
.lock-red { color: #F56C6C; font-size: 16px; vertical-align: middle; }
.upload-one.wide { width: 100%; }
.upload-one.wide .el-upload-list, .upload-one.wide .el-upload-list__item, .upload-one.wide .el-upload-list__item-name { max-width: none; width: 100%; }
.upload-one.wide .el-upload-list__item-name { white-space: normal; word-break: break-all; }
.upload-one.wide .el-upload__tip { margin-left: 8px; }
</style>
