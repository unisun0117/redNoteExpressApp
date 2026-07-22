package cn.qdm.tob.modules.system.privacy.vo;

import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.modules.system.privacy.enums.AuthType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私授权记录导出 VO
 */
@Data
public class AuthRecordExportVO {

    @ExcelColumn(title = "openid", index = 0)
    private String openid;

    @ExcelColumn(title = "手机号", index = 1)
    private String phone;

    @ExcelColumn(title = "授权类型", index = 2)
    private AuthType authType;

    @ExcelColumn(title = "文档版本号", index = 3)
    private String version;

    @ExcelColumn(title = "授权时间", index = 4, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime authTime;
}
