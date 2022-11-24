package com.chrisdjames1.temperatureanalysis.model.value;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum FnReadVariableArg {
    VARIABLE("variable"),
    SECTION_SPEC("section-spec");

    private static final Map<String, FnReadVariableArg> LOOKUP;

    private final String arg;

    static {
        LOOKUP = Arrays.stream(FnReadVariableArg.values()).collect(Collectors.toMap(
                FnReadVariableArg::getArg, Function.identity()));
    }
    public static Set<String> allArgs() {
        return LOOKUP.keySet();
    }

}
