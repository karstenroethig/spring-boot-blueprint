package karstenroethig.springbootblueprint.webapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
@Getter
@Setter
public class ApplicationProperties
{
	private String baseUrl = "http://localhost:8080";
}
