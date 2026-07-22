package cn.qdm.tob.framework.description;

public enum DescriptionStrategy {
    /** 追加影子字段（{fieldName}Name），保留原始编码值 */
    EXTENSION,
    /** 替换原字段值为显示文本 */
    REPLACEMENT
}
