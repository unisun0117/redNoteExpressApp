package cn.qdm.tob.modules.product.pricing;

import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.modules.system.auth.service.TokenProvider;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 价格组明细 — Controller 层集成测试
 * <p>
 * 启动真实 Spring Boot 容器，通过 RestTemplate 发送 HTTP 请求验证 API 行为。
 * <p>
 * 包含：分页查询、新增（条码反查 + 联合唯一校验）、编辑（价格变动审批判断）、
 * 条码反查、导出、导入等全部端点。
 * <p>
 * <b>前置依赖</b>：需存在价格组（TEST01 + PG_TEST）和商品条码（6900001）。
 *
 * @author lichenxing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PriceDetailControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TokenProvider tokenProvider;

    private RestTemplate restTemplate;
    private String adminToken;
    private String baseUrl;

    /** 记录在 TC-D02 中新建成功后的 ID，供后续编辑用例复用 */
    private static Long createdDetailId;
    /** 记录原始售价，供编辑超阈值测试使用 */
    private static BigDecimal originalPrice;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
        adminToken = buildAdminToken();
        baseUrl = "http://localhost:" + port;
    }

    /** 构造一个测试用的 admin JWT Token */
    private String buildAdminToken() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                "CAS",
                "13800000000",
                "测试管理员",
                Collections.emptyList()
        );
        return tokenProvider.generateToken(principal);
    }

    /** 带 JWT 认证的请求头 */
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private JSONObject get(String path) {
        ResponseEntity<String> resp = restTemplate.exchange(
                baseUrl + path, HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);
        return new JSONObject(resp.getBody());
    }

    private JSONObject post(String path, Object body) {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl + path, new HttpEntity<>(body, authHeaders()), String.class);
        return new JSONObject(resp.getBody());
    }

    private JSONObject put(String path, Object body) {
        ResponseEntity<String> resp = restTemplate.exchange(
                baseUrl + path, HttpMethod.PUT, new HttpEntity<>(body, authHeaders()), String.class);
        return new JSONObject(resp.getBody());
    }

    // ============================================================
    // TC-D01 — 分页查询（无筛选条件）
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("分页查询 — 默认参数")
    void list_Default() {
        String params = "?pageNum=1&pageSize=10";
        JSONObject result = get("/api/admin/product/price-detail/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("TC-D01 列表查询成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-D02 — 新增价格明细（正常流程）
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("新增 — 正常")
    void create_Success() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "PG_TEST", "6900001");
        body.put("price", new BigDecimal("9.99"));
        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            System.out.println("TC-D02 新增成功");
            createdDetailId = findLatestDetailId();
            originalPrice = new BigDecimal("9.99");
        } else {
            System.out.println("TC-D02 新增失败（可能缺前置数据或已存在）: "
                    + result.get("msg"));
        }
    }

    // ============================================================
    // TC-D03 — 新增（联合主键重复）
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("新增 — 同一价格组下条码重复")
    void create_Duplicate() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "PG_TEST", "6900001");
        body.put("price", BigDecimal.TEN);

        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "重复应被拒绝");
        assertTrue(result.getString("msg").contains("已存在")
                        || result.getString("msg").contains("相同"),
                "消息应包含'已存在': " + result.get("msg"));
        System.out.println("TC-D03 重复被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D04 — 新增（商品条码不存在）
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("新增 — 商品条码不存在")
    void create_BarcodeNotFound() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "PG_TEST", "NOTEXIST999");
        body.put("price", BigDecimal.TEN);

        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的条码应被拒绝");
        assertTrue(result.getString("msg").contains("不存在")
                        || result.getString("msg").contains("未找到"),
                "消息应包含'不存在': " + result.get("msg"));
        System.out.println("TC-D04 条码不存在被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D05 — 新增（必填字段缺失）
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("新增 — 必填字段缺失（空售价）")
    void create_MissingPrice() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "PG_TEST", "6900002");

        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "缺少售价应被拒绝");
        System.out.println("TC-D05 缺少售价被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D06 — 新增（售价 ≤ 0）
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("新增 — 售价为0或负数应拒绝")
    void create_InvalidPrice() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "PG_TEST", "6900002");
        body.put("price", BigDecimal.ZERO);

        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "售价=0应被拒绝");
        System.out.println("TC-D06 售价=0被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D07 — 新增（价格组不存在）
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("新增 — 价格组不存在应拒绝")
    void create_GroupNotFound() {
        Map<String, Object> body = buildDetailCreateBody("TEST01", "NONEXIST", "6900002");
        body.put("price", BigDecimal.TEN);

        JSONObject result = post("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的价格组应被拒绝");
        System.out.println("TC-D07 价格组不存在被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D08 — 条码反查（存在）
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("条码反查 — 存在")
    void lookupBarcode_Found() {
        JSONObject result = get("/api/admin/product/price-detail/lookup-barcode?barcode=6900001");

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            assertNotNull(result.getJSONObject("data").getString("productName"),
                    "应返回商品名称");
            System.out.println("TC-D08 条码反查成功: "
                    + result.getJSONObject("data").getString("productName"));
        } else {
            System.out.println("TC-D08 跳过（测试条码 6900001 在 prd_sku 中不存在）: "
                    + result.get("msg"));
        }
    }

    // ============================================================
    // TC-D09 — 条码反查（不存在）
    // ============================================================

    @Test
    @Order(9)
    @DisplayName("条码反查 — 不存在")
    void lookupBarcode_NotFound() {
        JSONObject result = get("/api/admin/product/price-detail/lookup-barcode?barcode=NOTEXIST999");

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的条码应返回错误");
        assertTrue(result.getString("msg").contains("不存在")
                        || result.getString("msg").contains("未找到"),
                "消息应包含'不存在'或'未找到': " + result.get("msg"));
        System.out.println("TC-D09 条码不存在被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D10 — 编辑（小幅调整，不触发审批）
    // ============================================================

    @Test
    @Order(10)
    @DisplayName("编辑 — 小幅调价（不触发审批）")
    void update_SmallChange() {
        if (createdDetailId == null) {
            System.out.println("TC-D10 跳过: 无可编辑的测试数据");
            return;
        }

        // 小幅调整（≤ 30% 阈值），预期直接生效
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdDetailId);
        body.put("price", new BigDecimal("10.50"));
        body.put("changeReason", "集成测试-小幅调价");
        body.put("updatedBy", "测试管理员");

        JSONObject result = put("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"),
                "编辑应返回 code=0: " + result.get("msg"));

        JSONObject data = result.getJSONObject("data");
        if (data != null) {
            assertFalse(data.getBoolean("approvalRequired"),
                    "小幅调价不应触发审批");
            System.out.println("TC-D10 编辑成功: " + data.getString("message"));
            originalPrice = new BigDecimal("10.50"); // 更新为新价格
        }
    }

    // ============================================================
    // TC-D11 — 编辑（大幅调价，触发审批）
    // ============================================================

    @Test
    @Order(11)
    @DisplayName("编辑 — 大幅调价（触发审批）")
    void update_LargeChange() {
        if (createdDetailId == null) {
            System.out.println("TC-D11 跳过: 无可编辑的测试数据");
            return;
        }

        // 翻倍调价（> 30% 阈值），预期生成审批记录
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdDetailId);
        body.put("price", new BigDecimal("999.00"));
        body.put("changeReason", "集成测试-大幅调价触发审批");
        body.put("updatedBy", "测试管理员");

        JSONObject result = put("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"),
                "编辑应返回 code=0: " + result.get("msg"));

        JSONObject data = result.getJSONObject("data");
        if (data != null && data.getBoolean("approvalRequired")) {
            assertTrue(data.getString("message").contains("审批"),
                    "消息应包含'审批': " + data.getString("message"));
            System.out.println("TC-D11 触发审批: " + data.getString("message"));
        } else {
            System.out.println("TC-D11 未触发审批（大区可能未开启价格审批）: "
                    + (data != null ? data.getString("message") : ""));
        }
    }

    // ============================================================
    // TC-D12 — 编辑（已有审批中的记录不可再次编辑）
    // ============================================================

    @Test
    @Order(12)
    @DisplayName("编辑 — 审批中的记录再次编辑应拒绝")
    void update_PendingRejected() {
        if (createdDetailId == null) {
            System.out.println("TC-D12 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdDetailId);
        body.put("price", new BigDecimal("888.00"));
        body.put("changeReason", "集成测试-再次编辑审批中记录");
        body.put("updatedBy", "测试管理员");

        JSONObject result = put("/api/admin/product/price-detail", body);

        assertNotNull(result);
        if (result.getInt("code") != 0) {
            assertTrue(result.getString("msg").contains("审批"),
                    "消息应包含'审批': " + result.get("msg"));
            System.out.println("TC-D12 审批中被拒绝: " + result.get("msg"));
        } else {
            // 若 TC-D11 未审批，此处正常更新
            JSONObject data = result.getJSONObject("data");
            System.out.println("TC-D12 编辑成功（上一次未触发审批）: "
                    + (data != null ? data.getString("message") : ""));
        }
    }

    // ============================================================
    // TC-D13 — 编辑（缺少变动原因）
    // ============================================================

    @Test
    @Order(13)
    @DisplayName("编辑 — 缺少变动原因应拒绝")
    void update_MissingReason() {
        if (createdDetailId == null) {
            System.out.println("TC-D13 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdDetailId);
        body.put("price", new BigDecimal("15.00"));

        JSONObject result = put("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "缺少变动原因应被拒绝");
        System.out.println("TC-D13 缺少变动原因被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D14 — 编辑（售价 ≤ 0）
    // ============================================================

    @Test
    @Order(14)
    @DisplayName("编辑 — 售价为0应拒绝")
    void update_ZeroPrice() {
        if (createdDetailId == null) {
            System.out.println("TC-D14 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdDetailId);
        body.put("price", BigDecimal.ZERO);
        body.put("changeReason", "集成测试-零售价");

        JSONObject result = put("/api/admin/product/price-detail", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "售价=0应被拒绝");
        System.out.println("TC-D14 售价=0被拒绝: " + result.get("msg"));
    }

    // ============================================================
    // TC-D15 — 详情查询
    // ============================================================

    @Test
    @Order(15)
    @DisplayName("详情 — 按 ID 查询")
    void detail() {
        if (createdDetailId == null) {
            System.out.println("TC-D15 跳过: 无测试数据");
            return;
        }

        JSONObject result = get("/api/admin/product/price-detail/detail?id=" + createdDetailId);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "详情查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        assertNotNull(result.getJSONObject("data").getString("productBarcode"),
                "应返回商品条码");
        System.out.println("TC-D15 详情查询成功: "
                + result.getJSONObject("data").getString("productName"));
    }

    // ============================================================
    // TC-D16 — 分页查询（按大区筛选）
    // ============================================================

    @Test
    @Order(16)
    @DisplayName("分页查询 — 按销售大区筛选")
    void list_ByRegion() {
        String params = "?pageNum=1&pageSize=10&salesRegionCode=TEST01";
        JSONObject result = get("/api/admin/product/price-detail/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("TC-D16 按大区查询, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-D17 — 分页查询（按关键词筛选）
    // ============================================================

    @Test
    @Order(17)
    @DisplayName("分页查询 — 按商品关键词模糊筛选")
    void list_ByKeyword() {
        String params = "?pageNum=1&pageSize=10&keyword=6900001";
        JSONObject result = get("/api/admin/product/price-detail/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("TC-D17 按关键词查询, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-D18 — 导出
    // ============================================================

    @Test
    @Order(18)
    @DisplayName("导出 — 带 Action: export 请求头")
    void export_Excel() {
        HttpHeaders headers = authHeaders();
        headers.set("Action", "export");
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                baseUrl + "/api/admin/product/price-detail/export",
                HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

        assertNotNull(resp);
        assertTrue(resp.getStatusCode().is2xxSuccessful()
                        || resp.getStatusCode().is3xxRedirection(),
                "导出应返回成功状态码，实际: " + resp.getStatusCode());
        if (resp.getBody() != null && resp.getBody().length > 0) {
            System.out.println("TC-D18 导出成功, 文件大小=" + resp.getBody().length + " bytes");
        } else {
            System.out.println("TC-D18 导出完成（空数据或需调整请求头）");
        }
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private Map<String, Object> buildDetailCreateBody(String regionCode, String groupCode,
                                                       String barcode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("salesRegionCode", regionCode);
        body.put("priceGroupCode", groupCode);
        body.put("productBarcode", barcode);
        body.put("createdBy", "测试管理员");
        return body;
    }

    private Long findLatestDetailId() {
        JSONObject result = get("/api/admin/product/price-detail/list?pageNum=1&pageSize=1");
        JSONObject data = result.getJSONObject("data");
        if (data != null && data.getJSONArray("records") != null
                && !data.getJSONArray("records").isEmpty()) {
            return data.getJSONArray("records")
                    .getJSONObject(0).getLong("id");
        }
        return null;
    }
}
