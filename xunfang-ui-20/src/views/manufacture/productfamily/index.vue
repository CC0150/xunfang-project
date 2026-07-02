<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="中文名称" prop="productFamilyNameCn">
        <el-input v-model="queryParams.productFamilyNameCn" placeholder="请输入中文名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="英文名称" prop="productFamilyNameEn">
        <el-input v-model="queryParams.productFamilyNameEn" placeholder="请输入英文名称" clearable @keyup.enter="handleQuery" />
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
    <el-table v-loading="loading" :data="familyList" @selection-change="handleSelectionChange" row-key="id">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="产品族编码" align="center"><template #default="{ row }">PF{{ row.id }}</template></el-table-column>
      <el-table-column label="中文名称" align="center" prop="productFamilyNameCn" />
      <el-table-column label="英文名称" align="center" prop="productFamilyNameEn" />
      <el-table-column label="描述" align="center" prop="description" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="familyRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="中文名称" prop="productFamilyNameCn">
          <el-input v-model="form.productFamilyNameCn" placeholder="请输入中文名称" />
        </el-form-item>
        <el-form-item label="英文名称" prop="productFamilyNameEn">
          <el-input v-model="form.productFamilyNameEn" placeholder="请输入英文名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="cancel">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="Family">
import { getCurrentInstance, ref, reactive, toRefs } from "vue"
import { listFamily, getFamily, addFamily, updateFamily, delFamily } from "@/api/manufacture/productfamily"
const { proxy } = getCurrentInstance()
const familyList = ref([]); const open = ref(false); const loading = ref(true); const showSearch = ref(true)
const ids = ref([]); const single = ref(true); const multiple = ref(true); const total = ref(0); const title = ref("")
const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, productFamilyNameCn: null, productFamilyNameEn: null },
  rules: {
    productFamilyNameCn: [{ required: true, message: "中文名称不能为空", trigger: "blur" }],
    productFamilyNameEn: [{ required: true, message: "英文名称不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)
function getList() { loading.value = true; listFamily(queryParams.value).then(r => { familyList.value = r.rows || []; total.value = r.total || 0 }).finally(() => loading.value = false) }
function cancel() { open.value = false; reset() }
function reset() { form.value = { id: null, productFamilyNameCn: null, productFamilyNameEn: null, description: null }; proxy.resetForm("familyRef") }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleSelectionChange(s) { ids.value = s.map(i => i.id); single.value = s.length != 1; multiple.value = !s.length }
function handleAdd() { reset(); open.value = true; title.value = "添加产品族" }
function handleUpdate(row) { reset(); getFamily(row.id).then(r => { form.value = r.data; open.value = true; title.value = "修改产品族" }) }
function submitForm() { proxy.$refs["familyRef"].validate(v => { if (v) (form.value.id ? updateFamily(form.value) : addFamily(form.value)).then(() => { proxy.$modal.msgSuccess("成功"); open.value = false; getList() }) }) }
function handleDelete(row) { const i = row.id ? [row.id] : ids.value; if (!i.length) return; proxy.$modal.confirm('确认删除？').then(() => delFamily(i)).then(() => { getList(); proxy.$modal.msgSuccess("删除成功") }).catch(() => {}) }
getList()
</script>
