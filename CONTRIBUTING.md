# Contributing to Loadscreens

Thank you for your interest in contributing to the Loadscreens plugin! This document provides guidelines and instructions for contributors.

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git
- A Java IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

### Setting up the Development Environment

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/Loadscreens.git
   cd Loadscreens
   ```
3. Build the project:
   ```bash
   mvn clean compile
   ```

## 📝 Code Standards

### Java Code Guidelines

- **Use Java 17+ features** where appropriate
- **Follow Oracle's Java Code Conventions**
- **Use meaningful variable and method names**
- **Keep methods focused and concise** (generally under 50 lines)
- **Use proper exception handling** with try-catch blocks
- **Validate all inputs** to prevent crashes from invalid data

### Documentation Requirements

- **All public classes must have JavaDoc** with class description and `@author` tag
- **All public methods must have JavaDoc** with parameter and return descriptions
- **Use `@param`, `@return`, `@throws` tags** appropriately
- **Include usage examples** in JavaDoc for complex methods

### Example JavaDoc:
```java
/**
 * Shows a loadscreen to the specified player.
 * 
 * <p>This method validates the input parameters and creates a new
 * loadscreen session if all conditions are met.</p>
 * 
 * @param player the player to show the loadscreen to
 * @param type the loadscreen type as defined in configuration
 * @param delay the delay in ticks before showing the loadscreen
 * @throws IllegalArgumentException if player is null or type is invalid
 * @since 1.0
 */
public void showLoadscreen(Player player, String type, int delay) {
    // Implementation
}
```

### Error Handling Standards

- **Always validate user inputs** (null checks, empty strings, negative numbers)
- **Use try-catch blocks** for operations that may fail
- **Log errors appropriately** using the plugin logger
- **Provide meaningful error messages** to users
- **Never suppress exceptions** without good reason

Example:
```java
public void processInput(String input) {
    if (input == null || input.trim().isEmpty()) {
        throw new IllegalArgumentException("Input cannot be null or empty");
    }
    
    try {
        // Processing logic
    } catch (Exception e) {
        logger.severe("Failed to process input: " + e.getMessage());
        throw new RuntimeException("Processing failed", e);
    }
}
```

## 🧪 Testing

### Writing Tests

- **Write unit tests** for new functionality
- **Use JUnit 5** and **Mockito** for testing
- **Test both success and failure scenarios**
- **Achieve reasonable test coverage** (aim for >80%)

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PlaceholderManagerTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Structure

Place tests in `src/test/java` mirroring the main package structure:
```
src/test/java/org/anonventions/loadscreens/
├── test/
│   ├── PlaceholderManagerTest.java
│   └── LoadscreenManagerTest.java
```

## 🔧 Building and Packaging

### Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package plugin JAR
mvn package

# Generate JavaDoc
mvn javadoc:javadoc

# Check code style
mvn checkstyle:check
```

## 📋 Pull Request Process

### Before Submitting

1. **Ensure all tests pass**: `mvn test`
2. **Check code style**: `mvn checkstyle:check`
3. **Update documentation** if you've changed APIs
4. **Add tests** for new functionality
5. **Update the changelog** in your PR description

### PR Guidelines

- **Create a feature branch** from `main` or `develop`
- **Use descriptive commit messages**
- **Keep PRs focused** on a single feature or bug fix
- **Include tests** for new functionality
- **Update documentation** as needed

### Commit Message Format

```
type(scope): short description

Longer description if needed, explaining what and why.

Fixes #123
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Examples:
- `feat(loadscreen): add typewriter effect support`
- `fix(placeholder): resolve null pointer in cache cleanup`
- `docs(readme): update installation instructions`

### PR Template

When submitting a PR, include:

- **Description** of changes made
- **Motivation** for the changes
- **Testing** performed
- **Breaking changes** (if any)
- **Screenshots** (for UI changes)

## 🐛 Reporting Issues

### Bug Reports

Include:
- **Minecraft version**
- **Plugin version**
- **Server software** (Paper/Spigot)
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Error logs** (if any)
- **Configuration** (if relevant)

### Feature Requests

Include:
- **Use case** description
- **Proposed solution**
- **Alternative solutions** considered
- **Implementation ideas** (if you have any)

## 🔄 Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR.MINOR.PATCH**
- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

### Release Checklist

- [ ] All tests pass
- [ ] Documentation updated
- [ ] Version bumped in `pom.xml`
- [ ] Changelog updated
- [ ] Tagged release created
- [ ] GitHub release published

## 🤝 Code of Conduct

### Our Standards

- **Be respectful** to all contributors
- **Use welcoming and inclusive language**
- **Accept constructive criticism** gracefully
- **Focus on what's best** for the community
- **Show empathy** towards other contributors

### Unacceptable Behavior

- Harassment or discriminatory language
- Personal attacks or trolling
- Publishing private information
- Other unprofessional conduct

## 📞 Getting Help

If you need help:

1. **Check the documentation** first
2. **Search existing issues** on GitHub
3. **Ask in our Discord** server
4. **Open a new issue** with the `question` label

## 🙏 Recognition

Contributors will be:
- **Listed in the README** credits section
- **Mentioned in release notes** for significant contributions
- **Given appropriate recognition** in commit messages and PRs

Thank you for contributing to Loadscreens! 🎉