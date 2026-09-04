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
└── db/migration/              # skrip migrasi Flyway (lihat catatan skema di bawah)

docker/
└── initdb/01-schema.sql       # skema database awal (auto-dijalankan Postgres saat volume baru dibuat)
```

Fitur yang sudah diimplementasikan: autentikasi JWT, manajemen siswa (CRUD + opsi, hasil pencarian
menyertakan nama kelas dari periode aktif), manajemen periode (CRUD + opsi), opsi kelas (scoped by
jenjang), manajemen kelas grup (CRUD + opsi), manajemen guru (CRUD + opsi, keunikan NIP per jenjang),
manajemen kelas siswa/enrollment (CRUD + batch insert), opsi jenis pembayaran, manajemen deposito
(CRUD, keunikan `siswaId` + `jenisPembayaranId`), dan Swagger/OpenAPI documentation.

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
- `GET /api/siswa/options` — daftar opsi siswa (untuk dropdown/select), dibatasi jenjang dari session
- `POST /api/siswa` — buat siswa baru
- `PUT /api/siswa/{id}` — update data siswa
- `DELETE /api/siswa/{id}` — hapus siswa

### Periode (`/api/periode`)

- `GET /api/periode` — daftar periode (dengan pagination, filter by nama/status, dibatasi jenjang dari session)
- `GET /api/periode/{id}` — detail periode berdasarkan ID
- `GET /api/periode/options` — daftar opsi periode (untuk dropdown/select)
- `POST /api/periode` — buat periode baru
- `PUT /api/periode/{id}` — update data periode
- `DELETE /api/periode/{id}` — hapus periode

### Guru (`/api/guru`)

- `GET /api/guru` — daftar guru (dengan pagination, filter by nama/nip/status, dibatasi jenjang dari session)
- `GET /api/guru/{id}` — detail guru berdasarkan ID
- `GET /api/guru/options` — daftar opsi guru (untuk dropdown/select), dibatasi jenjang dari session
- `POST /api/guru` — buat guru baru (validasi keunikan NIP per jenjang)
- `PUT /api/guru/{id}` — update data guru
- `DELETE /api/guru/{id}` — hapus guru

### Kelas (`/api/kelas`)

- `GET /api/kelas/options` — daftar opsi kelas (untuk dropdown/select), dibatasi jenjang dari session

### Kelas Grup (`/api/kelas-grup`)

- `GET /api/kelas-grup` — daftar kelas grup (dengan pagination, filter by nama/periodeId, dibatasi jenjang dari session lewat `kelasId`)
- `GET /api/kelas-grup/{id}` — detail kelas grup berdasarkan ID
- `GET /api/kelas-grup/options` — daftar opsi kelas grup (untuk dropdown/select), dibatasi jenjang dari session
- `POST /api/kelas-grup` — buat kelas grup baru (validasi referensi `kelasId`/`periodeId`/`waliKelasId` ke jenjang session)
- `PUT /api/kelas-grup/{id}` — update data kelas grup
- `DELETE /api/kelas-grup/{id}` — hapus kelas grup

### Kelas Siswa (`/api/kelas-siswa`)

Merepresentasikan pendaftaran (enrollment) seorang siswa ke sebuah kelas grup pada suatu periode,
lengkap dengan SPP per siswa.

- `GET /api/kelas-siswa` — daftar kelas siswa (dengan pagination, filter by `kelasGrupId`/`siswaId`/`periodeId`, dibatasi jenjang dari session)
- `GET /api/kelas-siswa/{id}` — detail kelas siswa berdasarkan ID
- `POST /api/kelas-siswa` — daftarkan satu siswa ke satu kelas grup (validasi referensi `siswaId`/`kelasGrupId` ke jenjang session, serta aturan satu siswa hanya boleh terdaftar di satu kelas grup per periode)
- `POST /api/kelas-siswa/batch` — daftarkan banyak siswa sekaligus ke satu kelas grup (`kelasGrupId` + list `siswaId`; `spp` diambil dari `KelasGrup.defaultSpp`; all-or-nothing — jika satu siswa gagal validasi, seluruh batch dibatalkan)
- `PUT /api/kelas-siswa/{id}` — update data kelas siswa (mis. pindah kelas grup, ubah SPP/potongan)
- `DELETE /api/kelas-siswa/{id}` — hapus kelas siswa

### Jenis Pembayaran (`/api/jenis-pembayaran`)

- `GET /api/jenis-pembayaran/options` — daftar opsi jenis pembayaran (untuk dropdown/select)

### Deposito (`/api/deposito`)

- `GET /api/deposito` — daftar deposito milik satu siswa (dengan pagination, wajib `siswaId`)
- `GET /api/deposito/{id}` — detail deposito berdasarkan ID
- `POST /api/deposito` — buat deposito baru (keunikan `siswaId` + `jenisPembayaranId`)
- `PUT /api/deposito/{id}` — update data deposito
- `DELETE /api/deposito/{id}` — hapus deposito

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
  dengan penamaan `V<n>__deskripsi.sql`. Migrasi yang sudah ada:
  - `V2__create_refresh_token_table.sql` — tabel `RefreshToken` untuk JWT refresh flow
  - `V3__seed_default_admin_user.sql` — seed user admin default
  - `V4__make_kelasgrup_walikelasid_nullable.sql` — `KelasGrup.waliKelasId` jadi nullable
  - `V5__drop_kelasgrup_walikelasid_periodeid_unique.sql` — drop unique constraint lama pada `(waliKelasId, periodeId)`
  - `V6__change_guru_nip_unique_to_per_jenjang.sql` — keunikan NIP guru jadi per jenjang (bukan global)
  - `V7__drop_trigger_set_timestamp_on_update.sql` — drop trigger auto-update `updatedAt` (digantikan `@UpdateTimestamp` di layer aplikasi)
  - `V8__add_kelassiswa_siswaid_kelasgrupid_unique.sql` — unique constraint pada `KelasSiswa("siswaId", "kelasGrupId")`
  - `V9__add_deposito_createdat_and_unique_constraint.sql` — kolom `createdAt` pada `Deposito` + unique constraint `("siswaId", "jenisPembayaranId")`

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
