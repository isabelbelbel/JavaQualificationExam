package com.qualification.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME = "basicAuth";

	@Bean
	public OpenAPI projectSchedulingOpenApi() {
		return new OpenAPI()
				.info(new Info().title("JavaQualificationExam API")
						.description(
								"REST API for project plans, tasks, dependencies, and schedule calculation")
						.version("1.0.0"))
				.components(new Components()
						.addSecuritySchemes(
								SECURITY_SCHEME,
						new SecurityScheme().type(SecurityScheme.Type.HTTP)
						.scheme("basic")));
	}
}