# E-commerce Platform

<div align="center">
  <img src="frontend/public/assets/logo.png" alt="Логотип приложения"/>
</div>

[![Backend](https://img.shields.io/badge/Backend-Spring_Boot-green?style=for-the-badge&logo=spring-boot)](./backend) [![Frontend](https://img.shields.io/badge/Frontend-React-blue?style=for-the-badge&logo=react)](./frontend) [![Docker](https://img.shields.io/badge/Docker-ready-blue?style=for-the-badge&logo=docker)](./docker-compose.yml) ![License](https://img.shields.io/badge/MIT-license-blue.svg?style=for-the-badge)

This is a full-stack e-commerce platform consisting of a React-based frontend and a Spring Boot backend. The entire application is containerized using Docker and can be orchestrated with Docker Compose.

## Documentation

- **[Backend Documentation](./backend/README.md)** - Detailed information about the Spring Boot backend
- **[Frontend Documentation](./frontend/README.md)** - React frontend documentation 
- **[API Documentation](./docs/api-specs.yml)** - API endpoints and usage


## Architecture Overview

The application is composed of several services that work together:

```
+-----------------+      +-----------------+      +----------------+
|   Frontend      |----->|   Backend       |----->|   PostgreSQL   |
| (React)         |      | (Spring Boot)   |      |   (Database)   |
+-----------------+      +-----------------+      +----------------+
                             |
                             |      +----------------+
                             +----->|     Redis      |
                             |      |    (Cache)     |
                             |      +----------------+
                             |
                             |      +----------------+
                             +----->|   RabbitMQ     |
                                    | (Message Broker)|
                                    +----------------+
```

## Services

The `docker-compose.yml` file defines the following services:

-   **`frontend`**: The user-facing React application.
-   **`backend`**: The Spring Boot application providing the core API.
-   **`postgres`**: The PostgreSQL database for data persistence.
-   **`redis`**: A Redis instance for caching.
-   **`rabbitmq`**: A RabbitMQ message broker for asynchronous communication.

## Getting Started

### Prerequisites

-   **Docker**: [Install Docker](https://docs.docker.com/get-docker/)
-   **Docker Compose**: [Install Docker Compose](https://docs.docker.com/compose/install/) (usually included with Docker Desktop)
-   **Git**: [Install Git](https://git-scm.com/downloads)

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd <repository-directory>
    ```

2.  **Environment Variables:**
    Create a `.env` file in the root directory by copying the example file:
    ```bash
    cp .env.example .env
    ```
    Review the `.env` file and customize the variables if needed. At a minimum, you might want to set passwords. The application will use the default values from `docker-compose.yml` if a `.env` file is not present.

3.  **Start the application:**
    Use Docker Compose to build the images and start all the services in the background.
    ```bash
    docker-compose up --build -d
    ```

4.  **Accessing the Services:**
    -   **Frontend Application**: [http://localhost:3000](http://localhost:3000)
    -   **Backend API**: [http://localhost:8080](http://localhost:8080)
    -   **RabbitMQ Management UI**: [http://localhost:15672](http://localhost:15672) (user: `admin`, pass: `password` or as set in your `.env`)

5.  **Stopping the Application:**
    To stop all running services, use the following command:
    ```bash
    docker-compose down
    ```
    To stop and remove the volumes (deleting all data), use:
    ```bash
    docker-compose down -v
    ```

## Project Structure

-   `backend/`: Contains the source code for the Spring Boot backend.
-   `frontend/`: Contains the source code for the React frontend.
-   `docs/`: Contains additional documentation.
-   `docker-compose.yml`: Defines all the services for the application.

## Authors

Artur (Backend)
- GitHub:@dolartand
- Email: dolartand@gmail.com

Dasha (Frontend)
- GitHub:@pimadasha
- Email: pimadasha2006@gmail.com

