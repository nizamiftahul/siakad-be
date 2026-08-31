# SIAKAD-BE

Backend service untuk **Sistem Informasi Akademik (SIAKAD)**.

Stack teknologi:

- **Spring Boot 4.1.1** (Java 25)
- **Spring Data JPA / Hibernate**
- **Flyway** untuk migrasi database
- **PostgreSQL**
- **Maven** sebagai build tool
- Lombok, Bean Validation, Spring Actuator

---

## Struktur Proyek

Struktur package mengikuti pola **package-by-feature**: setiap fitur punya package sendiri berisi controller, service, repository, entity, dan DTO-nya; kelas lintas-fitur (error handling, dsb.) berada di `common`.

```
src/main/java/com/siakad/
├── SiakadApplication.java     # Entry point
├── <fitur>/                   # mis. guru/, siswa/, kelas/ — controller, service,
│                               # repository, entity, dan DTO untuk fitur tsb dikelompokkan di sini
└── common/
    ├── exception/              # error handling lintas-fitur (GlobalExceptionHandler)
    └── response/               # envelope response standar (ApiResponse, Meta, Pagination, FieldError)

src/main/resources/
├── application.yml            # konfigurasi aplikasi
└── db/migration/              # skrip migrasi Flyway (saat ini kosong, lihat catatan skema di bawah)

docker/
└── initdb/01-schema.sql       # skema database awal (auto-dijalankan Postgres saat volume baru dibuat)
```

Fitur yang sudah diimplementasikan: autentikasi JWT, manajemen siswa (CRUD), dan Swagger/OpenAPI documentation.

---

## Cara Menjalankan

### 1. Jalankan PostgreSQL (Docker)

```bash
docker compose up -d
```

Atau jika PostgreSQL sudah berjalan di mesin Anda, sesuaikan kredensial di
`src/main/resources/application.yml` (atau lewat env):

| Variabel      | Default value                             |
| ------------- | ----------------------------------------- |
| `DB_URL`      | `jdbc:postgresql://localhost:5432/siakad` |
| `DB_USERNAME` | `siakad`                                  |
| `DB_PASSWORD` | `siakad`                                  |
| `SERVER_PORT` | `8080`                                    |

### 2. Build & jalankan aplikasi

```bash
# Compile + jalankan test
mvn clean test

# Jalankan aplikasi
mvn spring-boot:run
```

### 3. Verifikasi

- Health check: http://localhost:8080/actuator/health
- API Docs: http://localhost:8080/swagger-ui.html

---

## Endpoint API

### Autentikasi (`/api/auth`)

- `POST /api/auth/login` — login dengan email dan password, mengembalikan access token & refresh token
- `POST /api/auth/refresh` — refresh access token menggunakan refresh token
- `POST /api/auth/logout` — logout dan invalidate refresh token

### Siswa (`/api/siswa`)

- `GET /api/siswa` — daftar siswa (dengan pagination, filter by jenjang dari session)
- `GET /api/siswa/{id}` — detail siswa berdasarkan ID
- `POST /api/siswa` — buat siswa baru
- `PUT /api/siswa/{id}` — update data siswa
- `DELETE /api/siswa/{id}` — hapus siswa

### Periode (`/api/periode`)

- `GET /api/periode` — daftar periode (dengan pagination, filter by nama/status, dibatasi jenjang dari session)
- `GET /api/periode/{id}` — detail periode berdasarkan ID
- `POST /api/periode` — buat periode baru
- `PUT /api/periode/{id}` — update data periode
- `DELETE /api/periode/{id}` — hapus periode

---

## Skema Database

Skema awal database (32 tabel + 19 view legacy SIAKAD: `Siswa`, `Guru`, `Kelas`, `Pembayaran`, dll.)
di-restore dari `docker/initdb/01-schema.sql`, yang otomatis dijalankan Postgres lewat mekanisme
`/docker-entrypoint-initdb.d/` **hanya saat volume database dibuat pertama kali** (volume kosong).

- Untuk memuat ulang skema dari awal (mis. setelah mengubah `docker/initdb/01-schema.sql`):
  ```bash
  docker compose down -v
  docker compose up -d
  ```
  ⚠️ Perintah ini menghapus seluruh data di volume `siakad_pgdata`.
- Flyway tetap aktif (`spring.jpa.hibernate.ddl-auto: validate`) untuk mengelola perubahan skema
  _setelah_ skema awal ini — tambahkan migrasi baru di `src/main/resources/db/migration/`
  dengan penamaan `V<n>__deskripsi.sql`. Saat ini belum ada migrasi (direktori kosong) karena
  semua tabel sudah dibuat oleh `01-schema.sql`.

---

## Endpoint API

Belum ada endpoint REST aktif — akan ditambahkan per fitur mengikuti struktur package-by-feature
di atas.

---

## Struktur Response Standar

Seluruh endpoint REST wajib mengembalikan `ResponseEntity<ApiResponse<T>>` dari
`com.siakad.common.response.ApiResponse`, bukan entity/DTO mentah, supaya bentuk response konsisten
di semua endpoint.

**Sukses:**

```json
{
  "success": true,
  "message": "Data berhasil diambil",
  "data": { "id": 1, "name": "Item A" },
  "meta": { "timestamp": "2026-08-29T10:00:00Z" }
}
```

**Sukses dengan pagination** (list data), memakai `Pagination.from(Page<?>)`:

```json
{
  "success": true,
  "message": "Data berhasil diambil",
  "data": [{ "id": 1 }, { "id": 2 }],
  "meta": { "timestamp": "2026-08-29T10:00:00Z" },
  "pagination": {
    "page": 1,
    "size": 10,
    "totalElements": 97,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

> `page` bersifat 1-based ke arah client, sedangkan `Page` milik Spring Data 0-based — konversi
> hanya dilakukan di `Pagination.from(...)`. Controller yang menerima `page` dari client wajib
> mengurangi 1 sebelum membangun `PageRequest.of(page - 1, size)`.

**Error** — dibangun otomatis oleh `GlobalExceptionHandler` (`com.siakad.common.exception`), controller
tidak perlu menanganinya manual:

```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": [{ "field": "email", "message": "Format email tidak valid" }],
  "meta": {
    "timestamp": "2026-08-29T10:00:00Z",
    "path": "/api/users",
    "traceId": "abc-123"
  }
}
```

Status HTTP yang dipetakan: 404 (`ResourceNotFoundException`), 400 (`MethodArgumentNotValidException`,
`ConstraintViolationException`), 500 (fallback generik — pesan error tidak pernah bocor ke client,
detail asli dicatat di log server bersama `traceId`).

**Contoh penggunaan di controller:**

```java
return ResponseEntity.ok(ApiResponse.success("Data berhasil diambil", dto));
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Data berhasil dibuat", created));
return ResponseEntity.noContent().build(); // 204 tidak boleh punya body — jangan dibungkus ApiResponse
return ResponseEntity.ok(ApiResponse.paginatedSuccess("Data berhasil diambil", page.getContent(), Pagination.from(page)));
```
