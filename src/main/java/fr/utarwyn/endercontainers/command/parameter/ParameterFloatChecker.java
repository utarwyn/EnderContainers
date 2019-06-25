package fr.utarwyn.endercontainers.command.parameter;

public class ParameterFloatChecker implements ParameterChecker {

    @Override
    public boolean checkParam(String stringParam) {
        return ParameterChecker.isFloat(stringParam);
    }

}
