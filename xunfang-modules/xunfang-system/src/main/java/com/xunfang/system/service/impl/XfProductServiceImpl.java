//package com.xunfang.system.service.impl;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.xunfang.common.core.web.domain.AjaxResult;
//import com.xunfang.system.tools.DMEUtil;
//import com.xunfang.system.tools.RequestUtil;
//import com.xunfang.system.tools.TokenAndProject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//
///**
// * 讯方产品信息Service业务层处理
// *
// * @author xunfang
// * @date 2024-08-14
// */
//@Service
//public class XfProductServiceImpl implements IXfProductService
//{
//
//    @Autowired
//    private DMEUtil dmeUtil;
//
//    /**
//     * 查询讯方产品信息
//     *
//     * @param id 讯方产品信息主键
//     * @return 讯方产品信息
//     */
//    @Override
//    public XfProduct selectXfProductById(Long id) throws Exception {
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/get";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        params.put("id", id);
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        JSONObject jsonObject = JSONObject.parseObject(res);
//        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("data"));
//        String str = JSONObject.toJSONString(jsonArray);
//        return JSONObject.parseArray(str, XfProduct.class).get(0);
//    }
//
//
//    /**
//     * 查询讯方产品信息列表
//     *
//     * @param xfProduct 讯方产品信息
//     * @return 讯方产品信息
//     */
//    @Override
//    public List<XfProduct> selectXfProductList(XfProduct xfProduct) throws Exception {
//
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/find";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        JSONObject jsonObject = JSONObject.parseObject(res);
//        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("data"));
//        String str = JSONObject.toJSONString(jsonArray);
//        List<XfProduct> list = JSONObject.parseArray(str, XfProduct.class);
///*        List<XfProduct> result = list.stream()
//                .filter(p -> p.getUserId().equals(xfProduct.getUserId()))
//                .filter(p -> Optional.ofNullable(xfProduct.getProductName()).map(a -> p.getProductName().equals(xfProduct.getProductName())).orElse(true))
//                .filter(p -> Optional.ofNullable(xfProduct.getCategory()).map(b -> p.getCategory().equals(xfProduct.getCategory())).orElse(true))
//                .filter(p -> Optional.ofNullable(xfProduct.getUnitOfMeasurement()).map(c -> p.getUnitOfMeasurement().equals(xfProduct.getUnitOfMeasurement())).orElse(true))
//                .filter(p -> Optional.ofNullable(xfProduct.getProductStatus()).map(d -> p.getProductStatus().equals(xfProduct.getProductStatus())).orElse(true))
//                .filter(p -> Optional.ofNullable(xfProduct.getCostInformation()).map(e -> p.getCostInformation().equals(xfProduct.getCostInformation())).orElse(true))
//                .filter(p -> Optional.ofNullable(xfProduct.getSellingPrice()).map(f -> p.getSellingPrice().equals(xfProduct.getSellingPrice())).orElse(true))
//                .distinct().collect(Collectors.toList());*/
//        List<XfProduct> result = list.stream()
//                .filter(p -> Objects.equals(p.getUserId(), xfProduct.getUserId()))
//                .filter(p -> xfProduct.getProductName() == null || Objects.equals(p.getProductName(), xfProduct.getProductName()))
//                .filter(p -> xfProduct.getCategory() == null || Objects.equals(p.getCategory(), xfProduct.getCategory()))
//                .filter(p -> xfProduct.getUnitOfMeasurement() == null || Objects.equals(p.getUnitOfMeasurement(), xfProduct.getUnitOfMeasurement()))
//                .filter(p -> xfProduct.getProductStatus() == null || Objects.equals(p.getProductStatus(), xfProduct.getProductStatus()))
//                .filter(p -> xfProduct.getCostInformation() == null || Objects.equals(p.getCostInformation(), xfProduct.getCostInformation()))
//                .filter(p -> xfProduct.getSellingPrice() == null || Objects.equals(p.getSellingPrice(), xfProduct.getSellingPrice()))
//                .distinct()
//                .collect(Collectors.toList());
//
//        return result;
//    }
//
//    /**
//     * 新增讯方产品信息
//     *
//     * @param xfProduct 讯方产品信息
//     * @return 结果
//     */
//    @Override
//    public AjaxResult insertXfProduct(XfProduct xfProduct) throws Exception {
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/create";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        params.put("userId", xfProduct.getUserId());
//        params.put("productName", xfProduct.getProductName());
//        params.put("productDescribe", xfProduct.getProductDescribe());
//        params.put("category", xfProduct.getCategory());
//        params.put("productSpecification", xfProduct.getProductSpecification());
//        params.put("unitOfMeasurement", xfProduct.getUnitOfMeasurement());
//        params.put("productionMaterialDetail", xfProduct.getProductionMaterialDetail());
//        params.put("manufacturingDetails", xfProduct.getManufacturingDetails());
//        params.put("productStatus", xfProduct.getProductStatus());
//        params.put("costInformation", xfProduct.getCostInformation());
//        params.put("sellingPrice", xfProduct.getSellingPrice());
//        params.put("customerFeedback", xfProduct.getCustomerFeedback());
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        Object result = JSONObject.parseObject(res).get("result");
//        if("SUCCESS".equals(result.toString())){
//            return AjaxResult.success();
//        }else{
//            return AjaxResult.error();
//        }
//    }
//
//    /**
//     * 修改讯方产品信息
//     *
//     * @param xfProduct 讯方产品信息
//     * @return 结果
//     */
//    @Override
//    public AjaxResult updateXfProduct(XfProduct xfProduct) throws Exception {
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/update";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        params.put("id", xfProduct.getId());
//        params.put("userId", xfProduct.getUserId());
//        params.put("productName", xfProduct.getProductName());
//        params.put("productDescribe", xfProduct.getProductDescribe());
//        params.put("category", xfProduct.getCategory());
//        params.put("productSpecification", xfProduct.getProductSpecification());
//        params.put("unitOfMeasurement", xfProduct.getUnitOfMeasurement());
//        params.put("productionMaterialDetail", xfProduct.getProductionMaterialDetail());
//        params.put("manufacturingDetails", xfProduct.getManufacturingDetails());
//        params.put("productStatus", xfProduct.getProductStatus());
//        params.put("costInformation", xfProduct.getCostInformation());
//        params.put("sellingPrice", xfProduct.getSellingPrice());
//        params.put("customerFeedback", xfProduct.getCustomerFeedback());
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        Object result = JSONObject.parseObject(res).get("result");
//        if("SUCCESS".equals(result.toString())){
//            return AjaxResult.success();
//        }else{
//            return AjaxResult.error();
//        }
//    }
//
//    /**
//     * 批量删除讯方产品信息
//     *
//     * @param ids 需要删除的讯方产品信息主键
//     * @return 结果
//     */
//    @Override
//    public AjaxResult deleteXfProductByIds(Long[] ids) throws Exception {
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/batchDelete";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        params.put("ids", ids);
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        Object result = JSONObject.parseObject(res).get("result");
//        if("SUCCESS".equals(result.toString())){
//            return AjaxResult.success();
//        }else{
//            return AjaxResult.error();
//        }
//    }
//
//    /**
//     * 删除讯方产品信息信息
//     *
//     * @param id 讯方产品信息主键
//     * @return 结果
//     */
//    @Override
//    public AjaxResult deleteXfProductById(Long id) throws Exception {
//        TokenAndProject tap = dmeUtil.getToken();
//        String url = DMEUtil.projectUrl + "/dynamic/api/XfProduct/delete";
//        JSONObject paramsJson = new JSONObject();
//        JSONObject params = new JSONObject();
//        params.put("id", id);
//        paramsJson.put("params", params);
//        String res = RequestUtil.requestsPost(url, paramsJson.toString(), tap.getToken());
//        Object result = JSONObject.parseObject(res).get("result");
//        if("SUCCESS".equals(result.toString())){
//            return AjaxResult.success();
//        }else{
//            return AjaxResult.error();
//        }
//    }
//}
