# HLA Spec: JWT Authentication + RBAC Authorization

## Context

SIAKAD backend (Spring Boot 4.1.1 / Java 25) saat ini belum memiliki mekanisme autentikasi maupun otorisasi. Database PostgreSQL sudah memiliki tabel `User` dengan kolom `username`, `hashedPassword`, `role` (enum: KSatu, Admin, Guru, Siswa), dan `jenjang` (enum: TK, SD, SMP, SMA). Tidak ada dependency Spring Security maupun JWT library di project. Dokumen ini mendefinisikan arsitektur stateless JWT auth + RBAC agar bisa diimplementasi oleh model yang lebih murah tanpa perlu membuat keputusan arsitektural.

---

## 1. Dependencies (tambah ke `pom.xml`)

```xml
<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (JJWT 0.12.x) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-gson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

> **Kenapa `jjwt-gson` bukan `jjwt-jackson`?** Spring Boot 4.1.1 menggunakan Jackson 3.x (`tools.jackson.databind`). Module `jjwt-jackson` bergantung pada Jackson 2.x (`com.fasterxml.jackson.databind`) yang tidak ada di project ini. `jjwt-gson` menghindari konflik ini.

---

## 2. Configuration Properties

Tambah di `application.yml`:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:PLACEHOLDER_GANTI_DI_PRODUCTION_MIN_64_CHARS_BASE64}
    access-token-expiration-ms: 900000      # 15 menit
    refresh-token-expiration-ms: 604800000  # 7 hari
```

---

## 3. Database Migration

File: `src/main/resources/db/migration/V2__create_refresh_token_table.sql`

> V2 karena Flyway baseline-on-migrate default = V1.

```sql
CREATE TABLE public."RefreshToken" (
    id SERIAL PRIMARY KEY,
    "userId" INTEGER NOT NULL REFERENCES public."User"(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    "expiresAt" TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    "createdAt" TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX "RefreshToken_token_unique" ON public."RefreshToken" (token);
CREATE INDEX "RefreshToken_userId_idx" ON public."RefreshToken" ("userId");
```

---

## 4. Component Map

```
src/main/java/com/siakad/
  common/
    enums/
      Role.java                              # enum KSatu, Admin, Guru, Siswa
      Jenjang.java                           # enum TK, SD, SMP, SMA
    exception/
      GlobalExceptionHandler.java            # EDIT: tambah handler auth exceptions
      InvalidCredentialsException.java       # NEW
      InvalidTokenException.java             # NEW
  auth/
    config/
      JwtProperties.java                     # @ConfigurationProperties("app.jwt")
      SecurityConfig.java                    # SecurityFilterChain, beans
    controller/
      AuthController.java                    # POST /api/auth/login, /refresh, /logout
    dto/
      LoginRequest.java                      # record(username, password)
      LoginResponse.java                     # record(accessToken, refreshToken, user)
      UserInfo.java                          # record(id, username, name, role, jenjang)
      RefreshRequest.java                    # record(refreshToken)
      TokenResponse.java                     # record(accessToken, refreshToken)
    entity/
      UserEntity.java                        # JPA entity → tabel "User"
      RefreshTokenEntity.java                # JPA entity → tabel "RefreshToken"
    repository/
      UserRepository.java                    # findByUsername()
      RefreshTokenRepository.java            # findByTokenAndRevokedFalse(), revokeAllByUserId()
    security/
      JwtAuthenticationFilter.java           # OncePerRequestFilter
      AuthEntryPoint.java                    # 401 handler
      CustomAccessDeniedHandler.java         # 403 handler
      CustomUserDetailsService.java          # implements UserDetailsService
      UserPrincipal.java                     # implements UserDetails, wraps UserEntity
    service/
      JwtService.java                        # generate, parse, validate JWT
      AuthService.java                       # login, refresh, logout logic
```

---

## 5. Spesifikasi Komponen

### 5.1 Shared Enums (`com.siakad.common.enums`)

```java
// Role.java
public enum Role { KSatu, Admin, Guru, Siswa }

// Jenjang.java
public enum Jenjang { TK, SD, SMP, SMA }
```

Dipakai di `common.enums` karena akan digunakan oleh banyak feature package.

### 5.2 JwtProperties

```java
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpirationMs,
    long refreshTokenExpirationMs
) {}
```

### 5.3 UserEntity

