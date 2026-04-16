package cn.gideon.smartalert;

import cn.gideon.smartalert.business.alert.service.AlertTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class SmartAlertApplication implements CommandLineRunner {

    private final AlertTaskService alertTaskService;

    public static void main(String[] args) {
        SpringApplication.run(SmartAlertApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // 服务启动时恢复待执行任务
        alertTaskService.recoverPendingTasks();
    }
}
