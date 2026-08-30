# Code Review: Scoping Jenjang pada Modul Siswa

**Tanggal review:** 2026-08-30
**Scope:** working tree branch `master` — `src/main/java/com/siakad/siswa/{controller/SiswaController,service/SiswaService}.java`, beserta dampaknya ke `src/test/java/com/siakad/siswa/**`

Perubahan yang direview membuat `GET /api/siswa` mengambil `jenjang` dari session (`SecurityContextHolder`) alih-alih dari request param:

```java
// SiswaService.java — sebelum → sesudah
public Page<SiswaResponse> list(String nama, String nis, SiswaStatus status, Jenjang jenjang, Pageable pageable)
public Page<SiswaResponse> list(String nama, String nis, SiswaStatus status, Pageable pageable)
```

Arahnya benar — jenjang memang layak jadi batas isolasi tenant, bukan filter opsional yang dipilih client. Masalahnya, batas itu baru terpasang di satu jalur (`list`), sementara `getById`/`update`/`delete` dan seluruh jalur tulis masih memakai nilai dari luar. Setengah batas justru lebih berbahaya daripada tidak ada batas, karena memberi kesan endpoint sudah aman.

Setiap temuan di bawah mandiri: berisi lokasi, skenario gagal, dan patch yang bisa langsung diterapkan.

**Urutan pengerjaan yang disarankan:** #1 → #2 → #3 → #4 → #5 → #6.
Temuan #1 membuat `src/test` tidak compile, jadi kerjakan pertama supaya sisanya bisa diverifikasi.

---

## Ringkasan

| # | Severity | Lokasi | Masalah |
|---|----------|--------|---------|
| 1 | BLOCKER | `SiswaServiceTest.java:146`, `SiswaControllerTest.java:136` | Test masih memanggil `list()` versi 5 argumen → `src/test` tidak compile |
| 2 | HIGH | `SiswaService.java:69` | `findOrThrow` tanpa scoping → `getById`/`update`/`delete` bisa menyentuh jenjang lain |
| 3 | HIGH | `SiswaService.java:102` | `create`/`update` masih ambil `jenjang` dari request body → tulis lintas tenant |
| 4 | MEDIUM | `SiswaService.java:115` | `currentJenjang()` → 500 untuk anonymous, dan diam-diam mematikan filter jika jenjang null |
| 5 | LOW | `SiswaController.java:61` | Param `jenjang` dihapus tanpa jejak → client lama dapat 200 dengan data salah |
| 6 | LOW | `Role.java` / `SiswaService.java:47` | `KSatu` ikut terkunci ke satu jenjang, tanpa jalan keluar |

---

## 1. BLOCKER — Test masih memanggil `list()` versi 5 argumen

**Lokasi:** `src/test/java/com/siakad/siswa/service/SiswaServiceTest.java:143-146`, `src/test/java/com/siakad/siswa/controller/SiswaControllerTest.java:136-139`

**Masalah:** signature `list` sekarang 4 argumen, tapi kedua test masih mengoper 5. Akibatnya `src/test` gagal compile dan `mvn test` / `mvn verify` berhenti sebelum satu test pun dijalankan.

Di sisi service, stub-nya juga tidak lagi bisa terpenuhi tanpa autentikasi: `search(..., eq(Jenjang.SD), any())` sekarang menerima nilai dari `SecurityContextHolder`, bukan dari argumen. Tanpa principal terpasang, `currentJenjang()` malah melempar exception.

**Perbaikan — `SiswaServiceTest`:** pasang principal ber-jenjang sebelum memanggil `list`. Pola `UserEntity` → `UserPrincipal` → `UsernamePasswordAuthenticationToken` sudah dipakai di `createUsesAuthenticatedUsernameAsActor` (baris 89-100); ekstrak jadi helper supaya bisa dipakai ulang.

```java
private void authenticateAs(Jenjang jenjang) {
    UserEntity user = UserEntity.builder()
            .id(1)
            .username("admin")
            .name("Administrator")
            .role(Role.Admin)
            .jenjang(jenjang)
            .hashedPassword("hash")
            .build();
    UserPrincipal principal = new UserPrincipal(user);
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
}

@Test
void listDelegatesToRepositorySearch() {
    authenticateAs(Jenjang.SD);
    var page = new PageImpl<>(List.of(entity(1, "NIS001", Jenjang.SD)), PageRequest.of(0, 10), 1);
    when(siswaRepository.search(eq("Budi"), eq(null), eq(SiswaStatus.Aktif), eq(Jenjang.SD), any()))
            .thenReturn(page);

    var result = siswaService.list("Budi", null, SiswaStatus.Aktif, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).nis()).isEqualTo("NIS001");
}
```

`tearDown()` yang sudah ada (`SecurityContextHolder.clearContext()`) sudah menangani pembersihannya.

**Perbaikan — `SiswaControllerTest`:** cukup kurangi satu argumen di dua tempat.

