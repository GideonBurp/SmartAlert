package cn.gideon.smartalert.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 实名认证请求
 */
@Data
public class RealNameAuthRequest {
    
    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号格式不正确")
    private String idCardNumber;
    
    /**
     * 身份证正面照片URL
     */
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFrontImage;
    
    /**
     * 身份证反面照片URL
     */
    @NotBlank(message = "身份证反面照片不能为空")
    private String idCardBackImage;
}
