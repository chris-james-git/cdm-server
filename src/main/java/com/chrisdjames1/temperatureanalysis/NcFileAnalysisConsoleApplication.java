package com.chrisdjames1.temperatureanalysis;

import com.chrisdjames1.temperatureanalysis.model.value.AppFunction;
import com.chrisdjames1.temperatureanalysis.model.value.FnAvgVariableArg;
import com.chrisdjames1.temperatureanalysis.model.value.FnReadVariableArg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple tool for reading .nc files from, for example:
 *   <a href="https://berkeleyearth.org/data/">https://berkeleyearth.org/data/</a><br>
 * <br>
 * Also see <a href="https://docs.unidata.ucar.edu/netcdf-java/current/userguide/reading_cdm.html#using-toolsui-to-browse-the-metadata-of-a-dataset">reading_cdm.html</a><br>
 * <br>
 * This could be adapted into a REST API with a UI that shows data from any file, presenting the variables in drop-downs
 * and providing section-spec inputs.
 */
@Slf4j
//@SpringBootApplication
@Deprecated
public class NcFileAnalysisConsoleApplication {

    private static final String APP_NAME = "ncfa";

    private static final String ARG_PATH = "path";
    private static final String ARG_FUNCTION = "function";

    private static final String ARGS_REGEX = "^--([\\w-]*)=(.*)$";
    private static final String HELP_REGEX = "^--help$";
    private static final Set<String> MANDATORY_ARGS = Set.of(ARG_PATH);
    private static final Set<String> OPTIONAL_ARGS = Set.of();

    private static final Map<String, String> rootParams = new HashMap<>();
    private static AppFunction function;
    private static final Map<String, String> functionParams = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(NcFileAnalysisConsoleApplication.class, args);
        if (args.length == 0) {
            throw new IllegalStateException("Missing arguments!");
        }
        if (args[0].matches(HELP_REGEX)) {
            showHelp();
            return;
        }
        Pattern pattern = Pattern.compile(ARGS_REGEX);
        for (var arg : args) {
            Matcher matcher = pattern.matcher(arg);
            if (matcher.matches()) {
                String name = matcher.group(1);
                String value = matcher.group(2);
                processArg(name, value);
            } else {
                throw new IllegalArgumentException("Malformed argument: " + arg);
            }
        }
        execute();
    }

    private static void showHelp() {

        System.out.println("\nUsage:");
        System.out.println(APP_NAME + " [--option=<value>, ...]");

        System.out.println("\nOptions:");
        System.out.println("--" + ARG_PATH + " (Required) - Path to a .nc NetCDF format file.");
        System.out.println("--" + ARG_FUNCTION + " (Optional) - Function to perform. Depending on the function, " +
                "additional options must be provided.");

        System.out.println("\nFunctions:");

        System.out.println("\n" + AppFunction.READ_VARIABLE.getFunctionArgValue());
        System.out.println("\tDescription: Simple reader that prints the variable data.");
        System.out.println("\t" + AppFunction.READ_VARIABLE.getFunctionArgValue() + " options:");
        System.out.println("\t--" + FnReadVariableArg.VARIABLE.getArg() +
                " (Required) - Name of the variable to read.");
        System.out.println("\t--" + FnReadVariableArg.SECTION_SPEC.getArg() + " (Required) - Fortran 90-style array " +
                "definition of the range of data to read from the variable. e.g. :,1:2,3:3");

        System.out.println("\n" + AppFunction.AVG_VARIABLE.getFunctionArgValue());
        System.out.println("\tDescription: Calculates the average of the variable data.");
        System.out.println("\t" + AppFunction.AVG_VARIABLE.getFunctionArgValue() + " options:");
        System.out.println("\t--" + FnAvgVariableArg.VARIABLE.getArg() +
                " (Required) - Name of the variable to average.");
        System.out.println("\t--" + FnAvgVariableArg.SECTION_SPEC.getArg() + " (Required) - Fortran 90-style array " +
                "definition of the range of data to average from the variable. e.g. :,1:2,3:3");
    }

    private static void processArg(String name, String value) {
        if (MANDATORY_ARGS.contains(name) || OPTIONAL_ARGS.contains(name)) {
            rootParams.put(name, value);
            return;
        } else if (ARG_FUNCTION.equals(name)) {
            function = AppFunction.fromFunctionArgValue(value);
            return;
        } else if (function != null) {
            Set<String> functionArgs = function.getFunctionArgs();
            if (functionArgs.contains(name)) {
                functionParams.put(name, value);
                return;
            }
        }
        if (function == null) {
            throw new IllegalStateException("Unrecognised argument: '" + name +
                    "'. Did you forget to specify a function?");
        } else {
            throw new IllegalArgumentException("Unrecognised argument: '" + name +
                    "' for function '" + function.getFunctionArgValue() + "'");
        }
    }

    private static void execute() {
        for (String argName : MANDATORY_ARGS) {
            if (!rootParams.containsKey(argName)) {
                throw new IllegalStateException("Missing argument: " + argName);
            }
        }
        if (function != null) {
            Set<String> mandatoryFnArgs = function.getFunctionArgs();
            for (String fnArgName : mandatoryFnArgs) {
                if (!functionParams.containsKey(fnArgName)) {
                    throw new IllegalStateException("Missing mandatory function argument: " + fnArgName);
                }
            }
        }

        var firstFileReader = new NcFnProcessor();
        firstFileReader.openFileAndProcessFunction(rootParams.get(ARG_PATH), function, functionParams);
    }

}
