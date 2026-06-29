package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
import com.xunfang.system.tools.DMEUtil;
import com.xunfang.system.tools.RedisCache1;
import com.xunfang.system.tools.RequestUtil;
import com.xunfang.system.tools.TokenAndProject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class XfProductServiceImpl implements IXfProductService
{
    private static final Logger log = LoggerFactory.getLogger(XfProductServiceImpl.class);
    @Autowired
    private DMEUtil dmeUtil;
    @Autowired
    private RedisCache1 redisCache;

    /** 本地文件存储根目录 */
    @Value("${xunfang.file.upload-dir:D:/xunfang/uploadPath}")
    private String uploadDir;

    private static final String ENTITY = "XfProduct_20";
    private static final String REDIS_FILE_KEY = "manufacture:product:file";
    private static final String REDIS_META_KEY = "manufacture:product:meta";

    // ==================== 通用工具 ====================

    private JSONObject callDmeApi(String url, JSONObject body, String token) throws Exception {
        String res = RequestUtil.requestsPost(url, body.toString(), token);
        if (res == null || res.trim().isEmpty())
            throw new RuntimeException("DME接口响应为空，url=" + url);
        try { return new JSONObject(res); }
        catch (org.json.JSONException e) {
            log.error("DME响应JSON解析失败，url={}", url, e);
            throw new RuntimeException("DME接口返回数据格式异常", e);
        }
    }

    private String pickAlias(Object enumObj, String defaultVal) {
        if (enumObj instanceof JSONObject) {
            JSONObject eo = (JSONObject) enumObj;
            String v = eo.optString("alias", null);
            if (v == null || v.isEmpty()) v = eo.optString("enName", null);
            if (v != null && !v.isEmpty()) return v;
        }
        if (enumObj instanceof String) { String s = (String) enumObj; if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s; }
        return defaultVal;
    }

    private void addCond(JSONArray conditions, String name, String value, String operator) {
        try {
            JSONObject c = new JSONObject(); c.put("conditionName", name);
            JSONArray v = new JSONArray(); v.put(value);
            c.put("conditionValues", v); c.put("ignoreStr", false); c.put("multi", false); c.put("operator", operator);
            conditions.put(c);
        } catch (org.json.JSONException e) { throw new RuntimeException(e); }
    }

    private String normalizeWorkingState(JSONObject obj) {
        JSONObject wsObj = obj.optJSONObject("workingState");
        String ws = null;
        if (wsObj != null) ws = wsObj.optString("alias", wsObj.optString("enName", null));
        if (ws == null || ws.isEmpty()) ws = obj.optString("workingState", null);
        if (ws == null || ws.isEmpty()) return "CHECKED_IN";
        String u = ws.toUpperCase();
        if ("INWORK".equals(u)) return "CHECKED_OUT";
        if ("CHECKED_IN".equals(u)) return "CHECKED_IN";
        if ("CHECKED_OUT".equals(u)) return "CHECKED_OUT";
        return u;
    }

    private JSONObject getRaw(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        JSONArray arr = root.optJSONArray("data");
        if (arr == null || arr.length() == 0) throw new RuntimeException("DME get failed for id=" + id);
        return arr.getJSONObject(0);
    }

    // ==================== Redis 文件元数据 ====================

    private void saveFileMetaToRedis(String instanceId, String fileId, String fileName) {
        try {
            JSONObject meta = new JSONObject();
            meta.put("fileId", fileId);
            meta.put("fileName", fileName);
            redisCache.hset(REDIS_FILE_KEY, instanceId, meta.toString());
        } catch (Exception e) { log.error("保存文件元数据到Redis失败", e); }
    }

    private JSONObject getFileMetaFromRedis(String instanceId) {
        Object val = redisCache.hget(REDIS_FILE_KEY, instanceId);
        if (val != null) {
            try { return new JSONObject(val.toString()); } catch (Exception ignored) {}
        }
        return null;
    }

    private void deleteFileMetaFromRedis(String instanceId) {
        redisCache.hdel(REDIS_FILE_KEY, instanceId);
    }

    /** 保存产品元数据到Redis（productFamily、lifecycleStateName等FIND不返回的字段） */
    private void saveMetaToRedis(String instanceId, String productFamily, String lifecycleStateName) {
        saveMetaToRedis(instanceId, productFamily, lifecycleStateName, null);
    }
    private void saveMetaToRedis(String instanceId, String productFamily, String lifecycleStateName, String createTimeStr) {
        try {
            JSONObject meta = new JSONObject();
            if (productFamily != null) meta.put("productFamily", productFamily);
            if (lifecycleStateName != null) meta.put("lifecycleStateName", lifecycleStateName);
            if (createTimeStr != null) meta.put("createTime", createTimeStr);
            redisCache.hset(REDIS_META_KEY, instanceId, meta.toString());
        } catch (Exception e) { log.error("保存产品元数据到Redis失败", e); }
    }

    private JSONObject getMetaFromRedis(String instanceId) {
        Object val = redisCache.hget(REDIS_META_KEY, instanceId);
        if (val != null) {
            try { return new JSONObject(val.toString()); } catch (Exception ignored) {}
        }
        return null;
    }

    private void deleteMetaFromRedis(String instanceId) {
        redisCache.hdel(REDIS_META_KEY, instanceId);
    }

    // ==================== 本地文件存储 ====================

    private String saveFileLocally(MultipartFile file) throws Exception {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains("."))
            ext = originalName.substring(originalName.lastIndexOf('.'));
        Path target = dir.resolve(fileId + ext);
        file.transferTo(target.toFile());
        log.info("产品文件已保存: {}", target.toAbsolutePath());
        return fileId;
    }

    private void deleteLocalFile(String fileId) {
        if (fileId == null || fileId.isEmpty()) return;
        try {
            Path dir = Paths.get(uploadDir);
            if (Files.exists(dir)) {
                File[] files = dir.toFile().listFiles((d, name) -> name.startsWith(fileId));
                if (files != null) for (File f : files) f.delete();
            }
        } catch (Exception e) { log.warn("删除本地文件失败, fileId={}", fileId, e); }
    }

    /** 自动从 GET API 获取元数据并缓存到 Redis（用于弥补 FIND 不返回的字段） */
    private void autoFillMetaFromGet(XfVersionProduct p) {
        try {
            System.err.println("[CT-DEBUG] autoFillMetaFromGet called for id=" + p.getId() + ", current createTime=" + p.getCreateTime());
            JSONObject raw = getRaw(p.getId());
            String family = raw.optString("productFamily", null);
            String lcName = null;
            JSONObject lcState = raw.optJSONObject("lifecycleState");
            if (lcState != null) {
                lcName = lcState.optString("name", null);
                if (lcName == null || lcName.isEmpty()) lcName = lcState.optString("nameEn", null);
            }
            // 获取原始创建时间
            JSONObject master = raw.optJSONObject("master");
            String ctStr = null;
            if (master != null) ctStr = master.optString("createTime", raw.optString("createTime", null));
            System.err.println("[CT-DEBUG] id=" + p.getId() + ", raw.ct=" + raw.optString("createTime","null") + ", master=" + (master!=null) + ", ctStr=" + ctStr);
            if (family != null && !family.isEmpty()) {
                p.setProductFamily(family);
            }
            if (lcName != null && !lcName.isEmpty()) {
                p.setLifecycleStateName(lcName);
            }
            if (ctStr != null && !ctStr.isEmpty()) {
                try { p.setCreateTime(DMEUtil.dmeDateToExamDate(ctStr)); } catch (Exception ignored) {}
            }
            saveMetaToRedis(p.getId(), family, lcName, ctStr);
        } catch (Exception e) {
            log.warn("autoFillMeta failed for id={}: {}", p.getId(), e.getMessage());
        }
    }

    /** 从 Redis 填充文件信息 + 元数据（productFamily、lifecycleStateName） */
    private void fillFromRedis(XfVersionProduct p) {
        String id = p.getId();
        if (id == null || id.isEmpty()) return;

        // 文件信息
        JSONObject fileMeta = getFileMetaFromRedis(id);
        if (fileMeta != null) {
            p.setFileId(fileMeta.optString("fileId"));
            p.setFileName(fileMeta.optString("fileName"));
            p.setFileDownloadUrl("/manufacture/file/download"
                    + "?model_name=" + ENTITY
                    + "&instance_id=" + id
                    + "&file_id=" + p.getFileId()
                    + "&attribute_name=file");
            String fn = p.getFileName();
            if (fn != null && !fn.isEmpty()) {
                int dot = fn.lastIndexOf('.');
                p.setFileNameNoExt(dot > 0 ? fn.substring(0, dot) : fn);
            }
        }

        // 元数据（FIND不返回的字段：productFamily、lifecycleStateName、createTime）
        JSONObject meta = getMetaFromRedis(id);
        if (meta != null) {
            if (p.getProductFamily() == null || p.getProductFamily().isEmpty()) {
                p.setProductFamily(meta.optString("productFamily", null));
            }
            if (p.getLifecycleStateName() == null || p.getLifecycleStateName().isEmpty()) {
                p.setLifecycleStateName(meta.optString("lifecycleStateName", null));
            }
            String cachedCt = meta.optString("createTime", null);
            if (cachedCt != null && !cachedCt.isEmpty() && p.getCreateTime() == null) {
                try { p.setCreateTime(DMEUtil.dmeDateToExamDate(cachedCt)); } catch (Exception ignored) {}
            }
        }
    }

    // ==================== 查询 ====================

    @Override
    public XfVersionProduct selectXfProductById(String id) throws Exception {
        JSONObject raw = getRaw(id);
        XfVersionProduct p = mapToProduct(raw);
        // 同步GET拿到的完整数据到Redis，这样列表FIND时也能读取
        String ctStr = null;
        if (p.getCreateTime() != null) ctStr = DMEUtil.dateToUTCString(p.getCreateTime());
        saveMetaToRedis(p.getId(), p.getProductFamily(), p.getLifecycleStateName(), ctStr);
        fillFromRedis(p);
        return p;
    }

    private XfVersionProduct mapToProduct(JSONObject raw) {
        XfVersionProduct p = new XfVersionProduct();
        p.setId(raw.optString("id"));
        p.setProductName(raw.optString("productName"));
        String family = raw.optString("productFamily");
        p.setProductFamily((family != null && !family.isEmpty() && !"null".equals(family)) ? family : null);
        p.setCategory(pickAlias(raw.opt("category"), ""));
        p.setSpecificationModels(raw.optString("specificationModels"));
        p.setProductDescribe(raw.optString("productDescribe"));
        p.setDisplayVersion(raw.optString("iteration", raw.optString("version", null)));
        p.setUiWorkingState(normalizeWorkingState(raw));

        JSONObject master = raw.optJSONObject("master");
        if (master != null) p.setMasterId(master.optString("id"));

        JSONObject lcState = raw.optJSONObject("lifecycleState");
        if (lcState != null) {
            // lifecycleState 没有 alias，用 name/nameEn
            String name = lcState.optString("name", null);
            if (name == null || name.isEmpty()) name = lcState.optString("nameEn", "");
            p.setLifecycleStateName(name);
            JSONObject lcTemplate = raw.optJSONObject("lifecycleTemplate");
            if (lcTemplate != null) {
                p.setLifecycleTemplateId(lcTemplate.optString("id"));
            }
        }

        // 创建时间：raw.createTime → raw.lastUpdateTime → master.createTime
        String ct = raw.optString("createTime", null);
        if (ct == null || ct.isEmpty() || "null".equals(ct)) ct = raw.optString("lastUpdateTime", null);
        if ((ct == null || ct.isEmpty() || "null".equals(ct)) && master != null) {
            ct = master.optString("createTime", master.optString("lastUpdateTime", null));
        }
        if (ct != null && !ct.isEmpty() && !"null".equals(ct)) {
            try { p.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
        }
        return p;
    }

    @Override
    public TableDataInfo selectXfProductList(XfVersionProduct product, HttpServletRequest request) throws Exception {
        String ps = request.getParameter("pageSize"); if (ps == null || ps.isEmpty()) ps = "10";
        String pn = request.getParameter("pageNum"); if (pn == null || pn.isEmpty()) pn = "1";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/find/" + ps + "/" + pn;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isPresentAll", true); params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA"); params.put("sorts", new JSONArray());

        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "latest", "true", "=");
        if (product.getProductName() != null && !product.getProductName().isEmpty())
            addCond(conditions, "productName", product.getProductName(), "like");
        if (product.getProductFamily() != null && !product.getProductFamily().isEmpty())
            addCond(conditions, "productFamily", product.getProductFamily(), "like");
        if (product.getCategory() != null && !product.getCategory().isEmpty())
            addCond(conditions, "category", product.getCategory(), "=");
        filter.put("conditions", conditions); filter.put("ignoreStr", false); filter.put("joiner", "and"); filter.put("multi", false);
        params.put("filter", filter);

        JSONObject pj = new JSONObject(); pj.put("params", params);
        TokenAndProject tap = dmeUtil.getToken();
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if (!"SUCCESS".equalsIgnoreCase(root.optString("result", ""))) throw new RuntimeException("Query failed");

        JSONArray dataArr = root.optJSONArray("data");
        List<XfVersionProduct> rows = new ArrayList<>();
        if (dataArr != null) {
            for (int i = 0; i < dataArr.length(); i++) {
                XfVersionProduct row = mapToProduct(dataArr.getJSONObject(i));
                fillFromRedis(row);
                // 如果Redis中没有元数据，尝试从GET获取（并自动缓存到Redis）
                if ((row.getProductFamily() == null || row.getProductFamily().isEmpty())
                        || (row.getLifecycleStateName() == null || row.getLifecycleStateName().isEmpty())
                        || row.getCreateTime() == null) {
                    try { autoFillMetaFromGet(row); } catch (Exception ignored) {}
                }
                rows.add(row);
            }
        }
        long total = rows.size();
        JSONObject pi = root.optJSONObject("pageInfo");
        if (pi != null && pi.has("totalRows")) { long tr = pi.getLong("totalRows"); if (tr > 0) total = tr; }
        TableDataInfo t = new TableDataInfo(); t.setCode(200); t.setRows(rows); t.setTotal(total);
        return t;
    }

    // ==================== 新增 ====================

    @Override
    public AjaxResult insertXfProduct(XfVersionProduct p, MultipartFile file) throws Exception {
        p.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        // 文件保存到本地
        String fileId = null, fileName = null;
        if (file != null && !file.isEmpty()) {
            try { fileId = saveFileLocally(file); fileName = file.getOriginalFilename(); }
            catch (Exception e) { log.error("保存文件失败", e); return AjaxResult.error("文件保存失败: " + e.getMessage()); }
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/create";
        JSONObject params = new JSONObject();
        params.put("productName", p.getProductName());
        params.put("productFamily", p.getProductFamily());
        params.put("category", p.getCategory());
        params.put("specificationModels", nvl(p.getSpecificationModels()));
        params.put("productDescribe", nvl(p.getProductDescribe()));
        params.put("createTime", DMEUtil.dateToUTCString(p.getCreateTime()));
        params.put("master", new JSONObject());
        params.put("branch", new JSONObject());
        if (p.getLifecycleTemplate() != null) {
            params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        } else if (p.getLifecycleTemplateId() != null && !p.getLifecycleTemplateId().isEmpty()) {
            params.put("lifecycleTemplate", new JSONObject().put("id", p.getLifecycleTemplateId()));
        }
        if (p.getLifecycleState() != null) {
            params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        } else if (p.getLifecycleStateId() != null && !p.getLifecycleStateId().isEmpty()) {
            params.put("lifecycleState", new JSONObject().put("id", p.getLifecycleStateId()));
        }

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equals(root.optString("result", ""))) {
            // 获取新建实例的ID
            JSONArray dataArr = root.optJSONArray("data");
            String instanceId = (dataArr != null && dataArr.length() > 0) ? dataArr.getJSONObject(0).optString("id") : null;
            // 保存文件+元数据到 Redis
            if (fileId != null && instanceId != null)
                saveFileMetaToRedis(instanceId, fileId, fileName);
            // 保存产品族和生命周期信息（FIND不返回这些字段）
            String lcName = null;
            if (p.getLifecycleState() != null) {
                lcName = (String) p.getLifecycleState().getOrDefault("name", p.getLifecycleState().getOrDefault("alias", ""));
            }
            if (instanceId != null) {
                // 通过GET获取生命周期状态名（DME create 可能不返回）
                if (lcName == null) {
                    try {
                        JSONObject raw = getRaw(instanceId);
                        JSONObject ls = raw.optJSONObject("lifecycleState");
                        if (ls != null) lcName = ls.optString("name", ls.optString("nameEn", null));
                    } catch (Exception ignored) {}
                }
                saveMetaToRedis(instanceId, p.getProductFamily(), lcName);
            }
            return AjaxResult.success();
        }
        if (fileId != null) deleteLocalFile(fileId);
        return AjaxResult.error(root.optString("error_msg", "创建产品失败"));
    }

    // ==================== 修改 ====================

    @Override
    public AjaxResult updateXfProduct(XfVersionProduct p, MultipartFile file) throws Exception {
        p.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        JSONObject existing = getRaw(p.getId());

        // 附件处理
        String newFileId = null, newFileName = null;
        boolean wantClear = Boolean.TRUE.equals(p.getClearFile());
        String oldFileIdToDelete = null;
        JSONObject oldMeta = getFileMetaFromRedis(p.getId());
        if (oldMeta != null) oldFileIdToDelete = oldMeta.optString("fileId", null);

        if (file != null && !file.isEmpty()) {
            try { newFileId = saveFileLocally(file); newFileName = file.getOriginalFilename(); }
            catch (Exception e) { log.error("保存文件失败", e); return AjaxResult.error("文件保存失败: " + e.getMessage()); }
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/update";
        JSONObject params = new JSONObject();
        params.put("id", p.getId());
        params.put("productName", nvlOr(p.getProductName(), existing.optString("productName")));
        params.put("productFamily", nvlOr(p.getProductFamily(), existing.optString("productFamily")));
        params.put("category", nvlOr(p.getCategory(), existing.optString("category")));
        params.put("specificationModels", nvlOr(p.getSpecificationModels(), existing.optString("specificationModels")));
        params.put("productDescribe", nvlOr(p.getProductDescribe(), existing.optString("productDescribe")));
        params.put("updateTime", DMEUtil.dateToUTCString(p.getUpdateTime()));
        params.put("modifier", existing.optString("modifier", existing.optString("creator", "gzlg020")));
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : new JSONObject());
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : new JSONObject());

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equals(root.optString("result", ""))) {
            if (newFileId != null) {
                saveFileMetaToRedis(p.getId(), newFileId, newFileName);
                if (oldFileIdToDelete != null) deleteLocalFile(oldFileIdToDelete);
            } else if (wantClear) {
                deleteFileMetaFromRedis(p.getId());
                if (oldFileIdToDelete != null) deleteLocalFile(oldFileIdToDelete);
            }
            // 更新元数据到Redis
            if (p.getProductFamily() != null && !p.getProductFamily().isEmpty())
                saveMetaToRedis(p.getId(), p.getProductFamily(), null);
            return AjaxResult.success();
        }
        if (newFileId != null) deleteLocalFile(newFileId);
        return AjaxResult.error(root.optString("error_msg", "修改产品失败"));
    }

    // ==================== 检出/检入/状态更新 ====================

    @Override
    public AjaxResult checkOut(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/checkout";
        JSONObject params = new JSONObject();
        params.put("masterId", p.getMasterId());
        params.put("workCopyType", p.getWorkCopyType() != null ? p.getWorkCopyType() : "BOTH");
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result", "")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "检出失败"));
    }

    @Override
    public AjaxResult checkIn(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/checkin";
        JSONObject params = new JSONObject();
        params.put("masterId", p.getMasterId());
        params.put("viewNo", p.getViewNo() != null ? p.getViewNo() : "");
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result", "")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "检入失败"));
    }

    @Override
    public AjaxResult updateLifecycleStatus(XfVersionProduct p) throws Exception {
        return updateByAdmin(p);
    }

    @Override
    public AjaxResult updateByAdmin(XfVersionProduct p) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        // 根据文档：INWORK→update，否则→updateByAdmin
        boolean isInWork = "INWORK".equalsIgnoreCase(p.getWorkingState());
        String path = isInWork ? "/update" : "/updateByAdmin";
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + path;
        JSONObject params = new JSONObject();
        params.put("id", p.getId());
        params.put("workingState", p.getWorkingState() != null ? p.getWorkingState() : "CHECKED_IN");
        if (p.getLifecycleTemplate() != null) params.put("lifecycleTemplate", new JSONObject(p.getLifecycleTemplate()));
        else if (p.getLifecycleTemplateId() != null && !p.getLifecycleTemplateId().isEmpty())
            params.put("lifecycleTemplate", new JSONObject().put("id", p.getLifecycleTemplateId()));
        if (p.getLifecycleState() != null) params.put("lifecycleState", new JSONObject(p.getLifecycleState()));
        else if (p.getLifecycleStateId() != null && !p.getLifecycleStateId().isEmpty())
            params.put("lifecycleState", new JSONObject().put("id", p.getLifecycleStateId()));
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result", "")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "更新生命周期状态失败"));
    }

    // ==================== 删除 ====================

    @Override
    public AjaxResult deleteXfProductByMasterIds(String[] masterIds) throws Exception {
        for (String masterId : masterIds) {
            try { cleanupProductFileByMasterId(masterId); } catch (Exception ignored) {}
        }
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/batchDelete";
        JSONArray arr = new JSONArray(); for (String id : masterIds) arr.put(id);
        JSONObject params = new JSONObject(); params.put("masterIds", arr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result", "")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "批量删除失败"));
    }

    @Override
    public AjaxResult deleteXfProductByMasterId(String masterId) throws Exception {
        try { cleanupProductFileByMasterId(masterId); } catch (Exception ignored) {}
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/delete";
        JSONObject params = new JSONObject(); params.put("masterId", masterId);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        return "SUCCESS".equalsIgnoreCase(root.optString("result", "")) ? AjaxResult.success()
                : AjaxResult.error(root.optString("error_msg", "删除失败"));
    }

    /** 删除产品时清理关联的本地文件和 Redis 元数据 */
    private void cleanupProductFileByMasterId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/" + ENTITY + "/find/1/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8"); params.put("decrypt", false);
        params.put("isPresentAll", true); params.put("isNeedTotal", false);
        params.put("publicData", "INCLUDE_PUBLIC_DATA"); params.put("sorts", new JSONArray());
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "master.id", masterId, "=");
        addCond(conditions, "latest", "true", "=");
        filter.put("conditions", conditions); filter.put("ignoreStr", false); filter.put("joiner", "and"); filter.put("multi", false);
        params.put("filter", filter);

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            JSONArray dataArr = root.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                String instanceId = dataArr.getJSONObject(0).optString("id");
                JSONObject meta = getFileMetaFromRedis(instanceId);
                if (meta != null) {
                    String oldId = meta.optString("fileId", null);
                    if (oldId != null && !oldId.isEmpty()) deleteLocalFile(oldId);
                    deleteFileMetaFromRedis(instanceId);
                }
                deleteMetaFromRedis(instanceId);
            }
        }
    }

    // helpers
    private String nvl(String s) { return s != null ? s : ""; }
    private String nvlOr(String s, String def) { return (s != null && !s.isEmpty()) ? s : def; }
}
