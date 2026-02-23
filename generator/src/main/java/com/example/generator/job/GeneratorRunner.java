package com.example.generator.job;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * After launching the application, document generation begins.
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class GeneratorRunner implements ApplicationRunner {

    DocumentTasks documentTasks;

    public GeneratorRunner(DocumentTasks documentTasks) {
        this.documentTasks = documentTasks;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        documentTasks.generateDocuments();
    }

}
