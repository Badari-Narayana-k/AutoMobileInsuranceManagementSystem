-----

# AutoMobileInsuranceManagementSystem

## Project Overview

The **Auto Mobile Insurance Management System** is a robust, full-stack web application designed to streamline and automate the process of managing vehicle insurance policies and claims. Developed using Spring Boot and Thymeleaf, this system provides a dedicated portal for three distinct user roles: **Administrators**, **Insurance Agents**, and **Policyholders (Users)**, each with tailored functionalities to manage policies, process claims, and provide support efficiently.

This project was initially developed as an internship assignment and has since been significantly expanded and refined to serve as a comprehensive, real-world application, showcasing skills in full-stack development, secure authentication, file handling, and a user-friendly interface.

-----

## Key Features

The system implements a wide range of features to cover the entire insurance lifecycle:

### User Management & Authentication

  * **Role-Based Access Control (RBAC):** Secure login and access rights defined for Admin, Agent, and User roles.
  * **User Registration:** New users can register for an account.
  * **Session Management:** Secure user sessions to maintain state and enforce access.

### Policy Management

  * **Policy Creation & Listing:** Admins can create and manage various insurance policies (e.g., Car, Bike, Truck insurance).
  * **Policy Details:** Each policy includes comprehensive information such as Policy ID, Name, Type, Price, Coverage Rate, and Duration.
  * **Agent Assignment:** Policies are assigned to specific agents for management.
  * **Policy Purchase:** Registered users can browse available policies and purchase them.

### Claim Management

  * **Claim Initiation:** Policyholders can initiate claims against their purchased policies.
  * **Detailed Claim Submission:** Users can submit comprehensive claim details including:
      * Contact phone number and address
      * Vehicle name and number (if applicable)
      * Detailed description of the incident/claim event
  * **Proof Image Uploads:** Users can upload multiple proof images (JPG, PNG) for their claims.
      * **Client-Side Validation:** Immediate feedback for users on file size limits (e.g., Max 5MB per image, Max 5 images).
      * **Server-Side Validation:** Robust validation to prevent oversized files and ensure data integrity.
      * **Secure Storage:** Images are stored in a dedicated, external `uploads` directory.
      * **Dynamic Display:** Uploaded images are correctly served and displayed on relevant claim view pages.
  * **Claim Status Tracking:** Claims progress through various statuses:
      * `Premium Purchased` (initial state after policy purchase, before claim submission)
      * `Pending` (after user submits claim details, awaiting agent review)
      * `Approved` (by agent)
      * `Rejected` (by agent)
  * **Agent Claim Review:** Agents can view and filter claims assigned to their policies.
  * **Claim Approval/Rejection:** Agents can approve or reject pending claims, with the agent's response date recorded.

### Customer Support System

  * **Ticket Creation:** Users can raise support tickets for queries or issues.
  * **Ticket History:** Users can view the status and history of their submitted support tickets.

### Search & Filtering

  * **User Claims Search:** Policyholders can search and filter their claims by Claim ID, Policy Name, Status, or Date.
  * **Agent Claim Requests:** Agents can view and filter pending claim requests.

### User Profile Management

  * Users can access their profile details.

-----

## User Roles & Workflows

The system caters to three primary user roles, each with distinct responsibilities and accessible features:

### 1\. Admin

The Admin role is the highest level of access, overseeing the entire system.

  * **Work Done:**
      * Create, view, update, and delete insurance policies.
      * Assign policies to specific agents.
      * Manage user accounts (potentially activate/deactivate users).
      * View all claims and system activity.
  * **Key Functionalities:** Policy Management, User Management, System Oversight.

### 2\. Agent

Agents are responsible for managing policies and claims assigned to them.

  * **Work Done:**
      * View policies they are assigned to.
      * Review and process pending claims from policyholders.
      * Approve or reject claims based on supporting documentation.
      * Communicate with users regarding their claims (implicit, through claim status updates).
  * **Key Functionalities:** Claim Processing, Policy Oversight (for assigned policies).
  * **Accessible Paths:** `/agent/claim-requests`, `/agent/claim/{id}/view`, etc.

### 3\. User (Policyholder)

