package cn.qdm.tob.infrastructure.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointPermission {
    /** URL 模式，如 /api/admin/user/list */
    private String pattern;
    /** HTTP 方法，如 GET、POST */
    private String method;
    /** 接口描述，取自 @Operation 注解 */
    private String description;
    /** Controller 名称 + Action 名称，如 UserController#list */
    private String controllerAction;
    /** 所需权限码列表，如 ["sys:user:view"]；无权限要求时为空数组 */
    private List<String> authorities;
}