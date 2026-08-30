# Code Review: Siswa CRUD (fitur `com.siakad.siswa`)

**Tanggal review:** 2026-08-30
**Scope:** working tree branch `master` — `src/main/java/com/siakad/siswa/**`, `com/siakad/common/enums/{JK,SiswaStatus}.java`, `com/siakad/common/exception/{DuplicateResourceException,GlobalExceptionHandler}.java`, `src/test/java/com/siakad/siswa/**`

Dokumen ini berisi temuan review beserta perbaikan konkret. Setiap temuan mandiri: berisi lokasi, skenario gagal, dan patch yang bisa langsung diterapkan tanpa keputusan arsitektural tambahan.

**Urutan pengerjaan yang disarankan:** #2 → #4 → #1 → #7 → #6 → #5 → #3 → #8.
Temuan #2 kemungkinan besar membuat endpoint list utama gagal di PostgreSQL nyata, jadi kerjakan pertama.

---

## Ringkasan

| # | Severity | Lokasi | Masalah |
|---|----------|--------|---------|
| 1 | HIGH | `SiswaController.java:53` | `page`/`size` tanpa validasi → `page=0` menghasilkan 500, `size` tak terbatas |
| 2 | HIGH | `SiswaRepository.java:14` | `:status IS NULL` pada param enum PostgreSQL → query list default error di DB |
| 3 | MEDIUM | `SiswaRepository.java:14` | Query paging tanpa `ORDER BY` → urutan halaman tidak deterministik |
| 4 | MEDIUM | `SiswaService.java:62` | `updatedAt` tidak pernah di-set → selalu NULL |
| 5 | MEDIUM | `GlobalExceptionHandler.java:68` | Handler `DataIntegrityViolationException` hardcode pesan FK dan tidak logging |
| 6 | MEDIUM | `SiswaRequest.java:17` | Tidak ada `@Size` untuk kolom `varchar(45)` |
| 7 | MEDIUM | `SiswaController.java:57` | Enum/angka query param salah format → 500, bukan 400 |
| 8 | LOW | `SiswaService.java:59` | `update()` memperlakukan `status` beda dari field opsional lain |

---

## 1. HIGH — `page`/`size` tidak divalidasi

**Lokasi:** `src/main/java/com/siakad/siswa/controller/SiswaController.java:53`

**Masalah:** `PageRequest.of(page - 1, size)` melempar `IllegalArgumentException` jika `page <= 0` atau `size <= 0`. Banyak client mengasumsikan paging 0-based, jadi `GET /api/siswa?page=0` jatuh ke handler `Exception` generik dan mengembalikan **500 "Terjadi kesalahan pada server"** padahal seharusnya 400. Selain itu `size` tidak dibatasi — `?size=1000000` memuat seluruh tabel ke memori.

**Perbaikan:** tambahkan constraint pada parameter. `ConstraintViolationException` sudah dipetakan ke 400 di `GlobalExceptionHandler`, jadi cukup aktifkan `@Validated` di level class.

```java
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/siswa")
@RequiredArgsConstructor
@Validated                       // <-- tambah
public class SiswaController {

    // ...
    public ResponseEntity<ApiResponse<List<SiswaResponse>>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page minimal 1") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size minimal 1")
            @Max(value = 100, message = "size maksimal 100") int size,
            // ...
```

**Verifikasi:** `GET /api/siswa?page=0` → 400; `GET /api/siswa?size=1000` → 400.

---

## 2. HIGH — `:status IS NULL` pada param enum PostgreSQL

**Lokasi:** `src/main/java/com/siakad/siswa/repository/SiswaRepository.java:14`

**Masalah:** `status` dan `jenjang` dipetakan ke named enum PostgreSQL (`@JdbcTypeCode(SqlTypes.NAMED_ENUM)` di `SiswaEntity`). Saat nilainya null, Hibernate melakukan `setNull(..., Types.OTHER)` tanpa OID spesifik, dan PostgreSQL tidak bisa menentukan tipe predikat `$n IS NULL` yang berdiri sendiri → error `could not determine data type of parameter $n`.

**Skenario gagal:** `GET /api/siswa` tanpa filter `status`/`jenjang` — yaitu listing default — gagal di level database.

**Kenapa lolos test:** kedua test class men-mock `SiswaRepository`, jadi query JPQL asli tidak pernah dieksekusi.

**Perbaikan (opsi yang direkomendasikan):** ganti `@Query` dengan JPA `Specification` sehingga predikat enum hanya ditambahkan ketika nilainya tidak null.

1. Ubah interface repository:

