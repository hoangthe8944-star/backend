# Beatbox Company Backend

A comprehensive music streaming platform backend built with Spring Boot, designed to provide a seamless music experience including song streaming, playlist management, social interactions, and live room features.

## 🚀 Technology Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.4.6
- **Database:** MongoDB (NoSQL)
- **Caching:** Redis
- **Security:** Spring Security, JWT (JSON Web Token), OAuth2
- **Media Storage:** Cloudinary
- **Live Streaming:** Zego Cloud Integration
- **Payment:** MoMo API Integration
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build Tool:** Maven
- **Utilities:** Lombok, MapStruct, mp3agic

## 🛠 Key Features

- **User Management:** Authentication, Authorization, and OAuth2 social login.
- **Music Library:** Management of Songs, Albums, Artists, and Categories.
- **Streaming & Media:** Cloudinary integration for media storage and streaming.
- **Social Features:** Playlists, Comments, and Follow system.
- **Live Interaction:** Live rooms for real-time engagement using Zego.
- **Premium & Payments:** Subscription management and MoMo payment gateway integration.
- **Search:** Advanced search functionality with indexing.
- **AI Integration:** AI-powered features for enhanced user experience.
- **History:** Tracking user listening history.

## 📂 Project Structure

The project follows a standard Spring Boot layered architecture:

- `Controller/`: REST API endpoints.
- `Service/`: Business logic implementation.
- `Repository/`: Data access layer (MongoDB).
- `Entity/`: Domain models.
- `Dto/`: Data Transfer Objects for API requests/responses.
- `Security/`: JWT and OAuth2 configuration.
- `Config/`: Application configurations (Cloudinary, Mail, Security, etc.).
- `search/`: Dedicated module for search indexing and retrieval.

## ⚙️ Setup & Installation

1. **Prerequisites:**
   - Java 17+
   - MongoDB
   - Redis
   - Maven

2. **Configuration:**
   Update `src/main/resources/application.properties` with your environment-specific credentials:
   - MongoDB connection URI
   - Redis host/port
   - Cloudinary API keys
   - Zego App ID/Secret
   - MoMo API credentials
   - Mail server settings

3. **Build & Run:**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **API Documentation:**
   Once running, access the Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 📝 License
This project is a demo project for Spring Boot.