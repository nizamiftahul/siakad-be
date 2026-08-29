--
-- PostgreSQL database dump
--

-- Dumped from database version 17.0 (Debian 17.0-1.pgdg120+1)
-- Dumped by pg_dump version 17.0 (Debian 17.0-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it



--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS '';


--
-- Name: AbsensiStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."AbsensiStatus" AS ENUM (
    'Hadir',
    'Sakit',
    'TidakHadir',
    'Izin',
    'Lainnya'
);



--
-- Name: GuruStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."GuruStatus" AS ENUM (
    'Aktif',
    'TidakAktif'
);



--
-- Name: JK; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."JK" AS ENUM (
    'L',
    'P'
);



--
-- Name: Jenjang; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."Jenjang" AS ENUM (
    'TK',
    'SD',
    'SMP',
    'SMA'
);



--
-- Name: MetodePembayaran; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."MetodePembayaran" AS ENUM (
    'Tunai',
    'Transfer',
    'Online',
    'Transfer_BTN'
);



--
-- Name: PembayaranStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."PembayaranStatus" AS ENUM (
    'Lunas',
    'BelumLunas',
    'MenungguPembayaran',
    'Diterima',
    'Dibatalkan'
);



--
-- Name: PemberitahuanStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."PemberitahuanStatus" AS ENUM (
    'Terkirim',
    'Menunggu',
    'SedangBerlangsung'
);



--
-- Name: RepeatTipe; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."RepeatTipe" AS ENUM (
    'Weekly',
    'Monthly'
);



--
-- Name: Role; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."Role" AS ENUM (
    'KSatu',
    'Admin',
    'Guru',
    'Siswa'
);



--
-- Name: SekolahStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."SekolahStatus" AS ENUM (
    'Percobaan',
    'Aktif',
    'Tenggang',
    'TidakAktif'
);



--
-- Name: SiswaStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."SiswaStatus" AS ENUM (
    'Aktif',
    'Pindah',
    'Lulus'
);



--
-- Name: TipeAkademik; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."TipeAkademik" AS ENUM (
    'Pelajaran',
    'Kegiatan',
    'Ujian',
    'Ekskul',
    'Lainnya'
);



--
-- Name: TugasStatus; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public."TugasStatus" AS ENUM (
    'SedangBerlangsung',
    'Selesai'
);



--
-- Name: afterMigrate(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."afterMigrate"() RETURNS void
    LANGUAGE sql
    AS $$
DROP TABLE IF EXISTS "V_PembayaranSPP";
ALTER VIEW "V_PembayaranSPP_" RENAME TO "V_PembayaranSPP";

DROP TABLE IF EXISTS "V_Absensi";
ALTER VIEW "V_Absensi_" RENAME TO "V_Absensi"
$$;



--
-- Name: beforeMigrate(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public."beforeMigrate"() RETURNS void
    LANGUAGE sql
    AS $$
ALTER VIEW "V_PembayaranSPP" RENAME TO "V_PembayaranSPP_";
ALTER VIEW "V_Absensi" RENAME TO "V_Absensi_";

$$;



--
-- Name: trigger_set_nomor_invoice(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_set_nomor_invoice() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 DECLARE 
    DECLARE d char(9);
    DECLARE kodePembayaran char(3);
    DECLARE nomorInvoice char(12);
    DECLARE lastNumber char(3);
 BEGIN
    SELECT "kodePembayaran" INTO kodePembayaran FROM "JenisPembayaran" WHERE id = new."jenisPembayaranId";
    SELECT to_char(NOW(), 'YYMMDD') || kodePembayaran INTO d;
    SELECT "nomorInvoice" into nomorInvoice FROM "Pembayaran" WHERE SUBSTRING("nomorInvoice" from 1 for 9) = d ORDER BY "nomorInvoice" DESC LIMIT 1;      
    
    IF nomorInvoice IS NULL THEN
        new."nomorInvoice" = d || '001';
    ELSE
        SELECT LPAD((SUBSTRING(nomorInvoice from 10 for 3)::INTEGER + 1) || '', 3, '0') INTO lastNumber;
        new."nomorInvoice" = d || lastNumber;
    END IF;
    
    -- RAISE NOTICE 'd is currently %', d;

    return new;
 END; 
 
$$;



--
-- Name: trigger_set_timestamp_on_update(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_set_timestamp_on_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW."updatedAt" = NOW();
  RETURN NEW;
END;
$$;



--
-- Name: trigger_update_description_pembayaran_lainnya(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_update_description_pembayaran_lainnya() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
 DECLARE 
 BEGIN
    UPDATE "PembayaranLainnyaDetail" SET description = NEW.description WHERE "pembayaranLainnyaId" =  NEW.id;
    return NEW;
 END; 
 
$$;



--
-- Name: Absensi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Absensi" (
    id integer NOT NULL,
    "siswaId" integer,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    tanggal date NOT NULL,
    "semesterId" integer,
    foto text,
    "kelasSiswaId" integer NOT NULL,
    "waktuAbsensi" timestamp(6) with time zone,
    status public."AbsensiStatus" DEFAULT 'Hadir'::public."AbsensiStatus",
    "konfirmasiKehadiran" boolean DEFAULT false NOT NULL,
    "jamMulai" character varying(45) NOT NULL,
    "jamSelesai" character varying(45) NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    "kalenderAkademikId" integer NOT NULL
);



--
-- Name: Absensi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Absensi_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Absensi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Absensi_id_seq" OWNED BY public."Absensi".id;


--
-- Name: CallbackLog; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."CallbackLog" (
    id integer NOT NULL,
    data jsonb NOT NULL,
    type character varying(256) NOT NULL,
    status character varying DEFAULT 'success'::character varying,
    "errorStatus" text,
    "createdAt" timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);



--
-- Name: CallbackLog_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."CallbackLog_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: CallbackLog_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."CallbackLog_id_seq" OWNED BY public."CallbackLog".id;


--
-- Name: Deposito; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Deposito" (
    id integer NOT NULL,
    "siswaId" integer NOT NULL,
    "jenisPembayaranId" integer NOT NULL,
    deposito money NOT NULL,
    "updatedAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedBy" character varying NOT NULL
);



--
-- Name: Deposito_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Deposito_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Deposito_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Deposito_id_seq" OWNED BY public."Deposito".id;


--
-- Name: ErrorLogs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."ErrorLogs" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "errorCode" character varying(45) NOT NULL
);



--
-- Name: ErrorLogs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."ErrorLogs_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: ErrorLogs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."ErrorLogs_id_seq" OWNED BY public."ErrorLogs".id;


--
-- Name: GenerateVA; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."GenerateVA" (
    id integer NOT NULL,
    "siswaId" integer NOT NULL,
    jumlah money NOT NULL,
    terbayar money DEFAULT '$0.00'::money NOT NULL,
    "jenisPembayaranId" integer NOT NULL,
    "nomorVA" character(17) NOT NULL,
    "responseVA" jsonb NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone
);



--
-- Name: GenerateVA_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."GenerateVA_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: GenerateVA_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."GenerateVA_id_seq" OWNED BY public."GenerateVA".id;


--
-- Name: Guru; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Guru" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nip character varying(45) NOT NULL,
    nama character varying(45) NOT NULL,
    email character varying(45),
    "jenisKelamin" public."JK",
    alamat text,
    telepon character varying(45),
    status public."GuruStatus" DEFAULT 'Aktif'::public."GuruStatus" NOT NULL,
    "pendidikanTerakhir" character varying(45),
    "tglLahir" timestamp(6) with time zone,
    "tmptLahir" character varying(45),
    jabatan character varying(45),
    jenjang public."Jenjang" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45)
);



--
-- Name: Guru_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Guru_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Guru_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Guru_id_seq" OWNED BY public."Guru".id;


--
-- Name: ImportLogs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."ImportLogs" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    type character varying(45) NOT NULL,
    filepath text NOT NULL,
    result jsonb NOT NULL,
    "errorCode" character varying(45)
);



