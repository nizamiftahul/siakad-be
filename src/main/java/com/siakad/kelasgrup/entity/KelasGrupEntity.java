package com.siakad.kelasgrup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Pemetaan JPA ke tabel legacy {@code "KelasGrup"}.
 *
 * <p>Tabel ini tidak memiliki kolom {@code jenjang} sendiri; tenancy diturunkan secara
 * transitif dari {@code kelasId} (harus merujuk {@code Kelas} pada jenjang session) dan
 * divalidasi bersama {@code periodeId}/{@code waliKelasId} di service layer, bukan lewat
 * relasi JPA (mengikuti konvensi entity flat lain di codebase ini).
 *
 * <p>Kolom {@code defaultSpp} bertipe {@code money} di Postgres. Binding parameter
 * {@link BigDecimal} langsung ke kolom money gagal ("column is of type money but
 * expression is of type numeric"), sehingga penulisan memakai cast eksplisit
 * {@code ?::money} lewat {@link ColumnTransformer}; pembacaan tidak perlu cast karena
 * driver pgjdbc sudah bisa mem-parse kolom money ke {@link BigDecimal}.
 */
@Entity
@Table(name = "\"KelasGrup\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KelasGrupEntity {

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

    @Column(name = "nama", length = 45, nullable = false)
    private String nama;

    @Column(name = "\"kelasId\"", nullable = false)
    private Integer kelasId;

    @Column(name = "\"periodeId\"", nullable = false)
    private Integer periodeId;

    @Column(name = "\"waliKelasId\"")
    private Integer waliKelasId;

    @ColumnTransformer(write = "?::money")
    @Column(name = "\"defaultSpp\"", nullable = false, columnDefinition = "money")
    private BigDecimal defaultSpp;

    @Column(name = "icp")
    private Boolean icp;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
