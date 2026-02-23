package com.example.generator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.generator.job.DocumentTasks;
import com.example.generator.job.GeneratorRunner;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class GeneratorApplicationTests {

	@MockitoBean
	DocumentTasks tasks;

	@MockitoBean
	GeneratorRunner runner;

	@Test
	void contextLoads() {
	}

}
