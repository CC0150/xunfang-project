//package com.xunfang.system.controller;
//
//
//import com.xunfang.common.core.utils.poi.ExcelUtil;
//import com.xunfang.common.core.web.controller.BaseController;
//import com.xunfang.common.core.web.domain.AjaxResult;
//import com.xunfang.common.core.web.page.TableDataInfo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.List;
//
///**
// * 讯方产品信息Controller
// *
// * @author xunfang
// * @date 2024-08-14
// */
//@RestController
//@RequestMapping("/product")
//public class XfProductController extends BaseController
//{
//    @Autowired
//    private IXfProductService xfProductService;
//
//    /**
//     * 查询讯方产品信息列表
//     */
//    @GetMapping("/list")
//    public TableDataInfo list(HttpServletRequest request, XfProduct xfProduct) throws Exception {
///*        String userId = request.getHeader("userid")==null?"0":request.getHeader("userid");
//        xfProduct.setUserId(Long.parseLong(userId));*/
//        startPage();
//        List<XfProduct> list = xfProductService.selectXfProductList(xfProduct);
//        return getDataTable(list);
//    }
//
//    /**
//     * 导出讯方产品信息列表
//     */
//    @PostMapping("/export")
//    public void export(HttpServletRequest request,HttpServletResponse response, XfProduct xfProduct) throws Exception {
///*        String userId = request.getHeader("userid")==null?"0":request.getHeader("userid");
//        xfProduct.setUserId(Long.parseLong(userId));*/
//        List<XfProduct> list = xfProductService.selectXfProductList(xfProduct);
//        ExcelUtil<XfProduct> util = new ExcelUtil<XfProduct>(XfProduct.class);
//        util.exportExcel(response, list, "讯方产品信息数据");
//    }
//
//    /**
//     * 获取讯方产品信息详细信息
//     */
//    @GetMapping(value = "/{id}")
//    public AjaxResult getInfo(@PathVariable("id") Long id) throws Exception {
//        return success(xfProductService.selectXfProductById(id));
//    }
//
//    /**
//     * 新增讯方产品信息
//     */
//    @PostMapping("/add")
//    public AjaxResult add(HttpServletRequest request,@RequestBody XfProduct xfProduct) throws Exception {
//        /*String userId = request.getHeader("userid")==null?"0":request.getHeader("userid");
//        xfProduct.setUserId(Long.parseLong(userId));*/
//        return xfProductService.insertXfProduct(xfProduct);
//
//    }
//
//    /**
//     * 修改讯方产品信息
//     */
//    @PostMapping("/edit")
//    public AjaxResult edit(HttpServletRequest request,@RequestBody XfProduct xfProduct) throws Exception {
///*        String userId = request.getHeader("userid")==null?"0":request.getHeader("userid");
//        xfProduct.setUserId(Long.parseLong(userId));*/
//        return xfProductService.updateXfProduct(xfProduct);
//
//    }
//
//    /**
//     * 删除讯方产品信息
//     */
//	@DeleteMapping("/{ids}")
//    public AjaxResult remove(@PathVariable Long[] ids) throws Exception {
//        return xfProductService.deleteXfProductByIds(ids);
//    }
//
//    /**
//     * 删除讯方产品信息
//     */
//    @DeleteMapping("/delete/{id}")
//    public AjaxResult removeOne( @PathVariable("id") Long id) throws Exception {
//        return xfProductService.deleteXfProductById(id);
//    }
//}
