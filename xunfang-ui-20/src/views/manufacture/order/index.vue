<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="95px" class="search-form">
      <el-form-item label="采购订单号" prop="purchaseOrderCode">
        <el-input v-model="queryParams.purchaseOrderCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="采购日期" prop="purchaseDateStart">
        <el-date-picker v-model="queryParams.purchaseDateStart" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width:150px" />
      </el-form-item>
      <el-form-item label="至" prop="purchaseDateEnd" label-width="30px">
        <el-date-picker v-model="queryParams.purchaseDateEnd" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width:150px" />
      </el-form-item>
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="物料编码" prop="materialCode">
        <el-input v-model="queryParams.materialCode" placeholder="请输入" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width:120px">
          <el-option label="待确认" value="1" /><el-option label="已确认" value="2" /><el-option label="已发货" value="3" /><el-option label="已完成" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleBatchDelete">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange" style="width:100%">
      <el-table-column type="selection" width="50" align="center" fixed="left" />
      <el-table-column label="序号" width="55" align="center" type="index" fixed="left" />
      <el-table-column label="采购订单号" width="160" align="center" prop="purchaseOrderCode" show-overflow-tooltip />
      <el-table-column label="采购日期" width="120" align="center">
        <template #default="{ row }">{{ parseTime(row.purchaseDate, '{y}-{m}-{d}') }}</template>
      </el-table-column>
      <el-table-column label="供应商名称" width="180" align="center" prop="supplierName" show-overflow-tooltip />
      <el-table-column label="供应商联系人" width="120" align="center" prop="supplierLinkMan" />
      <el-table-column label="物料编码" width="140" align="center" prop="materialCode" show-overflow-tooltip />
      <el-table-column label="物料名称" width="160" align="center" prop="materialName" show-overflow-tooltip />
      <el-table-column label="规格型号" width="160" align="center" prop="specificationsModels" show-overflow-tooltip />
      <el-table-column label="采购数量" width="100" align="center" prop="purchaseQuantity" />
      <el-table-column label="单位" width="70" align="center" prop="unit" />
      <el-table-column label="单价" width="110" align="center">
        <template #default="{ row }">¥{{ row.unitPrice }}</template>
      </el-table-column>
      <el-table-column label="总价" width="120" align="center">
        <template #default="{ row }">¥{{ row.totalPrice }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status==='1'?'info':row.status==='2'?'warning':row.status==='3'?'':row.status==='4'?'success':'info'">
            {{ statusMap[row.status] || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" width="150" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="210" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status==='1'">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleSingleDelete(row)">删除</el-button>
            <el-button link type="success" @click="changeStatus(row,'2')">确认</el-button>
          </template>
          <template v-else-if="row.status==='2'">
            <el-button link type="warning" @click="changeStatus(row,'3')">发货</el-button>
          </template>
          <template v-else-if="row.status==='3'">
            <el-button link type="success" @click="changeStatus(row,'4')">完成</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="采购订单号" prop="purchaseOrderCode"><el-input v-model="form.purchaseOrderCode" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采购日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="点击选择" readonly @click="openSup"><template #suffix><el-icon><Search /></el-icon></template></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="供应商联系人" prop="supplierLinkMan"><el-input v-model="form.supplierLinkMan" placeholder="自动填入" disabled /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="物料编码" prop="materialCode"><el-input v-model="form.materialCode" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物料名称" prop="materialName"><el-input v-model="form.materialName" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="规格型号" prop="specificationsModels"><el-input v-model="form.specificationsModels" placeholder="请输入" /></el-form-item>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="采购数量" prop="purchaseQuantity"><el-input-number v-model="form.purchaseQuantity" :min="0" :precision="2" @change="calcTotal" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单位" prop="unit"><el-input v-model="form.unit" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单价" prop="unitPrice"><el-input-number v-model="form.unitPrice" :min="0" :precision="2" @change="calcTotal" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="总价"><el-input v-model="form.totalPrice" disabled><template #prefix>¥</template></el-input></el-form-item>
        <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 供应商选择弹窗 -->
    <el-dialog title="选择供应商" v-model="supplierOpen" width="1000px" append-to-body>
      <el-form :inline="true"><el-form-item label="供应商名称"><el-input v-model="supQueryParams.supplierName" placeholder="请输入" clearable @keyup.enter="getSupList" /></el-form-item>
        <el-form-item><el-button type="primary" icon="Search" @click="getSupList">搜索</el-button></el-form-item>
      </el-form>
      <el-table v-loading="supLoading" :data="superlierList" @row-click="handleSupplierSelect" highlight-current-row style="width:100%">
        <el-table-column width="50" align="center"><template #default="{ row }"><el-radio v-model="selectedSupplierId" :value="row.id" @click.stop @change="handleSupplierSelect(row)">&nbsp;</el-radio></template></el-table-column>
        <el-table-column label="编码" prop="supplierCode" min-width="120" show-overflow-tooltip />
        <el-table-column label="名称" prop="supplierName" min-width="150" show-overflow-tooltip />
        <el-table-column label="联系人" prop="linkMan" min-width="90" />
        <el-table-column label="电话" prop="linkPhone" min-width="120" />
        <el-table-column label="邮箱" prop="linkEmail" min-width="160" show-overflow-tooltip />
      </el-table>
      <pagination v-show="supTotal>0" :total="supTotal" v-model:page="supQueryParams.pageNum" v-model:limit="supQueryParams.pageSize" @pagination="getSupList" />
      <template #footer><el-button @click="supplierOpen=false">取 消</el-button><el-button type="primary" @click="confirmSelectSupplier">确 定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCurrentInstance, ref, reactive, toRefs } from "vue"
import { Search } from '@element-plus/icons-vue'
import { listPurchaseOrder, getPurchaseOrder, delPurchaseOrder, addPurchaseOrder, updatePurchaseOrder } from "@/api/manufacture/purchaseorder"
import { listSupplier } from "@/api/manufacture/supplier"
const { proxy } = getCurrentInstance()

const statusMap = { "1": "待确认", "2": "已确认", "3": "已发货", "4": "已完成" }

const orderList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true)
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("")
const selectedRows = ref([])

const data = reactive({
  form: { status: "1" },
  queryParams: { pageNum: 1, pageSize: 10, purchaseOrderCode: null, purchaseDateStart: null, purchaseDateEnd: null, supplierName: null, materialCode: null, status: null },
  rules: { purchaseOrderCode: [{ required: true, message: "采购订单号不能为空", trigger: "blur" }], supplierName: [{ required: true, message: "请选择供应商", trigger: "change" }] }
})
const { queryParams, form, rules } = toRefs(data)

// 供应商选择
const supplierOpen = ref(false); const superlierList = ref([]); const selectedSupplierId = ref(null); const curRow = ref({})
const supLoading = ref(false); const supTotal = ref(0)
const supQueryParams = reactive({ pageNum: 1, pageSize: 10, supplierName: null })

function getList() { loading.value = true; listPurchaseOrder(queryParams.value).then(r => { orderList.value = r.rows || []; total.value = r.total || 0 }).finally(() => loading.value = false) }
function cancel() { open.value = false; reset() }
function reset() { form.value = { status: "1" }; proxy.resetForm("orderRef") }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryParams.value.purchaseDateStart = null; queryParams.value.purchaseDateEnd = null; proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(s) { selectedRows.value = s; ids.value = s.map(i => i.id); single.value = s.length != 1; multiple.value = !s.length }
function handleAdd() { reset(); open.value = true; title.value = "添加采购订单" }
function handleUpdate(row) { reset(); getPurchaseOrder(row.id).then(r => { form.value = r.data; open.value = true; title.value = "修改采购订单" }) }

function calcTotal() { form.value.totalPrice = ((form.value.purchaseQuantity || 0) * (form.value.unitPrice || 0)).toFixed(2) }

function submitForm() {
  proxy.$refs["orderRef"].validate(v => { if (!v) return; (form.value.id ? updatePurchaseOrder : addPurchaseOrder)(form.value).then(() => { proxy.$modal.msgSuccess("操作成功"); open.value = false; getList() }) })
}

function handleSingleDelete(row) { proxy.$modal.confirm("确认删除？").then(() => delPurchaseOrder(row.id)).then(() => { proxy.$modal.msgSuccess("删除成功"); getList() }).catch(() => {}) }
function handleBatchDelete() {
  if (!ids.value.length) { proxy.$modal.msgWarning("请选择数据"); return }
  proxy.$modal.confirm(`确认删除 ${ids.value.length} 条？`).then(() => delPurchaseOrder(ids.value.join(','))).then(() => { proxy.$modal.msgSuccess("删除成功"); getList() }).catch(() => {})
}

function changeStatus(row, newStatus) { updatePurchaseOrder({ ...row, status: newStatus }).then(() => { proxy.$modal.msgSuccess("状态更新成功"); getList() }) }

function openSup() { supplierOpen.value = true; supQueryParams.pageNum = 1; selectedSupplierId.value = null; curRow.value = {}; getSupList() }
function getSupList() { supLoading.value = true; listSupplier(supQueryParams).then(r => { superlierList.value = r.rows || []; supTotal.value = r.total || 0 }).finally(() => supLoading.value = false) }
function handleSupplierSelect(row) { curRow.value = row; selectedSupplierId.value = row.id }
function confirmSelectSupplier() {
  if (!curRow.value.id) { proxy.$modal.msgWarning("请选择供应商"); return }
  form.value.supplierName = curRow.value.supplierName; form.value.supplierLinkMan = curRow.value.linkMan; supplierOpen.value = false
}
getList()
</script>

<style scoped>
.search-form { margin-bottom: 18px; }
.search-form :deep(.el-form-item) { margin-bottom: 8px; }
</style>
