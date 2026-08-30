# Barber Booking App

A full stack booking app for barber shops, built with **Spring Boot, React, and PostgreSQL**.

Clients can create an account, pick a barber, choose a date and time, book an appointment, and look over existing bookings. There's also an owner login for viewing and managing appointments.

## Features

* Create and manage client accounts
* Sign in as an existing client
* Choose between available barbers
* Select appointment date and time with a date/time picker
* Create and view appointments
* Delete existing appointments
* Block booking a time that's already taken
* Owner login, with appointment access meant for the shop owner
* Input validation in the browser and on the server

## Engineering Highlights

* **REST API with Spring Boot**
  Separate controllers expose endpoints for clients, appointments, barbers, and owners.

* **Persistent relational model with JPA**
  Appointments are entities linked to clients and barbers through `ManyToOne` relationships.

* **Booking conflict handling**
  Before a new booking is saved, appointment creation checks the repository for an existing appointment at that date and time.

* **Request validation**
  Client data uses Jakarta Bean Validation for required fields, email format, a positive age, and the other input rules.

* **Repository abstraction with Spring Data JPA**
  Persistence lives in repositories for clients, appointments, barbers, and owners.

* **React API integration**
  The frontend creates clients and appointments through the REST API and updates booking state after create and delete operations.

* **UI built with Material UI components**
  Booking, barber selection, authentication, and appointment views are split into reusable React components.

* **Backend test coverage**
  There are dedicated controller tests for client and appointment behavior.

## Tech Stack

### Backend

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA
* Hibernate Validator
* PostgreSQL
* Maven
* JUnit / Spring Boot Test

### Frontend

* React 18
* Material UI
* MUI Date Pickers
* Day.js
* Moment.js

## Architecture

The backend and frontend live in the same repo:

```text
BarberBookingApp/
├── src/
│   ├── main/
│   │   ├── java/com/example/SpringProject/
│   │   │   ├── clients/       # Domain entities and request models
│   │   │   ├── controller/    # REST API controllers
│   │   │   ├── error/         # Custom exceptions
│   │   │   └── repository/    # Spring Data repositories
│   │   └── resources/
│   │       └── application.properties
│   └── test/                  # Backend tests
│
├── clientfrontend/
│   └── src/
│       ├── components/        # React UI components
│       └── Pictures/
│
└── pom.xml
```

The React frontend talks to the Spring Boot API over HTTP. Spring Data JPA handles persistence to PostgreSQL.

### Data Model

An appointment connects a client with a barber at a specific date and time:

```text
Client ───< Appointment >─── Barber
```

Both relationships are stored as JPA entity relationships.

## API Overview

The backend exposes REST endpoints including:

```text
/clients
/appointments
/barber
/owner
```

Examples:

```http
POST   /clients
GET    /clients
GET    /clients/{id}
PUT    /clients/{id}
DELETE /clients/{id}

POST   /appointments
GET    /appointments
GET    /appointments/{id}
PUT    /appointments/{id}
DELETE /appointments/{id}

POST   /clients/login
POST   /owner/login
```

## Running Locally

### Prerequisites

Install:

* Java 17
* Node.js and npm
* PostgreSQL

### 1. Configure PostgreSQL

The backend expects a PostgreSQL database. Update:

```text
src/main/resources/application.properties
```

with credentials for your local PostgreSQL setup.

The current configuration expects a database named:

```text
DB
```

### 2. Start the backend

From the repository root:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API runs locally on:

```text
http://localhost:8080
```

### 3. Start the frontend

Open another terminal:

```bash
cd clientfrontend
npm install
npm start
```

The React app runs on:

```text
http://localhost:3000
```

## Current Scope

This repo is a learning and portfolio project, not a production booking platform.

A few production concerns are left out on purpose. Auth is a simple login flow in the app, not a production auth framework. Frontend API addresses are set up for local development, and the database config should be moved out of the app before you deploy.

## What This Project Demonstrates

This project shows how a relational web app comes together:

* designing a REST API with Spring Boot
* modelling relationships with JPA
* persisting application state in PostgreSQL
* validating incoming data
* handling booking constraints
* connecting a React frontend to a Java backend
* building interactive forms and appointment management UI
* testing backend controller behavior
