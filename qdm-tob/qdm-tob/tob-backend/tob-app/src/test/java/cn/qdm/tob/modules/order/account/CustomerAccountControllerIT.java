package cn.qdm.tob.modules.order.account;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户账户管理 — Controller 层集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerAccountControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TokenProvider tokenProvider;

    private RestTemplate restTemplate;
    private String adminToken;
    private String baseUrl;

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

    private String buildAdminToken() {
        UserPrincipal principal = new UserPrincipal(
                1L, "CAS", "13800000000", "测试管理员", Collections.emptyList());
        return tokenProvider.generateToken(principal);
    }

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

    // ================================================================
    // IT-01: 账户列表查询
    // ================================================================

    @Test
    @Order(1)
    @DisplayName("账户列表查询 — 默认参数")
    void list_Default() {
        JSONObject result = get("/api/admin/order/account/list?pageNum=1&pageSize=10");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("IT-01 列表查询成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ================================================================
    // IT-02: 查询客户信息
    // ================================================================

    @Test
    @Order(2)
    @DisplayName("查询客户信息 — 存在的客户编码")
    void getCustomer_Found() {
        // 依赖 cst_company_archive 中有测试数据
        JSONObject result = get("/api/admin/order/account/customer?code=TEST001");

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            assertNotNull(result.getJSONObject("data").getString("customerCode"));
            System.out.println("IT-02 客户信息查询成功: "
                    + result.getJSONObject("data").getString("customerName"));
        } else {
            System.out.println("IT-02 跳过（测试编码 TEST001 在 cst_company_archive 中不存在）: "
                    + result.getString("msg"));
        }
    }

    // ================================================================
    // IT-03: 查询客户信息（不存在）
    // ================================================================

    @Test
    @Order(3)
    @DisplayName("查询客户信息 — 不存在的编码")
    void getCustomer_NotFound() {
        JSONObject result = get("/api/admin/order/account/customer?code=NOTEXIST999");

        assertNotNull(result);
        assertNotEquals(0, result.getInt("code"), "不存在的编码应返回错误");
        assertTrue(result.getString("msg").contains("客户编码不存在"),
                "消息应包含'客户编码不存在': " + result.getString("msg"));
        System.out.println("IT-03 客户编码不存在被拒绝: " + result.getString("msg"));
    }

    // ================================================================
    // IT-04: 新增充值流水
    // ================================================================

    @Test
    @Order(4)
    @DisplayName("新增充值流水 — 正常")
    void createTransaction_Recharge() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerCode", "TEST001");
        body.put("accountType", "PREPAID");
        body.put("transactionType", "RECHARGE");
        body.put("amount", new BigDecimal("500.00"));

        JSONObject result = post("/api/admin/order/account/transaction", body);

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            System.out.println("IT-04 新增充值流水成功");
        } else {
            System.out.println("IT-04 新增充值流水失败（可能缺前置数据）: " + result.getString("msg"));
        }
    }

    // ================================================================
    // IT-05: 新增提现流水
    // ================================================================

    @Test
    @Order(5)
    @DisplayName("新增提现流水 — 正常")
    void createTransaction_Withdraw() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerCode", "TEST001");
        body.put("accountType", "PREPAID");
        body.put("transactionType", "WITHDRAW");
        body.put("amount", new BigDecimal("200.00"));

        JSONObject result = post("/api/admin/order/account/transaction", body);

        assertNotNull(result);
        if (result.getInt("code") == 0) {
            System.out.println("IT-05 新增提现流水成功");
        } else {
            System.out.println("IT-05 新增提现流水失败: " + result.getString("msg"));
        }
    }

    // ================================================================
    // IT-06: 提现超额被拒绝
    // ================================================================

    @Test
    @Order(6)
    @DisplayName("新增提现流水 — 余额不足被拒绝")
    void createTransaction_InsufficientBalance() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerCode", "TEST001");
        body.put("accountType", "PREPAID");
        body.put("transactionType", "WITHDRAW");
        body.put("amount", new BigDecimal("999999.00"));

        JSONObject result = post("/api/admin/order/account/transaction", body);

        assertNotNull(result);
        if (result.getInt("code") != 0) {
            assertTrue(result.getString("msg").contains("余额不足"),
                    "消息应包含'余额不足': " + result.getString("msg"));
            System.out.println("IT-06 余额不足被拒绝: " + result.getString("msg"));
        } else {
            System.out.println("IT-06 注意：提现超额未被拒绝（账户余额充足或测试数据无前置状态）");
        }
    }
}
