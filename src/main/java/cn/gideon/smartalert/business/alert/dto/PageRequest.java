package cn.gideon.smartalert.business.alert.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询请求
 */
@Data
public class PageRequest {
    
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;
}
