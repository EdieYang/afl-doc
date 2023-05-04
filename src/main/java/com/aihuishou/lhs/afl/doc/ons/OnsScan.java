package com.aihuishou.lhs.afl.doc.ons;

import com.aihuishou.common.mq.consumer.AliyunOnsConsumerProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class OnsScan implements ApplicationContextAware {


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Map<String, AliyunOnsConsumerProcessor> processorBeanMap = applicationContext.getBeansOfType(AliyunOnsConsumerProcessor.class);
            StringBuilder str = new StringBuilder();
            processorBeanMap.forEach((k, v) -> {
                str.append("Consumer：").append(k)
                        .append("<br> Topic：").append(v.getTopic())
                        .append("<br> ConsumerId：").append(v.getConsumerId())
                        .append("<br> Tag：").append(v.getTag())
                        .append("<br> GenericType：").append(v.getGenericType().toString())
                        .append("<br><br>");
            });
            bufferedWriterMethod(System.getProperty("user.dir") + File.separator + "ConsumerScan.md", str.toString());
            log.info("ConsumerScan file write success");
        } catch (BeansException e) {
            log.error("ConsumerScan Consumer error");
        } catch (IOException e) {
            log.error("ConsumerScan file write error");
        }
    }

    public static void bufferedWriterMethod(String filepath, String content) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath))) {
            bufferedWriter.write(content);
        }
    }
}
