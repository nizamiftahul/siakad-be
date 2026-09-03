ALTER TABLE public."Deposito"
    ADD COLUMN "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL;

CREATE UNIQUE INDEX "Deposito.siswaId_jenisPembayaranId_unique"
    ON public."Deposito" USING btree ("siswaId", "jenisPembayaranId");
