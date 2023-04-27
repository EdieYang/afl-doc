package com.aihuishou.lhs.afl.doc.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主体模块
 *
 * @author eddieYang
 * @date 2023/4/26
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizModule {

    private String subject;

    private String subjectName;

    private String desc;

    private String url;

    private String note;

    List<SceneModule> sceneModules;

}
