package com.example.test.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult<T> {

    private List<T> resultList;   // 当前页的数据

    private int page;          // 当前页

    private int size;          // 每页大小

    private boolean hasNext;   // 是否还有下一页

}
