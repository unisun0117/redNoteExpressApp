package cn.qdm.tob.client.sap.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SapOrderRequestDTO {
    public SapOrderRequestDTO() { }

    public SapOrderRequestDTO(List<SapOrderDTO> data) {
        this.data = data;
    }

    private List<SapOrderDTO> data;
}
