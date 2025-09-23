# E-commerce Backend

This document provides a detailed overview of the E-commerce backend service, including its architecture, implementation details, and instructions for setting it up and running it on different operating systems.

## Table of Contents

- [Core Technologies](#core-technologies)
- [Implementation Details](#implementation-details)
    - [Modular Architecture](#modular-architecture)
    - [Security](#security)
    - [Database Management](#database-management)
    - [Asynchronous Processing](#asynchronous-processing)
    - [Configuration](#configuration)
    - [Caching](#caching)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Running the Application Locally](#running-the-application-locally)
        - [On Linux/macOS](#on-linuxmacos)
        - [On Windows](#on-windows)
- [Running with Docker](#running-with-docker)
- [API Documentation](#api-documentation)

## Core Technologies

- **Java 17**: The primary programming language.
- **Spring Boot**: The core framework for building the application.
- **Spring Security**: For authentication and authorization.
- **JWT**: Used for stateless, token-based authentication.
- **Spring Data JPA / Hibernate**: For data persistence and ORM.
- **PostgreSQL**: The primary relational database.
- **Flyway**: For database schema migration management.
- **Redis**: For caching frequently accessed data.
- **RabbitMQ**: For asynchronous communication via a message broker.
- **Maven**: The build automation and dependency management tool.

## Implementation Details

### Modular Architecture

The application is organized into a modular structure located under `src/main/java/com/ecommerce/backend/modules`. Each module represents a core feature of the e-commerce platform, such as:
- `auth`: Handles user authentication and authorization.
- `user`: Manages user profiles and data.
- `product`: Manages product information.
- `category`: Manages product categories.
- `cart`: Manages user shopping carts.
- `order`: Manages customer orders.
- `inventory`: Manages product stock levels.
- `reviews` : Manages product reviews

This modular design promotes separation of concerns and makes the codebase easier to maintain and scale.

### Security

Security is managed by Spring Security and configured in `config/SecurityConfig.java`. Authentication is stateless and uses JWT. The `modules/auth/jwt` package contains all the necessary components for creating, parsing, and validating tokens.

### Database Management

- **Persistence**: I`m use Spring Data JPA with Hibernate as the ORM provider. Each module contains its own entities and repositories.
- **Migrations**: Database schema changes are managed by **Flyway**. The SQL migration scripts are located in `src/main/resources/db/migration`. When the application starts, Flyway automatically checks for new scripts and applies them to the database, ensuring the schema is always up to date.

### Asynchronous Processing

To handle long-running or non-critical tasks without blocking the main application thread, we use RabbitMQ. The `shared/outbox` package implements the **Transactional Outbox pattern**, which ensures that events are published reliably. When an important domain event occurs (e.g., an order is created), it is first saved as an event in the database within the same transaction. A separate process then reads these events and publishes them to RabbitMQ, ensuring no events are lost even if the message broker is temporarily unavailable.

### Configuration

The application uses YAML for configuration files, located in `src/main/resources`:
- `application.yml`: Contains the base configuration for all environments.
- `application-docker.yml`: Contains overrides specifically for the Docker environment.

### Caching

To improve performance, the application uses Redis for caching. The cache is configured in `config/CacheConfig.java`.

## Project Structure

A brief overview of the key directories:
```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/backend/
│   │   │   ├── config/         # Global configurations (Security, Cache, etc.)
│   │   │   ├── modules/        # Core feature modules (product, order, etc.)
│   │   │   └── shared/         # Shared code (exceptions, events, DTOs)
│   │   └── resources/
│   │       ├── db/migration/   # Flyway database migration scripts
│   │       ├── application.yml # Main application configuration
│   │       └── application-docker.yml # Docker-specific configuration
│   └── test/                   # Unit and integration tests
├── pom.xml                     # Maven project configuration
└── mvnw                        # Maven wrapper scripts
```

## Getting Started

### Prerequisites

- **Java 17 JDK**
- **Maven 3.x**
- Running instances of **PostgreSQL**, **Redis**, and **RabbitMQ**.

Before running, you must configure the connection details for these services in the `src/main/resources/application.yml` file.

## Running with Docker

The most straightforward way to run the backend is as part of the full application stack using Docker Compose. Please see the `README.md` file in the project root for instructions.

To build the backend image individually, run the following command from the `backend` directory:
```bash
docker build -t ecommerce-backend:latest .
```