Mapping ke tabel `"User"` (legacy Prisma, camelCase naming):

| Java field     | Type           | Column              | Notes                         |
|----------------|----------------|---------------------|-------------------------------|
| id             | Integer        | id                  | PK, `@GeneratedValue(IDENTITY)` |
| createdAt      | OffsetDateTime | `"createdAt"`       | NOT NULL, default             |
| updatedAt      | OffsetDateTime | `"updatedAt"`       | nullable                      |
| name           | String         | name                | varchar(45), nullable         |
| email          | String         | email               | varchar(45), nullable         |
| hashedPassword | String         | `"hashedPassword"`  | text, nullable                |
| role           | Role           | role                | PG enum, NOT NULL             |
| username       | String         | username            | varchar(45), UNIQUE, NOT NULL |
| siswaId        | Integer        | `"siswaId"`         | nullable                      |
| guruId         | Integer        | `"guruId"`          | nullable                      |
| jenjang        | Jenjang        | jenjang             | PG enum, NOT NULL             |

Catatan JPA mapping:
- `@Table(name = "\"User\"")` — User adalah reserved word di PostgreSQL
- Kolom camelCase: `@Column(name = "\"camelCaseName\"")`
- PostgreSQL enum: `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`
- Gunakan Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`

### 5.4 RefreshTokenEntity

| Java field | Type           | Column        | Notes                    |
|------------|----------------|---------------|--------------------------|
| id         | Integer        | id            | PK, SERIAL               |
| user       | UserEntity     | `"userId"`    | `@ManyToOne(LAZY)`, FK   |
| token      | String         | token         | UNIQUE, NOT NULL          |
| expiresAt  | OffsetDateTime | `"expiresAt"` | NOT NULL                 |
| revoked    | boolean        | revoked       | NOT NULL, default false  |
| createdAt  | OffsetDateTime | `"createdAt"` | NOT NULL                 |

### 5.5 Repositories

```java
// UserRepository
Optional<UserEntity> findByUsername(String username);

// RefreshTokenRepository
Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);

@Modifying
@Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
void revokeAllByUserId(@Param("userId") Integer userId);
```

### 5.6 UserPrincipal (implements UserDetails)

- Wraps `UserEntity`
- `getAuthorities()` → `List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))`
- `getPassword()` → `user.getHashedPassword()`
- `getUsername()` → `user.getUsername()`
- `isEnabled()` dll → return `true`
- Extra: `getId()`, `getRole()`, `getJenjang()`

### 5.7 CustomUserDetailsService

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .map(UserPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
}
```

### 5.8 JwtService

- Constructor: derive `SecretKey` dari `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))`
- `generateAccessToken(UserPrincipal)` → JWT dengan claims: `sub=userId`, `username`, `role`, `jenjang`, `iat`, `exp`
- `generateRefreshTokenValue()` → `UUID.randomUUID().toString()` (opaque, bukan JWT)
- `parseAccessToken(String)` → validate signature + expiry, return `Claims`
- `extractUserId/Username/Role/Jenjang(String)` → extract dari claims
- `isTokenValid(String)` → boolean wrapper

> Refresh token sengaja opaque (bukan JWT) karena sudah disimpan di DB untuk revocation — JWT tidak memberi nilai tambah.

### 5.9 JwtAuthenticationFilter (extends OncePerRequestFilter)

```
1. Extract header "Authorization: Bearer <token>"
2. Jika tidak ada → lanjut filter chain (SecurityConfig yg menentukan public/protected)
3. Parse token via JwtService
4. Jika valid & SecurityContext kosong:
   a. Load UserDetails via CustomUserDetailsService
   b. Set UsernamePasswordAuthenticationToken ke SecurityContext
5. Jika token invalid/expired → tulis error response langsung (lihat Error Handling)
6. Lanjut filter chain
```

`shouldNotFilter()`: skip `/api/auth/**` dan `/actuator/**`

**KRITIS**: Filter berjalan sebelum DispatcherServlet → `@ControllerAdvice` TIDAK menangkap exception dari filter. Error harus ditulis langsung ke `HttpServletResponse` menggunakan `ObjectMapper` + `ApiResponse.error()`.

> **Jackson 3.x**: Import `tools.jackson.databind.ObjectMapper`, BUKAN `com.fasterxml.jackson.databind.ObjectMapper`.

### 5.10 AuthEntryPoint (implements AuthenticationEntryPoint)

