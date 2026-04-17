package cn.gideon.smartalert.business.alert.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送任务响应DTO
 */
@Data
public class AlertTaskResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String theme;
    private String recipientName;
    private String recipientGender;
    private String recipientPhone;
    private String alertType;
    private String content;
    private LocalDateTime alertTime;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime createTime;
}