--
-- Name: ImportLogs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."ImportLogs_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: ImportLogs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."ImportLogs_id_seq" OWNED BY public."ImportLogs".id;


--
-- Name: JadwalPelajaran; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."JadwalPelajaran" (
    id integer NOT NULL,
    "guruId" integer NOT NULL,
    "kelasGrupId" integer NOT NULL,
    "pelajaranId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    hari character varying(45) NOT NULL,
    "waktuMulai" character varying(45) NOT NULL,
    "waktuSelesai" character varying(45) NOT NULL,
    kategori character varying(45)
);



--
-- Name: JadwalPelajaran_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."JadwalPelajaran_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: JadwalPelajaran_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."JadwalPelajaran_id_seq" OWNED BY public."JadwalPelajaran".id;


--
-- Name: JenisPembayaran; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."JenisPembayaran" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    jenis character varying(45) NOT NULL,
    jenjang public."Jenjang" NOT NULL,
    "kodePembayaran" character(3),
    "order" integer,
    kode character varying(5)
);



--
-- Name: JenisPembayaran_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."JenisPembayaran_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: JenisPembayaran_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."JenisPembayaran_id_seq" OWNED BY public."JenisPembayaran".id;


--
-- Name: JenisPenilaian; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."JenisPenilaian" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    jenis character varying(45) NOT NULL,
    jenjang public."Jenjang" NOT NULL
);



--
-- Name: JenisPenilaian_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."JenisPenilaian_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: JenisPenilaian_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."JenisPenilaian_id_seq" OWNED BY public."JenisPenilaian".id;


--
-- Name: KalenderAkademik; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."KalenderAkademik" (
    id integer NOT NULL,
    "guruId" integer NOT NULL,
    "pelajaranId" integer,
    "kelasGrupId" integer,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    hari character varying(45),
    "tglMulai" date NOT NULL,
    "tglSelesai" date NOT NULL,
    "jamMulai" character varying(45) NOT NULL,
    "jamSelesai" character varying(45) NOT NULL,
    exclude jsonb,
    repeat public."RepeatTipe",
    tipe public."TipeAkademik" NOT NULL,
    "isKegiatan" boolean DEFAULT false NOT NULL,
    title text,
    "isAbsensi" boolean DEFAULT false NOT NULL
);



--
-- Name: COLUMN "KalenderAkademik"."guruId"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public."KalenderAkademik"."guruId" IS 'bisa diartikan sebagai penanggung jawab kegiatan';


--
-- Name: COLUMN "KalenderAkademik".repeat; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public."KalenderAkademik".repeat IS 'Weekly Monthly';


--
-- Name: KalenderAkademik_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."KalenderAkademik_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: KalenderAkademik_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."KalenderAkademik_id_seq" OWNED BY public."KalenderAkademik".id;


--
-- Name: Kelas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Kelas" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nama character varying(45) NOT NULL,
    jenjang public."Jenjang" NOT NULL
);



--
-- Name: KelasGrup; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."KelasGrup" (
    id integer NOT NULL,
    "kelasId" integer NOT NULL,
    "periodeId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "defaultSpp" money DEFAULT '$0.00'::money NOT NULL,
    nama character varying(45) NOT NULL,
    "waliKelasId" integer NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    icp boolean DEFAULT false
);



--
-- Name: KelasGrup_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."KelasGrup_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: KelasGrup_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."KelasGrup_id_seq" OWNED BY public."KelasGrup".id;


--
-- Name: KelasSiswa; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."KelasSiswa" (
    id integer NOT NULL,
    "siswaId" integer NOT NULL,
    "kelasGrupId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    spp money NOT NULL,
    "potonganSpp" money DEFAULT '$0.00'::money NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45)
);



--
-- Name: KelasSiswa_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."KelasSiswa_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: KelasSiswa_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."KelasSiswa_id_seq" OWNED BY public."KelasSiswa".id;


--
-- Name: Kelas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Kelas_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Kelas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Kelas_id_seq" OWNED BY public."Kelas".id;


--
-- Name: LogPembayaranAPI; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."LogPembayaranAPI" (
    id integer NOT NULL,
    data jsonb NOT NULL,
    response jsonb NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    headers jsonb
);



--
-- Name: LogPembayaranAPI_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."LogPembayaranAPI_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: LogPembayaranAPI_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."LogPembayaranAPI_id_seq" OWNED BY public."LogPembayaranAPI".id;


--
-- Name: Notification; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Notification" (
    id integer NOT NULL,
    data jsonb NOT NULL,
    response jsonb NOT NULL,
    "siswaId" integer NOT NULL,
    "readStatus" character(1) DEFAULT '0'::bpchar NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "notificationHeaderId" integer NOT NULL,
    "sendStatus" character(1) DEFAULT '0'::bpchar NOT NULL
);



--
-- Name: NotificationHeader; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."NotificationHeader" (
    id integer NOT NULL,
    data jsonb NOT NULL,
    "sendTo" jsonb NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    response jsonb NOT NULL,
    jenjang character varying(10) NOT NULL,
    description text,
    success integer DEFAULT 0 NOT NULL,
    failure smallint DEFAULT '0'::smallint NOT NULL,
    pending smallint DEFAULT '0'::smallint NOT NULL
);



--
-- Name: NotificationHeader_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."NotificationHeader_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: NotificationHeader_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."NotificationHeader_id_seq" OWNED BY public."NotificationHeader".id;


--
-- Name: Notification_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Notification_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Notification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Notification_id_seq" OWNED BY public."Notification".id;


--
-- Name: Pdf; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Pdf" (
    id integer NOT NULL,
    nama character varying NOT NULL,
    konten jsonb NOT NULL,
    jenjang public."Jenjang" NOT NULL
);



--
-- Name: Pdf_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Pdf_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Pdf_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Pdf_id_seq" OWNED BY public."Pdf".id;


--
-- Name: Pelajaran; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Pelajaran" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nama character varying(45) NOT NULL,
    jenjang public."Jenjang" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45)
);



--
-- Name: Pelajaran_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Pelajaran_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Pelajaran_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Pelajaran_id_seq" OWNED BY public."Pelajaran".id;


--
-- Name: Pembayaran; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Pembayaran" (
    id integer NOT NULL,
    "kelasSiswaId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    jumlah money NOT NULL,
    "tglPembayaran" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "buktiPembayaran" text,
    "semesterId" integer,
    jenis character varying(45) NOT NULL,
    status public."PembayaranStatus" DEFAULT 'MenungguPembayaran'::public."PembayaranStatus" NOT NULL,
    "nomorInvoice" character varying(45) NOT NULL,
    "metodePembayaran" public."MetodePembayaran" DEFAULT 'Online'::public."MetodePembayaran" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    "jenisPembayaranId" integer,
    "nomorVA" character(17),
    "responseVA" jsonb,
    rincian jsonb
);



--
-- Name: PembayaranLainnya; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."PembayaranLainnya" (
    id integer NOT NULL,
    "kelasGrupId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    jumlah money NOT NULL,
    jenis character varying(45) NOT NULL,
    "semesterId" integer,
    "jenisPembayaranId" integer
);



--
-- Name: PembayaranLainnyaDetail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."PembayaranLainnyaDetail" (
    id integer NOT NULL,
    "pembayaranLainnyaId" integer NOT NULL,
    "pembayaranId" integer,
    "kelasSiswaId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    jumlah money NOT NULL,
    potongan money DEFAULT '$0.00'::money NOT NULL,
    status public."PembayaranStatus" DEFAULT 'BelumLunas'::public."PembayaranStatus" NOT NULL,
    "tglPembayaran" timestamp(6) with time zone
);



--
-- Name: PembayaranLainnyaDetail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PembayaranLainnyaDetail_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: PembayaranLainnyaDetail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."PembayaranLainnyaDetail_id_seq" OWNED BY public."PembayaranLainnyaDetail".id;


