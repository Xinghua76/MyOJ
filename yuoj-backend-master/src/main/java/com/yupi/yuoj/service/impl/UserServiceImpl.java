package com.yupi.yuoj.service.impl;

import static com.yupi.yuoj.constant.UserConstant.USER_LOGIN_STATE;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.UserMapper;
import com.yupi.yuoj.model.dto.user.UserQueryRequest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.enums.UserRoleEnum;
import com.yupi.yuoj.model.vo.LoginUserVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "yupi";

    @Resource
    private com.yupi.yuoj.utils.SmsUtils smsUtils;

    @Resource
    private com.yupi.yuoj.mapper.UserVerifyCodeMapper userVerifyCodeMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "userAccount too short");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password too short");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "passwords not match");
        }
        synchronized (userAccount.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_account", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "account exists");
            }
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "register failed");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "account error");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password error");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        queryWrapper.eq("status", 1);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "account or password error");
        }
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "user is banned");
        }
        Date now = new Date();
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(now);
        this.updateById(updateUser);
        user.setLastLoginTime(now);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByEmail(String email, HttpServletRequest request) {
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "email required");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("status", 1);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user not found");
        }
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "user is banned");
        }
        Date now = new Date();
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(now);
        this.updateById(updateUser);
        user.setLastLoginTime(now);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public boolean resetPasswordByEmail(String email, String newPassword) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params empty");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password too short");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "user not found");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(encryptPassword);
        return this.updateById(updateUser);
    }

    @Override
    public boolean updateUserPassword(String userPassword, String newPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params empty");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "new password too short");
        }
        User loginUser = this.getLoginUser(request);
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        if (!encryptOldPassword.equals(loginUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "old password error");
        }
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setUserPassword(encryptNewPassword);
        return this.updateById(updateUser);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "not logged in");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "params empty");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id)
                .eq(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "user_profile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "user_name", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void sendSmsCode(String phone, String scene) {
        if (StringUtils.isBlank(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不能为空");
        }
        // 生成验证码
        String code = cn.hutool.core.util.RandomUtil.randomNumbers(6);

        // 发送短信
        boolean sent = smsUtils.sendSms(phone, code);
        if (!sent) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "短信发送失败");
        }

        // 保存验证码到数据库
        com.yupi.yuoj.model.entity.UserVerifyCode userVerifyCode = new com.yupi.yuoj.model.entity.UserVerifyCode();
        userVerifyCode.setPhone(phone);
        userVerifyCode.setCode(code);
        userVerifyCode.setScene(scene);
        userVerifyCode.setStatus(0); // 未使用

        // 过期时间 5 分钟
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        userVerifyCode.setExpireTime(calendar.getTime());

        userVerifyCodeMapper.insert(userVerifyCode);
    }

    @Override
    public long userRegisterByPhone(String phone, String code, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(phone, code, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        // 校验验证码
        checkVerifyCode(phone, code, "register");

        synchronized (phone.intern()) {
            // 检查手机号是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号已注册");
            }

            // 检查账号是否已存在（因为默认账号设为了手机号）
            QueryWrapper<User> accountQueryWrapper = new QueryWrapper<>();
            accountQueryWrapper.eq("user_account", phone);
            long accountCount = this.baseMapper.selectCount(accountQueryWrapper);
            if (accountCount > 0) {
                // 如果手机号作为账号已存在，尝试生成随机账号
                // 这里简单抛出异常，或改为生成随机账号逻辑
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号已被用作账号");
            }

            // 创建用户
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserAccount(phone); // 默认账号为手机号
            user.setPhone(phone);
            user.setUserPassword(encryptPassword);
            user.setUserName("用户" + cn.hutool.core.util.RandomUtil.randomString(6));
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
            }
            return user.getId();
        }
    }

    @Override
    public boolean resetPasswordByPhone(String phone, String code, String newPassword) {
        if (StringUtils.isAnyBlank(phone, code, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }

        // 校验验证码
        checkVerifyCode(phone, code, "reset");

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号未注册");
        }

        // 更新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(encryptPassword);
        return this.updateById(updateUser);
    }

    @Override
    public LoginUserVO userLoginByPhone(String phone, String code, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(phone, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 校验验证码
        checkVerifyCode(phone, code, "login");

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("status", 1);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号未注册");
        }
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "user is banned");
        }

        Date now = new Date();
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(now);
        this.updateById(updateUser);
        user.setLastLoginTime(now);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 校验验证码并标记为已使用
     */
    private void checkVerifyCode(String phone, String code, String scene) {
        QueryWrapper<com.yupi.yuoj.model.entity.UserVerifyCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        queryWrapper.eq("scene", scene);
        queryWrapper.eq("status", 0); // 未使用
        queryWrapper.gt("expire_time", new Date()); // 未过期
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit 1");

        com.yupi.yuoj.model.entity.UserVerifyCode verifyCode = userVerifyCodeMapper.selectOne(queryWrapper);

        if (verifyCode == null || !verifyCode.getCode().equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 标记为已使用
        verifyCode.setStatus(1);
        userVerifyCodeMapper.updateById(verifyCode);
    }
}
