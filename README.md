# Şase Eşleştirme Web Uygulaması

Volkswagen Group araç şase eşleştirme ve stok yönetim sistemi.

## Teknoloji Stack

- Java 21 + Spring Boot 3.3.5
- Spring Web MVC + Thymeleaf (Layout Dialect)
- Spring Data JPA + Hibernate 6
- Spring Security 6 + Supabase JWT (OAuth2 Resource Server)
- PostgreSQL (Supabase hosted)
- Flyway (migration)
- Lombok + MapStruct
- Bootstrap 5 + DataTables.net + FullCalendar

## Gereksinimler

- JDK 21+
- Maven 3.9+
- Supabase projesi (DB + Auth)

## Çalıştırma

### 1. Environment Variable'ları Ayarla

Proje root'unda `.env` dosyası oluştur (git'e commit etme):

```
SUPABASE_DB_URL=jdbc:postgresql://<host>:5432/postgres
SUPABASE_DB_USERNAME=postgres.<project-ref>
SUPABASE_DB_PASSWORD=<db-password>
SUPABASE_PROJECT_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<anon-key>
SUPABASE_JWKS_URL=https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json
# Projede henüz asimetrik imzalara geçilmediyse (Dashboard ▸ Settings ▸ API ▸ JWT Secret / HS256):
# SUPABASE_JWT_SECRET="<dashboard'daki JWT Secret>"
```

### 2. Uygulamayı Başlat (Dev profili)

```bash
# Windows PowerShell — env variable'ları shell'e yükle, sonra çalıştır
$env:SUPABASE_DB_URL="jdbc:postgresql://..."
$env:SUPABASE_DB_USERNAME="postgres.xxx"
# ... diğerleri ...

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Veya IntelliJ / VS Code'da `.env` dosyasını Run Configuration'a ekle.

### 3. Prod için

```bash
APP_PROFILE=prod ./mvnw spring-boot:run
```

## Proje Yapısı

```
src/main/java/com/sase/app/
├── config/          → Security, Web, JPA konfigürasyonları
├── controller/
│   ├── web/         → Thymeleaf sayfa controller'ları
│   └── api/         → AJAX JSON endpoint'leri
├── entity/          → JPA entity'leri
├── repository/      → Spring Data JPA repository'leri
├── service/         → Business logic
├── dto/             → Java records (DTO)
├── mapper/          → MapStruct mapper'ları
├── security/        → JWT filter, principal
└── exception/       → Global exception handler
```

## Database Migration

Tablolar Supabase'de zaten mevcuttur. Flyway `baseline-on-migrate=true` ile
V1 baseline kabul edilir ve dokunmaz. Yeni migration'lar:

```
src/main/resources/db/migration/V2__yeni_degisiklik.sql
```

## Notlar

- `application.properties` / `application.yml`'e asla şifre yazma
- Tüm hassas bilgiler env variable'lardan okunur
- `ddl-auto=validate` — Hibernate schema'ya dokunmaz, sadece doğrular
