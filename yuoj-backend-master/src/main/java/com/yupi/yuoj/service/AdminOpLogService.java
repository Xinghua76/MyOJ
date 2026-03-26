package com.yupi.yuoj.service;

import com.yupi.yuoj.model.entity.AdminOpLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AdminOpLogService extends IService<AdminOpLog> {
    void log(String opType, String opDesc, Object opData, Long userId);
}