--
-- Name: PembayaranLainnya_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PembayaranLainnya_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: PembayaranLainnya_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."PembayaranLainnya_id_seq" OWNED BY public."PembayaranLainnya".id;


--
-- Name: PembayaranSPP; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."PembayaranSPP" (
    id integer NOT NULL,
    "kelasSiswaId" integer NOT NULL,
    "pembayaranId" integer,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    spp money NOT NULL,
    "potonganSpp" money DEFAULT '$0.00'::money NOT NULL,
    bulan integer NOT NULL,
    tahun integer NOT NULL,
    "tglPembayaran" timestamp(6) with time zone,
    status public."PembayaranStatus" DEFAULT 'BelumLunas'::public."PembayaranStatus" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    "semesterId" integer,
    "jenisPembayaranId" integer
);



--
-- Name: PembayaranSPP_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PembayaranSPP_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: PembayaranSPP_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."PembayaranSPP_id_seq" OWNED BY public."PembayaranSPP".id;


--
-- Name: Pembayaran_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Pembayaran_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Pembayaran_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Pembayaran_id_seq" OWNED BY public."Pembayaran".id;


--
-- Name: Pemberitahuan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Pemberitahuan" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    status public."PemberitahuanStatus" DEFAULT 'Menunggu'::public."PemberitahuanStatus" NOT NULL,
    respon jsonb,
    "siswaId" integer,
    "guruId" integer
);



--
-- Name: Pemberitahuan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Pemberitahuan_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Pemberitahuan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Pemberitahuan_id_seq" OWNED BY public."Pemberitahuan".id;


--
-- Name: Pengaturan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Pengaturan" (
    id integer NOT NULL,
    nama character varying(256) NOT NULL,
    isi jsonb NOT NULL
);



--
-- Name: Pengaturan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Pengaturan_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Pengaturan_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Pengaturan_id_seq" OWNED BY public."Pengaturan".id;


--
-- Name: Penilaian; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Penilaian" (
    id integer NOT NULL,
    "jenisPenilaianId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    tanggal timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "periodeId" integer NOT NULL,
    "guruId" integer NOT NULL,
    "pelajaranId" integer NOT NULL,
    "semesterId" integer,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    "kelasId" integer NOT NULL
);



--
-- Name: PenilaianDetail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."PenilaianDetail" (
    id integer NOT NULL,
    "kelasSiswaId" integer NOT NULL,
    "penilaianId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    description text,
    nilai character varying(45) NOT NULL,
    "jadwalPelajaranId" integer NOT NULL
);



--
-- Name: PenilaianDetail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."PenilaianDetail_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: PenilaianDetail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."PenilaianDetail_id_seq" OWNED BY public."PenilaianDetail".id;


--
-- Name: Penilaian_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Penilaian_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Penilaian_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Penilaian_id_seq" OWNED BY public."Penilaian".id;


--
-- Name: Periode; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Periode" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nama character varying(45) NOT NULL,
    "tglMulai" date NOT NULL,
    "tglSelesai" date NOT NULL,
    status boolean DEFAULT false NOT NULL,
    jenjang public."Jenjang" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45)
);



--
-- Name: Periode_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Periode_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Periode_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Periode_id_seq" OWNED BY public."Periode".id;


--
-- Name: Semester; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Semester" (
    id integer NOT NULL,
    "periodeId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nama character varying(45) NOT NULL,
    "tglMulai" date NOT NULL,
    "tglSelesai" date NOT NULL,
    status boolean DEFAULT false NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45)
);



--
-- Name: Semester_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Semester_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Semester_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Semester_id_seq" OWNED BY public."Semester".id;


--
-- Name: Siswa; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."Siswa" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    nisn character varying(45),
    nama character varying(45) NOT NULL,
    email character varying(45),
    "jenisKelamin" public."JK",
    alamat text,
    telepon character varying(45),
    status public."SiswaStatus" DEFAULT 'Aktif'::public."SiswaStatus" NOT NULL,
    "asalSekolah" character varying(45),
    "namaAyah" character varying(45),
    "pekerjaanAyah" character varying,
    "alamatAyah" text,
    "pendidikanAyah" character varying(45),
    "gajiAyah" integer,
    "namaIbu" character varying(45),
    "pekerjaanIbu" character varying(45),
    "alamatIbu" text,
    "pendidikanIbu" character varying(45),
    "gajiIbu" integer,
    "tglLahir" date,
    nis character varying(45) NOT NULL,
    "tmptLahir" character varying(45),
    domisili text,
    "hashedPassword" text,
    "namaWali" character varying(45),
    "pekerjaanWali" character varying(45),
    "alamatWali" text,
    "pendidikanWali" character varying(45),
    "gajiWali" integer,
    jenjang public."Jenjang" NOT NULL,
    "createdBy" character varying(45),
    "updatedBy" character varying(45),
    "isAlumni" boolean
);



--
-- Name: SiswaDeviceToken; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."SiswaDeviceToken" (
    id integer NOT NULL,
    "deviceToken" text NOT NULL,
    "siswaId" integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    description text,
    "deviceInfo" jsonb
);



--
-- Name: SiswaDeviceToken_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."SiswaDeviceToken_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: SiswaDeviceToken_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."SiswaDeviceToken_id_seq" OWNED BY public."SiswaDeviceToken".id;


--
-- Name: Siswa_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."Siswa_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: Siswa_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."Siswa_id_seq" OWNED BY public."Siswa".id;


--
-- Name: User; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."User" (
    id integer NOT NULL,
    "createdAt" timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "updatedAt" timestamp(6) with time zone,
    name character varying(45),
    email character varying(45),
    "hashedPassword" text,
    role public."Role" NOT NULL,
    username character varying(45) NOT NULL,
    "siswaId" integer,
    "guruId" integer,
    jenjang public."Jenjang" NOT NULL
);



--
-- Name: User_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."User_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    MAXVALUE 2147483647
    CACHE 1;



--
-- Name: User_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."User_id_seq" OWNED BY public."User".id;


--
-- Name: v_KelasSiswa; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_KelasSiswa" AS
 SELECT t.id,
    t."siswaId",
    kg.kelas,
    s.nama,
    s.nis,
    s.nisn,
    kg.jenjang,
    kg.periode,
    t."kelasGrupId",
    t.description
   FROM ((public."KelasSiswa" t
     JOIN ( SELECT t_1.id,
            t_1.nama AS kelas,
            p.jenjang,
            p.nama AS periode
           FROM (public."KelasGrup" t_1
             JOIN public."Periode" p ON ((p.id = t_1."periodeId")))
          WHERE (p.status IS TRUE)) kg ON ((kg.id = t."kelasGrupId")))
     JOIN public."Siswa" s ON (((s.id = t."siswaId") AND (s."isAlumni" IS NOT TRUE))));



