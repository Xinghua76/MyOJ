package com.yupi.yuoj.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云 SMS 工具类
 */
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
@Slf4j
public class SmsUtils {

    private String host;
    private String path;
    private String method;
    private String appcode;
    private String smsSignId;
    private String templateId;

    /**
     * 发送短信验证码
     *
     * @param mobile 手机号
     * @param code   验证码
     * @return 是否发送成功
     */
    public boolean sendSms(String mobile, String code) {
        String url = host + path;
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("mobile", mobile);
        // 参数格式：**code**:12345,**minute**:5
        queryMap.put("param", "**code**:" + code + ",**minute**:5");
        queryMap.put("smsSignId", smsSignId);
        queryMap.put("templateId", templateId);

        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "APPCODE " + appcode)
                    .form(queryMap)
                    .execute();
            
            String body = response.body();
            log.info("SMS response: {}", body);
            
            if (response.isOk()) {
                JSONObject jsonObject = JSONUtil.parseObj(body);
                // code 为 "0" 表示成功
                return "0".equals(jsonObject.getStr("code"));
            }
        } catch (Exception e) {
            log.error("Send SMS error", e);
        }
        return false;
    }
}
