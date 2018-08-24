package fr.utarwyn.endercontainers.commands.parameter;

public class ParameterFloatChecker implements ParameterChecker {

	@Override
	public boolean checkParam(String stringParam) {
		return ParameterChecker.isFloat(stringParam);
	}

}