```java
@Test
void listReturnsPaginatedEnvelope() {
    var page = new PageImpl<>(List.of(response()), PageRequest.of(0, 10), 1);
    when(siswaService.list(any(), any(), any(), any())).thenReturn(page);

    ResponseEntity<ApiResponse<List<SiswaResponse>>> response =
            controller.list(1, 10, null, null, null);
    ...
}
```

Setelah temuan #2 dan #3 diterapkan, test lain yang memanggil `create`/`update`/`getById` tanpa principal juga akan butuh `authenticateAs(...)` — helper di atas dipakai untuk itu.

---

## 2. HIGH — `findOrThrow` tanpa scoping, jalur detail dan mutasi tetap terbuka

**Lokasi:** `src/main/java/com/siakad/siswa/service/SiswaService.java:69`

**Masalah:** diff ini menetapkan jenjang sebagai batas isolasi untuk `list`, tapi `findOrThrow` masih `findById` polos:

```java
private SiswaEntity findOrThrow(Integer id) {
    return siswaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Siswa dengan id " + id + " tidak ditemukan"));
}
```

Admin SD yang tidak bisa **melihat** siswa SMA di daftar tetap bisa `GET /api/siswa/42`, mem-`PUT`, atau meng-`DELETE` id SMA mana pun. Karena `id` adalah integer berurutan, menemukan id milik jenjang lain hanya perlu menaikkan angka. Jadi filter di `list` hanya menyembunyikan data, tidak melindunginya.

**Perbaikan:** tambahkan query berscope di repository, lalu pakai di `findOrThrow` supaya seluruh jalur baca dan tulis terlindungi lewat satu titik.

```java
// SiswaRepository.java
Optional<SiswaEntity> findByIdAndJenjang(Integer id, Jenjang jenjang);
```

```java
// SiswaService.java
private SiswaEntity findOrThrow(Integer id) {
    return siswaRepository.findByIdAndJenjang(id, currentJenjang())
            .orElseThrow(() -> new ResourceNotFoundException("Siswa dengan id " + id + " tidak ditemukan"));
}
```

Pesan `ResourceNotFoundException` sengaja dipertahankan apa adanya: 404 untuk resource milik jenjang lain lebih baik daripada 403, karena 403 mengonfirmasi bahwa id tersebut ada.

---

## 3. HIGH — `create` dan `update` masih percaya `jenjang` dari request body

**Lokasi:** `src/main/java/com/siakad/siswa/service/SiswaService.java:102` (`toEntity`), pre-check NIS di `:29` dan `:53`

**Masalah:** pembacaan sudah session-scoped, tapi penulisan belum. `toEntity` menyalin `entity.setJenjang(r.jenjang())` langsung dari request, dan pengecekan duplikat NIS memakai `request.jenjang()`.

Skenario gagal: admin SD melakukan `POST /api/siswa` dengan `jenjang: SMA`. Record tersimpan ke tenant lain, lalu **hilang dari daftar admin itu sendiri** karena tersaring filter baru — data masuk ke sistem tanpa ada yang merasa membuatnya. Pengecekan duplikat NIS pun berjalan terhadap jenjang pilihan pengirim, sehingga tidak menjamin apa pun tentang jenjang aslinya. Pada `update`, field ini juga membuka jalan memindahkan siswa keluar dari jenjang sendiri.

**Perbaikan:** jadikan jenjang otoritatif dari session pada jalur tulis juga.

```java
public SiswaResponse create(SiswaRequest request) {
    Jenjang jenjang = currentJenjang();
    if (siswaRepository.existsByNisAndJenjang(request.nis(), jenjang)) {
        throw new DuplicateResourceException("NIS sudah terdaftar pada jenjang tersebut");
    }
    String actor = currentUsername();
    SiswaEntity entity = toEntity(request, new SiswaEntity());
    entity.setJenjang(jenjang);
    ...
}

public SiswaResponse update(Integer id, SiswaRequest request) {
    SiswaEntity entity = findOrThrow(id);            // sudah berscope setelah temuan #2
    Jenjang jenjang = entity.getJenjang();
    if (siswaRepository.existsByNisAndJenjangAndIdNot(request.nis(), jenjang, id)) {
        throw new DuplicateResourceException("NIS sudah terdaftar pada jenjang tersebut");
    }
    toEntity(request, entity);
    entity.setJenjang(jenjang);                      // abaikan jenjang dari body
    ...
}
```

Hapus juga `entity.setJenjang(r.jenjang())` dari `toEntity` supaya jenjang hanya punya satu sumber. Konsekuensinya `jenjang` di `SiswaRequest` menjadi field yang diabaikan — pilihannya: hapus dari DTO, atau tolak dengan 400 jika nilainya berbeda dari session. Menghapusnya lebih bersih, tapi itu perubahan kontrak API, jadi selaraskan dengan temuan #5.

---