Dipanggil saat request tanpa token mengakses endpoint protected:
- Status: 401
- Body: `ApiResponse.error("Autentikasi diperlukan", null, Meta.of(path, traceId))`
- Tulis langsung ke response stream

### 5.11 CustomAccessDeniedHandler (implements AccessDeniedHandler)

Dipanggil saat role user tidak cukup:
- Status: 403
- Body: `ApiResponse.error("Akses ditolak", null, Meta.of(path, traceId))`
- Tulis langsung ke response stream

### 5.12 SecurityConfig

```java
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean SecurityFilterChain filterChain(HttpSecurity http, ...) {
        // csrf: disable (stateless)
        // session: STATELESS
        // exceptionHandling: authEntryPoint + accessDeniedHandler
        // authorizeHttpRequests:
        //   /api/auth/** → permitAll
        //   /actuator/** → permitAll
        //   anyRequest → authenticated
        // addFilterBefore: jwtFilter sebelum UsernamePasswordAuthenticationFilter
    }

    @Bean PasswordEncoder → BCryptPasswordEncoder
    @Bean AuthenticationManager → dari AuthenticationConfiguration
    @Bean RoleHierarchy → "ROLE_KSatu > ROLE_Admin > ROLE_Guru > ROLE_Siswa"
    @Bean CorsConfigurationSource → origins configurable, default "*" (ketatkan di production)
}
```

### 5.13 AuthService

```java
@Service
@Transactional
public class AuthService {

    public LoginResponse login(LoginRequest request) {
        // 1. Authenticate via AuthenticationManager (delegates to CustomUserDetailsService + BCryptPasswordEncoder)
        //    Catch BadCredentialsException → throw InvalidCredentialsException("Username atau password salah")
        // 2. Extract UserPrincipal from Authentication
        // 3. Generate access token (JWT)
        // 4. Generate opaque refresh token, save RefreshTokenEntity to DB
        // 5. Build and return LoginResponse
    }

    public TokenResponse refresh(RefreshRequest request) {
        // 1. Look up RefreshTokenEntity by token value where revoked = false
        // 2. If not found → throw InvalidTokenException("Refresh token tidak valid")
        // 3. If expired → throw InvalidTokenException("Refresh token telah kedaluwarsa")
        // 4. Revoke old refresh token (set revoked = true) → ROTATION
        // 5. Load user from DB by userId
        // 6. Generate new access token + new refresh token
        // 7. Save new RefreshTokenEntity
        // 8. Return TokenResponse
    }

    public void logout(String refreshToken) {
        // 1. Find refresh token, mark as revoked
        // 2. Optionally: revokeAllByUserId to logout from all devices
    }
}
```

### 5.14 AuthController

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login berhasil", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token berhasil diperbarui", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.successMessage("Logout berhasil"));
    }
}
```

### 5.15 DTOs (semua Java records)

```java
// LoginRequest.java
public record LoginRequest(
    @NotBlank(message = "Username wajib diisi") String username,
    @NotBlank(message = "Password wajib diisi") String password
) {}

// LoginResponse.java
public record LoginResponse(String accessToken, String refreshToken, UserInfo user) {}

// UserInfo.java
public record UserInfo(Integer id, String username, String name, String role, String jenjang) {}

// RefreshRequest.java
public record RefreshRequest(
    @NotBlank(message = "Refresh token wajib diisi") String refreshToken
) {}

