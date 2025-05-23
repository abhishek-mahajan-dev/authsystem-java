# Setup and Run Guide for the Authentication System 

This guide will walk you through the steps to set up and run the Spring Boot Authentication System on your local machine.

## Prerequisites

Before you begin, ensure you have the following installed:

* **Java Development Kit (JDK) 17 or higher:**
    * [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
* **Apache Maven 3.6.0 or higher:**
    * [Download Maven](https://maven.apache.org/download.cgi)
* **MySQL Server 8.0 or higher:**
    * [Download MySQL Community Server](https://dev.mysql.com/downloads/mysql/)
* **An IDE (Integrated Development Environment) like IntelliJ IDEA or VS Code:**
    * [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/)
    * [VS Code](https://code.visualstudio.com/download) with Java Extension Pack
* **Lombok Plugin for your IDE:** If using IntelliJ, install the Lombok plugin. For VS Code, ensure the Java Extension Pack is installed.

## 1. Database Setup (MySQL)

1.  **Create a Database:**
    Open your MySQL client (e.g., MySQL Workbench, command line) and execute the following SQL command to create a new database:

    ```sql
    CREATE DATABASE authsystem;
    ```

2.  **Configure `application.properties`:**
    Navigate to `src/main/resources/application.properties` in your project. Update the database connection details to match your MySQL setup:

    ```properties
    # Database Configuration
    spring.datasource.url=jdbc:mysql://localhost:3306/authsystem?useSSL=false&serverTimezone=Asia/Kolkata
    spring.datasource.username=your_mysql_username # Replace with your MySQL username
    spring.datasource.password=your_mysql_password # Replace with your MySQL password
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

    # JPA and Hibernate Configuration
    spring.jpa.hibernate.ddl-auto=update # 'update' will create/update tables automatically
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
    ```
    * **`spring.jpa.hibernate.ddl-auto=update`**: This setting is crucial. When the application starts for the first time, Hibernate (JPA provider) will automatically create the `users` and `verification_token` tables in your `authsystem` database. For subsequent runs, it will update the schema if any changes are detected in your entity classes.

## 2. Email Configuration (SMTP)

The application sends emails for password setup and reset. You need to configure an SMTP server.

1.  **Update `application.properties`:**
    Add or update the following properties in `src/main/resources/application.properties` with your SMTP server details. A common choice for testing is a Gmail account (ensure "Less secure app access" is enabled or use an App Password if 2FA is on).

    ```properties
    # Email Configuration (SMTP)
    spring.mail.host=smtp.gmail.com
    spring.mail.port=587
    spring.mail.username=your_email@gmail.com # Replace with your sender email
    spring.mail.password=your_email_password # Replace with your App Password
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true
    spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
    ```
   

## 3. Build the Project

Open your terminal or command prompt, navigate to the root directory of your project (where `pom.xml` is located), and run the Maven build command:

```bash
mvn clean install
