package com.example.generator;

import org.springframework.boot.SpringApplication;

public class TestGeneratorApplication {

    public static void main(String[] args) {
		SpringApplication.from(GeneratorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
