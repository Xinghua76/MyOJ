﻿﻿﻿﻿﻿﻿﻿﻿-- Database schema for graduation project (MySQL 8.0+)
-- Keep ASCII-only to avoid encoding issues

CREATE DATABASE IF NOT EXISTS code_arena
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE code_arena;

-- Users
CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_account VARCHAR(64) NOT NULL COMMENT '账号',
  user_password VARCHAR(255) NOT NULL COMMENT '密码',
  user_name VARCHAR(64) NULL COMMENT '昵称',
  user_avatar VARCHAR(512) NULL COMMENT '头像',
  user_profile VARCHAR(512) NULL COMMENT '个人简介',
  email VARCHAR(128) NULL COMMENT '邮箱',
  phone VARCHAR(32) NULL COMMENT '手机号',
  gender TINYINT NULL COMMENT '性别(0未知/1男/2女)',
  user_role VARCHAR(32) NOT NULL DEFAULT 'user' COMMENT '角色(user/admin/ban)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1正常/0禁用)',
  last_login_time DATETIME NULL COMMENT '最近登录时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  UNIQUE KEY uk_user_account (user_account),
  UNIQUE KEY uk_email (email),
  UNIQUE KEY uk_phone (phone),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- Verification codes (register / reset password)
CREATE TABLE IF NOT EXISTS user_verify_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NULL COMMENT '用户ID',
  email VARCHAR(128) NULL COMMENT '邮箱',
  phone VARCHAR(32) NULL COMMENT '手机号',
  code VARCHAR(16) NOT NULL COMMENT '验证码',
  scene VARCHAR(32) NOT NULL COMMENT '场景(register/reset)',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0未用/1已用/2过期)',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_user_id (user_id),
  KEY idx_email (email),
  KEY idx_phone (phone),
  KEY idx_scene_status (scene, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码';

-- Questions
CREATE TABLE IF NOT EXISTS question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(256) NOT NULL COMMENT '题目标题',
  content TEXT NOT NULL COMMENT '题目内容',
  tags JSON NULL COMMENT '题目标签(JSON数组)',
  difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '难度(1易/2中/3难)',
  answer TEXT NULL COMMENT '参考答案',
  submit_num INT NOT NULL DEFAULT 0 COMMENT '提交数',
  accepted_num INT NOT NULL DEFAULT 0 COMMENT '通过数',
  judge_case JSON NULL COMMENT '判题用例(JSON数组)',
  judge_config JSON NULL COMMENT '判题配置(JSON对象)',
  thumb_num INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  favour_num INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  user_id BIGINT NOT NULL COMMENT '创建者ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1上架/0下架)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_user_id (user_id),
  KEY idx_difficulty (difficulty),
  KEY idx_status (status),
  KEY idx_title (title),
  KEY idx_submit_num (submit_num),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

-- Question submissions
CREATE TABLE IF NOT EXISTS question_submit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  language VARCHAR(64) NOT NULL COMMENT '编程语言',
  code TEXT NOT NULL COMMENT '提交代码',
  judge_info JSON NULL COMMENT '判题信息(JSON对象)',
  status INT NOT NULL DEFAULT 0 COMMENT '状态(0待判/1判题中/2成功/3失败)',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  contest_id BIGINT NULL COMMENT '比赛ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',   
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_question_id (question_id),
  KEY idx_contest_id (contest_id),
  KEY idx_user_id (user_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目提交';

-- Question favourites (collection)
CREATE TABLE IF NOT EXISTS question_favour (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  UNIQUE KEY uk_question_user (question_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目收藏';

-- Discussions / solutions
CREATE TABLE IF NOT EXISTS solution (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(256) NULL COMMENT '标题',
  content TEXT NOT NULL COMMENT '题解内容',
  like_num INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  comment_num INT NOT NULL DEFAULT 0 COMMENT '评论数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1正常/0隐藏)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_question_id (question_id),
  KEY idx_user_id (user_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题解';

CREATE TABLE IF NOT EXISTS solution_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  solution_id BIGINT NOT NULL COMMENT '题解ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  content TEXT NOT NULL COMMENT '评论内容',
  parent_id BIGINT NULL COMMENT '父评论ID',
  like_num INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1正常/0隐藏)',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_solution_id (solution_id),
  KEY idx_user_id (user_id),
  KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题解评论';

-- Contest
CREATE TABLE IF NOT EXISTS contest (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(256) NOT NULL COMMENT '比赛标题',
  description TEXT NULL COMMENT '比赛描述',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0未开始/1进行中/2已结束)',
  creator_id BIGINT NOT NULL COMMENT '创建者ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_time (start_time, end_time),
  KEY idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛';

CREATE TABLE IF NOT EXISTS contest_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  contest_id BIGINT NOT NULL COMMENT '比赛ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  score INT NOT NULL DEFAULT 100 COMMENT '题目分值',
  order_no INT NOT NULL DEFAULT 1 COMMENT '题目顺序',
  UNIQUE KEY uk_contest_question (contest_id, question_id),
  KEY idx_contest_id (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛题目';

CREATE TABLE IF NOT EXISTS contest_signup (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  contest_id BIGINT NOT NULL COMMENT '比赛ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  signup_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1已报名/0取消)',
  UNIQUE KEY uk_contest_user (contest_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛报名';

CREATE TABLE IF NOT EXISTS contest_rank (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  contest_id BIGINT NOT NULL COMMENT '比赛ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  solved_count INT NOT NULL DEFAULT 0 COMMENT '解决题数',
  total_score INT NOT NULL DEFAULT 0 COMMENT '总分',
  penalty INT NOT NULL DEFAULT 0 COMMENT '罚时',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_contest_user (contest_id, user_id),
  KEY idx_contest_id (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛排行榜';

-- System notices (admin)
CREATE TABLE IF NOT EXISTS notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  title VARCHAR(256) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  publisher_id BIGINT NOT NULL COMMENT '发布人ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1发布/0草稿)',
  publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  KEY idx_status (status),
  KEY idx_publisher_id (publisher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告';

-- 1. 题解点赞表 (支持唯一约束 userId + solutionId)
CREATE TABLE IF NOT EXISTS solution_thumb (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  solution_id BIGINT NOT NULL COMMENT '题解ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  UNIQUE KEY uk_solution_user (solution_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题解点赞';

-- 2. 评论点赞表
CREATE TABLE IF NOT EXISTS solution_comment_thumb (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  comment_id BIGINT NOT NULL COMMENT '评论ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  UNIQUE KEY uk_comment_user (comment_id, user_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题解评论点赞';