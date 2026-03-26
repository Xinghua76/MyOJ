package com.yupi.yuoj.manager;

import com.yupi.yuoj.config.MinioClientConfig;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储操作
 */
@Component
public class MinioManager {

    @Resource
    private MinioClientConfig minioClientConfig;

    @Resource
    private MinioClient minioClient;

    /**
     * 上传对象
     *
     * @param objectName 对象名
     * @param file 文件
     * @param contentType 内容类型
     */
    public void putObject(String objectName, File file, String contentType) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(minioClientConfig.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.length(), -1);
            if (contentType != null && !contentType.isEmpty()) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build());
        }
    }

    /**
     * 构建访问地址
     *
     * @param objectName 对象名
     * @return 访问地址
     */
    public String buildObjectUrl(String objectName) {
        String endpoint = minioClientConfig.getEndpoint();
        String bucket = minioClientConfig.getBucket();
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String path = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        return base + "/" + bucket + "/" + path;
    }
}
