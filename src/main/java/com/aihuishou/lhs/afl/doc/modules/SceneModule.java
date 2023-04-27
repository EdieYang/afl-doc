package com.aihuishou.lhs.afl.doc.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场景模块
 *
 * @author eddieYang
 * @date 2023/4/26
 * @since
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SceneModule {

    /**
     * 业务主体
     */
    private String subject;

    /**
     * 场景前提
     */
    private String caseWhen;

    /**
     * 入口说明
     */
    private String entryPoint;

    /**
     * 入口方法
     */
    private String entryMethod;
}
