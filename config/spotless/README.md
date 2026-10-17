# Java source hygiene

Spotless intentionally does not run a Java code-layout formatter. Indentation, wrapping, braces,
method chains, parameters, and other style choices remain under developer control.

Spotless only:

- targets maintained Java sources under `src/main/java` and `src/test/java`;
- groups and sorts imports using `config/spotless/java.importorder`;
- removes unused imports;
- removes trailing whitespace;
- ensures that every file ends with a newline.

Run the formatter with:

```powershell
.\gradlew.bat spotlessApply
```

Validate without modifying files with:

```powershell
.\gradlew.bat spotlessCheck
```

IDE code-style settings are intentionally not checked into this directory. Each developer can use
their preferred layout while Spotless enforces only the shared source-hygiene rules above.
