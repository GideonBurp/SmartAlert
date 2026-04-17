package cn.gideon.smartalert.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 用户分页查询请求
 */
@Data
public class UserPageRequest {
    
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
    
    /**
     * 用户名(模糊查询)
     */
    private String username;
    
    /**
     * 手机号(模糊查询)
     */
    private String telephone;
    
    /**
     * 用户状态: INIT-初始化, AUTH-已认证, FROZEN-冻结
     */
    private String state;
    
    /**
     * 用户角色: CUSTOMER-普通用户, ADMIN-管理员
     */
    private String userRole;
    
    /**
     * 是否实名认证
     */
    private Boolean certification;
}
