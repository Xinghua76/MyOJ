package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.mapper.UserVerifyCodeMapper;
import com.yupi.yuoj.model.entity.UserVerifyCode;
import com.yupi.yuoj.service.UserVerifyCodeService;
import java.util.Date;
import javax.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserVerifyCodeServiceImpl implements UserVerifyCodeService {

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRE_MS = 10 * 60 * 1000L;

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private UserVerifyCodeMapper userVerifyCodeMapper;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Override
    public void sendEmailCode(String email, String scene) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(scene)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String code = RandomStringUtils.randomNumeric(CODE_LENGTH);
        Date now = new Date();
        UserVerifyCode record = new UserVerifyCode();
        record.setEmail(email);
        record.setScene(scene);
        record.setCode(code);
        record.setStatus(0);
        record.setExpireTime(new Date(now.getTime() + EXPIRE_MS));
        userVerifyCodeMapper.insert(record);

        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.isNotBlank(mailFrom)) {
            message.setFrom(mailFrom);
        }
        message.setTo(email);
        message.setSubject("Verification Code");
        message.setText("Your verification code is: " + code + " (valid for 10 minutes)");
        mailSender.send(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEmailCode(String email, String scene, String code) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(scene) || StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserVerifyCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email)
                .eq("scene", scene)
                .eq("status", 0)
                .orderByDesc("create_time")
                .last("limit 1");
        UserVerifyCode latest = userVerifyCodeMapper.selectOne(queryWrapper);
        if (latest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "code not found");
        }
        Date now = new Date();
        if (latest.getExpireTime() != null && latest.getExpireTime().before(now)) {
            UserVerifyCode update = new UserVerifyCode();
            update.setId(latest.getId());
            update.setStatus(2);
            userVerifyCodeMapper.updateById(update);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "code expired");
        }
        if (!code.equals(latest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "code error");
        }
        UserVerifyCode update = new UserVerifyCode();
        update.setId(latest.getId());
        update.setStatus(1);
        userVerifyCodeMapper.updateById(update);
    }
}
