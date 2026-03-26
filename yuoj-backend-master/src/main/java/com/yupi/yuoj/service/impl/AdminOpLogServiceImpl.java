package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.mapper.AdminOpLogMapper;
import com.yupi.yuoj.model.entity.AdminOpLog;
import com.yupi.yuoj.service.AdminOpLogService;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import java.util.Date;

@Service
public class AdminOpLogServiceImpl extends ServiceImpl<AdminOpLogMapper, AdminOpLog>
    implements AdminOpLogService {
    
    private final static Gson GSON = new Gson();

    @Override
    public void log(String opType, String opDesc, Object opData, Long userId) {
        AdminOpLog log = new AdminOpLog();
        log.setUserId(userId);
        log.setOpType(opType);
        log.setOpDesc(opDesc);
        if (opData != null) {
            log.setOpData(GSON.toJson(opData));
        }
        log.setCreateTime(new Date());
        this.save(log);
    }
}
