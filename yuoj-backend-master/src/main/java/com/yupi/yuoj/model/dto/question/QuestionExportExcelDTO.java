package com.yupi.yuoj.model.dto.question;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 题目导出 Excel DTO
 */
@Data
public class QuestionExportExcelDTO {

    @ExcelProperty("题目ID")
    private Long id;

    @ExcelProperty("标题")
    private String title;

    @ExcelProperty("内容")
    private String content;

    @ExcelProperty("标签(逗号分隔)")
    private String tags;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("提交数")
    private Integer submitNum;

    @ExcelProperty("通过数")
    private Integer acceptedNum;

    @ExcelProperty("判题用例(JSON)")
    private String judgeCase;

    @ExcelProperty("判题配置(JSON)")
    private String judgeConfig;

    @ExcelProperty("点赞数")
    private Integer thumbNum;

    @ExcelProperty("收藏数")
    private Integer favourNum;

    @ExcelProperty("创建人ID")
    private Long userId;

    @ExcelProperty("创建时间")
    private Date createTime;

    @ExcelProperty("更新时间")
    private Date updateTime;
}
