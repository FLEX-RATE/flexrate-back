package com.flexrate.flexrate_back;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FlexrateBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlexrateBackApplication.class, args);
	}

}
