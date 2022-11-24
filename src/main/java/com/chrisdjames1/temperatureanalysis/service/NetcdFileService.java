package com.chrisdjames1.temperatureanalysis.service;

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

    // TODO: Move to properties
    private static final String PATH = "C:/Users/Chris James/Downloads/Land_and_Ocean_LatLong1.nc";

    private NetcdfFile ncFile;

    @PostConstruct
    public void postConstruct() {
        try {
            ncFile = NetcdfFiles.open(PATH);
        } catch (IOException e) {
            throw new RuntimeException("Could not open " + PATH, e);
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
            throw new RuntimeException("Could not close " + PATH, e);
        }
    }
}
