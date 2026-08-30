package com.siakad.siswa.entity;

import com.siakad.common.enums.JK;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.SiswaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Pemetaan JPA ke tabel legacy {@code "Siswa"}. Kolom {@code hashedPassword}
 * sengaja tidak
 * dipetakan karena tidak dipakai oleh fitur CRUD ini (reserved untuk login
 * siswa di masa depan).
 */
@Entity
@Table(name = "\"Siswa\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiswaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private OffsetDateTime updatedAt;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "nisn", length = 45)
    private String nisn;

    @Column(name = "nama", length = 45, nullable = false)
    private String nama;

    @Column(name = "email", length = 45)
    private String email;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "\"jenisKelamin\"", columnDefinition = "\"JK\"")
    private JK jenisKelamin;

    @Column(name = "alamat", columnDefinition = "text")
    private String alamat;

    @Column(name = "telepon", length = 45)
    private String telepon;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "\"SiswaStatus\"")
    private SiswaStatus status;

    @Column(name = "\"asalSekolah\"", length = 45)
    private String asalSekolah;

    @Column(name = "\"namaAyah\"", length = 45)
    private String namaAyah;

    @Column(name = "\"pekerjaanAyah\"")
    private String pekerjaanAyah;

    @Column(name = "\"alamatAyah\"", columnDefinition = "text")
    private String alamatAyah;

    @Column(name = "\"pendidikanAyah\"", length = 45)
    private String pendidikanAyah;

    @Column(name = "\"gajiAyah\"")
    private Integer gajiAyah;

    @Column(name = "\"namaIbu\"", length = 45)
    private String namaIbu;

    @Column(name = "\"pekerjaanIbu\"", length = 45)
    private String pekerjaanIbu;

    @Column(name = "\"alamatIbu\"", columnDefinition = "text")
    private String alamatIbu;

    @Column(name = "\"pendidikanIbu\"", length = 45)
    private String pendidikanIbu;

    @Column(name = "\"gajiIbu\"")
    private Integer gajiIbu;

    @Column(name = "\"tglLahir\"")
    private LocalDate tglLahir;

    @Column(name = "nis", length = 45, nullable = false)
    private String nis;

    @Column(name = "\"tmptLahir\"", length = 45)
    private String tmptLahir;

    @Column(name = "domisili", columnDefinition = "text")
    private String domisili;

    @Column(name = "\"namaWali\"", length = 45)
    private String namaWali;

    @Column(name = "\"pekerjaanWali\"", length = 45)
    private String pekerjaanWali;

    @Column(name = "\"alamatWali\"", columnDefinition = "text")
    private String alamatWali;

    @Column(name = "\"pendidikanWali\"", length = 45)
    private String pendidikanWali;

    @Column(name = "\"gajiWali\"")
    private Integer gajiWali;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;

    @Column(name = "\"isAlumni\"")
    private Boolean isAlumni;
}
