package com.yupi.yuoj.model.dto.question;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionImportExcelDTO {
    @ExcelProperty("标题")
    private String title;
    @ExcelProperty("内容")
    private String content;
    @ExcelProperty("标签(逗号分隔)")
    private String tags;
    @ExcelProperty("答案")
    private String answer;
    @ExcelProperty("判题用例(JSON)")
    private String judgeCase;
    @ExcelProperty("判题配置(JSON)")
    private String judgeConfig;
}
