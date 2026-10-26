package com.eventPro.api.Config;

import com.eventPro.api.Service.DataInitializer.AdminInitializerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitializerConfig {

    @Bean
    public ApplicationRunner initializeAdmin(AdminInitializerService service,
                                             @Value("${eventPro.admin.name}")String name,
                                             @Value("${eventPro.admin.email}")String email,
                                             @Value("${eventPro.admin.password}")String password){
        return (args) ->  service.createOneAdmin(name,email,password);
    }
}
