package cn.qdm.tob.modules.system.operator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * V6 数据库迁移验证测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("V6 运营人员表迁移")
class OperatorMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ===== 字段存在性 =====

    @Test
    @DisplayName("employee_code 列存在")
    void shouldHaveEmployeeCodeColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operator WHERE Field = 'employee_code'");
        assertThat(columns).hasSize(1);
    }

    @Test
    @DisplayName("login_id 列存在")
    void shouldHaveLoginIdColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operator WHERE Field = 'login_id'");
        assertThat(columns).hasSize(1);
    }

    @Test
    @DisplayName("created_by_name 列存在")
    void shouldHaveCreatedByNameColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operator WHERE Field = 'created_by_name'");
        assertThat(columns).hasSize(1);
    }

    @Test
    @DisplayName("updated_by_name 列存在")
    void shouldHaveUpdatedByNameColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operator WHERE Field = 'updated_by_name'");
        assertThat(columns).hasSize(1);
    }

    // ===== 旧字段删除 =====

    @Test
    @DisplayName("cas_user_id 列已不存在")
    void shouldNotHaveCasUserIdColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operator WHERE Field = 'cas_user_id'");
        assertThat(columns).isEmpty();
    }

    // ===== 索引 =====

    @Test
    @DisplayName("employee_code 有唯一索引")
    void shouldHaveUkEmployeeCodeIndex() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SHOW INDEX FROM sys_operator WHERE Key_name = 'uk_employee_code'");
        assertThat(indexes).isNotEmpty();
    }

    @Test
    @DisplayName("uk_cas_user_id 已删除")
    void shouldNotHaveUkCasUserIdIndex() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SHOW INDEX FROM sys_operator WHERE Key_name = 'uk_cas_user_id'");
        assertThat(indexes).isEmpty();
    }

    @Test
    @DisplayName("uk_mobile 索引保留")
    void shouldRetainUkMobileIndex() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SHOW INDEX FROM sys_operator WHERE Key_name = 'uk_mobile'");
        assertThat(indexes).isNotEmpty();
    }
}
