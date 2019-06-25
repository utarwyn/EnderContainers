package fr.utarwyn.endercontainers.command.parameter;

class ParameterDoubleChecker implements ParameterChecker {

    @Override
    public boolean checkParam(String stringParam) {
        return ParameterChecker.isDouble(stringParam);
    }

}
