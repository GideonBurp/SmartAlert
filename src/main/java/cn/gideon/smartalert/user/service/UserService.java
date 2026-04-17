package cn.gideon.smartalert.user.service;

import cn.gideon.smartalert.common.exception.BusinessException;
import cn.gideon.smartalert.user.constant.UserRole;
import cn.gideon.smartalert.user.constant.UserStateEnum;
import cn.gideon.smartalert.user.dto.LoginRequest;
import cn.gideon.smartalert.user.dto.RealNameAuthRequest;
import cn.gideon.smartalert.user.dto.RegisterRequest;
import cn.gideon.smartalert.user.dto.UpdateUserInfoRequest;
import cn.gideon.smartalert.user.dto.UserPageRequest;
import cn.gideon.smartalert.user.entity.User;
import cn.gideon.smartalert.user.mapper.UserMapper;
import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.smartalert.web.filter.SlidingWindowRateLimiter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    private SlidingWindowRateLimiter slidingWindowRateLimiter;

    /**
     * 验证码 Redis Key 前缀
     */
    private static final String VERIFY_CODE_PREFIX = "verify_code:";
    
    /**
     * 验证码有效期（秒）
     */
    private static final long VERIFY_CODE_EXPIRE = 300;


    /**
     * 管理员注册（需要密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getTelephone, request.getTelephone());
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new BusinessException("手机号已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        // TODO: 密码加密，建议使用BCrypt
        user.setPassword(request.getPassword());
        user.setTelephone(request.getTelephone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername());
        user.setGender(request.getGender() != null ? request.getGender() : "UNKNOWN");
        user.setState(UserStateEnum.INIT.name());
        user.setUserRole(UserRole.CUSTOMER.name());
        user.setCertification(false);
        user.setInviteCode(generateInviteCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);

        userMapper.insert(user);
        log.info("管理员注册成功: {}", user.getUsername());
        
        return user.getId();
    }

    /**
     * 发送验证码
     */
    public void sendVerifyCode(String telephone) {
        Boolean access = slidingWindowRateLimiter.tryAcquire(telephone, 1, 60);

        if (!access) {
            throw new BusinessException("请求频繁");
        }
        // 生成6位随机验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        
        // 存储到Redis，有效期5分钟
        String key = VERIFY_CODE_PREFIX + telephone;
        redisTemplate.opsForValue().set(key, code, VERIFY_CODE_EXPIRE, TimeUnit.SECONDS);
        
        // TODO: 实际项目中需要调用短信服务发送验证码
        log.info("手机号 {} 的验证码为: {}", telephone, code);
    }

    /**
     * 手机号验证码登录（未注册自动注册）
     */
    @Transactional(rollbackFor = Exception.class)
    public String loginByCode(LoginRequest request) {
        String telephone = request.getTelephone();
        String code = request.getCode();
        
        // 验证验证码
        String key = VERIFY_CODE_PREFIX + telephone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        
        // 验证码正确后删除
        redisTemplate.delete(key);
        
        // 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTelephone, telephone);
        User user = userMapper.selectOne(wrapper);
        
        // 如果用户不存在，自动注册
        if (user == null) {
            user = autoRegister(telephone);
            log.info("手机号 {} 自动注册成功", telephone);
        }
        
        // 检查用户状态
        if (UserStateEnum.FROZEN.name().equals(user.getState())) {
            throw new BusinessException("账号已被冻结");
        }
        
        // 登录，生成token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        
        log.info("用户登录成功: {}", user.getTelephone());
        return token;
    }

    /**
     * 自动注册用户
     */
    private User autoRegister(String telephone) {
        User user = new User();
        user.setUsername("user_" + telephone.substring(7)); // 使用后4位作为用户名
        user.setPassword(""); // 验证码登录不需要密码
        user.setTelephone(telephone);
        user.setNickname("用户" + telephone.substring(7));
        user.setGender("UNKNOWN");
        user.setState(UserStateEnum.INIT.name());
        user.setUserRole(UserRole.CUSTOMER.name());
        user.setCertification(false);
        user.setInviteCode(generateInviteCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
        log.info("用户登出成功");
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        // 简单实现：使用时间戳+随机数
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 只更新非空字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户信息更新成功: {}", userId);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // MyBatis-Plus会自动处理逻辑删除
        userMapper.deleteById(userId);
        
        // 如果用户已登录，强制登出
        if (StpUtil.isLogin(userId)) {
            StpUtil.logout(userId);
        }
        
        log.info("用户删除成功: {}", userId);
    }

    /**
     * 冻结用户
     */
    public void freezeUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (UserStateEnum.FROZEN.name().equals(user.getState())) {
            throw new BusinessException("用户已被冻结");
        }

        user.setState(UserStateEnum.FROZEN.name());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 强制登出
        if (StpUtil.isLogin(userId)) {
            StpUtil.logout(userId);
        }

        log.info("用户冻结成功: {}", userId);
    }

    /**
     * 解冻用户
     */
    public void unfreezeUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!UserStateEnum.FROZEN.name().equals(user.getState())) {
            throw new BusinessException("用户未被冻结");
        }

        user.setState(UserStateEnum.INIT.name());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户解冻成功: {}", userId);
    }

    /**
     * 实名认证
     */
    @Transactional(rollbackFor = Exception.class)
    public void realNameAuth(Long userId, RealNameAuthRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getCertification()) {
            throw new BusinessException("用户已完成实名认证");
        }

        if (UserStateEnum.FROZEN.name().equals(user.getState())) {
            throw new BusinessException("账号已被冻结，无法认证");
        }

        // TODO: 实际项目中需要调用第三方API验证身份证信息
        // TODO: 保存身份证照片到对象存储
        
        // 更新认证状态
        user.setCertification(true);
        user.setState(UserStateEnum.AUTH.name());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户实名认证成功: {}", userId);
    }

    /**
     * 分页条件查询用户列表
     */
    public Page<User> getUserPage(UserPageRequest request) {
        // 创建分页对象
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊查询
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        
        // 手机号模糊查询
        if (request.getTelephone() != null && !request.getTelephone().isEmpty()) {
            wrapper.like(User::getTelephone, request.getTelephone());
        }
        
        // 用户状态精确查询
        if (request.getState() != null && !request.getState().isEmpty()) {
            wrapper.eq(User::getState, request.getState());
        }
        
        // 用户角色精确查询
        if (request.getUserRole() != null && !request.getUserRole().isEmpty()) {
            wrapper.eq(User::getUserRole, request.getUserRole());
        }
        
        // 实名认证状态查询
        if (request.getCertification() != null) {
            wrapper.eq(User::getCertification, request.getCertification());
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(User::getCreateTime);
        
        // 执行分页查询
        return userMapper.selectPage(page, wrapper);
    }
}
