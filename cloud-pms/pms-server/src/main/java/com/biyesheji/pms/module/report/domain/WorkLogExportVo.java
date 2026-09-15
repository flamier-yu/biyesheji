package com.biyesheji.pms.module.report.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 工时报表导出模型
 */
@Data
public class WorkLogExportVo {

    @ExcelProperty("项目名称")
    @ColumnWidth(24)
    private String projectName;

    @ExcelProperty("任务名称")
    @ColumnWidth(24)
    private String taskName;

    @ExcelProperty("填报人")
    @ColumnWidth(12)
    private String nickName;

    @ExcelProperty("工作日期")
    @ColumnWidth(14)
    private String workDate;

    @ExcelProperty("工时数")
    @ColumnWidth(10)
    private Double hours;

    @ExcelProperty("工作内容")
    @ColumnWidth(40)
    private String content;

    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;
}
