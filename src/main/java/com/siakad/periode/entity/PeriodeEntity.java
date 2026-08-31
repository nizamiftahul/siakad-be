package com.siakad.periode.entity;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Pemetaan JPA ke tabel legacy {@code "Periode"}.
 *
 * <p>Kolom {@code jenjang} menjadi batasan tenancy: setiap record periode terkait
 * dengan jenjang tertentu, dan akses selalu dibatasi ke jenjang dari session akun
 * (sama seperti {@code SiswaEntity}). Kolom {@code status} adalah flag boolean
 * aktif/non-aktif, bukan enum.
 */
@Entity
@Table(name = "\"Periode\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodeEntity {

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

    @Column(name = "\"tglMulai\"", nullable = false)
    private LocalDate tglMulai;

    @Column(name = "\"tglSelesai\"", nullable = false)
    private LocalDate tglSelesai;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}