package com.juv3nil3.icdg.config;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaParserConfig {

    @Bean(name = "customJavaParser")
    public JavaParser javaParser() {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        return new JavaParser(configuration);
    }
}