```java
public interface SiswaRepository extends JpaRepository<SiswaEntity, Integer>,
        JpaSpecificationExecutor<SiswaEntity> {

    boolean existsByNisAndJenjang(String nis, Jenjang jenjang);

    boolean existsByNisAndJenjangAndIdNot(String nis, Jenjang jenjang, Integer id);
}
```

Hapus method `search(...)` beserta `@Query`-nya.

2. Buat `src/main/java/com/siakad/siswa/repository/SiswaSpecifications.java`:

```java
package com.siakad.siswa.repository;

import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import com.siakad.siswa.entity.SiswaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SiswaSpecifications {

    private SiswaSpecifications() {}

    public static Specification<SiswaEntity> search(String nama, String nis,
                                                    SiswaStatus status, Jenjang jenjang) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (nama != null && !nama.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nama")),
                        "%" + nama.toLowerCase() + "%"));
            }
            if (nis != null && !nis.isBlank()) {
                predicates.add(cb.equal(root.get("nis"), nis));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (jenjang != null) {
                predicates.add(cb.equal(root.get("jenjang"), jenjang));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

3. Sesuaikan `SiswaService.list(...)`:

```java
return siswaRepository.findAll(
        SiswaSpecifications.search(nama, nis, status, jenjang), pageable)
        .map(SiswaResponse::from);
```

**Verifikasi:** wajib ada integration test yang menjalankan query terhadap PostgreSQL asli (Testcontainers atau DB dev), bukan repository yang di-mock. Minimal satu test: list tanpa filter, dan satu test: list dengan filter `status=Aktif&jenjang=SD`.

---

## 3. MEDIUM — Query paging tanpa `ORDER BY`

**Lokasi:** `src/main/java/com/siakad/siswa/repository/SiswaRepository.java:14`

**Masalah:** PostgreSQL tidak menjamin urutan baris tanpa `ORDER BY`. Controller hanya mengirim `PageRequest.of(page - 1, size)` tanpa `Sort`, sehingga menelusuri `?page=1` lalu `?page=2` bisa menampilkan baris yang sama dua kali atau melewatkan baris.

**Perbaikan:** pakai sort default deterministik di controller (digabung dengan perbaikan #1):

```java
Page<SiswaResponse> result = siswaService.list(nama, nis, status, jenjang,
        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
```

---

## 4. MEDIUM — `updatedAt` tidak pernah di-set

**Lokasi:** `src/main/java/com/siakad/siswa/service/SiswaService.java:62` (dan `:35` untuk create)

**Masalah:** kolom `updatedAt` tidak punya default di DB maupun trigger — pada stack Prisma lama nilainya diisi di application layer. `create()` hanya mengisi `createdAt`; `update()` mengisi `updatedBy` tapi tidak `updatedAt`. Akibatnya setelah PUT apa pun, `SiswaResponse.updatedAt` tetap null sementara `updatedBy` berubah: client tidak bisa tahu kapan record terakhir berubah, dan konsumer sync/audit yang membaca `updatedAt` rusak.

**Perbaikan (opsi A, minimal):** set eksplisit di kedua jalur.

```java
// create()
entity.setCreatedAt(OffsetDateTime.now());
entity.setUpdatedAt(OffsetDateTime.now());

// update()
entity.setUpdatedAt(OffsetDateTime.now());
entity.setUpdatedBy(currentUsername());
```

**Opsi B (lebih tahan lupa):** anotasi `@CreationTimestamp` / `@UpdateTimestamp` di `SiswaEntity` dan hapus set manual `createdAt`. Pilih salah satu, jangan campur.

---

## 5. MEDIUM — Handler `DataIntegrityViolationException` hardcode pesan FK dan tidak logging

**Lokasi:** `src/main/java/com/siakad/common/exception/GlobalExceptionHandler.java:68`

**Masalah:** semua pelanggaran integritas dijawab 409 `"Data tidak bisa diproses karena masih direferensikan data lain"`. Kasus salah yang konkret:

- Nilai melebihi `varchar(45)` (lihat #6) dilaporkan sebagai "masih direferensikan data lain" — menyesatkan.
- Dua POST bersamaan dengan `nis`+`jenjang` yang sama lolos pre-check `existsByNisAndJenjang`, salah satunya kena unique constraint `Siswa_nis_jenjang`, tetapi user melihat pesan FK alih-alih "NIS sudah terdaftar".

Selain itu tidak ada logging sama sekali, sehingga penyebab asli tidak terlihat di production.

**Perbaikan:**

```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                      HttpServletRequest request) {
    Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
    log.error("Data integrity violation pada {} (traceId={})", request.getRequestURI(), meta.traceId(), ex);

    String message = "Data tidak bisa diproses karena masih direferensikan data lain";
    if (ex.getCause() instanceof ConstraintViolationException cve) {   // org.hibernate.exception
        String constraint = cve.getConstraintName();
        if (constraint != null && constraint.contains("nis_jenjang")) {
            message = "NIS sudah terdaftar pada jenjang tersebut";
        }
    }
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(message, null, meta));
}
```

Tambahkan `@Slf4j` pada class jika belum ada. Perhatikan import: `org.hibernate.exception.ConstraintViolationException`, bukan `jakarta.validation.ConstraintViolationException` yang sudah dipakai di file yang sama — gunakan nama fully-qualified jika bentrok.

---

## 6. MEDIUM — Tidak ada `@Size` untuk kolom `varchar(45)`

**Lokasi:** `src/main/java/com/siakad/siswa/dto/SiswaRequest.java:17`

**Masalah:** `nama`, `nis`, `nisn`, `email`, `telepon`, `asalSekolah`, `namaAyah`, seluruh kolom `pendidikan*`/`nama*`/`pekerjaan*`, dan `tmptLahir` bertipe `character varying(45)` di database. Mengirim `nama` sepanjang 60 karakter lolos bean validation, sampai ke DB, lalu kembali sebagai 409 dengan pesan FK yang menyesatkan (#5) — bukan 400 dengan error per-field.

**Perbaikan:** tambahkan `@Size(max = 45)` pada semua field yang di-back oleh `varchar(45)`. Contoh:

```java
@NotBlank(message = "Nama wajib diisi")
@Size(max = 45, message = "Nama maksimal 45 karakter") String nama,

