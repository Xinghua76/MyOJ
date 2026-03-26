package com.yupi.yuoj.service;

public interface UserVerifyCodeService {

    void sendEmailCode(String email, String scene);

    void verifyEmailCode(String email, String scene, String code);
}
