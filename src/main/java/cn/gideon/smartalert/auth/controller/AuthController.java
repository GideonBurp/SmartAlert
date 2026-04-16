package cn.gideon.smartalert.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.smartalert.common.response.Result;
import cn.gideon.smartalert.user.dto.LoginRequest;
import cn.gideon.smartalert.user.dto.RegisterRequest;
import cn.gideon.smartalert.user.dto.SendCodeRequest;
import cn.gideon.smartalert.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 管理员注册（需要密码）
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("message", "注册成功");
        
        return Result.success("注册成功", data);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/sendCode")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        userService.sendVerifyCode(request.getTelephone());
        return Result.success("验证码已发送", null);
    }

    /**
     * 手机号验证码登录（未注册自动注册）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.loginByCode(request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenName", StpUtil.getTokenName());
        
        return Result.success("登录成功", data);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前登录状态
     */
    @GetMapping("/isLogin")
    public Result<Boolean> isLogin() {
        return Result.success(StpUtil.isLogin());
    }

    /**
     * 获取当前用户ID
     */
    @GetMapping("/getLoginId")
    public Result<Long> getLoginId() {
        if (!StpUtil.isLogin()) {
            return Result.error("未登录");
        }
        return Result.success(StpUtil.getLoginIdAsLong());
    }
}
