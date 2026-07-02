package com.xunfang.manufacture.service.impl;

import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
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
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class XfPartServiceImpl implements IXfPartService
{
    private static final Logger log = LoggerFactory.getLogger(XfPartServiceImpl.class);
    @Autowired
    private DMEUtil dmeUtil;
    @Autowired
    private RedisCache1 redisCache;

    /** 本地文件存储根目录（与 xunfang-file 服务共用路径） */
    @Value("${xunfang.file.upload-dir:D:/xunfang/uploadPath}")
    private String uploadDir;

    /** Redis Hash key 前缀 */
    private static final String REDIS_FILE_KEY = "manufacture:part:file";

    // ==================== 工具方法 ====================

    private JSONObject callDmeApi(String url, JSONObject body, String token) throws Exception {
        String res = RequestUtil.requestsPost(url, body.toString(), token);
        if (res == null || res.trim().isEmpty()) {
            throw new RuntimeException("DME接口响应为空，url=" + url);
        }
        try {
            return new JSONObject(res);
        } catch (org.json.JSONException e) {
            log.error("DME响应JSON解析失败，url={}, response={}", url, res);
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
        if (enumObj instanceof String) {
            String s = (String) enumObj;
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
        }
        return defaultVal;
    }

    private JSONObject getRawPart(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/get";
        JSONObject p = new JSONObject(); p.put("id", id);
        JSONObject pj = new JSONObject(); pj.put("params", p);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        JSONArray dataArr = root.optJSONArray("data");
        if (dataArr == null || dataArr.length() == 0) {
            throw new RuntimeException("DME查询失败，id=" + id + "，响应: " + root.toString());
        }
        return dataArr.getJSONObject(0);
    }

    private JSONObject flattenEnums(JSONObject obj) {
        String[] fields = {"status", "partType", "purchaseOrManufacture", "workingState"};
        for (String f : fields) {
            try {
                Object enumVal = obj.opt(f);
                if (enumVal instanceof JSONObject) {
                    obj.put(f, pickAlias(enumVal, ""));
                }
            } catch (org.json.JSONException ignored) {}
        }
        return obj;
    }

    private String normalizeWorkingState(JSONObject obj) {
        String ws = null;
        JSONObject wsObj = obj.optJSONObject("workingState");
        if (wsObj != null) {
            ws = wsObj.optString("alias", wsObj.optString("enName", null));
        }
        if (ws == null || ws.isEmpty()) {
            ws = obj.optString("workingState", null);
        }
        if (ws == null || ws.isEmpty()) return "CHECKED_IN";
        String u = ws.toUpperCase();
        if ("INWORK".equals(u)) return "CHECKED_OUT";
        if ("CHECKED_IN".equals(u)) return "CHECKED_IN";
        if ("CHECKED_OUT".equals(u)) return "CHECKED_OUT";
        return u;
    }

    private Map<String, Object> jsonObjectToMap(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) return map;
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = obj.opt(key);
            if (val instanceof JSONObject) val = jsonObjectToMap((JSONObject) val);
            else if (val instanceof JSONArray) {
                JSONArray arr = (JSONArray) val;
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    Object item = arr.opt(i);
                    if (item instanceof JSONObject) item = jsonObjectToMap((JSONObject) item);
                    list.add(item);
                }
                val = list;
            }
            map.put(key, val);
        }
        return map;
    }

    private void addCond(JSONArray conditions, String name, String value, String operator) {
        try {
            JSONObject c = new JSONObject();
            c.put("conditionName", name);
            JSONArray v = new JSONArray(); v.put(value);
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", operator);
            conditions.put(c);
        } catch (org.json.JSONException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Redis 文件元数据 ====================

    /** Redis 存储格式：{"fileId":"uuid","fileName":"xxx.pdf"} */
    private void saveFileMetaToRedis(String instanceId, String fileId, String fileName) {
        try {
            JSONObject meta = new JSONObject();
            meta.put("fileId", fileId);
            meta.put("fileName", fileName);
            redisCache.hset(REDIS_FILE_KEY, instanceId, meta.toString());
            log.debug("文件元数据已存Redis: partId={}, fileId={}, fileName={}", instanceId, fileId, fileName);
        } catch (org.json.JSONException e) {
            log.error("保存文件元数据到Redis失败", e);
        }
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

    // ==================== 本地文件存储 ====================

    private String saveFileLocally(MultipartFile file) throws Exception {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        Path target = dir.resolve(fileId + ext);
        file.transferTo(target.toFile());
        log.info("文件已保存到本地: {}", target.toAbsolutePath());
        return fileId;
    }

    private void deleteLocalFile(String fileId) {
        if (fileId == null || fileId.isEmpty()) return;
        try {
            Path dir = Paths.get(uploadDir);
            if (Files.exists(dir)) {
                File[] files = dir.toFile().listFiles((d, name) -> name.startsWith(fileId));
                if (files != null) {
                    for (File f : files) { f.delete(); }
                }
            }
        } catch (Exception e) {
            log.warn("删除本地文件失败, fileId={}", fileId, e);
        }
    }

    /**
     * 填充单个 Part 行数据的文件信息
     * 优先级：Redis > DME extAttrs（实现 IDME 直传附件与本系统的双向同步）
     */
    private void fillFileInfo(XfVersionPart row, JSONObject raw) {
        String id = row.getId();
        if (id == null || id.isEmpty()) return;
        JSONObject meta = getFileMetaFromRedis(id);
        // instanceId 未命中，尝试 masterId
        if (meta == null && row.getMasterId() != null && !row.getMasterId().isEmpty()
                && !row.getMasterId().equals(id)) {
            meta = getFileMetaFromRedis(row.getMasterId());
            if (meta != null) {
                saveFileMetaToRedis(id, meta.optString("fileId"), meta.optString("fileName"));
            }
        }
        // Redis 仍未命中，从 DME extAttrs 提取（IDME 直传的文件）
        if (meta == null && raw != null) {
            meta = extractFileFromExtAttrs(raw);
            if (meta != null) {
                saveFileMetaToRedis(id, meta.optString("fileId"), meta.optString("fileName"));
            }
        }
        if (meta != null) {
            row.setFileId(meta.optString("fileId"));
            row.setFileName(meta.optString("fileName"));
            // 构造下载 URL
            row.setFileDownloadUrl("/manufacture/part/file/download"
                    + "?model_name=XfPart01_20"
                    + "&instance_id=" + id
                    + "&file_id=" + row.getFileId()
                    + "&attribute_name=File_20");
            // 提取无扩展名展示名
            String fn = row.getFileName();
            if (fn != null && !fn.isEmpty()) {
                int dot = fn.lastIndexOf('.');
                row.setFileNameNoExt(dot > 0 ? fn.substring(0, dot) : fn);
            }
        }
    }

    /** 从 DME extAttrs 中提取文件信息 */
    private JSONObject extractFileFromExtAttrs(JSONObject raw) {
        try {
            JSONArray extAttrs = raw.optJSONArray("extAttrs");
            if (extAttrs == null) return null;
            for (int i = 0; i < extAttrs.length(); i++) {
                JSONObject attr = extAttrs.getJSONObject(i);
                Object valObj = attr.opt("value");
                if (valObj instanceof JSONArray) {
                    JSONArray values = (JSONArray) valObj;
                    if (values.length() > 0) {
                        JSONObject fileObj = values.getJSONObject(0);
                        JSONObject meta = new JSONObject();
                        meta.put("fileId", fileObj.optString("id", fileObj.optString("fileId")));
                        meta.put("fileName", fileObj.optString("fileName", fileObj.optString("name")));
                        return meta;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析extAttrs失败: {}", e.getMessage());
        }
        return null;
    }

    // ==================== 查询详情 ====================

    @Override
    public XfVersionPart selectXfPartById(String id) throws Exception {
        JSONObject raw = getRawPart(id);
        flattenEnums(raw);

        XfVersionPart part = new XfVersionPart();
        part.setId(raw.optString("id"));
        part.setPartName(raw.optString("partName"));
        part.setPartNameEn(raw.optString("partNameEn"));
        part.setPartType(pickAlias(raw.opt("partType"), "Ma"));
        part.setSpecificationsModel(raw.optString("specificationsModel"));
        part.setUnit(raw.optString("unit"));
        part.setStatus(pickAlias(raw.opt("status"), "Enable"));
        part.setPurchaseOrManufacture(pickAlias(raw.opt("purchaseOrManufacture"), "Manu"));
        part.setPartDeclaration(raw.optString("partDeclaration"));
        part.setDisplayVersion(raw.optString("iteration", raw.optString("version", null)));

        JSONObject master = raw.optJSONObject("master");
        if (master != null) part.setMasterId(master.optString("id"));

        part.setUiWorkingState(normalizeWorkingState(raw));
        part.setCreator(raw.optString("creator"));
        part.setModifier(raw.optString("modifier"));
        String ct = raw.optString("createTime", null);
        if (ct == null || ct.isEmpty()) ct = raw.optString("lastUpdateTime", null);
        if (ct != null && !ct.isEmpty()) {
            try { part.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
        }

        // 从 Redis + DME extAttrs 填充文件信息
        fillFileInfo(part, raw);
        return part;
    }

    // ==================== 列表查询 ====================

    @Override
    public TableDataInfo selectXfPartList(XfVersionPart xfPart, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/find/" + pageSize + "/" + pageNum;

        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", true);
        params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());

        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "latest", "true", "=");

        if (xfPart.getPartName() != null && !xfPart.getPartName().isEmpty())
            addCond(conditions, "partName", xfPart.getPartName(), "like");
        if (xfPart.getPartType() != null && !xfPart.getPartType().isEmpty())
            addCond(conditions, "partType", xfPart.getPartType(), "=");
        if (xfPart.getStatus() != null && !xfPart.getStatus().isEmpty())
            addCond(conditions, "status", xfPart.getStatus(), "=");
        if (xfPart.getPurchaseOrManufacture() != null && !xfPart.getPurchaseOrManufacture().isEmpty())
            addCond(conditions, "purchaseOrManufacture", xfPart.getPurchaseOrManufacture(), "=");

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);

        JSONObject paramsJson = new JSONObject();
        paramsJson.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        JSONObject root = callDmeApi(url, paramsJson, tap.getToken());
        if (!"SUCCESS".equalsIgnoreCase(root.optString("result", "")))
            throw new RuntimeException("Query failed: " + root.toString());

        JSONArray dataArr = root.optJSONArray("data");
        List<XfVersionPart> rows = new ArrayList<>();

        if (dataArr != null) {
            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject item = dataArr.getJSONObject(i);
                flattenEnums(item);

                XfVersionPart row = new XfVersionPart();
                row.setId(item.optString("id"));
                row.setPartName(item.optString("partName"));
                row.setPartNameEn(item.optString("partNameEn"));
                row.setPartType(pickAlias(item.opt("partType"), ""));
                row.setSpecificationsModel(item.optString("specificationsModel"));
                row.setUnit(item.optString("unit"));
                row.setStatus(pickAlias(item.opt("status"), ""));
                row.setPurchaseOrManufacture(pickAlias(item.opt("purchaseOrManufacture"), ""));
                row.setPartDeclaration(item.optString("partDeclaration"));
                row.setDisplayVersion(item.optString("iteration", item.optString("version", null)));
                String ct = item.optString("createTime", null);
                if (ct == null || ct.isEmpty()) ct = item.optString("lastUpdateTime", null);
                if (ct != null && !ct.isEmpty()) {
                    try { row.setCreateTime(DMEUtil.dmeDateToExamDate(ct)); } catch (Exception ignored) {}
                }

                JSONObject master = item.optJSONObject("master");
                if (master != null) row.setMasterId(master.optString("id"));

                row.setUiWorkingState(normalizeWorkingState(item));

                // 从 Redis + DME extAttrs 填充文件信息
                fillFileInfo(row, item);

                rows.add(row);
            }
        }

        long total = rows.size();
        JSONObject pageInfo = root.optJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.has("totalRows")) {
            long tr = pageInfo.getLong("totalRows");
            if (tr > 0) total = tr;
        }

        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);
        tab.setRows(rows);
        tab.setTotal(total);
        return tab;
    }

    // ==================== 新增 ====================

    @Override
    public AjaxResult insertXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        xfPart.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        // 如果有文件，保存到本地
        String fileId = null;
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            try {
                fileId = saveFileLocally(file);
                fileName = file.getOriginalFilename();
            } catch (Exception e) {
                log.error("保存文件到本地失败", e);
                return AjaxResult.error("文件保存失败: " + e.getMessage());
            }
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/create";
        JSONObject params = new JSONObject();
        params.put("partName", xfPart.getPartName());
        params.put("partNameEn", xfPart.getPartNameEn() != null ? xfPart.getPartNameEn() : "");
        params.put("partType", xfPart.getPartType() != null ? xfPart.getPartType() : "Ma");
        params.put("specificationsModel", xfPart.getSpecificationsModel() != null ? xfPart.getSpecificationsModel() : "");
        params.put("unit", xfPart.getUnit() != null ? xfPart.getUnit() : "");
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : "Enable");
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture() != null ? xfPart.getPurchaseOrManufacture() : "Manu");
        params.put("partDeclaration", xfPart.getPartDeclaration() != null ? xfPart.getPartDeclaration() : "");
        params.put("createTime", DMEUtil.dateToUTCString(xfPart.getCreateTime()));
        params.put("master", new JSONObject());
        params.put("branch", new JSONObject());

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equals(root.optString("result", ""))) {
            // DME 创建成功后，获取 instanceId 并保存文件元数据到 Redis
            JSONArray dataArr = root.optJSONArray("data");
            if (fileId != null && dataArr != null && dataArr.length() > 0) {
                String instanceId = dataArr.getJSONObject(0).optString("id");
                saveFileMetaToRedis(instanceId, fileId, fileName);
            }
            return AjaxResult.success();
        }
        if (fileId != null) deleteLocalFile(fileId);
        return AjaxResult.error(root.optString("error_msg", "创建Part失败"));
    }

    // ==================== 修改 ====================

    @Override
    public AjaxResult updateXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        xfPart.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();

        JSONObject existing = getRawPart(xfPart.getId());

        // 处理附件
        String newFileId = null;
        String newFileName = null;
        boolean wantClear = Boolean.TRUE.equals(xfPart.getClearFile());

        // 获取旧文件信息
        String oldFileIdToDelete = null;
        JSONObject oldMeta = getFileMetaFromRedis(xfPart.getId());
        if (oldMeta != null) {
            oldFileIdToDelete = oldMeta.optString("fileId", null);
        }

        if (file != null && !file.isEmpty()) {
            try {
                newFileId = saveFileLocally(file);
                newFileName = file.getOriginalFilename();
            } catch (Exception e) {
                log.error("保存文件到本地失败", e);
                return AjaxResult.error("文件保存失败: " + e.getMessage());
            }
        }

        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/update";
        JSONObject params = new JSONObject();
        params.put("id", xfPart.getId());
        params.put("partName", xfPart.getPartName() != null ? xfPart.getPartName() : existing.optString("partName"));
        params.put("partNameEn", xfPart.getPartNameEn() != null ? xfPart.getPartNameEn() : existing.optString("partNameEn"));
        params.put("partType", xfPart.getPartType() != null ? xfPart.getPartType() : pickAlias(existing.opt("partType"), "Ma"));
        params.put("specificationsModel", xfPart.getSpecificationsModel() != null ? xfPart.getSpecificationsModel() : existing.optString("specificationsModel"));
        params.put("unit", xfPart.getUnit() != null ? xfPart.getUnit() : existing.optString("unit"));
        params.put("status", xfPart.getStatus() != null ? xfPart.getStatus() : pickAlias(existing.opt("status"), "Enable"));
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture() != null ? xfPart.getPurchaseOrManufacture() : pickAlias(existing.opt("purchaseOrManufacture"), "Manu"));
        params.put("partDeclaration", xfPart.getPartDeclaration() != null ? xfPart.getPartDeclaration() : existing.optString("partDeclaration"));
        params.put("updateTime", DMEUtil.dateToUTCString(xfPart.getUpdateTime()));
        params.put("modifier", existing.optString("modifier", existing.optString("creator", "gzlg020")));
        params.put("master", existing.optJSONObject("master") != null ? existing.getJSONObject("master") : new JSONObject());
        params.put("branch", existing.optJSONObject("branch") != null ? existing.getJSONObject("branch") : new JSONObject());

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equals(root.optString("result", ""))) {
            // 更新 Redis 文件元数据
            if (newFileId != null) {
                saveFileMetaToRedis(xfPart.getId(), newFileId, newFileName);
                if (oldFileIdToDelete != null) deleteLocalFile(oldFileIdToDelete);
            } else if (wantClear) {
                deleteFileMetaFromRedis(xfPart.getId());
                if (oldFileIdToDelete != null) deleteLocalFile(oldFileIdToDelete);
            }
            return AjaxResult.success();
        }
        if (newFileId != null) deleteLocalFile(newFileId);
        return AjaxResult.error(root.optString("error_msg", "修改Part失败"));
    }

    // ==================== 检出/检入 ====================

    @Override
    public AjaxResult checkOut(XfVersionPart xfVersionPart) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkout";
        JSONObject params = new JSONObject();
        params.put("masterId", xfVersionPart.getMasterId());
        params.put("workCopyType", xfVersionPart.getWorkCopyType() != null
                ? xfVersionPart.getWorkCopyType() : "BOTH");
        params.put("modifier", "gzlg020");
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", "检出失败"));
    }

    @Override
    public AjaxResult checkIn(XfVersionPart xfVersionPart) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/checkin";
        JSONObject params = new JSONObject();
        params.put("masterId", xfVersionPart.getMasterId());
        params.put("modifier", "gzlg020");
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", "检入失败"));
    }

    // ==================== 删除 ====================

    @Override
    public AjaxResult deleteXfPartByMasterIds(String[] masterIds) throws Exception {
        for (String masterId : masterIds) {
            try { cleanupPartFileByMasterId(masterId); } catch (Exception ignored) {}
        }
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/batchDelete";
        JSONObject params = new JSONObject();
        JSONArray idsArr = new JSONArray();
        for (String id : masterIds) idsArr.put(id);
        params.put("masterIds", idsArr);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", "批量删除失败"));
    }

    @Override
    public AjaxResult deleteXfPartByMasterId(String masterId) throws Exception {
        try { cleanupPartFileByMasterId(masterId); } catch (Exception ignored) {}
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/delete";
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            return AjaxResult.success();
        }
        return AjaxResult.error(root.optString("error_msg", "删除失败"));
    }

    /**
     * 删除 Part 时清理关联的本地文件和 Redis 元数据
     */
    private void cleanupPartFileByMasterId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.apiExecute + "/XfPart01_20/find/1/1";
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", true);
        params.put("isNeedTotal", false);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        addCond(conditions, "master.id", masterId, "=");
        addCond(conditions, "latest", "true", "=");
        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);

        JSONObject pj = new JSONObject(); pj.put("params", params);
        JSONObject root = callDmeApi(url, pj, tap.getToken());
        if ("SUCCESS".equalsIgnoreCase(root.optString("result", ""))) {
            JSONArray dataArr = root.optJSONArray("data");
            if (dataArr != null && dataArr.length() > 0) {
                String instanceId = dataArr.getJSONObject(0).optString("id");
                JSONObject meta = getFileMetaFromRedis(instanceId);
                if (meta != null) {
                    String oldFileId = meta.optString("fileId", null);
                    if (oldFileId != null && !oldFileId.isEmpty()) deleteLocalFile(oldFileId);
                    deleteFileMetaFromRedis(instanceId);
                }
            }
        }
    }

    // ==================== 文件下载 ====================

    @Override
    public void downloadFile(String fileId, String modelName, String instanceId,
                             String attributeName, String filename,
                             HttpServletResponse response) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        HttpURLConnection conn = dmeUtil.openDownloadConnection(
                fileId, modelName, attributeName, instanceId,
                dmeUtil.getApplicationId(), false, tap.getToken());

        int code = conn.getResponseCode();
        if (code == 200) {
            String ct = conn.getContentType();
            String cl = conn.getHeaderField("Content-Length");
            String disp = conn.getHeaderField("Content-Disposition");

            writeDownloadResponse(response, conn.getInputStream(), ct, cl, filename, disp, fileId);
            conn.disconnect();
            return;
        }

        if (code == 301 || code == 302) {
            String location = conn.getHeaderField("Location");
            conn.disconnect();
            if (location != null) {
                HttpURLConnection r2 = (HttpURLConnection) new java.net.URL(location).openConnection();
                r2.setConnectTimeout(10000);
                r2.setReadTimeout(60000);
                r2.connect();
                int r2Code = r2.getResponseCode();
                if (r2Code == 200) {
                    writeDownloadResponse(response, r2.getInputStream(),
                            r2.getContentType(), r2.getHeaderField("Content-Length"),
                            filename, r2.getHeaderField("Content-Disposition"), fileId);
                } else {
                    response.setStatus(r2Code);
                }
                r2.disconnect();
                return;
            }
        }

        response.setStatus(code);
        conn.disconnect();
    }

    private void writeDownloadResponse(HttpServletResponse resp, InputStream body,
                                       String ct, String cl, String queryFilename,
                                       String cd, String fileId) throws Exception {
        resp.setContentType(ct != null ? ct : "application/octet-stream");

        // 文件名优先级：前端传参 > Content-Disposition > fileId 兜底
        String finalName = (queryFilename != null && !queryFilename.trim().isEmpty())
                ? URLDecoder.decode(queryFilename, "UTF-8")
                : null;
        if (finalName == null && cd != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("filename\\*\\s*=\\s*[^']*''([^;]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(cd);
            if (m.find()) {
                try { finalName = URLDecoder.decode(m.group(1).replace("\"", "").trim(), "UTF-8"); }
                catch (Exception ignored) { finalName = m.group(1).replace("\"", "").trim(); }
            } else {
                m = java.util.regex.Pattern
                        .compile("filename\\s*=\\s*\"?([^\";]+)\"?", java.util.regex.Pattern.CASE_INSENSITIVE)
                        .matcher(cd);
                if (m.find()) finalName = m.group(1).trim();
            }
        }
        if (finalName == null || finalName.trim().isEmpty()) finalName = fileId;

        String asciiName = finalName.replaceAll("[^A-Za-z0-9._-]", "_");
        if (asciiName.isEmpty()) asciiName = "download";
        String encoded = URLEncoder.encode(finalName, "UTF-8").replace("+", "%20");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encoded);

        if (cl != null) resp.setHeader("Content-Length", cl);
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Content-Transfer-Encoding", "binary");

        byte[] buf = new byte[8192];
        int n;
        OutputStream os = resp.getOutputStream();
        while ((n = body.read(buf)) >= 0) os.write(buf, 0, n);
        os.flush();
    }
}
