## Struktur Response Standar

Pola envelope yang paling umum:

```json
{
  "success": true,
  "message": "Data berhasil diambil",
  "data": { ... },
  "meta": {
    "timestamp": "2026-08-29T10:00:00Z",
    "page": 1,
    "totalPages": 5
  }
}
```

Untuk error:

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

## Prinsip Utama

**1. Konsisten di semua endpoint** — jangan ada endpoint yang return array langsung, sementara endpoint lain pakai wrapper. Semua harus punya bentuk yang sama.

**2. HTTP status code tetap dipakai dengan benar** — jangan selalu return 200 lalu taruh error di body. Gunakan:

- 200 OK, 201 Created, 204 No Content
- 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 422 Unprocessable Entity
- 500 Internal Server Error

**3. Pisahkan `data` dari metadata** — jangan campur field pagination/info dengan data aktual.

**4. Format error yang bisa di-parse frontend** — sertakan field spesifik yang error (untuk validasi), kode error yang konsisten (bukan cuma pesan teks), dan trace ID untuk debugging.

**5. Naming convention konsisten** — pilih camelCase atau snake_case, jangan campur.

Berikut format response pagination yang umum dipakai, plus rekomendasi mana yang paling standar.

## Format Umum (Rekomendasi Utama)

```json
{
  "success": true,
  "message": "Data berhasil diambil",
  "data": [
    { "id": 1, "name": "Item A" },
    { "id": 2, "name": "Item B" }
  ],
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

Field yang idealnya selalu ada:

- **page** — halaman saat ini (mulai dari 0 atau 1, pilih salah satu dan konsisten)
- **size** — jumlah item per halaman
- **totalElements** — total data keseluruhan (bukan cuma yang di halaman ini)
- **totalPages** — total halaman
- **hasNext / hasPrevious** — biar frontend gak perlu hitung manual

## Variasi Lain yang Sering Dipakai

**Gaya "meta" (dipakai Laravel, banyak REST API):**

```json
{
  "data": [...],
  "meta": {
    "current_page": 1,
    "per_page": 10,
    "total": 97,
    "last_page": 10
  },
  "links": {
    "first": "/api/users?page=1",
    "last": "/api/users?page=10",
    "next": "/api/users?page=2",
    "prev": null
  }
}
```

**Cursor-based pagination** (lebih cocok untuk data real-time/infinite scroll, dipakai Twitter/Facebook API):

```json
{
  "data": [...],
  "pagination": {
    "nextCursor": "eyJpZCI6MTAwfQ==",
    "hasMore": true
  }
}
```

Cocok kalau data sering berubah/insert baru, karena offset-based pagination bisa "geser" hasil saat data baru masuk di tengah proses paging.
