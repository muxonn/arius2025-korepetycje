# How to Set Up and Run the Application

This guide explains how to set up and run the Flask application using Docker.

## Prerequisites

Ensure you have the following installed:
- Docker  
- Docker Compose  

## Steps to Run the Application  

### 1. Clone the Repository  
Clone the repository to your local machine:  
```bash
git clone <repository-url>
cd <repository-directory>/backend
```

### 2. Start the Application with Docker Compose
Run the following command from the `/backend` folder:
```bash
docker compose down
docker compose build
docker compose up -d
```

This will start both the Flask applications and the PostgreSQL database in separate containers.

### 3. Access the application

- Main application: http://127.0.0.1:5000
- Email microservice:  http://127.0.0.1:5001
- API Documentation (Swagger): http://127.0.0.1:5000/apidocs


### 4. Shutting down
```bash
docker compose down
```