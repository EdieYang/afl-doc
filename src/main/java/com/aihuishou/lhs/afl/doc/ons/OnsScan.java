package com.aihuishou.lhs.afl.doc.ons;

import com.aihuishou.common.mq.consumer.AliyunOnsConsumerProcessor;
import com.aihuishou.lhs.afl.doc.util.MdKiller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class OnsScan implements ApplicationContextAware {


    public static final String[] TABLE_TITLE = {"Consumer", "Topic", "ConsumerId", "Tag", "GenericType"};

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Map<String, AliyunOnsConsumerProcessor> processorBeanMap = applicationContext.getBeansOfType(AliyunOnsConsumerProcessor.class);
            List<List<String>> table = new ArrayList<>();
            table.add(Arrays.asList(TABLE_TITLE));
            processorBeanMap.forEach((k, v) -> {
                List<String> line = new ArrayList<>();
                line.add(k);
                line.add(v.getTopic());
                line.add(v.getConsumerId());
                line.add(v.getTag());
                line.add(v.getGenericType().toString());

                table.add(line);
            });
            String[][] markdownTable = new String[table.size()][table.get(0).size()];
            for (int i = 0; i < markdownTable.length; i++) {
                markdownTable[i] = table.get(i).toArray(new String[table.get(0).size()]);
            }

            markdown(markdownTable);
            log.info("ConsumerScan file write success");
        } catch (BeansException e) {
            log.error("ConsumerScan Consumer error");
        } catch (IOException e) {
            log.error("ConsumerScan file write error");
        }
    }


    private void markdown(String[][] table) throws IOException {
        MdKiller.SectionBuilder bd = MdKiller.of();
        bd.bigTitle("Ons消息订阅表");
        bd.br();
        bd.text("topic & consumer", MdKiller.Style.RED)
                .table()
                .data(table)
                .endTable();

        String markdown = bd.build();

        FileWriter writer = new FileWriter("ONS.md");
        writer.write(markdown);
        writer.flush();
    }
}
