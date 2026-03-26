package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.DeleteRequest;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.model.dto.user.EmailCodeLoginRequest;
import com.yupi.yuoj.model.dto.user.EmailCodeSendRequest;
import com.yupi.yuoj.model.dto.user.EmailPasswordResetRequest;
import com.yupi.yuoj.model.dto.user.SmsCodeSendRequest;
import com.yupi.yuoj.model.dto.user.UserRegisterByPhoneRequest;
import com.yupi.yuoj.model.dto.user.UserResetPasswordByPhoneRequest;
import com.yupi.yuoj.model.dto.user.UserAddRequest;
import com.yupi.yuoj.model.dto.user.UserLoginRequest;
import com.yupi.yuoj.model.dto.user.UserQueryRequest;
import com.yupi.yuoj.model.dto.user.UserRegisterRequest;
import com.yupi.yuoj.model.dto.user.UserUpdateMyRequest;
import com.yupi.yuoj.model.dto.user.UserUpdatePasswordRequest;
import com.yupi.yuoj.model.dto.user.UserUpdateRequest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.LoginUserVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.service.UserVerifyCodeService;
import com.yupi.yuoj.service.AdminOpLogService;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User endpoints
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserVerifyCodeService userVerifyCodeService;

    @Resource
    private AdminOpLogService adminOpLogService;

    @PostMapping("/email/code/send")
    public BaseResponse<Boolean> sendEmailCode(@RequestBody EmailCodeSendRequest request) {
        if (request == null || StringUtils.isBlank(request.getEmail()) || StringUtils.isBlank(request.getScene())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userVerifyCodeService.sendEmailCode(request.getEmail(), request.getScene());
        return ResultUtils.success(true);
    }

    @PostMapping("/sms/code/send")
    public BaseResponse<Boolean> sendSmsCode(@RequestBody SmsCodeSendRequest request) {
        if (request == null || StringUtils.isBlank(request.getPhone()) || StringUtils.isBlank(request.getScene())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.sendSmsCode(request.getPhone(), request.getScene());
        return ResultUtils.success(true);
    }

    @PostMapping("/register/phone")
    public BaseResponse<Long> userRegisterByPhone(@RequestBody UserRegisterByPhoneRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phone = request.getPhone();
        String code = request.getCode();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        if (StringUtils.isAnyBlank(phone, code, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegisterByPhone(phone, code, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/password/reset/phone")
    public BaseResponse<Boolean> resetPasswordByPhone(@RequestBody UserResetPasswordByPhoneRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phone = request.getPhone();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();
        if (StringUtils.isAnyBlank(phone, code, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "passwords not match");
        }
        boolean result = userService.resetPasswordByPhone(phone, code, newPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();
        if (StringUtils.isAnyBlank(userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "email required");
        }
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "code required");
        }
        if (StringUtils.isBlank(userAccount)) {
            // 邮箱注册时自动生成账号
            userAccount = "email_" + RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        }
        userVerifyCodeService.verifyEmailCode(email, "register", code);
        User exist = userService.getOne(new QueryWrapper<User>().eq("email", email));
        if (exist != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "email exists");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        if (StringUtils.isNotBlank(email)) {
            User update = new User();
            update.setId(result);
            update.setEmail(email);
            userService.updateById(update);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/login/email")
    public BaseResponse<LoginUserVO> userLoginByEmail(@RequestBody EmailCodeLoginRequest emailLoginRequest,
            HttpServletRequest request) {
        if (emailLoginRequest == null
                || StringUtils.isAnyBlank(emailLoginRequest.getEmail(), emailLoginRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userVerifyCodeService.verifyEmailCode(emailLoginRequest.getEmail(), "login", emailLoginRequest.getCode());
        LoginUserVO loginUserVO = userService.userLoginByEmail(emailLoginRequest.getEmail(), request);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/login/phone")
    public BaseResponse<LoginUserVO> userLoginByPhone(@RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phone = userLoginRequest.getPhone();
        String code = userLoginRequest.getCode();
        if (StringUtils.isAnyBlank(phone, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLoginByPhone(phone, code, request);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/password/reset/email")
    public BaseResponse<Boolean> resetPasswordByEmail(@RequestBody EmailPasswordResetRequest resetRequest) {
        if (resetRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String email = resetRequest.getEmail();
        String code = resetRequest.getCode();
        String newPassword = resetRequest.getNewPassword();
        String confirmPassword = resetRequest.getConfirmPassword();
        if (StringUtils.isAnyBlank(email, code, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "passwords not match");
        }
        userVerifyCodeService.verifyEmailCode(email, "reset", code);
        boolean result = userService.resetPasswordByEmail(email, newPassword);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        String userPassword = userAddRequest.getUserPassword();
        if (StringUtils.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "password required");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex(("yupi" + userPassword).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        User loginUser = userService.getLoginUser(request);
        adminOpLogService.log("user_delete", "Delete user " + deleteRequest.getId(), deleteRequest, loginUser.getId());
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        User loginUser = userService.getLoginUser(request);
        adminOpLogService.log("user_update", "Update user " + user.getId(), user, loginUser.getId());
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update/password")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserUpdatePasswordRequest updatePasswordRequest,
            HttpServletRequest request) {
        if (updatePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPassword = updatePasswordRequest.getUserPassword();
        String newPassword = updatePasswordRequest.getNewPassword();
        String confirmPassword = updatePasswordRequest.getConfirmPassword();
        if (StringUtils.isAnyBlank(userPassword, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "passwords not match");
        }
        boolean result = userService.updateUserPassword(userPassword, newPassword, request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
