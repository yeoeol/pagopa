# Java formatting

The Gradle build is the source of truth for formatting and import order.

- Indentation: 4 spaces
- Continuation indentation: 8 spaces
- Maximum line length: 120
- Method chains: keep the first call on the current line and always put each following call on its own line
- Intentional manual line breaks: preserved by both Eclipse JDT and the IntelliJ scheme
- Wrapped method parameters: one parameter per line, including the first parameter, with the closing parenthesis on its own line
- New wildcard imports: avoided by the IntelliJ scheme; existing wildcard imports are left unchanged
- Static imports: placed in the final import group
- Generated QueryDSL sources: excluded from formatting

Run the formatter with:

```powershell
.\gradlew.bat spotlessApply
```

Validate without modifying files with:

```powershell
.\gradlew.bat spotlessCheck
```

## IntelliJ IDEA

Import `config/spotless/intellij-java-code-style.xml` from **Settings > Editor > Code Style > Java**,
then copy the imported scheme to the project. Enable **Optimize imports** under
**Settings > Tools > Actions on Save** if imports should be reorganized whenever a file is saved.

The checked import order separates JDK, Jakarta, Spring, other external libraries, project code,
and static imports with blank lines. `spotlessApply` enforces the same order in local builds and CI.
