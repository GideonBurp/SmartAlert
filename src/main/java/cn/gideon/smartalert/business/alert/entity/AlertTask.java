package cn.gideon.smartalert.business.alert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时推送任务实体
 */
@Data
@TableName("alert_task")
public class AlertTask implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 主题
     */
    private String theme;

    /**
     * 接收人姓名
     */
    private String recipientName;

    /**
     * 接收人性别: MALE-男, FEMALE-女, UNKNOWN-未知
     */
    private String recipientGender;

    /**
     * 接收人手机号
     */
    private String recipientPhone;

    /**
     * 推送类型
     */
    private String alertType;

    /**
     * 推送内容
     */
    private String content;

    /**
     * 推送时间
     */
    private LocalDateTime alertTime;

    /**
     * 状态: 0-待推送, 1-已推送, 2-失败
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