// TokenResponse.java
public record TokenResponse(String accessToken, String refreshToken) {}
```

---

## 6. Endpoint Contracts

### POST /api/auth/login

**Request:**
```json
{ "username": "admin", "password": "rahasia123" }
```

**200 OK:**
```json
{
  "success": true,
  "message": "Login berhasil",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "uuid-string",
    "user": { "id": 1, "username": "admin", "name": "Administrator", "role": "Admin", "jenjang": "SMA" }
  },
  "meta": { "timestamp": "2026-08-29T10:30:00Z" }
}
```

**401 (kredensial salah):**
```json
{
  "success": false,
  "message": "Username atau password salah",
  "meta": { "timestamp": "...", "path": "/api/auth/login", "traceId": "..." }
}
```

### POST /api/auth/refresh

**Request:**
```json
{ "refreshToken": "uuid-string" }
```

**200 OK:**
```json
{
  "success": true,
  "message": "Token berhasil diperbarui",
  "data": { "accessToken": "eyJ...(baru)", "refreshToken": "uuid-baru" },
  "meta": { "timestamp": "..." }
}
```

**401 (token invalid/expired):**
```json
{
  "success": false,
  "message": "Refresh token tidak valid",
  "meta": { "timestamp": "...", "path": "/api/auth/refresh", "traceId": "..." }
}
```

### POST /api/auth/logout

**Request:**
```json
{ "refreshToken": "uuid-string" }
```

**200 OK:**
```json
{
  "success": true,
  "message": "Logout berhasil",
  "meta": { "timestamp": "..." }
}
```

### Protected Endpoint Error Responses

**401 (tanpa/invalid token):**
```json
{ "success": false, "message": "Autentikasi diperlukan", "meta": { "timestamp": "...", "path": "...", "traceId": "..." } }
```

**401 (token expired):**
```json
{ "success": false, "message": "Token telah kedaluwarsa", "meta": { "timestamp": "...", "path": "...", "traceId": "..." } }
```

**403 (role tidak cukup):**
```json
{ "success": false, "message": "Akses ditolak", "meta": { "timestamp": "...", "path": "...", "traceId": "..." } }
```

---

## 7. RBAC Permission Matrix

Hierarchy: **KSatu > Admin > Guru > Siswa** (higher role inherits semua permission role di bawahnya).

| Level Akses             | Annotation                           | KSatu | Admin | Guru | Siswa |
|-------------------------|--------------------------------------|:-----:|:-----:|:----:|:-----:|
| Public (no auth)        | `permitAll()` di SecurityConfig      |  Y    |  Y    |  Y   |  Y    |
| Semua user terautentikasi | `.anyRequest().authenticated()`     |  Y    |  Y    |  Y   |  Y    |
| Siswa ke atas           | `@PreAuthorize("hasRole('Siswa')")`  |  Y    |  Y    |  Y   |  Y    |
| Guru ke atas            | `@PreAuthorize("hasRole('Guru')")`   |  Y    |  Y    |  Y   |  N    |
| Admin ke atas           | `@PreAuthorize("hasRole('Admin')")`  |  Y    |  Y    |  N   |  N    |
| KSatu saja              | `@PreAuthorize("hasRole('KSatu')")`  |  Y    |  N    |  N   |  N    |

Contoh penggunaan di controller masa depan:
```java
@PreAuthorize("hasRole('Admin')")
@GetMapping("/api/users")
public ResponseEntity<ApiResponse<List<UserDto>>> listUsers() { ... }

@PreAuthorize("hasRole('Guru')")
@PostMapping("/api/penilaian")
public ResponseEntity<ApiResponse<Void>> createPenilaian(...) { ... }

// Akses berdasarkan jenjang via SpEL:
@PreAuthorize("hasRole('Admin') and #principal.jenjang.name() == 'SMA'")
public ResponseEntity<...> smaOnly(@AuthenticationPrincipal UserPrincipal principal) { ... }
```

---

## 8. Token Lifecycle

```
LOGIN                    VALIDATE (setiap request)        REFRESH                  LOGOUT
─────                    ────────────────────────         ───────                  ──────
username+password        Authorization: Bearer <jwt>      refreshToken (body)      refreshToken (body)
       │                          │                            │                        │
       ▼                          ▼                            ▼                        ▼
AuthenticationManager     JwtAuthFilter.parse()          Cari di DB (not revoked)  Revoke di DB
       │                          │                            │
       ▼                          ▼                            ▼
Generate accessToken     Set SecurityContext             Revoke token lama
(JWT, 15min)             (UserPrincipal)                 Generate token baru
       │                                                 (rotation)
       ▼
Generate refreshToken
(opaque UUID, 7 hari)
       │
       ▼
