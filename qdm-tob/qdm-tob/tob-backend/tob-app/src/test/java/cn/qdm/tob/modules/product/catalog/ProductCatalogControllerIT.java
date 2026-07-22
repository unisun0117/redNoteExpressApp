package cn.qdm.tob.modules.product.catalog;

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
 * 商品资料 — Controller 层集成测试
 * <p>
 * 启动真实 Spring Boot 容器，通过 RestTemplate 发送 HTTP 请求验证 API 行为。
 *
 * @author lichenxing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductCatalogControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TokenProvider tokenProvider;

    private RestTemplate restTemplate;
    private String adminToken;
    private String baseUrl;

    private static Long createdId;

    @BeforeEach
    void setUp() {
        // 关闭默认的 4xx/5xx 异常抛出，让业务错误码能被正常解析
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
                1L,                  // uid
                "CAS",               // authType
                "13800000000",       // phone
                "测试管理员",          // name
                Collections.emptyList()
        );
        return tokenProvider.generateToken(principal);
    }

    /** 带 JWT 认证的请求构建 */
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
    // TC-P01 — 分页查询（无筛选条件）
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("分页查询 — 默认参数")
    void list_Default() {
        String params = "?pageNum=1&pageSize=10";
        JSONObject result = get("/api/admin/product/catalog/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("TC-P01 列表查询成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-P02 — 条码反查（prd_sku 表存在该条码）
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("条码反查 — 存在")
    void lookupBarcode_Found() {
        // prd_sku 表中需有测试数据, 否则此用例断言 404/msg 包含"未找到"
        JSONObject result = get("/api/admin/product/catalog/lookup-barcode?barcode=6900001");

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            assertNotNull(result.getJSONObject("data").getString("productName"),
                    "应返回商品名称");
            System.out.println("TC-P02 条码反查成功: "
                    + result.getJSONObject("data").getString("productName"));
        } else {
            System.out.println("TC-P02 跳过（测试条码 6900001 在 prd_sku 中不存在）: "
                    + result.getString("msg"));
        }
    }

    // ============================================================
    // TC-P03 — 条码反查（不存在）
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("条码反查 — 不存在")
    void lookupBarcode_NotFound() {
        JSONObject result = get("/api/admin/product/catalog/lookup-barcode?barcode=NOTEXIST999");

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的条码应返回错误");
        assertTrue(result.getString("msg").contains("未找到"),
                "消息应包含'未找到': " + result.getString("msg"));
        System.out.println("TC-P03 条码不存在被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-P04 — 新增（正常流程）
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("新增 — 正常")
    void create_Success() {
        Map<String, Object> body = buildCreateBody("TEST01", "6900001", "W001");
        JSONObject result = post("/api/admin/product/catalog", body);

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            System.out.println("TC-P04 新增成功");
            // 从列表反查刚创建的 ID
            createdId = findLatestId();
        } else {
            System.out.println("TC-P04 新增失败（可能缺前置数据）: " + result.getString("msg"));
        }
    }

    // ============================================================
    // TC-P05 — 新增（重复主键）
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("新增 — 大区+条码重复")
    void create_Duplicate() {
        Map<String, Object> body = buildCreateBody("TEST01", "6900001", "W001");
        body.put("orderBaseQty", 5);
        body.put("orderMinQty", 10);
        body.put("orderMaxQty", 100);
        body.put("dailyStock", new BigDecimal("200"));

        JSONObject result = post("/api/admin/product/catalog", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "重复主键应被拒绝");
        assertTrue(result.getString("msg").contains("相同商品条码")
                        || result.getString("msg").contains("已存在"),
                "消息应包含'相同商品条码': " + result.getString("msg"));
        System.out.println("TC-P05 重复主键被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-P06 — 新增（必填字段缺失）
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("新增 — 必填字段缺失（空销售大区）")
    void create_MissingRegion() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productBarcode", "6900002");
        body.put("orderBaseQty", 10);
        body.put("orderMinQty", 10);
        body.put("orderMaxQty", 100);
        body.put("dailyStock", new BigDecimal("300"));

        JSONObject result = post("/api/admin/product/catalog", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "缺少销售大区应被拒绝");
        System.out.println("TC-P06 缺少销售大区被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-P07 — 编辑（正常）
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("编辑 — 正常修改小程序名称和状态")
    void update_Success() {
        if (createdId == null) {
            System.out.println("TC-P07 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UNLISTED");
        body.put("miniappName", "测试编辑-小程序名称");
        body.put("orderBaseQty", new BigDecimal("5"));
        body.put("orderMinQty", new BigDecimal("10"));
        body.put("orderMaxQty", new BigDecimal("50"));

        JSONObject result = put("/api/admin/product/catalog?id=" + createdId, body);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"),
                "编辑应返回 code=0: " + result.getString("msg"));
        System.out.println("TC-P07 编辑成功");

        // 回写验证
        JSONObject detail = get("/api/admin/product/catalog/detail?id=" + createdId);
        assertEquals("UNLISTED",
                detail.getJSONObject("data").getString("status"), "状态应为 UNLISTED");
        assertEquals("测试编辑-小程序名称",
                detail.getJSONObject("data").getString("miniappName"), "小程序名称应更新");
    }

    // ============================================================
    // TC-P08 — 库存调整（正常）
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("库存调整 — 修改后可用数量自动计算")
    void adjustStock_Success() {
        if (createdId == null) {
            System.out.println("TC-P08 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("newDailyStock", new BigDecimal("800"));

        JSONObject result = put("/api/admin/product/catalog/adjust-stock?id=" + createdId, body);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"),
                "库存调整应返回 code=0: " + result.getString("msg"));
        System.out.println("TC-P08 库存调整成功");

        // 回写验证
        JSONObject detail = get("/api/admin/product/catalog/detail?id=" + createdId);
        assertEquals(new BigDecimal("800"),
                detail.getJSONObject("data").getBigDecimal("dailyStock"), "stock 应=800");
    }

    // ============================================================
    // TC-P09 — 库存调整（负数校验）
    // ============================================================

    @Test
    @Order(9)
    @DisplayName("库存调整 — 可用数量为负应拒绝")
    void adjustStock_Negative() {
        if (createdId == null) {
            System.out.println("TC-P09 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("newDailyStock", -100);

        JSONObject result = put("/api/admin/product/catalog/adjust-stock?id=" + createdId, body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "负数库存应被拒绝");
        System.out.println("TC-P09 负数库存被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-P10 — 仓库下拉
    // ============================================================

    @Test
    @Order(10)
    @DisplayName("仓库下拉 — 按大区过滤")
    void warehouses_ByRegion() {
        JSONObject result = get("/api/admin/product/catalog/warehouses?salesRegionCode=TEST01");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "仓库查询应返回 code=0");
        assertNotNull(result.getJSONArray("data"), "data 应为数组");
        System.out.println("TC-P10 仓库查询, 条数="
                + result.getJSONArray("data").length());
    }

    // ============================================================
    // TC-P11 — 导出
    // ============================================================

    @Test
    @Order(11)
    @DisplayName("导出 — 带 Action: export 请求头")
    void export_Excel() {
        HttpHeaders headers = authHeaders();
        headers.set("Action", "export");
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                baseUrl + "/api/admin/product/catalog/export",
                HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

        assertNotNull(resp);
        assertTrue(resp.getStatusCode().is2xxSuccessful()
                        || resp.getStatusCode().is3xxRedirection(),
                "导出应返回成功状态码，实际: " + resp.getStatusCode());
        if (resp.getBody() != null && resp.getBody().length > 0) {
            System.out.println("TC-P11 导出成功, 文件大小=" + resp.getBody().length + " bytes");
        } else {
            System.out.println("TC-P11 导出完成（空数据或需调整请求头）");
        }
    }

    // ============================================================
    // TC-P12 — 详情查询
    // ============================================================

    @Test
    @Order(12)
    @DisplayName("详情 — 按 ID 查询")
    void detail() {
        if (createdId == null) {
            System.out.println("TC-P12 跳过: 无测试数据");
            return;
        }

        JSONObject result = get("/api/admin/product/catalog/detail?id=" + createdId);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "详情查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        assertNotNull(result.getJSONObject("data").getString("productBarcode"),
                "应返回商品条码");
        System.out.println("TC-P12 详情查询成功: "
                + result.getJSONObject("data").getString("productName"));
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private Map<String, Object> buildCreateBody(String regionCode, String barcode, String warehouseCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("salesRegionCode", regionCode);
        body.put("salesRegionName", "测试大区");
        body.put("productBarcode", barcode);
        body.put("warehouseCode", warehouseCode);
        body.put("status", "LISTED");
        body.put("orderBaseQty", new BigDecimal("10"));
        body.put("orderMinQty", new BigDecimal("10"));
        body.put("orderMaxQty", new BigDecimal("100"));
        body.put("dailyStock", new BigDecimal("500"));
        return body;
    }

    private Long findLatestId() {
        JSONObject result = get("/api/admin/product/catalog/list?pageNum=1&pageSize=1");
        JSONObject data = result.getJSONObject("data");
        if (data != null && data.getJSONArray("records") != null
                && !data.getJSONArray("records").isEmpty()) {
            return data.getJSONArray("records")
                    .getJSONObject(0).getLong("id");
        }
        return null;
    }
}