Users are the policyholders who interact with the system to manage their insurance.

  * **Work Done:**
      * Register and log in to their personal dashboard.
      * Browse and purchase various insurance policies.
      * View their purchased policies and their associated claims.
      * Initiate a claim for a purchased policy (when status is 'Premium Purchased').
      * Submit detailed claim information, including uploading proof images.
      * Track the real-time status of their claims (Pending, Approved, Rejected).
      * Raise support tickets for assistance and view their ticket history.
      * View their personal profile.
  * **Key Functionalities:** Policy Purchase, Claim Submission & Tracking, Support Ticketing, Profile Management.
  * **Accessible Paths:** `/user/home`, `/policies`, `/user/claims`, `/user/claims/request/{claimId}`, `/user/support/raise`, `/user/support/history`, `/profile`, etc.

-----

## Technologies Used

This project leverages a modern tech stack to provide a robust and scalable solution:

  * **Backend:**
      * **Spring Boot:** Framework for rapid application development.
      * **Spring MVC:** Web framework for handling HTTP requests and responses.
      * **Spring Data JPA:** For simplified data access and persistence with a relational database.
      * **Java 17+:** Programming language.
  * **Frontend:**
      * **Thymeleaf:** Server-side templating engine for rendering dynamic HTML.
      * **HTML5 & CSS3:** Standard web markup and styling.
      * **Bootstrap 5.3.3:** CSS framework for responsive design and pre-built UI components.
      * **Bootstrap Icons:** For scalable vector icons.
      * **JavaScript:** For client-side interactivity, form validation, and dynamic UI updates (e.g., modal display for file size errors).
  * **Database:**
      * (Implicitly, from JPA, typically configured for H2 in-memory for development, easily configurable for MySQL/PostgreSQL in production.)
  * **Build Tool:**
      * **Maven:** Project management and comprehension tool.
  * **Version Control:**
      * **Git:** For source code management.

-----

## Project Structure Overview

```
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/aims/prod/
│   │   │       ├── Config/          # Spring configurations (e.g., WebConfig for resource handling)
│   │   │       ├── Controller/      # Handles HTTP requests (Admin, Agent, User related logic)
│   │   │       ├── Entity/          # JPA Entities mapping to database tables (User, Policy, Claim, etc.)
│   │   │       ├── Repository/      # Spring Data JPA repositories for database operations
│   │   │       └── AimsProdApplication.java # Main Spring Boot application entry point
│   │   ├── resources/
│   │   │   ├── static/              # Static web resources (CSS, JS, images that are part of the app)
│   │   │   │   └── uploads/         # **Runtime created/managed directory for user-uploaded images**
│   │   │   ├── templates/           # Thymeleaf HTML templates (*.html)
│   │   │   └── application.properties # Application configuration (database, port, custom properties like upload-dir)
│   └── test/
│       └── java/...                 # Unit and integration tests
├── uploads/                     # **The actual directory where user-uploaded images are stored at runtime**
├── pom.xml                      # Maven project object model file (dependencies, build config)
├── README.md                    # This documentation file
└── .gitignore                   # Specifies intentionally untracked files to ignore
```

*Note: The `uploads/` directory at the project root is created and managed by the application at runtime based on the `file.upload-dir=./uploads` configuration. It's external to the `src/main/resources/static/` directory for persistence and proper serving.*

-----

## Setup and Run Locally

To get this project up and running on your local machine, follow these steps:

