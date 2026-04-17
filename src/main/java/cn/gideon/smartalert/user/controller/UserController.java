package cn.gideon.smartalert.user.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.smartalert.common.response.Result;
import cn.gideon.smartalert.user.dto.RealNameAuthRequest;
import cn.gideon.smartalert.user.dto.UpdateUserInfoRequest;
import cn.gideon.smartalert.user.entity.User;
import cn.gideon.smartalert.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getCurrentUser();
        // 隐藏敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@Valid @RequestBody UpdateUserInfoRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除用户（注销账号）
     */
    @DeleteMapping("/delete")
    public Result<Void> deleteUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.deleteUser(userId);
        return Result.success("账号已注销", null);
    }

    /**
     * 实名认证
     */
    @PostMapping("/realNameAuth")
    public Result<Void> realNameAuth(@Valid @RequestBody RealNameAuthRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        userService.realNameAuth(userId, request);
        return Result.success("实名认证提交成功", null);
    }

    /**
     * 冻结用户（管理员权限）
     */
    @PostMapping("/freeze/{userId}")
    @SaCheckRole("ADMIN")
    public Result<Void> freezeUser(@PathVariable Long userId) {
        userService.freezeUser(userId);
        return Result.success("用户已冻结", null);
    }

    /**
     * 解冻用户（管理员权限）
     */
    @PostMapping("/unfreeze/{userId}")
    @SaCheckRole("ADMIN")
    public Result<Void> unfreezeUser(@PathVariable Long userId) {
        userService.unfreezeUser(userId);
        return Result.success("用户已解冻", null);
    }
}
