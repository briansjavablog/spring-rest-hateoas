package com.blog.rest.hateoas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

@SuppressWarnings("deprecation")
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {     	
    	return new ObjectMapper()
    				.registerModule(new JSR310Module())
    				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}