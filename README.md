# How to Set Up and Run the Application

This guide will help you set up and run the Flask application. Follow the steps below to ensure everything works correctly.

## Prerequisites

Make sure you have the following installed on your machine:

1. **Python 3.10+**
2. **Docker and Docker Compose**

## Steps to Run the Application

### 1. Clone the Repository
First, clone the repository to your local machine:
```bash
git clone <repository-url>
cd <repository-directory>
```

### 2. Set Up a Virtual Environment
Create and activate a virtual environment for Python dependencies:

#### On Linux/MacOS:
```bash
python3 -m venv venv
source venv/bin/activate
```

#### On Windows:
```bash
python -m venv venv
venv\Scripts\activate
```

### 3. Install Required Python Packages
Install the dependencies listed in the `requirements.txt` file:
```bash
pip install -r requirements.txt
```

### 4. Start the Database with Docker Compose
Start the PostgreSQL database using Docker Compose:
```bash
docker-compose up -d
```

This will create and start the database container defined in the `docker-compose.yml` file.

### 5. Initialize the Database
Ensure the database is initialized and ready to use. The application automatically initializes the database tables on startup, but you can manually verify this step if needed.

### 6. Run the Flask Application
Start the Flask development server with the following command:
```bash
flask --app server run --host=0.0.0.0
```

This will start the server and make it accessible on your local network.

### 7. Access the Application
Open your browser and navigate to:
```
http://127.0.0.1:5000
```

### 8. Access the Swagger documentation
Open your browser and navigate to:
```
http://127.0.0.1:5000/apidocs
```

If you are running the application on a server, replace `127.0.0.1` with your server’s IP address.

## Additional Notes
- If you encounter any errors related to the database, ensure Docker is running and the database container is healthy.
- For debugging, use the `--debug` flag when running the Flask server.

## Shutting Down
To stop the application and database:

1. Stop the Flask server using `CTRL+C`.
2. Stop and remove Docker containers:
   ```bash
   docker-compose down
   ```

