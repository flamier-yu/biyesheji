package com.biyesheji.pms.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页返回结果
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页数据")
    private List<T> rows;

    public PageResult() {
        this.total = 0L;
        this.rows = new ArrayList<>();
    }

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 分页对象转换：把 Entity 分页转换为 VO 分页
     */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> converter) {
        List<V> list = page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), list);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>();
    }
}
