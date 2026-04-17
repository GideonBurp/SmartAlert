package cn.gideon.smartalert.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新用户信息请求
 */
@Data
public class UpdateUserInfoRequest {
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    @Pattern(regexp = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$", message = "邮箱格式不正确")
    private String email;
    
    /**
     * 性别: MALE-男, FEMALE-女, UNKNOWN-未知
     */
    private String gender;
    
    /**
     * 头像URL
     */
    private String avatar;
}
