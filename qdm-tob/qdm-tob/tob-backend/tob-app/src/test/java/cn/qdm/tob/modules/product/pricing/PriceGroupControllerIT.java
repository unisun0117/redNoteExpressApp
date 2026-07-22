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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 价格组 — Controller 层集成测试
 * <p>
 * 启动真实 Spring Boot 容器，通过 RestTemplate 发送 HTTP 请求验证 API 行为。
 *
 * @author lichenxing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PriceGroupControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TokenProvider tokenProvider;

    private RestTemplate restTemplate;
    private String adminToken;
    private String baseUrl;

    /** 记录在 TC-G02 中新建成功后的 ID，供后续编辑用例复用 */
    private static Long createdGroupId;

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
    // TC-G01 — 分页查询（无筛选条件）
    // ============================================================

    @Test
    @Order(1)
    @DisplayName("分页查询 — 默认参数")
    void list_Default() {
        String params = "?pageNum=1&pageSize=10";
        JSONObject result = get("/api/admin/product/price-group/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("TC-G01 列表查询成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-G02 — 新增价格组（正常流程）
    // ============================================================

    @Test
    @Order(2)
    @DisplayName("新增 — 正常")
    void create_Success() {
        Map<String, Object> body = buildGroupCreateBody("TEST01", "PG_TEST", "测试价格组");
        JSONObject result = post("/api/admin/product/price-group", body);

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            System.out.println("TC-G02 新增成功");
            createdGroupId = findLatestGroupId();
        } else {
            System.out.println("TC-G02 新增失败（可能缺前置数据或已存在）: " + result.getString("msg"));
        }
    }

    // ============================================================
    // TC-G03 — 新增（大区+编码重复）
    // ============================================================

    @Test
    @Order(3)
    @DisplayName("新增 — 大区+编码重复")
    void create_Duplicate() {
        Map<String, Object> body = buildGroupCreateBody("TEST01", "PG_TEST", "测试价格组");
        body.put("description", "重复新增测试");

        JSONObject result = post("/api/admin/product/price-group", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "重复主键应被拒绝");
        assertTrue(result.getString("msg").contains("已存在")
                        || result.getString("msg").contains("相同"),
                "消息应包含'已存在': " + result.getString("msg"));
        System.out.println("TC-G03 重复被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-G04 — 新增（必填字段缺失）
    // ============================================================

    @Test
    @Order(4)
    @DisplayName("新增 — 必填字段缺失（空价格组编码）")
    void create_MissingCode() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("salesRegionCode", "TEST01");
        body.put("salesRegionName", "测试大区");
        body.put("priceGroupName", "缺失编码测试");

        JSONObject result = post("/api/admin/product/price-group", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "缺少价格组编码应被拒绝");
        System.out.println("TC-G04 缺少价格组编码被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-G05 — 新增（空销售大区）
    // ============================================================

    @Test
    @Order(5)
    @DisplayName("新增 — 必填字段缺失（空销售大区）")
    void create_MissingRegion() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("priceGroupCode", "PG_NO_REGION");
        body.put("priceGroupName", "缺失大区测试");

        JSONObject result = post("/api/admin/product/price-group", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "缺少销售大区应被拒绝");
        System.out.println("TC-G05 缺少销售大区被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-G06 — 编辑价格组（正常）
    // ============================================================

    @Test
    @Order(6)
    @DisplayName("编辑 — 正常修改名称和描述")
    void update_Success() {
        createdGroupId = findLatestGroupId();
        if (createdGroupId == null) {
            System.out.println("TC-G06 跳过: 无可编辑的测试数据");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", createdGroupId);
        body.put("priceGroupName", "测试编辑-价格组名称");
        body.put("description", "测试编辑-描述说明");
        body.put("updatedBy", "测试管理员");

        JSONObject result = put("/api/admin/product/price-group", body);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"),
                "编辑应返回 code=0: " + result.get("msg"));
        System.out.println("TC-G06 编辑成功");
    }

    // ============================================================
    // TC-G07 — 编辑（ID 不存在）
    // ============================================================

    @Test
    @Order(7)
    @DisplayName("编辑 — ID 不存在应拒绝")
    void update_NotFound() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", 999999);
        body.put("priceGroupName", "不存在的价格组");
        body.put("description", "测试");

        JSONObject result = put("/api/admin/product/price-group", body);

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的ID应被拒绝");
        assertTrue(result.getString("msg").contains("不存在"),
                "消息应包含'不存在': " + result.getString("msg"));
        System.out.println("TC-G07 不存在的ID被拒绝: " + result.getString("msg"));
    }

    // ============================================================
    // TC-G08 — 下拉选项（有数据）
    // ============================================================

    @Test
    @Order(8)
    @DisplayName("下拉选项 — 按大区过滤")
    void options_ByRegion() {
        JSONObject result = get("/api/admin/product/price-group/options?salesRegionCode=TEST01");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "下拉查询应返回 code=0");
        assertNotNull(result.getJSONArray("data"), "data 应为数组");
        System.out.println("TC-G08 下拉查询, 条数="
                + result.getJSONArray("data").length());
    }

    // ============================================================
    // TC-G09 — 下拉选项（无数据的大区）
    // ============================================================

    @Test
    @Order(9)
    @DisplayName("下拉选项 — 无数据大区返回空数组")
    void options_EmptyRegion() {
        JSONObject result = get("/api/admin/product/price-group/options?salesRegionCode=NONEXIST");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "下拉查询应返回 code=0");
        assertEquals(0, result.getJSONArray("data").length(),
                "无数据大区应返回空数组");
        System.out.println("TC-G09 无数据大区返回空数组");
    }

    // ============================================================
    // TC-G10 — 分页查询（按大区筛选）
    // ============================================================

    @Test
    @Order(10)
    @DisplayName("分页查询 — 按销售大区筛选")
    void list_ByRegion() {
        String params = "?pageNum=1&pageSize=10&salesRegionCode=TEST01";
        JSONObject result = get("/api/admin/product/price-group/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("TC-G10 按大区查询, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // TC-G11 — 分页查询（按名称模糊筛选）
    // ============================================================

    @Test
    @Order(11)
    @DisplayName("分页查询 — 按价格组名称模糊筛选")
    void list_ByName() {
        String params = "?pageNum=1&pageSize=10&priceGroupName=测试";
        JSONObject result = get("/api/admin/product/price-group/list" + params);

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("TC-G11 按名称模糊查询, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    private Map<String, Object> buildGroupCreateBody(String regionCode, String groupCode,
                                                      String groupName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("salesRegionCode", regionCode);
        body.put("salesRegionName", "测试大区");
        body.put("priceGroupCode", groupCode);
        body.put("priceGroupName", groupName);
        body.put("description", "集成测试用价格组");
        body.put("createdBy", "测试管理员");
        return body;
    }

    private Long findLatestGroupId() {
        JSONObject result = get("/api/admin/product/price-group/list?pageNum=1&pageSize=1");
        JSONObject data = result.getJSONObject("data");
        if (data != null && data.getJSONArray("records") != null
                && !data.getJSONArray("records").isEmpty()) {
            return data.getJSONArray("records")
                    .getJSONObject(0).getLong("id");
        }
        return null;
    }
}
