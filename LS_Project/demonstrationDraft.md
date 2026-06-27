# Demonstration Draft — Phase 2

## Overview

Each group has a maximum of **15 minutes** for their demonstration, during which one or more selected students must complete the following steps.

---

## Steps

### 1. Clone the repository
Clone the group repository into a new folder.

### 2. Checkout Phase 2 tag
Check out the Phase 2 tag (`0.2.*`).

### 3. Build the project
Use Gradle from the command line to clean and build the project.

### 4. Build again
Use Gradle from the command line to clean and build the project **again**.

### 5. Launch the HTTP server
From the command line, launch the HTTP server with the **PostgreSQL** implementation.

### 6. API Testing (via HTTP REST client or Postman)

| # | Action |
|---|--------|
| 6.1 | Create two users |
| 6.2 | Attempt to create another user with the **same email** as the first user |
| 6.3 | Create four locations: `Portugal → Lisboa → Oeiras → Paço de Arcos` |
| 6.4 | List the **child locations** of Lisboa |
| 6.5 | Add **three houses** with location set to Paço de Arcos |
| 6.6 | List **all houses** |
| 6.7 | Show the **details** of one house |
| 6.8 | Create **two bookings** for the first house using the first user |
| 6.9 | Attempt to create a booking with an **invalid date format** and another that **overlaps** with an existing booking |
| 6.10 | List all **bookings for the first house** |
| 6.11 | Show the **details** of one booking |
| 6.12 | List all houses using **pagination** with page size of 2, navigating through all pages |
| 6.13 | **Remove** a booking |
| 6.14 | Attempt to show the details of the **previously removed** booking |

### 7. Launch the browser

### 8. SPA Navigation
Navigate to the application root and **explore all edges** of the navigation graph.

---

> **Tip:** To optimize time during the presentation, prepare a file with all necessary HTTP requests for step 6 in advance, or alternatively a **Postman collection**.
