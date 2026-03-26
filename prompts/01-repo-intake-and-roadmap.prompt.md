我在二次开发一个开源项目做毕设。下面是仓库目录结构与关键配置。
/
├── prompts/
│   ├── sql/
│   │   ├── create_table.sql
│   │   └── post_es_mapping.json
│   ├── 00-system.prompt.md
│   └── 10-auth.prompt.md
├── yuoj-backend-master/
│   ├── sql/
│   │   ├── create_table.sql
│   │   └── post_es_mapping.json
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yupi/yuoj/
│   │   │   │   ├── annotation/
│   │   │   │   ├── aop/
│   │   │   │   ├── common/
│   │   │   │   ├── config/
│   │   │   │   ├── constant/
│   │   │   │   ├── controller/
│   │   │   │   ├── esdao/
│   │   │   │   ├── exception/
│   │   │   │   ├── job/
│   │   │   │   ├── judge/
│   │   │   │   ├── manager/
│   │   │   │   ├── mapper/
│   │   │   │   ├── model/
│   │   │   │   ├── service/
│   │   │   │   ├── utils/
│   │   │   │   └── MainApplication.java
│   │   │   └── resources/
│   │   │       ├── mapper/
│   │   │       ├── application.yml
│   │   │       └── ...
│   │   └── test/
│   ├── pom.xml
│   └── README.md
└── yuoj-frontend-master/
    ├── public/
    ├── src/
    │   ├── access/
    │   ├── assets/
    │   ├── components/
    │   ├── layouts/
    │   ├── plugins/
    │   ├── router/
    │   ├── store/
    │   ├── views/
    │   │   ├── admin/
    │   │   ├── post/
    │   │   ├── question/
    │   │   └── user/
    │   ├── App.vue
    │   └── main.ts
    ├── package.json
    ├── tsconfig.json
    └── vue.config.js
{
  "name": "yuoj-frontend",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "serve": "vue-cli-service serve",
    "build": "vue-cli-service build",
    "lint": "vue-cli-service lint"
  },
  "dependencies": {
    "@bytemd/plugin-gfm": "^1.21.0",
    "@bytemd/plugin-highlight": "^1.21.0",
    "@bytemd/vue-next": "^1.21.0",
    "axios": "^1.4.0",
    "core-js": "^3.8.3",
    "moment": "^2.29.4",
    "monaco-editor": "^0.41.0",
    "monaco-editor-webpack-plugin": "^7.1.0",
    "vue": "^3.2.13",
    "vue-router": "^4.0.3",
    "vuex": "^4.0.0"
  },
  "devDependencies": {
    "@arco-design/web-vue": "^2.49.1",
    "@typescript-eslint/eslint-plugin": "^5.4.0",
    "@typescript-eslint/parser": "^5.4.0",
    "@vue/cli-plugin-babel": "~5.0.0",
    "@vue/cli-plugin-eslint": "~5.0.0",
    "@vue/cli-plugin-router": "~5.0.0",
    "@vue/cli-plugin-typescript": "~5.0.0",
    "@vue/cli-plugin-vuex": "~5.0.0",
    "@vue/cli-service": "~5.0.0",
    "@vue/eslint-config-typescript": "^9.1.0",
    "eslint": "^7.32.0",
    "eslint-config-prettier": "^8.3.0",
    "eslint-plugin-prettier": "^4.0.0",
    "eslint-plugin-vue": "^8.0.3",
    "openapi-typescript-codegen": "^0.25.0",
    "prettier": "^2.4.1",
    "typescript": "~4.5.5"
  }
}

spring:
  application:
    name: yuoj-backend
  # 默认 dev 环境
  profiles:
    active: dev
  # 支持 swagger3
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  # session 配置
  session:
    # todo 取消注释开启分布式 session（需先配置 Redis）
    store-type: redis
    # 30 天过期
    timeout: 2592000
  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/code_arena?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
    username: root
    password: 123456

  rabbitmq:
    host: localhost
    port: 5672
    virtual-host: /code-arena
    username: codearena
    password: codearena


  # Redis 配置
  redis:
    database: 1
    host: localhost
    port: 6379
    timeout: 5000
    password: 123456
  # Elasticsearch 配置
  elasticsearch:
    uris: http://localhost:9200
    username: root
    password: 123456
  # 文件上传
  servlet:
    multipart:
      # 大小限制
      max-file-size: 10MB
  # 邮件配置
  mail:
    host: smtp.qq.com
    port: 587
    username: 1952521955@qq.com
    password: wdevfscwbtdycabf
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000
server:
  address: 0.0.0.0
  port: 8121
  servlet:
    context-path: /api
    # cookie 30 天过期
    session:
      cookie:
        max-age: 2592000
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: isDelete # 全局逻辑删除字段名
      logic-delete-value: 1 # 逻辑已删除
      logic-not-delete-value: 0 # 逻辑未删除
# 对象存储
# todo 需替换配置
minio:
  endpoint: http://localhost:9000  # MinIO 服务地址
  access-key: minioadmin  # MinIO 访问密钥
  secret-key: minioadmin  # MinIO 密钥
  bucket: code-arena  # 存储桶名称
# 代码沙箱配置
codesandbox:
  type: remote



请你：
1) 识别技术栈（后端框架/ORM/鉴权/数据库/前端框架/构建工具）。
2) 画出当前模块划分与主要数据流（登录→提交→判题→展示）。
3) 给出“按里程碑迭代”的开发路线图（MVP→完善→答辩增强），每个里程碑明确：功能范围、涉及表、涉及接口、涉及页面、预计风险。
4) 生成一份“改造为毕设的差异点清单”（我新增了哪些模块/我做了哪些工程化/安全/性能优化），方便写论文与答辩。