Simpan RefreshToken di DB
```

**Refresh Token Rotation**: Setiap kali refresh dipakai, token lama di-revoke dan token baru digenerate. Mencegah replay attack.

**Password Compatibility**: Library bcrypt Node.js menghasilkan hash `$2a$`/`$2b$` yang kompatibel dengan `BCryptPasswordEncoder` Spring. Verifikasi dengan inspect hash existing di database.

---

## 9. Error Handling Strategy (3 Layer)

| Layer | Lokasi | Mekanisme | Error yang ditangani |
|-------|--------|-----------|---------------------|
| **Filter** | `JwtAuthenticationFilter` | Tulis `ApiResponse` langsung ke response stream via `ObjectMapper` | Malformed/expired/invalid JWT |
| **Security** | `AuthEntryPoint` + `CustomAccessDeniedHandler` | Tulis langsung ke response stream | 401 (no token), 403 (insufficient role) |
| **Controller** | `GlobalExceptionHandler` | `@ExceptionHandler` (existing pattern) | `InvalidCredentialsException` (401), `InvalidTokenException` (401) |

Custom exceptions baru di `com.siakad.common.exception/`:
- `InvalidCredentialsException extends RuntimeException`
- `InvalidTokenException extends RuntimeException`

Tambah handler di `GlobalExceptionHandler`:
```java
@ExceptionHandler(InvalidCredentialsException.class)
→ 401, ApiResponse.error(ex.getMessage(), null, Meta.of(path, traceId))

