<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px" class="search-form">
      <el-form-item label="供应商编码" prop="supplierCode">
        <el-input v-model="queryParams.supplierCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="供应商类型" prop="supplierType">
        <el-select v-model="queryParams.supplierType" placeholder="请选择" clearable style="width:120px">
          <el-option label="原材料" value="1" /><el-option label="设备" value="2" /><el-option label="零件" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="合作状态" prop="cooperativeStatus">
        <el-select v-model="queryParams.cooperativeStatus" placeholder="请选择" clearable style="width:120px">
          <el-option label="合作中" value="1" /><el-option label="暂停" value="2" /><el-option label="终止" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="supplierList" @selection-change="handleSelectionChange" style="width:100%">
      <el-table-column type="selection" width="50" align="center" fixed="left" />
      <el-table-column label="序号" width="55" align="center" type="index" fixed="left" />
      <el-table-column label="供应商编码" width="140" align="center" prop="supplierCode" show-overflow-tooltip />
      <el-table-column label="供应商名称" width="180" align="center" prop="supplierName" show-overflow-tooltip />
      <el-table-column label="联系人" width="100" align="center" prop="linkMan" />
      <el-table-column label="联系电话" width="140" align="center" prop="linkPhone" />
      <el-table-column label="联系邮箱" width="200" align="center" prop="linkEmail" show-overflow-tooltip />
      <el-table-column label="供应商类型" width="110" align="center">
        <template #default="{ row }">{{ typeMap[row.supplierType] || '-' }}</template>
      </el-table-column>
      <el-table-column label="地址" width="220" align="center" prop="address" show-overflow-tooltip />
      <el-table-column label="供应范围" width="200" align="center" prop="scopeOfSupply" show-overflow-tooltip />
      <el-table-column label="合作状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.cooperativeStatus==='1'?'success':row.cooperativeStatus==='2'?'warning':'info'">
            {{ statusMap[row.cooperativeStatus] || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" width="150" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="650px" append-to-body>
      <el-form ref="supplierRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="供应商编码" prop="supplierCode"><el-input v-model="form.supplierCode" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商名称" prop="supplierName"><el-input v-model="form.supplierName" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="联系人" prop="linkMan"><el-input v-model="form.linkMan" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话" prop="linkPhone"><el-input v-model="form.linkPhone" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="联系邮箱" prop="linkEmail"><el-input v-model="form.linkEmail" placeholder="请输入" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商类型" prop="supplierType">
              <el-select v-model="form.supplierType" placeholder="请选择" style="width:100%">
                <el-option label="原材料" value="1" /><el-option label="设备" value="2" /><el-option label="零件" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作状态" prop="cooperativeStatus">
              <el-select v-model="form.cooperativeStatus" placeholder="请选择" style="width:100%">
                <el-option label="合作中" value="1" /><el-option label="暂停" value="2" /><el-option label="终止" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" prop="address"><el-input v-model="form.address" placeholder="请输入" /></el-form-item>
        <el-form-item label="供应范围" prop="scopeOfSupply"><el-input v-model="form.scopeOfSupply" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Supplier">
import { getCurrentInstance, ref, reactive, toRefs } from "vue"
import { listSupplier, getSupplier, delSupplier, addSupplier, updateSupplier } from "@/api/manufacture/supplier"
const { proxy } = getCurrentInstance()

const typeMap = { "1": "原材料", "2": "设备", "3": "零件" }
const statusMap = { "1": "合作中", "2": "暂停", "3": "终止" }

const supplierList = ref([]); const open = ref(false); const loading = ref(true)
const showSearch = ref(true); const ids = ref([]); const single = ref(true); const multiple = ref(true)
const total = ref(0); const title = ref("")

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, supplierCode: null, supplierName: null, supplierType: null, cooperativeStatus: null },
  rules: { supplierName: [{ required: true, message: "供应商名称不能为空", trigger: "blur" }] }
})
const { queryParams, form, rules } = toRefs(data)

function getList() { loading.value = true; listSupplier(queryParams.value).then(r => { supplierList.value = r.rows || []; total.value = r.total || 0 }).finally(() => loading.value = false) }
function cancel() { open.value = false; reset() }
function reset() { form.value = {}; proxy.resetForm("supplierRef") }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(s) { ids.value = s.map(i => i.id); single.value = s.length != 1; multiple.value = !s.length }
function handleAdd() { reset(); open.value = true; title.value = "添加供应商信息" }
function handleUpdate(row) { reset(); getSupplier(row.id || ids.value[0]).then(r => { form.value = r.data; open.value = true; title.value = "修改供应商信息" }) }
function submitForm() {
  proxy.$refs["supplierRef"].validate(v => { if (!v) return; (form.value.id ? updateSupplier : addSupplier)(form.value).then(() => { proxy.$modal.msgSuccess("操作成功"); open.value = false; getList() }) })
}
function handleDelete(row) {
  const id = row && typeof row === 'object' ? row.id : null
  const delIds = id ? [id] : ids.value
  if (!delIds.length) { proxy.$modal.msgWarning("请选择要删除的数据"); return }
  proxy.$modal.confirm(`确认删除 ${delIds.length} 条供应商？`).then(() => delSupplier(delIds.join(','))).then(() => { proxy.$modal.msgSuccess("删除成功"); getList() }).catch(() => {})
}
getList()
</script>

<style scoped>
.search-form { margin-bottom: 18px; }
.search-form :deep(.el-form-item) { margin-bottom: 8px; }
</style>
