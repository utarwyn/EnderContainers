package fr.utarwyn.endercontainers.commands.parameter;

class ParameterIntChecker implements ParameterChecker {

	@Override
	public boolean checkParam(String stringParam) {
		return ParameterChecker.isInteger(stringParam);
	}

}