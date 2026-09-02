CREATE UNIQUE INDEX "KelasSiswa.siswaId_kelasGrupId_unique" ON public."KelasSiswa" USING btree ("siswaId", "kelasGrupId");
