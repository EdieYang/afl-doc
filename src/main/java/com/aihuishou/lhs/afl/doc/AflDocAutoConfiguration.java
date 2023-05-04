package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.ons.OnsScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文档自动装配
 *
 * @author eddieYang
 * @date 2023/5/4
 * @since
 */
@Configuration
public class AflDocAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "afl.doc.scan-ons.enabled")
    public OnsScan onsScan() {
        return new OnsScan();
    }

    @Bean
    @ConditionalOnProperty(value = "afl.doc.scan-doc.enabled")
    public DocScanRegistrar docScanRegistrar() {
        return new DocScanRegistrar();
    }

}
