package fr.utarwyn.endercontainers.command.parameter;

class ParameterIntChecker implements ParameterChecker {

	@Override
	public boolean checkParam(String stringParam) {
		return ParameterChecker.isInteger(stringParam);
	}

}