@Size(max = 45, message = "Email maksimal 45 karakter") String email,

@NotBlank(message = "NIS wajib diisi")
@Size(max = 45, message = "NIS maksimal 45 karakter") String nis,
```

Cek ulang lebar kolom sebenarnya di schema untuk `description`, `alamat`, dan `domisili` sebelum memberi batas — jangan asumsikan 45 untuk kolom teks panjang.

---

## 7. MEDIUM — Enum/angka query param salah format menghasilkan 500

**Lokasi:** `src/main/java/com/siakad/siswa/controller/SiswaController.java:57`

**Masalah:** `?status=AKTIF` atau `?jenjang=sd` (huruf besar/kecil salah — enum-nya `Aktif` dan `SD`), `?page=abc`, maupun `GET /api/siswa/abc` melempar `MethodArgumentTypeMismatchException`. Exception ini tidak punya handler dan jatuh ke handler `Exception` generik → 500.

**Perbaikan:** tambahkan handler di `GlobalExceptionHandler`:

```java
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
    Meta meta = Meta.of(request.getRequestURI(), TraceIdUtil.newTraceId());
    String message = "Nilai parameter '" + ex.getName() + "' tidak valid: " + ex.getValue();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message, null, meta));
}
```

**Verifikasi:** `GET /api/siswa?status=AKTIF` → 400 dengan nama parameter yang salah disebutkan.

---

## 8. LOW — `update()` memperlakukan `status` beda dari field opsional lain

**Lokasi:** `src/main/java/com/siakad/siswa/service/SiswaService.java:59`

**Masalah:** `status` yang null mempertahankan nilai lama, tetapi `isAlumni`, `email`, `alamat`, `namaAyah`, dan seterusnya yang null menghapus nilai tersimpan karena `toEntity` selalu assign tanpa cek null. Client yang melakukan PUT parsial tanpa `status` akan mempertahankan status, tapi mengosongkan data orang tua/wali.

**Perbaikan:** pilih satu semantik dan konsisten.

- **Opsi A (direkomendasikan, paling sederhana):** PUT bersemantik full-replace. Hapus cek null pada `status`, dan alih-alih itu pakai default `SiswaStatus.Aktif` bila null seperti pada `create()`. Dokumentasikan di Javadoc `SiswaRequest` bahwa field yang dikosongkan akan menghapus nilai.
- **Opsi B:** buat endpoint `PATCH` terpisah dengan semantik null-means-keep untuk seluruh field, dan biarkan `PUT` full-replace.

Jangan biarkan campuran seperti sekarang.

---

## Catatan test

Kedua test class yang ada men-mock `SiswaRepository`, sehingga tidak ada satu pun test yang mengeksekusi query JPQL/Specification terhadap PostgreSQL. Temuan #2 dan #3 hanya bisa dicegah oleh integration test dengan database asli. Tambahkan minimal satu `@DataJpaTest` (atau Testcontainers) yang mencakup:

1. `list` tanpa filter (regression test untuk #2)
2. `list` dengan filter `status` dan `jenjang`
3. Paging halaman 1 dan 2 pada dataset >`size` baris, memastikan tidak ada baris yang terduplikasi (regression test untuk #3)
