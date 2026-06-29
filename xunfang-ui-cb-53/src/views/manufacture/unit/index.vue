<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="单位名称" prop="unitName" label-width="auto">
        <el-input v-model="queryParams.unitName" placeholder="请输入单位名称" clearable @keyup.enter="handleQuery" />
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="unitList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单位编码" align="center">
        <template #default="scope">UN{{ scope.row.id }}</template>
      </el-table-column>
      <el-table-column label="单位名称" align="center" prop="unitName" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bottom">
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增弹窗（动态多行） -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <div v-for="(item, index) in units" :key="index" class="unit-item">
        <span class="unit-index">{{ index + 1 }}</span>
        <el-input v-model="item.name" placeholder="单位名称" style="width:200px;margin-right:10px" />
        <el-button v-if="units.length > 1" type="danger" icon="Minus" circle size="small" @click="removeUnit(index)" />
      </div>
      <div style="margin-top:10px">
        <el-button type="primary" icon="Plus" circle size="small" @click="addUnitRow" />
      </div>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Unit">
import { listUnit, delUnit, addUnit } from "@/api/manufacture/unit"

const { proxy } = getCurrentInstance()
const unitList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const units = ref([{ name: "" }])

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, unitName: null },
  rules: {}
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listUnit(queryParams.value).then(r => {
    unitList.value = r.rows || []
    total.value = r.total || 0
    loading.value = false
  })
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(s) { ids.value = s.map(i => i.id); single.value = s.length != 1; multiple.value = !s.length }

/** 新增按钮 — 打开动态多行弹窗 */
function handleAdd() {
  units.value = [{ name: "" }]
  open.value = true
  title.value = "添加单位"
}

/** 动态添加一行 */
function addUnitRow() { units.value.push({ name: "" }) }

/** 删除一行 */
function removeUnit(index) { units.value.splice(index, 1) }

/** 提交 — 一次性传数组 */
async function submitForm() {
  try {
    const payload = units.value.map(u => {
      if (!u.name) {
        proxy.$modal.msgError("单位名称不能为空")
        throw new Error("单位名称不能为空")
      }
      return { unitName: u.name }
    })
    await addUnit(payload)
    proxy.$modal.msgSuccess("新增成功")
    open.value = false
    getList()
  } catch (e) {
    proxy.$modal.msgError("提交失败")
  }
}

/** 删除 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm(`是否确认删除单位名称为"${row.unitName}"的数据项？`)
    .then(() => delUnit(_ids))
    .then(() => { getList(); proxy.$modal.msgSuccess("删除成功") })
    .catch(() => {})
}

getList()
</script>

<style scoped>
.pagination-bottom { padding: 10px 0 20px; display: flex; justify-content: flex-end; }
.unit-item { display: flex; align-items: center; margin-bottom: 10px; }
.unit-index { width: 20px; display: inline-block; }
</style>
