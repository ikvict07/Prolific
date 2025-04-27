![Icon](static/icon.png)

## Overview

Prolific is a modern, cross-platform project management application built with JavaFX and Spring Boot. It helps developers organize and manage their projects by automatically detecting project types, providing a clean user interface for project navigation, and offering tools to streamline development workflows.

## Features

- **Project Detection**: Automatically identifies different project types based on file patterns
- **Project Management**: Add, remove, and organize your development projects
- **Star Projects**: Mark your favorite or frequently used projects for quick access
- **Modern UI**: Clean, responsive interface with customizable themes
- **Cross-Platform**: Works on Windows, macOS, and Linux

## Technologies

- **Java 23**: Built with the latest Java features
- **JavaFX 23.0.1**: For the modern, responsive UI
- **Spring Boot**: For dependency injection and application framework

## Getting Started

### Prerequisites

- Java 23 or higher
- Gradle (optional, wrapper included)

### Installation

#### From Source

1. Clone the repository:
   ```
   git clone https://github.com/nevertouchgrass/prolific.git
   cd prolific
   ```

2. Build the application:
   ```
   ./gradlew build
   ```

3. Run the application:
   - On Linux: `./gradlew runLinux`
   - On Windows: `./gradlew runWindows`
   - On Mac: `./gradlew runMac`

#### Using Installer

Not yet implemented :(

## Usage

### Adding Projects

1. Click the settings button in the top-right corner
2. Select "Add Project"
3. Browse to your project directory
4. Prolific will automatically detect the project type and add it to your list

### Managing Projects

- **Star a Project**: Click the star icon on a project to mark it as a favorite
- **Remove a Project**: Right-click on a project and select "Remove"
- **Project Settings**: Right-click on a project to access additional settings

## Building from Source

### Creating a Native Installer

```
./gradlew jpackage
```

This will create a native installer for your platform in the `build/jpackage` directory.
