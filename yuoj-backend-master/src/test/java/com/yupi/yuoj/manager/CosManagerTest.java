package com.yupi.yuoj.manager;

import java.io.File;
import javax.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * MinIO 操作测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
@Disabled("需要本地/测试环境 MinIO 服务与配置，默认不执行")
class CosManagerTest {

    @Resource
    private MinioManager minioManager;

    @Test
    void putObject() {
        try {
            minioManager.putObject("test.json", new File("test.json"), "application/json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
