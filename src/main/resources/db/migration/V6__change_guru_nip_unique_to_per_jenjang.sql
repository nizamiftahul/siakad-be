DROP INDEX public."Guru.nip_unique";
CREATE UNIQUE INDEX "Guru.nip_jenjang_unique" ON public."Guru" USING btree (nip, jenjang);