@ExceptionHandler(InvalidTokenException.class)
→ 401, ApiResponse.error(ex.getMessage(), null, Meta.of(path, traceId))
```

---

## 10. Implementation Order

Urutan menjamin setiap file bisa compile berdasarkan dependency-nya.

| Step | Files | Depends On |
|------|-------|------------|
| 1 | `pom.xml` — tambah dependencies | — |
| 2 | `application.yml` — tambah `app.jwt.*` | — |
| 3 | `V2__create_refresh_token_table.sql` | — |
| 4 | `Role.java`, `Jenjang.java` | — |
| 5 | `JwtProperties.java` | Step 1 |
| 6 | `UserEntity.java`, `RefreshTokenEntity.java` | Step 4 |
| 7 | `UserRepository.java`, `RefreshTokenRepository.java` | Step 6 |
| 8 | `InvalidCredentialsException.java`, `InvalidTokenException.java` | — |
| 9 | `UserPrincipal.java` | Step 6 |
| 10 | `CustomUserDetailsService.java` | Step 7, 9 |
| 11 | `JwtService.java` | Step 5, 9 |
| 12 | DTOs: `LoginRequest`, `LoginResponse`, `UserInfo`, `RefreshRequest`, `TokenResponse` | — |
| 13 | `AuthEntryPoint.java`, `CustomAccessDeniedHandler.java` | existing ApiResponse, Meta |
| 14 | `JwtAuthenticationFilter.java` | Step 10, 11 |
| 15 | `SecurityConfig.java` | Step 5, 13, 14 |
| 16 | `AuthService.java` | Step 7, 10, 11, 12 |
| 17 | `AuthController.java` | Step 16 |
| 18 | `GlobalExceptionHandler.java` — EDIT: tambah handler | Step 8 |
| 19 | Tests | All |

---

## 11. Implementation Notes

1. **Jackson 3.x**: Semua import ObjectMapper harus dari `tools.jackson.databind`, BUKAN `com.fasterxml.jackson.databind`
2. **Hibernate PG Enum**: Gunakan `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`. Jika error saat validate, tambah `columnDefinition = "\"Role\""` di `@Column`
3. **Tabel "User"**: Nama `User` adalah reserved word PostgreSQL — wajib pakai `@Table(name = "\"User\"")`
4. **Logout endpoint**: Public (di bawah `/api/auth/**` permitAll) — refresh token sendiri sudah jadi proof of ownership. Memaksa access token valid untuk logout akan memblokir user yang access token-nya sudah expired
5. **`@EnableMethodSecurity`**: Menggantikan `@EnableGlobalMethodSecurity` yang deprecated. Otomatis mengaktifkan `@PreAuthorize`
6. **RoleHierarchy bean**: Otomatis digunakan oleh authorization system Spring Security 6.x+ saat didaftarkan sebagai bean
7. **SiakadApplicationTests**: Setelah Spring Security masuk classpath, test `contextLoads()` mungkin fail. Tambah `@MockBean` untuk security beans atau buat test profile

---

## 12. Verification Checklist

1. **Compile**: `./mvnw compile` — pastikan zero error
2. **Flyway migration**: Start app, cek tabel `RefreshToken` terbuat dan `flyway_schema_history` mencatat V2
3. **Login flow**: `POST /api/auth/login` dengan kredensial valid → 200 + accessToken + refreshToken
4. **Invalid login**: Kirim password salah → 401 + `"Username atau password salah"`
5. **Protected endpoint**: `GET /actuator/health` tanpa token → 200 (public). Request ke endpoint lain tanpa token → 401
6. **Token validation**: Akses endpoint protected dengan `Authorization: Bearer <accessToken>` → 200
7. **Token expired**: Tunggu 15 menit (atau set expiry pendek untuk testing) → 401 `"Token telah kedaluwarsa"`
8. **Refresh**: `POST /api/auth/refresh` dengan refreshToken → 200 + token baru. Cek token lama di DB sudah `revoked=true`
9. **RBAC**: Login sebagai Siswa, akses endpoint `@PreAuthorize("hasRole('Admin')")` → 403
10. **Logout**: `POST /api/auth/logout` → 200. Coba refresh dengan token yang sama → 401

---

## Critical Files Reference

| File | Path |
|------|------|
| POM | `pom.xml` |
| App config | `src/main/resources/application.yml` |
| ApiResponse | `src/main/java/com/siakad/common/response/ApiResponse.java` |
| Meta | `src/main/java/com/siakad/common/response/Meta.java` |
| GlobalExceptionHandler | `src/main/java/com/siakad/common/exception/GlobalExceptionHandler.java` |
| DB Schema | `docker/initdb/01-schema.sql` |

---

## 13. Catatan Implementasi Aktual (Addendum — diverifikasi 2026-08-30)

Semua komponen pada dokumen ini sudah diimplementasi dan diverifikasi (compile zero-error, 24 test hijau, dan
runtime end-to-end terhadap PostgreSQL via Docker). Deviasi/penyesuaian dari spesifikasi di atas — wajib dibaca
karena mengubah API Spring Security yang dipakai:

1. **Spring Security versi project ini adalah 7.1.1** (bukan 6.x), sehingga **tidak ada constructor tanpa argumen**:
   - `DaoAuthenticationProvider`: wajib `new DaoAuthenticationProvider(userDetailsService)` — `setUserDetailsService(...)` sudah dihapus di 7.x.
   - `RoleHierarchyImpl`: wajib static factory `RoleHierarchyImpl.fromHierarchy("ROLE_KSatu > ROLE_Admin > ROLE_Guru > ROLE_Siswa")` — constructor no-arg & `setHierarchy(...)` sudah dihapus di 7.x.
2. **`JacksonConfig` (baru, `com.siakad.common.config`)** — bean `jsonMapper` (`@Qualifier("jsonMapper")`, Jackson 3.x) dipindah dari `SecurityConfig` ke class ini. Jika bean tetap di `SecurityConfig`, terjadi **circular dependency**: `securityConfig` → `jwtAuthenticationFilter` → `jsonMapper` (definisi di `securityConfig`).
3. **Default `app.jwt.secret`** dulu `PLACEHOLDER_GANTI_DI_PRODUCTION_MIN_64_CHARS_BASE64` (mengandung `_`, bukan base64 valid) → membuat boot gagal dengan `io.jsonwebtoken.io.DecodingException`. Diubah menjadi base64 valid 64-char (decode → 48 byte) agar aplikasi bisa boot di dev. **Tetap wajib diganti via env `JWT_SECRET` di production.**
4. **403 untuk `@PreAuthorize` ditangani di `GlobalExceptionHandler`**, bukan di `CustomAccessDeniedHandler`. Sebab: denial *method-level* dilempar di dalam DispatcherServlet (bukan filter chain), jadi `AccessDeniedHandler` filter tidak terpanggil dan exception sampai ke `@RestControllerAdvice`. Tanpa handler `AccessDeniedException` ini, denials berubah menjadi 500. `CustomAccessDeniedHandler` tetap dipakai untuk denial level *filter*/URL.
5. **Verifikasi runtime RBAC** (hierarchy KSatu > Admin > Guru > Siswa) terkonfirmasi end-to-end: KSatu → akses semua; Admin → semua; Guru → Siswa+Guru tapi 403 di Admin; Siswa → hanya Siswa, 403 di Guru & Admin; SpEL jenjang (`#principal.jenjang.name()` == 'SMA') berfungsi.
6. **Refresh-token rotation** terverifikasi: token lama di-revoke di DB dan pemakaian ulang → 401 `"Refresh token tidak valid"`. Logout → refresh setelahnya → 401.
7. Tabel `RefreshToken` dibuat oleh Flyway V2; `flyway_schema_history` mencatat `version=2`. Data user test yang dipakai untuk verifikasi sudah dihapus kembali setelah pengujian.
