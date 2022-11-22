package com.chrisdjames1.temperatureanalysis.controller;

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

    @GetMapping(path = "variable")
    public ResponseEntity<String> readVariable(@RequestParam(name = "variable") String variable,
                                               @RequestParam(name = "section-spec") String sectionSpec) {

        return new ResponseEntity<>(analysisService.readVariable(variable, sectionSpec), HttpStatus.OK);
    }
}
