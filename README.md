# Stock Management System

This application is a demonstration of a **Stock Management System** built using **Spring Boot** with the following features:
- Secure database connection using MySQL.
- Redis cache integration for optimized performance.
- Multithreading for high throughput and low latency.
- Logging for performance metrics and cache hit/miss rates.
- Benchmarking for sequential vs. parallel execution.

---

## **Setup Instructions**

### **Prerequisites**
1. Install **Java 17** or higher.
2. Install **MySQL** and ensure it is running.
3. Install **Redis** and ensure it is running.
4. Install a Git client (e.g., GitHub Desktop, CLI).
5. Install an IDE (e.g., IntelliJ IDEA)

---

### **Steps to Run Locally**

1. **Clone the Repository**
   ```bash
   git clone <GITHUB_REPO_LINK>
   cd <PROJECT_DIRECTORY>
   
---

### Set Up MySQL
Create a database named stock_management.
Update the application.yml file with your database credentials:
```bash
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stockDb
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
   ``` 
---

### Set Up Redis
Start the Redis server (default port: 6379)

### Access Endpoints
Swagger or Postman can be used to test API endpoints:
``` bash
GET /stocks/{id}: Fetch a stock by ID.
POST /stocks/create: Create a new stock.
PUT /stocks/update: Update an existing stock.
GET /stocks/benchmark: Run benchmarking for sequential vs parallel execution.
```
---

## Code Architecture

### Layers and Components
 #### Controller Layer (StockController): Handles HTTP requests and sends responses.
#### Service Layer (StockService): Implements business logic, caching, and database interactions.
#### Repository Layer (StockRepository): Manages database operations.
#### Configuration Classes:
#### RedisConfig: Configures RedisTemplate for cache interactions.
#### AppConfig: Configures ExecutorService for multithreading.
---
### Utility Classes:
#### BenchmarkService: Handles performance benchmarking.
#### Logger: CacheMetricsLogger
---




### Technologies Used
#### Backend: Spring Boot (Java 17+)
#### Database: MySQL
#### Cache: Redis
#### Concurrency: ExecutorService
#### Testing: JUnit, Mockito
#### Logging: SLF4J with StopWatch for metrics
