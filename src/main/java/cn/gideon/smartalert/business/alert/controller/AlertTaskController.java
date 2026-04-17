package cn.gideon.smartalert.business.alert.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.smartalert.business.alert.dto.CreateAlertTaskRequest;
import cn.gideon.smartalert.business.alert.dto.AlertTaskResponse;
import cn.gideon.smartalert.business.alert.dto.PageRequest;
import cn.gideon.smartalert.business.alert.dto.PageResponse;
import cn.gideon.smartalert.business.alert.dto.UpdateAlertTaskRequest;
import cn.gideon.smartalert.business.alert.service.AlertTaskService;
import cn.gideon.smartalert.common.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    /**
     * 查询任务列表（不分页）
     */
    @GetMapping("/list")
    public Result<List<AlertTaskResponse>> getTaskList(@RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AlertTaskResponse> tasks = alertTaskService.getTaskList(userId, status);
        return Result.success(tasks);
    }

    /**
     * 分页查询任务列表
     */
    @GetMapping("/page")
    public Result<PageResponse<AlertTaskResponse>> getTaskPage(
            @RequestParam(required = false) Integer status,
            @Valid PageRequest pageRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        PageResponse<AlertTaskResponse> page = alertTaskService.getTaskPage(userId, status, pageRequest);
        return Result.success(page);
    }

    /**
     * 查询任务详情
     */
    @GetMapping("/{taskId}")
    public Result<AlertTaskResponse> getTaskDetail(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        AlertTaskResponse task = alertTaskService.getTaskDetail(taskId, userId);
        return Result.success(task);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        alertTaskService.deleteTask(taskId, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 更新任务
     */
    @PutMapping("/{taskId}")
    public Result<Void> updateTask(@PathVariable Long taskId, @Valid @RequestBody UpdateAlertTaskRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        alertTaskService.updateTask(taskId, request, userId);
        return Result.success("更新成功", null);
    }
}
