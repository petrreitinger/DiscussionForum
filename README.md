# DiscussionForum

**CZ** | [EN](#english)

## Czech

Webova aplikace diskuzniho fora postavena na Spring Boot. Umoznuje uzivatelu vytvaret komunity, psat prispevky, komentovat, hlasovat a vyhledavat obsah. Inspirovano Redditem.

### Pozadavky

- Java 17+
- Maven 3.8+
- MySQL 8.0+ (nebo H2 pro testovani)

### Spusteni

1. Vytvorte MySQL databazi:
   ```sql
   CREATE DATABASE discussionforum;
   ```

2. Nastavte promenne prostredi:
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/discussionforum
   export DB_USERNAME=root
   export DB_PASSWORD=vaseheslo
   export ADMIN_PASSWORD=silneheslo   # povinne pro vytvoreni admin uctu
   ```

3. Sestavte a spustte:
   ```bash
   cd DiscussionForum
   mvn spring-boot:run
   ```

4. Otevrete `http://localhost:8080` v prohlizeci.

### Admin ucet

Admin ucet se vytvori automaticky pokud je nastavena promenna `ADMIN_PASSWORD`. Vychozi uzivatelske jmeno je `admin` (lze zmenit pres `ADMIN_USERNAME`).

### Funkce

- Registrace a prihlaseni uzivatelu (Spring Security + BCrypt)
- Vytvareni a sprava komunit
- Psani prispevky s prilohami (obrazky, dokumenty)
- Vnorene komentare s odpovedi
- Hlasovani (upvote/downvote) pro prispevky i komentare
- Ukladani prispevku
- Fulltextove vyhledavani (prispevky, komentare, uzivatele)
- Razeni feedu: Hot, New, Top
- Responzivni UI s mobilni podporou

### Struktura projektu

```
src/main/java/discussionforum/
  config/          - Konfigurace (upload souboru, web, globalni rady)
  controllers/     - Spring MVC kontrolery
  exception/       - Vlastni vyjimky a handlery
  model/           - JPA entity a DTO
  repository/      - Spring Data JPA repozitare
  security/        - Spring Security konfigurace
  service/         - Business logika
src/main/resources/
  templates/       - Thymeleaf sablony
  static/          - CSS, JS
  application*.properties - Konfigurace prostredi
```

### Technologie

- Spring Boot 3.3.2
- Spring Security 6.3
- Spring Data JPA + Hibernate
- Thymeleaf
- MySQL / H2
- Bootstrap 5.3
- Maven

---

<a id="english"></a>

## English

A discussion forum web application built with Spring Boot. Allows users to create communities, write posts, comment, vote, and search content. Inspired by Reddit.

### Requirements

- Java 17+
- Maven 3.8+
- MySQL 8.0+ (or H2 for testing)

### Getting Started

1. Create a MySQL database:
   ```sql
   CREATE DATABASE discussionforum;
   ```

2. Set environment variables:
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/discussionforum
   export DB_USERNAME=root
   export DB_PASSWORD=yourpassword
   export ADMIN_PASSWORD=strongpassword   # required to create admin account
   ```

3. Build and run:
   ```bash
   cd DiscussionForum
   mvn spring-boot:run
   ```

4. Open `http://localhost:8080` in your browser.

### Admin Account

The admin account is created automatically when `ADMIN_PASSWORD` is set. Default username is `admin` (configurable via `ADMIN_USERNAME`).

### Features

- User registration and login (Spring Security + BCrypt)
- Community creation and management
- Posts with file attachments (images, documents)
- Nested comments with replies
- Voting (upvote/downvote) for posts and comments
- Post bookmarking
- Full-text search (posts, comments, users)
- Feed sorting: Hot, New, Top
- Responsive UI with mobile support

### Project Structure

```
src/main/java/discussionforum/
  config/          - Configuration (file upload, web, global advice)
  controllers/     - Spring MVC controllers
  exception/       - Custom exceptions and handlers
  model/           - JPA entities and DTOs
  repository/      - Spring Data JPA repositories
  security/        - Spring Security configuration
  service/         - Business logic
src/main/resources/
  templates/       - Thymeleaf templates
  static/          - CSS, JS
  application*.properties - Environment configuration
```

### Tech Stack

- Spring Boot 3.3.2
- Spring Security 6.3
- Spring Data JPA + Hibernate
- Thymeleaf
- MySQL / H2
- Bootstrap 5.3
- Maven
