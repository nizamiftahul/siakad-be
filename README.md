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
    └── exception/              # error handling lintas-fitur (Problem Details)

src/main/resources/
├── application.yml            # konfigurasi aplikasi
└── db/migration/              # skrip migrasi Flyway (saat ini kosong, lihat catatan skema di bawah)

docker/
└── initdb/01-schema.sql       # skema database awal (auto-dijalankan Postgres saat volume baru dibuat)
```

> Saat ini belum ada fitur REST aktif — package fitur akan ditambahkan seiring pengembangan
> (mis. `guru/`, `siswa/`, `kelas/`).

---

## Cara Menjalankan

### 1. Jalankan PostgreSQL (Docker)

```bash
docker compose up -d
```

Atau jika PostgreSQL sudah berjalan di mesin Anda, sesuaikan kredensial di
`src/main/resources/application.yml` (atau lewat env):

| Variabel      | Default value                       |
|---------------|-------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/siakad` |
| `DB_USERNAME` | `siakad`                            |
| `DB_PASSWORD` | `siakad`                            |
| `SERVER_PORT` | `8080`                              |

### 2. Build & jalankan aplikasi

```bash
# Compile + jalankan test
mvn clean test

# Jalankan aplikasi
mvn spring-boot:run
```

### 3. Verifikasi

- Health check: http://localhost:8080/actuator/health

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
  *setelah* skema awal ini — tambahkan migrasi baru di `src/main/resources/db/migration/`
  dengan penamaan `V<n>__deskripsi.sql`. Saat ini belum ada migrasi (direktori kosong) karena
  semua tabel sudah dibuat oleh `01-schema.sql`.

---

## Endpoint API

Belum ada endpoint REST aktif — akan ditambahkan per fitur mengikuti struktur package-by-feature
di atas.