package com.chrisdjames1.temperatureanalysis.controller;

import com.chrisdjames1.temperatureanalysis.model.cdm.dataaccesslayer.CdmGroup;
import com.chrisdjames1.temperatureanalysis.service.AnalysisService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class AnalysisController {

    private AnalysisService analysisService;

    @GetMapping(path = "read/root/raw")
    public ResponseEntity<String> readRootGroupRaw() {
        return new ResponseEntity<>(analysisService.readRootGroupRaw(), HttpStatus.OK);
    }

    @GetMapping(path = "read/root")
    public ResponseEntity<CdmGroup> readRootGroup() {
        return new ResponseEntity<>(analysisService.readRootGroupSchema(), HttpStatus.OK);
    }

    @GetMapping(path = "read/variable/string")
    public ResponseEntity<String> readVariableToString(@RequestParam(name = "variable") String variable,
            @RequestParam(name = "section-spec") String sectionSpec) {

        return new ResponseEntity<>(analysisService.readVariableToString(variable, sectionSpec), HttpStatus.OK);
    }
}
