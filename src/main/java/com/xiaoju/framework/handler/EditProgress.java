package com.xiaoju.framework.handler;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditProgress {

    //操作哪些节点
    private List<String> progressIds;

    //将节点赋予progress什么值
    private Integer progress;
}
