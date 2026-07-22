package cn.qdm.tob.modules.system.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseVO {
    private String token;
    private long expiresIn;
    private String tokenType;

    public TokenResponseVO(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
}
