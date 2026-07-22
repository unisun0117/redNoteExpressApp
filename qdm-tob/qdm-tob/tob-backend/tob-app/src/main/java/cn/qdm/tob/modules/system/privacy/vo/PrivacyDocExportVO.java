package cn.qdm.tob.modules.system.privacy.vo;

import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.modules.system.privacy.enums.DocStatus;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 隐私文档导出 VO（排除富文本字段）
 */
@Data
public class PrivacyDocExportVO {

    @ExcelColumn(title = "文档类型", index = 0)
    private DocType docType;

    @ExcelColumn(title = "版本号", index = 1)
    private String version;

    @ExcelColumn(title = "H5链接", index = 2)
    private String h5Url;

    @ExcelColumn(title = "附件链接", index = 3)
    private String fileUrl;

    @ExcelColumn(title = "状态", index = 4)
    private DocStatus status;

    @ExcelColumn(title = "备注", index = 5)
    private String remark;

    @ExcelColumn(title = "创建人", index = 6)
    private String createdBy;

    @ExcelColumn(title = "创建时间", index = 7, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @ExcelColumn(title = "修改人", index = 8)
    private String updatedBy;

    @ExcelColumn(title = "修改时间", index = 9, format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
