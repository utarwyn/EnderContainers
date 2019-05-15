package fr.utarwyn.endercontainers.command.parameter;

public interface ParameterChecker {

	boolean checkParam(String stringParam);

	static boolean isInteger(String string) {
		try {
			Integer.parseInt(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	static boolean isFloat(String string) {
		try {
			Float.parseFloat(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
