# DS Assignment 2

## Group Information
- Group Number:
- Eman Hussein  20226023
- Mariam Mazen  2022611

## System Architecture
This system consists of 3 microservices:

| Service | Technology | Port |
|---|---|---|
| User Service | Jakarta EE (EJBs) on WildFly | 8080 |
| Offer Service | Spring Boot | 8081 |
| Booking Service | Spring Boot + RabbitMQ | 8082 |

## EJB Types Used
- **Singleton EJB** (`DatabaseInit.java`) — Runs once at startup, initializes the database
- **Stateless EJB** (`UserService.java`) — Handles all business logic, stateless for thread safety

## Prerequisites
- Java 17
- Maven
- Docker Desktop
- WildFly 35.0.0.Final (extracted to `C:\wildfly\wildfly-35.0.0.Final`)

## How to Run

### Step 1 — Start Docker Desktop
Open Docker Desktop and wait for green "Engine running"

### Step 2 — Start RabbitMQ
```bash
docker-compose up -d
```
Verify at: http://localhost:15672 (username: admin, password: admin)

### Step 3 — Start WildFly
```bash
C:\wildfly\wildfly-35.0.0.Final\bin\standalone.bat
```
Wait for: `WildFly Full 35.0.0.Final started`

### Step 4 — Build and Deploy User Service
```bash
cd user-service
mvn clean package
copy target\user-service.war C:\wildfly\wildfly-35.0.0.Final\standalone\deployments\
```

### Step 5 — Start Offer Service
Open a new terminal:
```bash
cd offer-service
mvn spring-boot:run
```

### Step 6 — Start Booking Service
Open a new terminal:
```bash
cd booking-service
mvn spring-boot:run
```

## Verify Everything is Running

| Service | URL |
|---|---|
| RabbitMQ Dashboard | http://localhost:15672 |
| User Service | http://localhost:8080/user-service/api/users/all |
| Offer Service | http://localhost:8081/api/offers |
| Booking Service | http://localhost:8082/api/bookings/all |

## API Endpoints

### User Service (port 8080)
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/users/register | Register customer or provider |
| POST | /api/users/login | Login |
| POST | /api/users/{id}/add-funds | Add wallet funds |
| GET | /api/users/{id}/wallet | View wallet balance |
| GET | /api/users/all | Admin: view all users |
| GET | /api/users/categories | View all categories |
| POST | /api/users/categories | Admin: add category |

### Offer Service (port 8081)
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/offers | Provider creates offer |
| GET | /api/offers | View all active offers |
| GET | /api/offers/category/{category} | Browse by category |
| GET | /api/offers/provider/{id} | Provider views own offers |
| GET | /api/offers/{id} | Get offer by ID |
| PUT | /api/offers/{id} | Update offer |

### Booking Service (port 8082)
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/bookings | Create a booking |
| GET | /api/bookings/customer/{id} | Customer booking history |
| GET | /api/bookings/provider/{id} | Provider completed services |
| GET | /api/bookings/all | Admin: all bookings |
| GET | /api/bookings/notifications/customer | Customer notifications |
| GET | /api/bookings/notifications/provider | Provider notifications |

## Notes & Assumptions
- H2 in-memory file database used for simplicity (no external DB installation needed)
- Admin account is auto-created at startup (username: admin, password: admin123)
- Each service has its own independent database as per microservice architecture
- Services communicate via REST calls (no shared databases)
- RabbitMQ handles async notifications for booking confirmations and rejections
- Wallet deduction happens before booking creation — if booking fails, refund is automatic