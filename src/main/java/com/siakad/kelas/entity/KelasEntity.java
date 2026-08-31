package com.siakad.kelas.entity;

import com.siakad.common.enums.Jenjang;
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

import java.time.OffsetDateTime;

/**
 * Pemetaan JPA ke tabel legacy {@code "Kelas"}.
 *
 * <p>Kolom {@code nama} memiliki unique index global (bukan per-jenjang), sesuai
 * {@code "Kelas.nama_unique"} pada skema legacy. Kolom {@code jenjang} menjadi
 * batasan tenancy: akses selalu dibatasi ke jenjang dari session akun (sama
 * seperti {@code SiswaEntity} / {@code PeriodeEntity}).
 */
@Entity
@Table(name = "\"Kelas\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KelasEntity {

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

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;
}
