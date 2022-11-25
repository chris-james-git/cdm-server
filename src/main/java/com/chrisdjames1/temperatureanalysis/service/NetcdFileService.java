package com.chrisdjames1.temperatureanalysis.service;

import com.chrisdjames1.temperatureanalysis.config.CdmDatasetProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@Service
public class NetcdFileService {

    private final CdmDatasetProperties properties;

    private NetcdfFile ncFile;

    protected NetcdFileService(CdmDatasetProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void postConstruct() {
        try {
            ncFile = NetcdfFiles.open(properties.getPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not open " + properties.getPath(), e);
        }
    }

    public NetcdfFile getNcFile() {
        return ncFile;
    }

    @PreDestroy
    public void preDestroy() {
        try {
            ncFile.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not close " + properties.getPath(), e);
        }
    }
}
