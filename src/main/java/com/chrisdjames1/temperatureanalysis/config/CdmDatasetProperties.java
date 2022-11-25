package com.chrisdjames1.temperatureanalysis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cdm.dataset")
public class CdmDatasetProperties {

    private String path;

}
