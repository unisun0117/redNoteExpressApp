package cn.qdm.tob.framework.excel.converter;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.model.RecordStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryConverterTest {

    // ======================== convert() 导出方向 ========================

    @Test
    void convert_describableEnum_returnsDescription() {
        DictionaryConverter converter = new DictionaryConverter(List.of());
        Object result = converter.convert(RecordStatus.ACTIVE, dummyField());
        assertThat(result).isEqualTo("启用");
    }

    @Test
    void convert_null_returnsNull() {
        DictionaryConverter converter = new DictionaryConverter(List.of());
        Object result = converter.convert(null, dummyField());
        assertThat(result).isNull();
    }

    @Test
    void convert_nonDescribable_usesDictionary() {
        DescriptionProvider provider = group -> Map.of("A", "选项A");
        DictionaryConverter converter = new DictionaryConverter(List.of(provider));
        Object result = converter.convert("A", dummyField());
        assertThat(result).isEqualTo("选项A");
    }

    // ======================== revert() 导入方向 ========================

    @Test
    void revert_describableEnumField_matchesByDescription() throws Exception {
        Field field = TestDTO.class.getDeclaredField("status");
        DictionaryConverter converter = new DictionaryConverter(List.of());

        Object result = converter.revert("启用", field);

        assertThat(result).isEqualTo(RecordStatus.ACTIVE);
    }

    @Test
    void revert_describableEnumField_noMatch_returnsNull() throws Exception {
        Field field = TestDTO.class.getDeclaredField("status");
        DictionaryConverter converter = new DictionaryConverter(List.of());

        Object result = converter.revert("不存在", field);

        assertThat(result).isNull();
    }

    @Test
    void revert_nonEnumField_usesDictionary() throws Exception {
        Field field = TestDTO.class.getDeclaredField("nonEnum");
        DescriptionProvider provider = group -> Map.of("A", "选项A");
        DictionaryConverter converter = new DictionaryConverter(List.of(provider));

        Object result = converter.revert("选项A", field);

        assertThat(result).isEqualTo("A");
    }

    // ======================== helper ========================

    static class TestDTO {
        @Description("STATUS")
        RecordStatus status;

        @Description("TYPE")
        String nonEnum;
    }

    private static Field dummyField() {
        try {
            return TestDTO.class.getDeclaredField("status");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
