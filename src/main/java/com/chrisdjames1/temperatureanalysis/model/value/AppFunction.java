package com.chrisdjames1.temperatureanalysis.model.value;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum AppFunction {

    READ_ROOT_GROUP("read-root-group", Set.of()),
    READ_VARIABLE("read-variable", FnReadVariableArg.allArgs()),
    AVG_VARIABLE("avg-variable", FnAvgVariableArg.allArgs());

    private static final Map<String, AppFunction> LOOKUP;

    private final String functionArgValue;
    private final Set<String> functionArgs;

    static {
        LOOKUP = Arrays.stream(AppFunction.values()).collect(Collectors.toMap(
                AppFunction::getFunctionArgValue, Function.identity()));
    }

    public static AppFunction fromFunctionArgValue(String functionArgValue) {
        return Objects.requireNonNull(LOOKUP.get(functionArgValue),
                "Unrecognised function arg value: " + functionArgValue);
    }
}
