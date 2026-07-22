package cn.qdm.tob.modules.order.flow;

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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资金流水 — Controller 层集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountTransactionControllerIT {

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
            public boolean hasError(ClientHttpResponse response) { return false; }
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

    // ================================================================
    // IT-01: 流水列表查询
    // ================================================================

    @Test
    @Order(1)
    @DisplayName("流水列表查询 — 默认参数")
    void list_Default() {
        JSONObject result = get("/api/admin/order/transaction/list?pageNum=1&pageSize=10");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("IT-01 流水列表查询成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ================================================================
    // IT-02: 汇总统计
    // ================================================================

    @Test
    @Order(2)
    @DisplayName("汇总统计 — 默认（全部）")
    void summary_Default() {
        JSONObject result = get("/api/admin/order/transaction/summary");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "汇总应返回 code=0");
        assertNotNull(result.getJSONObject("data"), "data 不能为空");
        System.out.println("IT-02 汇总统计成功: "
                + result.getJSONObject("data").toString());
    }

    // ================================================================
    // IT-03: 流水列表 — 按收支类型筛选
    // ================================================================

    @Test
    @Order(3)
    @DisplayName("流水列表 — 按收支类型 INCOME 筛选")
    void list_FilterByIncomeExpenseType() {
        JSONObject result = get("/api/admin/order/transaction/list"
                + "?pageNum=1&pageSize=10&incomeExpenseType=INCOME");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("IT-03 按收支类型筛选成功, total="
                + result.getJSONObject("data").getInt("total"));
    }

    // ================================================================
    // IT-04: 流水列表 — 按时间范围筛选
    // ================================================================

    @Test
    @Order(4)
    @DisplayName("流水列表 — 按时间范围筛选")
    void list_FilterByTimeRange() {
        JSONObject result = get("/api/admin/order/transaction/list"
                + "?pageNum=1&pageSize=10&startTime=2026-01-01T00:00:00&endTime=2026-12-31T23:59:59");

        assertNotNull(result);
        assertEquals(0, result.getInt("code"), "查询应返回 code=0");
        System.out.println("IT-04 按时间范围筛选成功, total="
                + result.getJSONObject("data").getInt("total"));
    }
}