### Prerequisites

  * **Java Development Kit (JDK) 17 or higher:** [Download from Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenAdoptium](https://adoptium.net/).
  * **Maven 3.6.0 or higher:** [Download from Apache Maven](https://maven.apache.org/download.cgi).
  * **Git:** [Download from Git-SCM](https://git-scm.com/downloads).
  * **An IDE (Optional but Recommended):** IntelliJ IDEA, VS Code with Java extensions, or Eclipse.

### Steps

1.  **Clone the Repository:**

    ```bash
    git clone <your-repository-url>
    cd vehicle-insurance-management-system # or whatever your project's root folder is named
    ```

2.  **Configure Application Properties:**
    Open `src/main/resources/application.properties` and ensure the following essential configurations are present:

    ```properties
    # Server Port (optional, defaults to 8080)
    server.port=8080

    # File Upload Configuration
    # This path is relative to the directory where the application is run from
    file.upload-dir=./uploads 
    spring.servlet.multipart.max-file-size=5MB
    spring.servlet.multipart.max-request-size=50MB

    # Database Configuration (Example for H2 In-Memory - Adjust for MySQL/PostgreSQL if needed)
    spring.h2.console.enabled=true
    spring.datasource.url=jdbc:h2:mem:aimsdb;DB_CLOSE_DELAY=-1
    spring.datasource.driverClassName=org.h2.Driver
    spring.datasource.username=sa
    spring.datasource.password=
    spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
    spring.jpa.hibernate.ddl-auto=update # Use 'create' to drop and recreate schema on startup
    ```

3.  **Build the Project:**
    Navigate to the project root directory in your terminal (where `pom.xml` is located) and run the Maven clean install command:

    ```bash
    mvn clean install
    ```

    This will download dependencies and build the project.

4.  **Run the Application:**
    You can run the Spring Boot application using the Maven Spring Boot plugin:

    ```bash
    mvn spring-boot:run
    ```

    Alternatively, you can run the generated JAR file:

    ```bash
    java -jar target/aims-prod-0.0.1-SNAPSHOT.jar # Adjust JAR name if different
    ```

    If using an IDE, you can run the `AimsProdApplication.java` file directly.

5.  **Create Uploads Directory:**
    Upon the first run, a folder named `uploads` should be created in your project's root directory (at the same level as `src` and `pom.xml`). This is where all user-uploaded images will be stored. Ensure your application has write permissions to this directory.

-----

## Usage Instructions

Once the application is running (typically on `http://localhost:8080`), you can access it through your web browser.

### Initial Setup / Dummy Data

  * The application will likely start with an empty database (if using `ddl-auto=create`). You might need to manually register initial users or implement a data-seeding mechanism for Admins/Agents.
  * **Recommended:** If `ddl-auto=create` is used, the application will create tables. For initial testing, you might need to register a user first, then manually change their role in the database to 'admin' or 'agent' (e.g., via H2 console at `http://localhost:8080/h2-console` if enabled and configured). Alternatively, implement a `@PostConstruct` method to seed initial users for each role.

### Login Credentials (Example)

  * **For Users:** Register a new account via the UI.
  * **For Admin/Agent:** You'll need to create these roles in the database manually or via a pre-seeding mechanism if not already implemented.
      * *Example (if seeded):*
          * **Admin:** `username: admin@example.com`, `password: password` (or as per your seeding)
          * **Agent:** `username: agent@example.com`, `password: password` (or as per your seeding)

### Navigation & Workflow

1.  **Register/Login:**

      * Navigate to the base URL (`http://localhost:8080/login`).
      * If you're a new user, register an account.
      * Log in with your credentials.

2.  **User Workflow:**

      * **Home Dashboard:** View summaries (e.g., `http://localhost:8080/user/home`).
      * **Browse Policies:** Go to `/policies` to view available policies.
      * **Purchase Policy:** Select a policy and proceed with purchase (this will create a claim with 'Premium Purchased' status).
      * **My Claims:** Go to `/user/claims` to view your purchased policies/claims.
      * **Submit Claim Details:** For a claim with 'Premium Purchased' status, click "Claim Now" to fill out the detailed form, including uploading proof images.
      * **Track Claims:** Monitor the status of your submitted claims.
      * **Customer Support:** Raise tickets via `/user/support/raise` and view history via `/user/support/history`.

3.  **Agent Workflow:**

      * **Login as Agent.**
      * **Claim Requests:** Navigate to `/agent/claim-requests` to see all pending claims assigned to policies you manage.
      * **View Claim Details:** Click "View" on a pending claim to see all submitted details and proof images.
      * **Approve/Reject:** Based on the review, you can approve or reject the claim.

4.  **Admin Workflow:**

      * **Login as Admin.**
      * **Policy Management:** Access features to add new policies, update existing ones, and assign agents to policies.
      * **User Management:** (If implemented) Manage user accounts.
      * **System Overview:** View all claims and system data.

-----

## Future Enhancements (Ideas for further development)

  * **Email Notifications:** Implement email alerts for claim status changes, ticket updates, etc.
  * **Chatbot/Live Chat:** Add a real-time communication feature for customer support.
  * **Agent Dashboard:** A dedicated dashboard for agents to manage their policies and claims more efficiently.
  * **Policy Renewal Reminders:** Automated reminders for policy renewals.
  * **Advanced Reporting:** Generate reports on policies, claims, and user activity.
  * **Image Optimization:** Implement image compression/resizing on upload for better performance.
  * **User Interface/Experience (UI/UX) Improvements:** Further refine the design and responsiveness.
  * **Unit & Integration Tests:** Expand test coverage for robust code.
  * **Security Hardening:** Implement more advanced security measures (e.g., password policies, brute-force protection, CSRF tokens more broadly).

-----
