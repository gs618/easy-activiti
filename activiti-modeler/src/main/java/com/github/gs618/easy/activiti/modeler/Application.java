package com.github.gs618.easy.activiti.modeler;

import com.github.gs618.easy.starter.swagger.EnableSwagger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author s.c.gao
 */
@SpringBootApplication
@EnableSwagger
@EnableAutoConfiguration(exclude = {org.activiti.spring.boot.SecurityAutoConfiguration.class})
@Slf4j
public class Application {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        log.info(" ========== " + applicationContext.getId() + " started ==========");
    }

}
