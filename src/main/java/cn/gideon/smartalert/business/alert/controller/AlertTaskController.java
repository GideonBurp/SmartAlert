package cn.gideon.smartalert.business.alert.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.smartalert.business.alert.dto.CreateAlertTaskRequest;
import cn.gideon.smartalert.business.alert.service.AlertTaskService;
import cn.gideon.smartalert.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 推送任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
public class AlertTaskController {

    private final AlertTaskService alertTaskService;

    /**
     * 创建推送任务
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createTask(@Valid @RequestBody CreateAlertTaskRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long taskId = alertTaskService.createTask(request, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("taskId", taskId);
        data.put("message", "任务创建成功");

        return Result.success("任务创建成功", data);
    }
}
