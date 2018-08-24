package fr.utarwyn.endercontainers.commands.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parameter {

	public final static Parameter INT = new Parameter(new ParameterIntChecker());

	public final static Parameter FLOAT = new Parameter(new ParameterFloatChecker());

	public final static Parameter DOUBLE = new Parameter(new ParameterDoubleChecker());

	public final static Parameter STRING = new Parameter(null);

	private ParameterChecker checker;

	private List<Object> tabCompletions;

	private boolean optional;

	private Parameter(ParameterChecker checker) {
		this(checker, new ArrayList<>());
	}

	private Parameter(ParameterChecker checker, List<Object> tabCompletions) {
		this.checker = checker;
		this.tabCompletions = tabCompletions;
	}

	public List<String> getTabCompletions() {
		List<String> completions = new ArrayList<>();

		if (this.tabCompletions == null) return null;

		for (Object obj : this.tabCompletions)
			if (obj != null) {
				completions.add(obj.toString());
			}

		return completions;
	}

	public boolean isOptional() {
		return this.optional;
	}

	public Parameter withTabCompletions(Object... tabCompletions) {
		return new Parameter(this.checker, Arrays.asList(tabCompletions));
	}

	public Parameter optional() {
		Parameter parameter = new Parameter(this.checker, this.tabCompletions);
		parameter.optional = true;
		return parameter;
	}

	public Parameter withPlayersCompletion() {
		return new Parameter(this.checker, null);
	}

	public boolean check(String value) {
		return this.checker == null || this.checker.checkParam(value);
	}

	public boolean equalsTo(Parameter parameter) {
		return this.checker == parameter.checker;
	}

	public static Parameter withCustomChecker(ParameterChecker checker) {
		if (checker == null)
			throw new NullPointerException("The parameter checker cannot be null!");

		return new Parameter(checker);
	}

}