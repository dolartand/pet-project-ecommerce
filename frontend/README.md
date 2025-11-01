# E-commerce Frontend

![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react) ![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue?style=for-the-badge&logo=typescript) ![SCSS](https://img.shields.io/badge/Styling-SCSS-pink?style=for-the-badge&logo=sass) ![Axios](https://img.shields.io/badge/API-Axios-purple?style=for-the-badge&logo=axios)

This document provides a detailed overview of the E-commerce frontend application, built with React. It covers the project's architecture, key implementation details, and instructions for setup and local development.

## Table of Contents

- [Core Technologies](#core-technologies)
- [Implementation Details](#implementation-details)
    - [Component-Based Architecture](#component-based-architecture)
    - [State Management](#state-management)
    - [Authentication & Security](#authentication--security)
    - [API Communication](#api-communication)
    - [Styling](#styling)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Environment Variables](#environment-variables)
    - [Installation & Running](#installation--running)
- [Available Scripts](#available-scripts)

## Core Technologies

- **React 18**: The core library for building the user interface.
- **TypeScript**: For static typing and improved code quality.
- **Axios**: For making HTTP requests to the backend API.
- **SCSS**: For advanced styling with variables, nesting, and mixins.
- **[npm]**: Package manager.

## Implementation Details

### Component-Based Architecture

A project follows a clear component-based architecture to promote reusability and separation of concerns:

- **pages/**:  Contains top-level components that represent entire pages of the application (MainPage, ShoppingBagPage, OrdersPage, etc.).
- **components/layout/**: Contains larger, structural components that form the main layout of the application (Header, Sidebar, and various modals).
 
Client-side navigation is managed within the main App.tsx component through conditional rendering based on the application's state (isCartOpen, selectedProductId, etc.).
This approach avoids a full page reload for a smoother single-page application (SPA) experience.

### State Management

State management is handled primarily using standard React Hooks (useState, useEffect, useCallback).
For global state, such as user authentication status, the application utilizes React Context. This is abstracted away into a custom hook, useAuth(), which provides components with easy access to the authentication state (isLoggedIn) and related functions without prop drilling.

### Authentication & Security

Authentication is stateless and based on JWT (JSON Web Tokens). The flow is as follows:
1. A user logs in via the AuthMode component.
2. Upon successful authentication, the backend returns a JWT.
3. The token is stored securely in the browser's localStorage.
4. A pre-configured Axios interceptor automatically attaches the **Authorization: Bearer <token>** header to every subsequent protected API request.
5. The **useAuth()** hook checks for the presence of the token to determine the *isLoggedIn* status, which is used to conditionally render UI elements and protect application sections.

### API Communication

All communication with the backend is handled by **Axios**. The base configuration is centralized in **src/api/axios.js**. To handle both public and protected endpoints correctly, two separate Axios instances are configured:

- api: The primary instance with an interceptor that automatically includes the JWT for authenticated requests.
- apiPublic: A "clean" instance without any interceptors, used for public requests like login, registration, and password reset, ensuring no invalid or expired tokens are accidentally sent.

### Styling

The project is styled using **SCSS** for its features like variables, nesting and mixins. The styling is organised as follows:

- A common.scss file contains shared mixins (@mixin) and variables for consistent design across the admin pages.
- Each  component or page has its own dedicated .scss/.css file for component-specific styles.
- A single modular CSS file (.module.css) is used where locally scoped class names are required to avoid style collisions.

## Project Structure

```
frontend/
├── public/
├── src/
│   ├── api/          # Axios configuration
│   ├── components/   # Reusable UI components (layout, ui)
│   ├── context/      # React Context providers (e.g., AuthContext)
│   ├── hooks/        # Custom React hooks (e.g., useAuth)
│   ├── pages/        # Top-level page components
│   ├── styles/       # Global styles, variables, and mixins
│   ├── test/         # Unit and e2e tests
│   ├── utils/        # Helper functions (e.g., formatters)
│   └── App.tsx       # Main application component
├── .env              # Example template for environment variables
└── package.json
```

## Getting Started

### Prerequisites

- **Node.js**: `v18.x` or higher.
- **[npm]**: `v9.x` or higher.

### Environment Variables

Before running the application, you need to create a `.env` file in the project root.
Copy the contents of `.env` and fill in the required values.

Then, open the newly created .env file and set the required variables. The most important variable is the base URL for the backend API.
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

### Installation & Running

1.  Clone the repository.
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Run the development server:
    ```bash
    npm start
    ```
The application will be available at `http://localhost:3000`.

## Available Scripts

- `npm start`: Runs the app in development mode.
- `npm run build`: Builds the app for production.
- `npm test`: Launches the test runner.