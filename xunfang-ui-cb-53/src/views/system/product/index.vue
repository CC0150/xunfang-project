<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户id" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户id"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品名称" prop="productName">
        <el-input
          v-model="queryParams.productName"
          placeholder="请输入产品名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品描述" prop="productDescribe">
        <el-input
          v-model="queryParams.productDescribe"
          placeholder="请输入产品描述"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品类别" prop="category">
        <el-input
          v-model="queryParams.category"
          placeholder="请输入产品类别"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="产品规格" prop="productSpecification">
        <el-input
          v-model="queryParams.productSpecification"
          placeholder="请输入产品规格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="计量单位" prop="unitOfMeasurement">
        <el-input
          v-model="queryParams.unitOfMeasurement"
          placeholder="请输入计量单位"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生产材料明细" prop="productionMaterialDetail">
        <el-input
          v-model="queryParams.productionMaterialDetail"
          placeholder="请输入生产材料明细"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="制作工艺及工艺详情" prop="manufacturingDetails">
        <el-input
          v-model="queryParams.manufacturingDetails"
          placeholder="请输入制作工艺及工艺详情"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成本信息" prop="costInformation">
        <el-input
          v-model="queryParams.costInformation"
          placeholder="请输入成本信息"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="销售价格" prop="sellingPrice">
        <el-input
          v-model="queryParams.sellingPrice"
          placeholder="请输入销售价格"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="客户反馈" prop="customerFeedback">
        <el-input
          v-model="queryParams.customerFeedback"
          placeholder="请输入客户反馈"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:product:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:product:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:product:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:product:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column v-if="false" label="产品id" align="center" prop="id" />
      <el-table-column v-if="false" label="用户id" align="center" prop="userId" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="产品描述" align="center" prop="productDescribe" />
      <el-table-column label="产品类别" align="center" prop="category" />
      <el-table-column label="产品规格" align="center" prop="productSpecification" />
      <el-table-column label="计量单位" align="center" prop="unitOfMeasurement" />
      <el-table-column label="生产材料明细" align="center" prop="productionMaterialDetail" />
      <el-table-column label="制作工艺及工艺详情" align="center" prop="manufacturingDetails" />
      <el-table-column label="成本信息" align="center" prop="costInformation" />
      <el-table-column label="销售价格" align="center" prop="sellingPrice" />
      <el-table-column label="客户反馈" align="center" prop="customerFeedback" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:product:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:product:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改产品信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="productRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户id" prop="userId">
          <el-input v-model="form.userId" placeholder="请输入用户id" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="产品描述" prop="productDescribe">
          <el-input v-model="form.productDescribe" placeholder="请输入产品描述" />
        </el-form-item>
        <el-form-item label="产品类别" prop="category">
          <el-input v-model="form.category" placeholder="请输入产品类别" />
        </el-form-item>
        <el-form-item label="产品规格" prop="productSpecification">
          <el-input v-model="form.productSpecification" placeholder="请输入产品规格" />
        </el-form-item>
        <el-form-item label="计量单位" prop="unitOfMeasurement">
          <el-input v-model="form.unitOfMeasurement" placeholder="请输入计量单位" />
        </el-form-item>
        <el-form-item label="生产材料明细" prop="productionMaterialDetail">
          <el-input v-model="form.productionMaterialDetail" placeholder="请输入生产材料明细" />
        </el-form-item>
        <el-form-item label="制作工艺及工艺详情" prop="manufacturingDetails">
          <el-input v-model="form.manufacturingDetails" placeholder="请输入制作工艺及工艺详情" />
        </el-form-item>
        <el-form-item label="成本信息" prop="costInformation">
          <el-input v-model="form.costInformation" placeholder="请输入成本信息" />
        </el-form-item>
        <el-form-item label="销售价格" prop="sellingPrice">
          <el-input v-model="form.sellingPrice" placeholder="请输入销售价格" />
        </el-form-item>
        <el-form-item label="客户反馈" prop="customerFeedback">
          <el-input v-model="form.customerFeedback" placeholder="请输入客户反馈" />
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

<script setup name="Product">
import { listProduct, getProduct, delProduct, addProduct, updateProduct } from "@/api/system/product"

const { proxy } = getCurrentInstance()

const productList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: null,
    productName: null,
    productDescribe: null,
    category: null,
    productSpecification: null,
    unitOfMeasurement: null,
    productionMaterialDetail: null,
    manufacturingDetails: null,
    productStatus: null,
    costInformation: null,
    sellingPrice: null,
    customerFeedback: null,
    createUser: null,
    updateUser: null
  },
  rules: {
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询产品信息列表 */
function getList() {
  loading.value = true
  listProduct(queryParams.value).then(response => {
    productList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
function cancel() {
  open.value = false
  reset()
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    userId: null,
    productName: null,
    productDescribe: null,
    category: null,
    productSpecification: null,
    unitOfMeasurement: null,
    productionMaterialDetail: null,
    manufacturingDetails: null,
    productStatus: null,
    costInformation: null,
    sellingPrice: null,
    customerFeedback: null,
    createUser: null,
    updateUser: null
  }
  proxy.resetForm("productRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加产品信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getProduct(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改产品信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["productRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateProduct(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addProduct(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除产品信息编号为"' + _ids + '"的数据项？').then(function() {
    return delProduct(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/product/export', {
    ...queryParams.value
  }, `product_${new Date().getTime()}.xlsx`)
}

getList()
</script>
