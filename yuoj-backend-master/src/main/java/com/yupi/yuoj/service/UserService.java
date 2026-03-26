package com.yupi.yuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuoj.model.dto.user.UserQueryRequest;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.LoginUserVO;
import com.yupi.yuoj.model.vo.UserVO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO userLoginByEmail(String email, HttpServletRequest request);

    boolean resetPasswordByEmail(String email, String newPassword);

    boolean updateUserPassword(String userPassword, String newPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    User getLoginUserPermitNull(HttpServletRequest request);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User user);

    boolean userLogout(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    UserVO getUserVO(User user);

    List<UserVO> getUserVO(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param scene 场景
     */
    void sendSmsCode(String phone, String scene);

    /**
     * 手机号注册
     *
     * @param phone         手机号
     * @param code          验证码
     * @param userPassword  密码
     * @param checkPassword 校验密码
     * @return 用户 ID
     */
    long userRegisterByPhone(String phone, String code, String userPassword, String checkPassword);

    /**
     * 手机号重置密码
     *
     * @param phone       手机号
     * @param code        验证码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPasswordByPhone(String phone, String code, String newPassword);

    /**
     * 手机号登录
     *
     * @param phone   手机号
     * @param code    验证码
     * @param request HttpServletRequest
     * @return LoginUserVO
     */
    LoginUserVO userLoginByPhone(String phone, String code, HttpServletRequest request);
}
