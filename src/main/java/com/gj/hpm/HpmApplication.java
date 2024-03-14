package com.gj.hpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.config.WebConfig;

@RestController
@SpringBootApplication
@Import(WebConfig.class)
public class HpmApplication {

	public static void main(String[] args) {
		SpringApplication.run(HpmApplication.class, args);
	}

}
