package cn.gideon.smartalert.business.alert.service;

import cn.gideon.smartalert.business.alert.dto.CreateAlertTaskRequest;
import cn.gideon.smartalert.business.alert.entity.AlertTask;
import cn.gideon.smartalert.business.alert.mapper.AlertTaskMapper;
import cn.gideon.smartalert.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 推送任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertTaskService {

    private final AlertTaskMapper alertTaskMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis ZSet Key
     */
    private static final String ALERT_SCHEDULE_KEY = "alert:schedule";

    /**
     * 分布式锁前缀
     */
    private static final String ALERT_LOCK_PREFIX = "alert:lock:";

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 重试延迟时间（秒）
     */
    private static final long RETRY_DELAY_SECONDS = 300;

    /**
     * 每次扫描的任务数量
     */
    private static final int SCAN_BATCH_SIZE = 100;

    /**
     * 扫描时间窗口（秒）
     */
    private static final long SCAN_TIME_WINDOW = 20;

    /**
     * 时间格式：yyyy-MM-dd:HH-mm-ss
     */
    private static final DateTimeFormatter ALERT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH-mm-ss");

    /**
     * 创建推送任务
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(CreateAlertTaskRequest request, Long userId) {
        // 解析推送时间字符串
        LocalDateTime alertTime;
        try {
            alertTime = LocalDateTime.parse(request.getAlertTime(), ALERT_TIME_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("推送时间格式错误，正确格式：yyyy-MM-dd:HH-mm-ss，例如：2026-04-17:03-05-00");
        }

        // 验证推送时间
        if (alertTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("推送时间不能早于当前时间");
        }

        // 创建任务实体
        AlertTask task = new AlertTask();
        task.setUserId(userId);
        task.setTheme(request.getTheme());
        task.setRecipientName(request.getRecipientName());
        task.setRecipientGender(request.getRecipientGender() != null ? request.getRecipientGender() : "UNKNOWN");
        task.setRecipientPhone(request.getRecipientPhone());
        task.setAlertType(request.getAlertType());
        task.setContent(request.getContent());
        task.setAlertTime(alertTime);
        task.setStatus(0); // 待推送
        task.setRetryCount(0);

        // 保存到数据库
        alertTaskMapper.insert(task);

        // 添加到 Redis ZSet
        long timestamp = alertTime.toEpochSecond(ZoneOffset.of("+8"));
        redisTemplate.opsForZSet().add(ALERT_SCHEDULE_KEY, task.getId().toString(), timestamp);

        log.info("创建推送任务成功: taskId={}, alertTime={}", task.getId(), alertTime);
        return task.getId();
    }

    /**
     * 定时扫描并执行任务（每5秒执行一次）
     */
    @Scheduled(fixedDelay = 5000)
    public void scanAndExecuteTasks() {
        try {
            // 1. 从 MySQL 抓取所有待推送且已到时间的任务
            LocalDateTime now = LocalDateTime.now();

            LambdaQueryWrapper<AlertTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AlertTask::getStatus, 0) // 待推送
                    .le(AlertTask::getAlertTime, now) // 推送时间 <= 当前时间
                    .orderByAsc(AlertTask::getAlertTime) // 按时间排序
                    .last("LIMIT " + SCAN_BATCH_SIZE);
            
            List<AlertTask> tasks = alertTaskMapper.selectList(wrapper);
            
            log.debug("扫描到 {} 个待执行任务，当前时间: {}", tasks != null ? tasks.size() : 0, now);
            
            if (tasks == null || tasks.isEmpty()) {
                return;
            }

            // 打印任务详情用于调试
            for (AlertTask task : tasks) {
                log.debug("任务ID: {}, 推送时间: {}, 状态: {}", task.getId(), task.getAlertTime(), task.getStatus());
            }

            // 2. 将任务加入 Redis ZSet（幂等操作）
            for (AlertTask task : tasks) {
                long timestamp = task.getAlertTime().toEpochSecond(ZoneOffset.of("+8"));
                redisTemplate.opsForZSet().add(ALERT_SCHEDULE_KEY, task.getId().toString(), timestamp);
            }

            // 3. 从 Redis ZSet 查询所有到期任务
            long currentTimestamp = System.currentTimeMillis() / 1000;
            Set<String> taskIds = redisTemplate.opsForZSet()
                    .rangeByScore(ALERT_SCHEDULE_KEY, 0, currentTimestamp);

            if (taskIds == null || taskIds.isEmpty()) {
                return;
            }

            log.info("扫描到 {} 个到期任务", taskIds.size());

            // 4. 执行到期任务
            for (String taskId : taskIds) {
                executeTaskWithLock(taskId);
            }

        } catch (Exception e) {
            log.error("定时扫描任务异常", e);
        }
    }

    /**
     * 使用分布式锁执行任务
     */
    private void executeTaskWithLock(String taskId) {
        String lockKey = ALERT_LOCK_PREFIX + taskId;
        
        // 尝试获取分布式锁，有效期10秒
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(locked)) {
            log.debug("任务正在执行中，跳过: taskId={}", taskId);
            return;
        }

        try {
            executeTask(Long.parseLong(taskId));
            // 执行成功后从 ZSet 移除
            redisTemplate.opsForZSet().remove(ALERT_SCHEDULE_KEY, taskId);
        } catch (Exception e) {
            log.error("任务执行失败: taskId={}", taskId, e);
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 执行单个推送任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(Long taskId) {
        AlertTask task = alertTaskMapper.selectById(taskId);
        
        if (task == null) {
            log.warn("任务不存在: taskId={}", taskId);
            return;
        }

        // 检查状态，防止重复执行
        if (task.getStatus() != 0) {
            log.debug("任务已处理，跳过: taskId={}, status={}", taskId, task.getStatus());
            return;
        }

        try {
            // 执行推送逻辑
            sendNotification(task);

            // 更新状态为已推送
            task.setStatus(1);
            alertTaskMapper.updateById(task);

            log.info("推送任务执行成功: taskId={}, recipient={}", taskId, task.getRecipientPhone());

        } catch (Exception e) {
            log.error("推送任务执行失败: taskId={}", taskId, e);

            // 更新失败状态和重试次数
            task.setStatus(2);
            task.setRetryCount(task.getRetryCount() + 1);
            alertTaskMapper.updateById(task);

            // 如果未达到最大重试次数，重新加入 ZSet 延迟重试
            if (task.getRetryCount() < MAX_RETRY_COUNT) {
                long retryTime = System.currentTimeMillis() / 1000 + RETRY_DELAY_SECONDS;
                redisTemplate.opsForZSet().add(ALERT_SCHEDULE_KEY, taskId.toString(), retryTime);
                log.info("任务将在 {} 秒后重试: taskId={}", RETRY_DELAY_SECONDS, taskId);
            } else {
                log.error("任务达到最大重试次数，停止重试: taskId={}", taskId);
            }
        }
    }

    /**
     * 发送推送通知
     */
    private void sendNotification(AlertTask task) {
        // TODO: 实现实际的推送逻辑（短信、站内信等）
        log.info("执行推送 - 主题: {}, 接收人: {}, 手机号: {}, 内容: {}", 
                task.getTheme(), 
                task.getRecipientName(), 
                task.getRecipientPhone(), 
                task.getContent());
        
        // 模拟推送延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 服务启动时恢复待执行任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void recoverPendingTasks() {
        log.info("开始恢复待执行任务...");

        // 查询所有待推送且未到时间的任务
        LambdaQueryWrapper<AlertTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertTask::getStatus, 0)
                .gt(AlertTask::getAlertTime, LocalDateTime.now());

        List<AlertTask> pendingTasks = alertTaskMapper.selectList(wrapper);

        if (pendingTasks == null || pendingTasks.isEmpty()) {
            log.info("没有需要恢复的任务");
            return;
        }

        // 重新加入 Redis ZSet
        int count = 0;
        for (AlertTask task : pendingTasks) {
            long timestamp = task.getAlertTime().toEpochSecond(ZoneOffset.of("+8"));
            redisTemplate.opsForZSet().add(ALERT_SCHEDULE_KEY, task.getId().toString(), timestamp);
            count++;
        }

        log.info("成功恢复 {} 个待执行任务", count);
    }
}
