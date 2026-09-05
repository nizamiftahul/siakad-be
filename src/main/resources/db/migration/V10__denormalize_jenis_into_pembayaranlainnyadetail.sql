ALTER TABLE public."PembayaranLainnyaDetail"
    ADD COLUMN "jenis" character varying(45),
    ADD COLUMN "jenisPembayaranId" integer;

UPDATE public."PembayaranLainnyaDetail" t
SET "jenis" = pl.jenis,
    "jenisPembayaranId" = pl."jenisPembayaranId"
FROM public."PembayaranLainnya" pl
WHERE pl.id = t."pembayaranLainnyaId";

ALTER TABLE public."PembayaranLainnyaDetail"
    ALTER COLUMN "jenis" SET NOT NULL;

ALTER TABLE public."PembayaranLainnyaDetail"
    ADD CONSTRAINT "PembayaranLainnyaDetail_jenisPembayaranId_fkey"
        FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id)
        ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE public."PembayaranLainnyaDetail"
    ALTER COLUMN "pembayaranLainnyaId" DROP NOT NULL;