## 4. MEDIUM — `currentJenjang()` gagal ke arah yang salah di kedua ujungnya

**Lokasi:** `src/main/java/com/siakad/siswa/service/SiswaService.java:115-121`

```java
private Jenjang currentJenjang() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
        return principal.getJenjang();
    }
    throw new IllegalStateException("Jenjang tidak ditemukan pada session");
}
```

**Masalah, dua arah berlawanan:**

1. **Anonymous → 500, bukan 401.** Untuk request tanpa autentikasi, Spring Security memasang `AnonymousAuthenticationToken` dengan principal berupa String `"anonymousUser"`. Jadi `auth != null` lolos, `instanceof` gagal, dan `IllegalStateException` jatuh ke handler `Exception` generik di `GlobalExceptionHandler.java:122` → **500 "Terjadi kesalahan pada server"**. Saat ini endpoint dilindungi security chain, jadi belum terlihat; tapi setiap `permitAll` atau test slice di masa depan akan melaporkan error server padahal masalahnya autentikasi.

2. **Jenjang null → filter mati diam-diam.** `UserPrincipal` yang valid tapi `jenjang`-nya null akan **return null**, dan `search(..., null, ...)` mengartikan null sebagai "tanpa filter" (`:jenjang IS NULL OR ...` di `SiswaRepository.java:19`). Hasilnya seluruh siswa lintas jenjang ikut terkirim. Ironisnya inilah kasus yang dijanjikan pesan errornya — "Jenjang tidak ditemukan pada session" — tapi justru tidak pernah terdeteksi.

**Perbaikan:** pakai `AccessDeniedException` (sudah dipetakan ke 403 di `GlobalExceptionHandler.java:111`) dan tutup kasus null secara eksplisit.

```java
import org.springframework.security.access.AccessDeniedException;

private Jenjang currentJenjang() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
        throw new AccessDeniedException("Akses ditolak: sesi tidak valid");
    }
    Jenjang jenjang = principal.getJenjang();
    if (jenjang == null) {
        throw new AccessDeniedException("Akun tidak memiliki jenjang");
    }
    return jenjang;
}
```

Perhatikan bedanya dengan `currentUsername()` di atasnya yang sengaja fallback ke `"system"` — itu benar untuk kolom audit, tapi salah untuk batas keamanan. Batas keamanan harus fail-closed.

---

## 5. LOW — Param `jenjang` dihapus tanpa jejak untuk client lama

**Lokasi:** `src/main/java/com/siakad/siswa/controller/SiswaController.java:61`

**Masalah:** Spring mengabaikan query param yang tidak dikenal. Client yang masih memanggil `GET /api/siswa?jenjang=SMA` menerima data SD dengan status **200** dan tanpa petunjuk apa pun bahwa filternya diabaikan — kegagalan yang paling sulit dilacak, karena tidak ada error di mana pun.

**Perbaikan:** perbarui anotasi OpenAPI pada endpoint list agar menyebutkan bahwa hasil otomatis dibatasi jenjang milik akun. Jika ada client eksternal yang masih aktif, tambahkan penolakan eksplisit selama masa transisi:

```java
@RequestParam(required = false) String jenjang  // deprecated
// lalu di body: jika jenjang != null → lempar IllegalArgumentException/400
// dengan pesan "Param jenjang tidak lagi didukung; hasil dibatasi jenjang akun"
```

Rilis satu-dua versi dengan penolakan ini, baru hapus paramnya sepenuhnya.

---

## 6. LOW — `KSatu` ikut terkunci ke satu jenjang

**Lokasi:** `src/main/java/com/siakad/common/enums/Role.java`, `src/main/java/com/siakad/siswa/service/SiswaService.java:47`

**Masalah:** `KSatu` adalah puncak hierarki peran (`KSatu > Admin > Guru > Siswa`), tapi setelah perubahan ini tidak punya jalan apa pun untuk melihat data lintas jenjang. Kalau memang disengaja, tidak masalah — hanya perlu tercatat. Kalau tidak, peran itu butuh pengecualian.

**Perbaikan (jika lintas jenjang memang diperlukan untuk `KSatu`):**

```java
private Jenjang currentJenjang() {
    // ... validasi principal seperti temuan #4
    if (principal.getRole() == Role.KSatu) {
        return null;   // null = tanpa filter di SiswaRepository.search
    }
    return jenjang;
}
```

Jika ditempuh, jangan pakai jalur `null` yang sama untuk `findOrThrow` dan jalur tulis — buat cabang eksplisit, supaya "sengaja lintas jenjang" tidak pernah tertukar dengan "jenjang tidak diketahui" seperti pada temuan #4.

---

## Catatan verifikasi

Review dilakukan atas diff working tree (range diff kosong). Temuan #1 diverifikasi langsung terhadap call site di `src/test`; temuan #2-#4 terhadap `SiswaService.java`, `SiswaRepository.java`, dan `GlobalExceptionHandler.java` pada revisi yang sama.