--
-- Name: v_HistoryLainnya; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_HistoryLainnya" AS
 SELECT s.nis,
    s.nama AS "namaSiswa",
    vks.kelas,
    s.jenjang,
    (t.jumlah - t.potongan) AS jumlah,
    t."kelasSiswaId",
    pl.jenis,
    pl."jenisPembayaranId",
    t.id,
    vks."kelasGrupId",
    t.description,
    t."tglPembayaran",
    ks."siswaId"
   FROM ((((public."PembayaranLainnyaDetail" t
     JOIN public."PembayaranLainnya" pl ON ((pl.id = t."pembayaranLainnyaId")))
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."v_KelasSiswa" vks ON ((vks.id = t."kelasSiswaId")))
  WHERE (t.status = 'Lunas'::public."PembayaranStatus");



--
-- Name: v_HistoryNotificaton; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_HistoryNotificaton" AS
 SELECT t.id,
    t.data,
    t.response,
    t."siswaId",
    t."readStatus",
    t."createdAt",
    s.nis,
    s.nama,
    s.jenjang
   FROM (public."Notification" t
     JOIN public."Siswa" s ON ((s.id = t."siswaId")));



--
-- Name: v_HistorySPP; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_HistorySPP" AS
 SELECT s.nis,
    s.nama AS "namaSiswa",
    vks.kelas,
    s.jenjang,
    (t.spp - t."potonganSpp") AS jumlah,
    t."kelasSiswaId",
    t.bulan,
    t.tahun,
    t.id,
    vks."kelasGrupId",
    t."tglPembayaran",
    t."jenisPembayaranId",
    ks."siswaId"
   FROM (((public."PembayaranSPP" t
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."v_KelasSiswa" vks ON ((vks.id = t."kelasSiswaId")))
  WHERE (t.status = 'Lunas'::public."PembayaranStatus");



--
-- Name: v_Pembayaran; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_Pembayaran" AS
 SELECT t.id,
    t."kelasSiswaId",
    t."createdAt",
    t."updatedAt",
    t.description,
    t.jumlah,
        CASE
            WHEN ((t."responseVA" ->> 'tgl'::text) IS NOT NULL) THEN (to_timestamp(concat((t."responseVA" ->> 'tgl'::text), (t."responseVA" ->> 'jam'::text)), 'DDMMYYHH24MISS'::text) - '07:00:00'::interval)
            ELSE t."tglPembayaran"
        END AS "tglPembayaran",
    t."buktiPembayaran",
    t."semesterId",
    t.jenis,
    t.status,
    t."nomorInvoice",
    t."metodePembayaran",
    t."createdBy",
    t."updatedBy",
    t."jenisPembayaranId",
    t."nomorVA",
    s.nis,
    s.nama AS "namaSiswa",
    kg.nama AS kelas,
    s.jenjang,
    ks."kelasGrupId"
   FROM (((public."Pembayaran" t
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."KelasGrup" kg ON ((ks."kelasGrupId" = kg.id)));



--
-- Name: v_TagihanLainnya; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TagihanLainnya" AS
 SELECT s.nis,
    s.nama AS "namaSiswa",
    vks.kelas,
    s.jenjang,
    (t.jumlah - t.potongan) AS tagihan,
    vks.id AS "kelasSiswaId",
    pl.jenis,
    pl."jenisPembayaranId",
    t.id,
    vks."kelasGrupId",
    t.jumlah,
    t.potongan,
    t.description,
    ks."siswaId"
   FROM (((((public."PembayaranLainnyaDetail" t
     JOIN public."PembayaranLainnya" pl ON ((pl.id = t."pembayaranLainnyaId")))
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."v_KelasSiswa" vks ON ((vks."siswaId" = s.id)))
     JOIN public."JenisPembayaran" jp ON (((jp.id = pl."jenisPembayaranId") AND (jp."kodePembayaran" IS NOT NULL))))
  WHERE (t.status = 'BelumLunas'::public."PembayaranStatus");



--
-- Name: v_TagihanSPP; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TagihanSPP" AS
 SELECT s.nis,
    s.nama AS "namaSiswa",
    vks.kelas,
    s.jenjang,
    (t.spp - t."potonganSpp") AS tagihan,
    vks.id AS "kelasSiswaId",
    t.bulan,
    t.tahun,
    t.id,
    vks."kelasGrupId",
    t.spp AS jumlah,
    t."potonganSpp" AS potongan,
    ks."siswaId",
    t."jenisPembayaranId"
   FROM (((public."PembayaranSPP" t
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."v_KelasSiswa" vks ON ((vks."siswaId" = s.id)))
  WHERE (t.status = 'BelumLunas'::public."PembayaranStatus")
  ORDER BY t.tahun DESC, t.bulan DESC;



--
-- Name: v_Tagihan; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_Tagihan" AS
 SELECT "v_TagihanSPP".id,
    "v_TagihanSPP".nis,
    "v_TagihanSPP"."namaSiswa",
    "v_TagihanSPP".kelas,
    "v_TagihanSPP".jenjang,
    "v_TagihanSPP"."kelasSiswaId",
    "v_TagihanSPP"."kelasGrupId",
    "v_TagihanSPP".jumlah,
    "v_TagihanSPP".potongan,
    "v_TagihanSPP".tagihan,
    'SPP'::text AS jenis,
    ((
        CASE
            WHEN ("v_TagihanSPP".bulan = 1) THEN 'Januari'::text
            WHEN ("v_TagihanSPP".bulan = 2) THEN 'Februari'::text
            WHEN ("v_TagihanSPP".bulan = 3) THEN 'Maret'::text
            WHEN ("v_TagihanSPP".bulan = 4) THEN 'April'::text
            WHEN ("v_TagihanSPP".bulan = 5) THEN 'Mei'::text
            WHEN ("v_TagihanSPP".bulan = 6) THEN 'Juni'::text
            WHEN ("v_TagihanSPP".bulan = 7) THEN 'Juli'::text
            WHEN ("v_TagihanSPP".bulan = 8) THEN 'Agustus'::text
            WHEN ("v_TagihanSPP".bulan = 9) THEN 'September'::text
            WHEN ("v_TagihanSPP".bulan = 10) THEN 'Oktober'::text
            WHEN ("v_TagihanSPP".bulan = 11) THEN 'November'::text
            WHEN ("v_TagihanSPP".bulan = 12) THEN 'Desember'::text
            ELSE NULL::text
        END || ' '::text) || "v_TagihanSPP".tahun) AS description,
    "v_TagihanSPP"."siswaId",
    "v_TagihanSPP"."jenisPembayaranId"
   FROM public."v_TagihanSPP"
UNION
 SELECT "v_TagihanLainnya".id,
    "v_TagihanLainnya".nis,
    "v_TagihanLainnya"."namaSiswa",
    "v_TagihanLainnya".kelas,
    "v_TagihanLainnya".jenjang,
    "v_TagihanLainnya"."kelasSiswaId",
    "v_TagihanLainnya"."kelasGrupId",
    "v_TagihanLainnya".jumlah,
    "v_TagihanLainnya".potongan,
    "v_TagihanLainnya".tagihan,
    "v_TagihanLainnya".jenis,
    "v_TagihanLainnya".description,
    "v_TagihanLainnya"."siswaId",
    "v_TagihanLainnya"."jenisPembayaranId"
   FROM public."v_TagihanLainnya";



--
-- Name: v_SummaryTagihan; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_SummaryTagihan" AS
 SELECT nis,
    "namaSiswa",
    kelas,
    jenjang,
    "kelasSiswaId",
    sum(tagihan) AS tagihan,
    "kelasGrupId",
    ( SELECT sum("Deposito".deposito) AS sum
           FROM public."Deposito"
          WHERE ("Deposito"."siswaId" = t."siswaId")) AS deposito
   FROM public."v_Tagihan" t
  GROUP BY nis, "namaSiswa", kelas, jenjang, "kelasSiswaId", "kelasGrupId", "siswaId";



--
-- Name: v_SummaryTagihanLainnya; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_SummaryTagihanLainnya" AS
 SELECT nis,
    "namaSiswa",
    kelas,
    jenjang,
    sum(tagihan) AS tagihan,
    "kelasSiswaId",
    "kelasGrupId",
    sum(deposito) AS deposito
   FROM ( SELECT t_1.nis,
            t_1."namaSiswa",
            t_1.kelas,
            t_1.jenjang,
            sum((t_1.jumlah - t_1.potongan)) AS tagihan,
            t_1."kelasSiswaId",
            t_1."kelasGrupId",
            ( SELECT "Deposito".deposito
                   FROM public."Deposito"
                  WHERE (("Deposito"."siswaId" = t_1."siswaId") AND ("Deposito"."jenisPembayaranId" = t_1."jenisPembayaranId"))) AS deposito
           FROM public."v_TagihanLainnya" t_1
          GROUP BY t_1.nis, t_1."namaSiswa", t_1.kelas, t_1.jenjang, t_1."kelasSiswaId", t_1."kelasGrupId", t_1."siswaId", t_1."jenisPembayaranId") t
  GROUP BY nis, "namaSiswa", kelas, jenjang, "kelasSiswaId", "kelasGrupId";



--
-- Name: v_SummaryTagihanSPP; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_SummaryTagihanSPP" AS
 SELECT nis,
    "namaSiswa",
    kelas,
    jenjang,
    sum(tagihan) AS tagihan,
    "kelasSiswaId",
    "kelasGrupId",
    ( SELECT "Deposito".deposito
           FROM public."Deposito"
          WHERE (("Deposito"."siswaId" = t."siswaId") AND ("Deposito"."jenisPembayaranId" = t."jenisPembayaranId"))) AS deposito
   FROM public."v_TagihanSPP" t
  GROUP BY nis, "namaSiswa", kelas, jenjang, "kelasSiswaId", "kelasGrupId", "siswaId", "jenisPembayaranId";



--
-- Name: v_SummaryTagihanSPP_Now; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_SummaryTagihanSPP_Now" AS
 SELECT nis,
    "namaSiswa",
    kelas,
    jenjang,
    sum(tagihan) AS tagihan,
    "kelasSiswaId",
    "kelasGrupId",
    ( SELECT "Deposito".deposito
           FROM public."Deposito"
          WHERE (("Deposito"."siswaId" = t."siswaId") AND ("Deposito"."jenisPembayaranId" = t."jenisPembayaranId"))) AS deposito
   FROM public."v_TagihanSPP" t
  WHERE (((tahun = (to_char(now(), 'YYYY'::text))::integer) AND (bulan <= (to_char(now(), 'MM'::text))::integer)) OR (tahun < (to_char(now(), 'YYYY'::text))::integer))
  GROUP BY nis, "namaSiswa", kelas, jenjang, "kelasSiswaId", "kelasGrupId", "siswaId", "jenisPembayaranId";



--
-- Name: v_TagihanSPP_Now; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TagihanSPP_Now" AS
 SELECT s.nis,
    s.nama AS "namaSiswa",
    vks.kelas,
    s.jenjang,
    (t.spp - t."potonganSpp") AS tagihan,
    vks.id AS "kelasSiswaId",
    t.bulan,
    t.tahun,
    t.id,
    vks."kelasGrupId",
    t.spp AS jumlah,
    t."potonganSpp" AS potongan,
    ks."siswaId",
    t."jenisPembayaranId"
   FROM (((public."PembayaranSPP" t
     JOIN public."KelasSiswa" ks ON ((ks.id = t."kelasSiswaId")))
     JOIN public."Siswa" s ON ((s.id = ks."siswaId")))
     LEFT JOIN public."v_KelasSiswa" vks ON ((vks."siswaId" = s.id)))
  WHERE ((t.status = 'BelumLunas'::public."PembayaranStatus") AND (((t.tahun = (to_char(now(), 'YYYY'::text))::integer) AND (t.bulan <= (to_char(now(), 'MM'::text))::integer)) OR (t.tahun < (to_char(now(), 'YYYY'::text))::integer)))
  ORDER BY t.tahun DESC, t.bulan DESC;



--
-- Name: v_Tagihan_Now; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_Tagihan_Now" AS
 SELECT "v_TagihanSPP_Now".id,
    "v_TagihanSPP_Now".nis,
    "v_TagihanSPP_Now"."namaSiswa",
    "v_TagihanSPP_Now".kelas,
    "v_TagihanSPP_Now".jenjang,
    "v_TagihanSPP_Now"."kelasSiswaId",
    "v_TagihanSPP_Now"."kelasGrupId",
    "v_TagihanSPP_Now".jumlah,
    "v_TagihanSPP_Now".potongan,
    "v_TagihanSPP_Now".tagihan,
    'SPP'::text AS jenis,
    ((
        CASE
            WHEN ("v_TagihanSPP_Now".bulan = 1) THEN 'Januari'::text
            WHEN ("v_TagihanSPP_Now".bulan = 2) THEN 'Februari'::text
            WHEN ("v_TagihanSPP_Now".bulan = 3) THEN 'Maret'::text
            WHEN ("v_TagihanSPP_Now".bulan = 4) THEN 'April'::text
            WHEN ("v_TagihanSPP_Now".bulan = 5) THEN 'Mei'::text
            WHEN ("v_TagihanSPP_Now".bulan = 6) THEN 'Juni'::text
            WHEN ("v_TagihanSPP_Now".bulan = 7) THEN 'Juli'::text
            WHEN ("v_TagihanSPP_Now".bulan = 8) THEN 'Agustus'::text
            WHEN ("v_TagihanSPP_Now".bulan = 9) THEN 'September'::text
            WHEN ("v_TagihanSPP_Now".bulan = 10) THEN 'Oktober'::text
            WHEN ("v_TagihanSPP_Now".bulan = 11) THEN 'November'::text
            WHEN ("v_TagihanSPP_Now".bulan = 12) THEN 'Desember'::text
            ELSE NULL::text
        END || ' '::text) || "v_TagihanSPP_Now".tahun) AS description,
    "v_TagihanSPP_Now"."siswaId",
    "v_TagihanSPP_Now"."jenisPembayaranId",
    (("v_TagihanSPP_Now".tahun)::text || lpad(("v_TagihanSPP_Now".bulan)::text, 2, '0'::text)) AS _description
   FROM public."v_TagihanSPP_Now"
UNION
 SELECT "v_TagihanLainnya".id,
    "v_TagihanLainnya".nis,
    "v_TagihanLainnya"."namaSiswa",
    "v_TagihanLainnya".kelas,
    "v_TagihanLainnya".jenjang,
    "v_TagihanLainnya"."kelasSiswaId",
    "v_TagihanLainnya"."kelasGrupId",
    "v_TagihanLainnya".jumlah,
    "v_TagihanLainnya".potongan,
    "v_TagihanLainnya".tagihan,
    "v_TagihanLainnya".jenis,
    "v_TagihanLainnya".description,
    "v_TagihanLainnya"."siswaId",
    "v_TagihanLainnya"."jenisPembayaranId",
    "v_TagihanLainnya".description AS _description
   FROM public."v_TagihanLainnya";



--
-- Name: v_SummaryTagihan_Now; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_SummaryTagihan_Now" AS
 SELECT nis,
    "namaSiswa",
    kelas,
    jenjang,
    "kelasSiswaId",
    sum(tagihan) AS tagihan,
    "kelasGrupId",
    ( SELECT sum("Deposito".deposito) AS sum
           FROM public."Deposito"
          WHERE ("Deposito"."siswaId" = t."siswaId")) AS deposito
   FROM public."v_Tagihan_Now" t
  GROUP BY nis, "namaSiswa", kelas, jenjang, "kelasSiswaId", "kelasGrupId", "siswaId";



--
-- Name: v_TotalGuru; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TotalGuru" AS
 SELECT count(1) AS count,
    jenjang
   FROM public."Guru"
  WHERE (status = 'Aktif'::public."GuruStatus")
  GROUP BY jenjang;



--
-- Name: v_TotalPelunasan; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TotalPelunasan" AS
 SELECT sum(sum) AS sum,
    jenjang
   FROM ( SELECT sum("v_HistorySPP".jumlah) AS sum,
            "v_HistorySPP".jenjang
           FROM public."v_HistorySPP"
          GROUP BY "v_HistorySPP".jenjang
        UNION
         SELECT sum("v_HistoryLainnya".jumlah) AS sum,
            "v_HistoryLainnya".jenjang
           FROM public."v_HistoryLainnya"
          GROUP BY "v_HistoryLainnya".jenjang) t
  GROUP BY jenjang;



--
-- Name: v_TotalSiswa; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TotalSiswa" AS
 SELECT count(1) AS count,
    jenjang
   FROM public."v_KelasSiswa"
  GROUP BY jenjang;



--
-- Name: v_TotalTagihan; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public."v_TotalTagihan" AS
 SELECT sum(tagihan) AS sum,
    jenjang
   FROM public."v_Tagihan"
  GROUP BY jenjang;



--
-- Name: Absensi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Absensi" ALTER COLUMN id SET DEFAULT nextval('public."Absensi_id_seq"'::regclass);


--
-- Name: CallbackLog id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."CallbackLog" ALTER COLUMN id SET DEFAULT nextval('public."CallbackLog_id_seq"'::regclass);


--
-- Name: Deposito id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Deposito" ALTER COLUMN id SET DEFAULT nextval('public."Deposito_id_seq"'::regclass);


--
-- Name: ErrorLogs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."ErrorLogs" ALTER COLUMN id SET DEFAULT nextval('public."ErrorLogs_id_seq"'::regclass);


--
-- Name: GenerateVA id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."GenerateVA" ALTER COLUMN id SET DEFAULT nextval('public."GenerateVA_id_seq"'::regclass);


--
-- Name: Guru id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Guru" ALTER COLUMN id SET DEFAULT nextval('public."Guru_id_seq"'::regclass);


--
-- Name: ImportLogs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."ImportLogs" ALTER COLUMN id SET DEFAULT nextval('public."ImportLogs_id_seq"'::regclass);


--
-- Name: JadwalPelajaran id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JadwalPelajaran" ALTER COLUMN id SET DEFAULT nextval('public."JadwalPelajaran_id_seq"'::regclass);


--
-- Name: JenisPembayaran id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JenisPembayaran" ALTER COLUMN id SET DEFAULT nextval('public."JenisPembayaran_id_seq"'::regclass);


--
-- Name: JenisPenilaian id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JenisPenilaian" ALTER COLUMN id SET DEFAULT nextval('public."JenisPenilaian_id_seq"'::regclass);


--
-- Name: KalenderAkademik id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KalenderAkademik" ALTER COLUMN id SET DEFAULT nextval('public."KalenderAkademik_id_seq"'::regclass);


--
-- Name: Kelas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Kelas" ALTER COLUMN id SET DEFAULT nextval('public."Kelas_id_seq"'::regclass);


--
-- Name: KelasGrup id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasGrup" ALTER COLUMN id SET DEFAULT nextval('public."KelasGrup_id_seq"'::regclass);


--
-- Name: KelasSiswa id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasSiswa" ALTER COLUMN id SET DEFAULT nextval('public."KelasSiswa_id_seq"'::regclass);


--
-- Name: LogPembayaranAPI id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."LogPembayaranAPI" ALTER COLUMN id SET DEFAULT nextval('public."LogPembayaranAPI_id_seq"'::regclass);


--
-- Name: Notification id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Notification" ALTER COLUMN id SET DEFAULT nextval('public."Notification_id_seq"'::regclass);


--
-- Name: NotificationHeader id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."NotificationHeader" ALTER COLUMN id SET DEFAULT nextval('public."NotificationHeader_id_seq"'::regclass);


--
-- Name: Pdf id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pdf" ALTER COLUMN id SET DEFAULT nextval('public."Pdf_id_seq"'::regclass);


--
-- Name: Pelajaran id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pelajaran" ALTER COLUMN id SET DEFAULT nextval('public."Pelajaran_id_seq"'::regclass);


--
-- Name: Pembayaran id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pembayaran" ALTER COLUMN id SET DEFAULT nextval('public."Pembayaran_id_seq"'::regclass);


--
-- Name: PembayaranLainnya id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnya" ALTER COLUMN id SET DEFAULT nextval('public."PembayaranLainnya_id_seq"'::regclass);


--
-- Name: PembayaranLainnyaDetail id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnyaDetail" ALTER COLUMN id SET DEFAULT nextval('public."PembayaranLainnyaDetail_id_seq"'::regclass);


--
-- Name: PembayaranSPP id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranSPP" ALTER COLUMN id SET DEFAULT nextval('public."PembayaranSPP_id_seq"'::regclass);


--
-- Name: Pemberitahuan id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pemberitahuan" ALTER COLUMN id SET DEFAULT nextval('public."Pemberitahuan_id_seq"'::regclass);


--
-- Name: Pengaturan id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pengaturan" ALTER COLUMN id SET DEFAULT nextval('public."Pengaturan_id_seq"'::regclass);


--
-- Name: Penilaian id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian" ALTER COLUMN id SET DEFAULT nextval('public."Penilaian_id_seq"'::regclass);


--
-- Name: PenilaianDetail id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PenilaianDetail" ALTER COLUMN id SET DEFAULT nextval('public."PenilaianDetail_id_seq"'::regclass);


--
-- Name: Periode id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Periode" ALTER COLUMN id SET DEFAULT nextval('public."Periode_id_seq"'::regclass);


--
-- Name: Semester id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Semester" ALTER COLUMN id SET DEFAULT nextval('public."Semester_id_seq"'::regclass);


--
-- Name: Siswa id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Siswa" ALTER COLUMN id SET DEFAULT nextval('public."Siswa_id_seq"'::regclass);


--
-- Name: SiswaDeviceToken id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."SiswaDeviceToken" ALTER COLUMN id SET DEFAULT nextval('public."SiswaDeviceToken_id_seq"'::regclass);


--
-- Name: User id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."User" ALTER COLUMN id SET DEFAULT nextval('public."User_id_seq"'::regclass);


--
-- Name: Absensi Absensi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Absensi"
    ADD CONSTRAINT "Absensi_pkey" PRIMARY KEY (id);


--
-- Name: CallbackLog CallbackLog_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."CallbackLog"
    ADD CONSTRAINT "CallbackLog_pkey" PRIMARY KEY (id);


--
-- Name: Deposito Deposito_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Deposito"
    ADD CONSTRAINT "Deposito_pkey" PRIMARY KEY (id);


--
-- Name: ErrorLogs ErrorLogs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."ErrorLogs"
    ADD CONSTRAINT "ErrorLogs_pkey" PRIMARY KEY (id);


--
-- Name: GenerateVA GenerateVA_nomorVA; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."GenerateVA"
    ADD CONSTRAINT "GenerateVA_nomorVA" UNIQUE ("nomorVA");


--
-- Name: GenerateVA GenerateVA_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."GenerateVA"
    ADD CONSTRAINT "GenerateVA_pkey" PRIMARY KEY (id);


--
-- Name: Guru Guru_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Guru"
    ADD CONSTRAINT "Guru_pkey" PRIMARY KEY (id);


--
-- Name: ImportLogs ImportLogs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."ImportLogs"
    ADD CONSTRAINT "ImportLogs_pkey" PRIMARY KEY (id);


--
-- Name: JadwalPelajaran JadwalPelajaran_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JadwalPelajaran"
    ADD CONSTRAINT "JadwalPelajaran_pkey" PRIMARY KEY (id);


--
-- Name: JenisPembayaran JenisPembayaran_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JenisPembayaran"
    ADD CONSTRAINT "JenisPembayaran_pkey" PRIMARY KEY (id);


--
-- Name: JenisPenilaian JenisPenilaian_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JenisPenilaian"
    ADD CONSTRAINT "JenisPenilaian_pkey" PRIMARY KEY (id);


--
-- Name: KalenderAkademik KalenderAkademik_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KalenderAkademik"
    ADD CONSTRAINT "KalenderAkademik_pkey" PRIMARY KEY (id);


--
-- Name: KelasGrup KelasGrup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasGrup"
    ADD CONSTRAINT "KelasGrup_pkey" PRIMARY KEY (id);


--
-- Name: KelasSiswa KelasSiswa_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasSiswa"
    ADD CONSTRAINT "KelasSiswa_pkey" PRIMARY KEY (id);


--
-- Name: Kelas Kelas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Kelas"
    ADD CONSTRAINT "Kelas_pkey" PRIMARY KEY (id);


--
-- Name: LogPembayaranAPI LogPembayaranAPI_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."LogPembayaranAPI"
    ADD CONSTRAINT "LogPembayaranAPI_pkey" PRIMARY KEY (id);


--
-- Name: NotificationHeader NotificationHeader_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."NotificationHeader"
    ADD CONSTRAINT "NotificationHeader_pkey" PRIMARY KEY (id);


--
-- Name: Notification Notification_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Notification"
    ADD CONSTRAINT "Notification_pkey" PRIMARY KEY (id);


--
-- Name: Pdf Pdf_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pdf"
    ADD CONSTRAINT "Pdf_pkey" PRIMARY KEY (id);


--
-- Name: Pelajaran Pelajaran_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pelajaran"
    ADD CONSTRAINT "Pelajaran_pkey" PRIMARY KEY (id);


--
-- Name: PembayaranLainnyaDetail PembayaranLainnyaDetail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnyaDetail"
    ADD CONSTRAINT "PembayaranLainnyaDetail_pkey" PRIMARY KEY (id);


--
-- Name: PembayaranLainnya PembayaranLainnya_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnya"
    ADD CONSTRAINT "PembayaranLainnya_pkey" PRIMARY KEY (id);


--
-- Name: PembayaranSPP PembayaranSPP_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranSPP"
    ADD CONSTRAINT "PembayaranSPP_pkey" PRIMARY KEY (id);


--
-- Name: Pembayaran Pembayaran_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pembayaran"
    ADD CONSTRAINT "Pembayaran_pkey" PRIMARY KEY (id);


--
-- Name: Pemberitahuan Pemberitahuan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pemberitahuan"
    ADD CONSTRAINT "Pemberitahuan_pkey" PRIMARY KEY (id);


--
-- Name: Pengaturan Pengaturan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pengaturan"
    ADD CONSTRAINT "Pengaturan_pkey" PRIMARY KEY (id);


--
-- Name: PenilaianDetail PenilaianDetail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PenilaianDetail"
    ADD CONSTRAINT "PenilaianDetail_pkey" PRIMARY KEY (id);


--
-- Name: Penilaian Penilaian_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_pkey" PRIMARY KEY (id);


--
-- Name: Periode Periode_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Periode"
    ADD CONSTRAINT "Periode_pkey" PRIMARY KEY (id);


--
-- Name: Semester Semester_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Semester"
    ADD CONSTRAINT "Semester_pkey" PRIMARY KEY (id);


--
-- Name: SiswaDeviceToken SiswaDeviceToken_deviceToken_siswaId_deviceInfo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."SiswaDeviceToken"
    ADD CONSTRAINT "SiswaDeviceToken_deviceToken_siswaId_deviceInfo" UNIQUE ("deviceToken", "siswaId", "deviceInfo");


--
-- Name: SiswaDeviceToken SiswaDeviceToken_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."SiswaDeviceToken"
    ADD CONSTRAINT "SiswaDeviceToken_pkey" PRIMARY KEY (id);


--
-- Name: Siswa Siswa_nis_jenjang; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Siswa"
    ADD CONSTRAINT "Siswa_nis_jenjang" UNIQUE (nis, jenjang);


--
-- Name: Siswa Siswa_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Siswa"
    ADD CONSTRAINT "Siswa_pkey" PRIMARY KEY (id);


--
-- Name: User User_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (id);


--
-- Name: Guru.nip_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "Guru.nip_unique" ON public."Guru" USING btree (nip);


--
-- Name: Kelas.nama_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "Kelas.nama_unique" ON public."Kelas" USING btree (nama);


--
-- Name: KelasGrup.nama_periodeId_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "KelasGrup.nama_periodeId_unique" ON public."KelasGrup" USING btree (nama, "periodeId");


--
-- Name: KelasGrup.waliKelasId_periodeId_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "KelasGrup.waliKelasId_periodeId_unique" ON public."KelasGrup" USING btree ("waliKelasId", "periodeId");


--
-- Name: PembayaranSPP.kelasSiswaId_bulan_tahun_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "PembayaranSPP.kelasSiswaId_bulan_tahun_unique" ON public."PembayaranSPP" USING btree ("kelasSiswaId", bulan, tahun);


--
-- Name: User.guruId_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "User.guruId_index" ON public."User" USING btree ("guruId");


--
-- Name: User.siswaId_index; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "User.siswaId_index" ON public."User" USING btree ("siswaId");


--
-- Name: User.username_unique; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX "User.username_unique" ON public."User" USING btree (username);


--
-- Name: PembayaranLainnya PembayaranLainnya_au; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER "PembayaranLainnya_au" AFTER UPDATE ON public."PembayaranLainnya" FOR EACH ROW EXECUTE FUNCTION public.trigger_update_description_pembayaran_lainnya();


--
-- Name: Pembayaran Pembayaran_bi; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER "Pembayaran_bi" BEFORE INSERT ON public."Pembayaran" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_nomor_invoice();


--
-- Name: Absensi absensi_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER absensi_bu BEFORE UPDATE ON public."Absensi" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Guru guru_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER guru_bu BEFORE UPDATE ON public."Guru" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: JenisPembayaran jenis_pembayaran_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER jenis_pembayaran_bu BEFORE UPDATE ON public."JenisPembayaran" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: JenisPenilaian jenis_penilaian_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER jenis_penilaian_bu BEFORE UPDATE ON public."JenisPenilaian" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: KalenderAkademik kalender_akademik_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER kalender_akademik_bu BEFORE UPDATE ON public."KalenderAkademik" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Kelas kelas_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER kelas_bu BEFORE UPDATE ON public."Kelas" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: KelasGrup kelas_grup_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER kelas_grup_bu BEFORE UPDATE ON public."KelasGrup" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: KelasSiswa kelas_siswa_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER kelas_siswa_bu BEFORE UPDATE ON public."KelasSiswa" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Pelajaran pelajaran_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pelajaran_bu BEFORE UPDATE ON public."Pelajaran" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Pembayaran pembayaran_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pembayaran_bu BEFORE UPDATE ON public."Pembayaran" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: PembayaranLainnya pembayaran_lainnya_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pembayaran_lainnya_bu BEFORE UPDATE ON public."PembayaranLainnya" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: PembayaranLainnyaDetail pembayaran_lainnya_detail_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pembayaran_lainnya_detail_bu BEFORE UPDATE ON public."PembayaranLainnyaDetail" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: PembayaranSPP pembayaran_spp_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pembayaran_spp_bu BEFORE UPDATE ON public."PembayaranSPP" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Pemberitahuan pemberitahuan_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER pemberitahuan_bu BEFORE UPDATE ON public."Pemberitahuan" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Penilaian penilaian_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER penilaian_bu BEFORE UPDATE ON public."Penilaian" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: PenilaianDetail penilaian_detail_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER penilaian_detail_bu BEFORE UPDATE ON public."PenilaianDetail" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Periode periode_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER periode_bu BEFORE UPDATE ON public."Periode" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Semester semester_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER semester_bu BEFORE UPDATE ON public."Semester" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Siswa siswa_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER siswa_bu BEFORE UPDATE ON public."Siswa" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: SiswaDeviceToken siswa_device_token_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER siswa_device_token_bu BEFORE UPDATE ON public."SiswaDeviceToken" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: User user_bu; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER user_bu BEFORE UPDATE ON public."User" FOR EACH ROW EXECUTE FUNCTION public.trigger_set_timestamp_on_update();


--
-- Name: Absensi Absensi_kalenderAkademikId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Absensi"
    ADD CONSTRAINT "Absensi_kalenderAkademikId_fkey" FOREIGN KEY ("kalenderAkademikId") REFERENCES public."KalenderAkademik"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Absensi Absensi_kelasSiswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Absensi"
    ADD CONSTRAINT "Absensi_kelasSiswaId_fkey" FOREIGN KEY ("kelasSiswaId") REFERENCES public."KelasSiswa"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Deposito Deposito_jenisPembayaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Deposito"
    ADD CONSTRAINT "Deposito_jenisPembayaranId_fkey" FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: Deposito Deposito_siswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Deposito"
    ADD CONSTRAINT "Deposito_siswaId_fkey" FOREIGN KEY ("siswaId") REFERENCES public."Siswa"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: GenerateVA GenerateVA_jenisPembayaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."GenerateVA"
    ADD CONSTRAINT "GenerateVA_jenisPembayaranId_fkey" FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id);


--
-- Name: GenerateVA GenerateVA_siswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."GenerateVA"
    ADD CONSTRAINT "GenerateVA_siswaId_fkey" FOREIGN KEY ("siswaId") REFERENCES public."Siswa"(id);


--
-- Name: JadwalPelajaran JadwalPelajaran_guruId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JadwalPelajaran"
    ADD CONSTRAINT "JadwalPelajaran_guruId_fkey" FOREIGN KEY ("guruId") REFERENCES public."Guru"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: JadwalPelajaran JadwalPelajaran_kelasGrupId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JadwalPelajaran"
    ADD CONSTRAINT "JadwalPelajaran_kelasGrupId_fkey" FOREIGN KEY ("kelasGrupId") REFERENCES public."KelasGrup"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: JadwalPelajaran JadwalPelajaran_pelajaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."JadwalPelajaran"
    ADD CONSTRAINT "JadwalPelajaran_pelajaranId_fkey" FOREIGN KEY ("pelajaranId") REFERENCES public."Pelajaran"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: KalenderAkademik KalenderAkademik_guruId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KalenderAkademik"
    ADD CONSTRAINT "KalenderAkademik_guruId_fkey" FOREIGN KEY ("guruId") REFERENCES public."Guru"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: KalenderAkademik KalenderAkademik_kelasGrupId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KalenderAkademik"
    ADD CONSTRAINT "KalenderAkademik_kelasGrupId_fkey" FOREIGN KEY ("kelasGrupId") REFERENCES public."KelasGrup"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: KalenderAkademik KalenderAkademik_pelajaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KalenderAkademik"
    ADD CONSTRAINT "KalenderAkademik_pelajaranId_fkey" FOREIGN KEY ("pelajaranId") REFERENCES public."Pelajaran"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: KelasGrup KelasGrup_kelasId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasGrup"
    ADD CONSTRAINT "KelasGrup_kelasId_fkey" FOREIGN KEY ("kelasId") REFERENCES public."Kelas"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: KelasGrup KelasGrup_periodeId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasGrup"
    ADD CONSTRAINT "KelasGrup_periodeId_fkey" FOREIGN KEY ("periodeId") REFERENCES public."Periode"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: KelasGrup KelasGrup_waliKelasId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasGrup"
    ADD CONSTRAINT "KelasGrup_waliKelasId_fkey" FOREIGN KEY ("waliKelasId") REFERENCES public."Guru"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: KelasSiswa KelasSiswa_kelasGrupId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasSiswa"
    ADD CONSTRAINT "KelasSiswa_kelasGrupId_fkey" FOREIGN KEY ("kelasGrupId") REFERENCES public."KelasGrup"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: KelasSiswa KelasSiswa_siswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."KelasSiswa"
    ADD CONSTRAINT "KelasSiswa_siswaId_fkey" FOREIGN KEY ("siswaId") REFERENCES public."Siswa"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: Notification Notification_siswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Notification"
    ADD CONSTRAINT "Notification_siswaId_fkey" FOREIGN KEY ("siswaId") REFERENCES public."Siswa"(id);


--
-- Name: PembayaranLainnyaDetail PembayaranLainnyaDetail_kelasSiswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnyaDetail"
    ADD CONSTRAINT "PembayaranLainnyaDetail_kelasSiswaId_fkey" FOREIGN KEY ("kelasSiswaId") REFERENCES public."KelasSiswa"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PembayaranLainnyaDetail PembayaranLainnyaDetail_pembayaranLainnyaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnyaDetail"
    ADD CONSTRAINT "PembayaranLainnyaDetail_pembayaranLainnyaId_fkey" FOREIGN KEY ("pembayaranLainnyaId") REFERENCES public."PembayaranLainnya"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PembayaranLainnya PembayaranLainnya_jenisPembayaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnya"
    ADD CONSTRAINT "PembayaranLainnya_jenisPembayaranId_fkey" FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PembayaranLainnya PembayaranLainnya_kelasGrupId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranLainnya"
    ADD CONSTRAINT "PembayaranLainnya_kelasGrupId_fkey" FOREIGN KEY ("kelasGrupId") REFERENCES public."KelasGrup"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PembayaranSPP PembayaranSPP_jenisPembayaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranSPP"
    ADD CONSTRAINT "PembayaranSPP_jenisPembayaranId_fkey" FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PembayaranSPP PembayaranSPP_kelasSiswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PembayaranSPP"
    ADD CONSTRAINT "PembayaranSPP_kelasSiswaId_fkey" FOREIGN KEY ("kelasSiswaId") REFERENCES public."KelasSiswa"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: Pembayaran Pembayaran_jenisPembayaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pembayaran"
    ADD CONSTRAINT "Pembayaran_jenisPembayaranId_fkey" FOREIGN KEY ("jenisPembayaranId") REFERENCES public."JenisPembayaran"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: Pembayaran Pembayaran_kelasSiswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Pembayaran"
    ADD CONSTRAINT "Pembayaran_kelasSiswaId_fkey" FOREIGN KEY ("kelasSiswaId") REFERENCES public."KelasSiswa"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: PenilaianDetail PenilaianDetail_jadwalPelajaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PenilaianDetail"
    ADD CONSTRAINT "PenilaianDetail_jadwalPelajaranId_fkey" FOREIGN KEY ("jadwalPelajaranId") REFERENCES public."JadwalPelajaran"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: PenilaianDetail PenilaianDetail_kelasSiswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PenilaianDetail"
    ADD CONSTRAINT "PenilaianDetail_kelasSiswaId_fkey" FOREIGN KEY ("kelasSiswaId") REFERENCES public."KelasSiswa"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: PenilaianDetail PenilaianDetail_penilaianId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."PenilaianDetail"
    ADD CONSTRAINT "PenilaianDetail_penilaianId_fkey" FOREIGN KEY ("penilaianId") REFERENCES public."Penilaian"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Penilaian Penilaian_guruId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_guruId_fkey" FOREIGN KEY ("guruId") REFERENCES public."Guru"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Penilaian Penilaian_jenisPenilaianId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_jenisPenilaianId_fkey" FOREIGN KEY ("jenisPenilaianId") REFERENCES public."JenisPenilaian"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Penilaian Penilaian_kelasId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_kelasId_fkey" FOREIGN KEY ("kelasId") REFERENCES public."Kelas"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Penilaian Penilaian_pelajaranId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_pelajaranId_fkey" FOREIGN KEY ("pelajaranId") REFERENCES public."Pelajaran"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Penilaian Penilaian_periodeId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Penilaian"
    ADD CONSTRAINT "Penilaian_periodeId_fkey" FOREIGN KEY ("periodeId") REFERENCES public."Periode"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: Semester Semester_periodeId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."Semester"
    ADD CONSTRAINT "Semester_periodeId_fkey" FOREIGN KEY ("periodeId") REFERENCES public."Periode"(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: SiswaDeviceToken SiswaDeviceToken_siswaId_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."SiswaDeviceToken"
    ADD CONSTRAINT "SiswaDeviceToken_siswaId_fkey" FOREIGN KEY ("siswaId") REFERENCES public."Siswa"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


