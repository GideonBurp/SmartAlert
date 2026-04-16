package cn.gideon.smartalert.business.alert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建推送任务请求DTO
 */
@Data
public class CreateAlertTaskRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主题
     */
    @NotBlank(message = "主题不能为空")
    private String theme;

    /**
     * 接收人姓名
     */
    @NotBlank(message = "接收人姓名不能为空")
    private String recipientName;

    /**
     * 接收人性别: MALE-男, FEMALE-女, UNKNOWN-未知
     */
    private String recipientGender;

    /**
     * 接收人手机号
     */
    @NotBlank(message = "接收人手机号不能为空")
    private String recipientPhone;

    /**
     * 推送类型
     */
    @NotBlank(message = "推送类型不能为空")
    private String alertType;

    /**
     * 推送内容
     */
    @NotBlank(message = "推送内容不能为空")
    private String content;

    /**
     * 推送时间（格式：yyyy-MM-dd:HH-mm-ss）
     */
    @NotBlank(message = "推送时间不能为空")
    private String alertTime;
}
