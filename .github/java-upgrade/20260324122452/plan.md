# Upgrade Plan: honeyrun (20260324122452)

- **Generated**: 2026-03-24 12:24:52
- **HEAD Branch**: main
- **HEAD Commit ID**: 9f0045f

## Available Tools

**JDKs**
- JDK 17.0.16: /Users/baptistecaillerie/.jdk/jdk-17.0.16/jdk-17.0.16+8/Contents/Home/bin (installed in Step 1, used by Step 2)
- JDK 21.0.6: /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin (required by Steps 3-4)

**Build Tools**
- Maven 3.9.14: /Users/baptistecaillerie/.maven/maven-3.9.14/bin/mvn (installed in Step 1, used by Steps 2-4)
- Maven Wrapper: Not present

## Guidelines

- Upgrade Java runtime to LTS Java 21.

## Options

- Working branch: appmod/java-upgrade-20260324122452
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java runtime from 17 to 21 (LTS)

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 17 | 21 | User requested LTS Java 21 runtime upgrade |
| Maven (CLI) | Not installed | 3.9.0+ | Maven 3.9+ is required/recommended for stable Java 21 builds |
| Maven Wrapper | Not present | N/A | Project has no wrapper; a compatible Maven CLI must be installed |
| maven-compiler-plugin | Implicit (not pinned) | 3.11.0+ | Plugin should be pinned for reliable Java 21 source/target handling |
| maven-surefire-plugin | Implicit (not pinned) | 3.1.0+ | Modern Surefire is recommended for Java 17+ and test-runtime stability |
| org.mariadb.jdbc:mariadb-java-client | 3.4.1 | 3.4.1 | - |

### Derived Upgrades

- Upgrade project Java source/target from 17 to 21 in pom.xml (explicit user goal).
- Install Maven 3.9.11 because no Maven is currently installed and Java 21 verification requires a compatible build tool.
- Add and pin maven-compiler-plugin to 3.11.0+ so Java 21 compilation behavior is explicit and reproducible.
- Add and pin maven-surefire-plugin to 3.1.0+ to reduce test execution incompatibilities on Java 21.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Required JDK/build tools are missing for baseline and Java 21 validation.
  - **Changes to Make**:
    - [ ] Install JDK 17.
    - [ ] Install Maven 3.9.11.
    - [ ] Verify JDK/Maven binaries are available.
  - **Verification**:
    - Command: list_jdks and mvn -v
    - Expected: JDK 17 and JDK 21 available, Maven 3.9.11 available.

- **Step 2: Setup Baseline**
  - **Rationale**: Capture compile/test baseline before modifying Java target.
  - **Changes to Make**:
    - [ ] Run baseline compile with Java 17.
    - [ ] Run baseline tests with Java 17.
    - [ ] Record pass/fail counts for comparison.
  - **Verification**:
    - Command: mvn -q clean test-compile && mvn -q clean test
    - JDK: installed JDK 17 path
    - Expected: Baseline compile and tests documented.

- **Step 3: Upgrade Build Configuration to Java 21**
  - **Rationale**: Apply minimal, explicit Maven changes required for Java 21 runtime/compile target.
  - **Changes to Make**:
    - [ ] Update pom.xml properties maven.compiler.source/target from 17 to 21.
    - [ ] Add maven-compiler-plugin 3.11.0+ configuration.
    - [ ] Add maven-surefire-plugin 3.2.x configuration.
    - [ ] Fix any compilation issues caused by Java target change.
  - **Verification**:
    - Command: mvn -q clean test-compile
    - JDK: /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin
    - Expected: Compilation SUCCESS for main and test code.

- **Step 4: Final Validation**
  - **Rationale**: Ensure upgrade goals are fully met with clean rebuild and full tests.
  - **Changes to Make**:
    - [ ] Verify Java 21 is set in pom.xml.
    - [ ] Run clean compile and test on Java 21.
    - [ ] Fix all remaining test failures until 100% pass.
  - **Verification**:
    - Command: mvn -q clean test
    - JDK: /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home/bin
    - Expected: Compilation SUCCESS + 100% tests pass.

## Key Challenges

- **Missing build tooling on machine**
  - **Challenge**: Maven is not currently installed, blocking any build verification.
  - **Strategy**: Install Maven 3.9.11 in Step 1 and use its full path in all build commands.

- **Potential test fragility after Java target bump**
  - **Challenge**: Tests may rely on Java 17 behavior or implicit tool defaults.
  - **Strategy**: Pin compiler/surefire plugin versions and perform iterative test-fix loop in Step 4.

## Plan Review

- Coverage check: Plan includes mandatory Setup Environment, Setup Baseline, upgrade step(s), and Final Validation.
- Feasibility check: Required tools are available or installable with built-in tooling.
- Limitation check: No known unfixable limitations identified at planning stage.
