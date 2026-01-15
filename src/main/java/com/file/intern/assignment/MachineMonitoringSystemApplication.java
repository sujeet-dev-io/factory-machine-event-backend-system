package com.file.intern.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.file.intern.assignment.entity")
@EnableJpaRepositories("com.file.intern.assignment.repository")
public class MachineMonitoringSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(MachineMonitoringSystemApplication.class, args);
	}

}
