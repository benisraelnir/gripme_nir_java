package com.github.benisraelnir.gripme;

import com.github.benisraelnir.gripme.cli.GripCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication
public class GripmeApplication {
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(GripmeApplication.class, args)));
    }

    @Bean
    public CommandLineRunner commandLineRunner(GripCommand gripCommand, IFactory factory) {
        return args -> {
            int exitCode = new CommandLine(gripCommand, factory).execute(args);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        };
    }
}
