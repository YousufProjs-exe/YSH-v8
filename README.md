# YSH — Yousuf Shell v8

> A Java-based graphical shell built from scratch with real filesystem operations, LAN networking, customization, and developer-focused features.

**YSH v8.GUI** is a major version of YSH, combining a terminal-style graphical interface with real filesystem interaction, LAN communication, file sharing, command history, themes, developer mode, and various built-in utilities.

![YSH v8 GUI](https://github.com/YousufProjs-exe/YSH-v8/blob/main/YSH-SS/YSH%20v8.png)

## Features

* Real filesystem navigation and file operations
* Graphical terminal interface
* LAN chat and group communication
* LAN file sharing
* User management and announcements
* Command history and command completion
* Multiple terminal themes
* Developer mode
* Built-in calculator and notes
* System information
* Search, tree, head and tail utilities
* Konami code and other Easter eggs
* Time-sensitive greetings and shell customization

## Built With

* **Java 17**
* **Java Swing**
* Java NIO / Filesystem APIs
* TCP networking with `Socket` and `ServerSocket`

## Screenshots

### Main Interface

![YSH v8 main interface](screenshots/ysh-v8-main.png)

### Filesystem

![YSH v8 filesystem commands](screenshots/ysh-v8-filesystem.png)

### LAN Networking

![YSH v8 LAN networking](screenshots/ysh-v8-network.png)

### Themes

![YSH v8 themes](screenshots/ysh-v8-themes.png)

## Getting Started

### Requirements

* Java 17 or later
* Windows, Linux, or another Java-supported platform

### Run

```bash
java -jar YSH.jar
```

Alternatively, compile the source with Java 17 and launch the main class.

## Networking

YSH v8 includes LAN networking features for communication between YSH instances on the same network.

Supported functionality includes:

* Hosting a chat server
* Connecting to another YSH host
* Sending messages
* Listing connected users
* Kicking users
* Sending announcements
* LAN device scanning
* LAN file sharing

## Project History

YSH began as a command-line shell project and gradually evolved into a graphical shell with filesystem access, networking, utilities, and its own interface.

**v8** represents one of the major steps in that evolution before the modular architecture introduced in later versions.

## License

This project is open source and available under the **MIT License**.

## Author

**Khaja Yousuf Uddin**

- GitHub: [@YousufProjs-exe](https://github.com/YousufProjs-exe)
- YSH Website: [yshweb.netlify.app](https://yshweb.netlify.app/)
- Download YSH: [YSH-v8](https://github.com/YousufProjs-exe/YSH-v8/releases/)
