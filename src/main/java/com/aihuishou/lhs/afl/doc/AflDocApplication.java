package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.annotation.EnableAflDocScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@EnableAflDocScan
public class AflDocApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(AflDocApplication.class, args);
    }


}
