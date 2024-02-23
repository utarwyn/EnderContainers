# Contributing

Thank you for your interest in contributing to EnderContainers! We appreciate your help,
however in order to keep the source code clean and simple for everyone, we kindly ask you
to read the following rules before contributing.

* **Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).**\
  Try to respect these conventions as much as possible to keep a clean and readable code.
* **Target Java 8 for source and compilation.**
* **Write complete Javadocs.**\
  Make sure that your `@param` and `@return` fields are not just blank.
* **Wrap code to a 100 column limit.**\
  As this is recommended by the Google Java Style Guide,
  it helps us to keep the code more readable.
* **Use only spaces for indentation.**\
  Our indents are 4-spaces long, and tabs are unacceptable.
* **Make sure the code is efficient by testing it.**\
  Take some time to test your code and make sure it is free of
  bugs and memory leaks. Do not duplicate code.
* **Keep commit summaries under 70 characters.**\
  For more details, place your text on new lines after the summary.
* **Write unit tests.**\
  If possible, write unit tests for complex algorithms.


Checklist
---------

Ready to submit? Perform the checklist below:

1. Am I using 4-space wide to indent my source code?
2. Have I written proper Javadocs? Are the @param and @return fields actually filled out?
3. Have I combined my commits into a reasonably small number (if not one)
   commit using `git rebase`?
4. Have I made my pull request too large? Pull requests should introduce
   small sets of changes at a time. Major changes should be discussed with
   the team prior to starting work.
5. Are my commit messages descriptive?

Avoid creating too many commits when creating a PR.
Maybe the usage of [`git rebase`](http://learn.github.com/p/rebasing.html) could be a nice idea.

Requirements
------------

- Java 11+
- Gradle 8+
