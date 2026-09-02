package com.siakad.kelassiswa.entity;

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
 * Pemetaan JPA ke tabel legacy {@code "KelasSiswa"}.
 *
 * <p>Kolom {@code spp} dan {@code potonganSpp} bertipe {@code money} di Postgres. Binding
 * parameter {@link BigDecimal} langsung ke kolom money gagal ("column is of type money but
 * expression is of type numeric"), sehingga penulisan memakai cast eksplisit
 * {@code ?::money} lewat {@link ColumnTransformer}; pembacaan tidak perlu cast karena
 * driver pgjdbc sudah bisa mem-parse kolom money ke {@link BigDecimal}.
 */
@Entity
@Table(name = "\"KelasSiswa\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KelasSiswaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private OffsetDateTime updatedAt;

    @Column(name = "\"siswaId\"", nullable = false)
    private Integer siswaId;

    @Column(name = "\"kelasGrupId\"", nullable = false)
    private Integer kelasGrupId;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ColumnTransformer(write = "?::money")
    @Column(name = "spp", nullable = false, columnDefinition = "money")
    private BigDecimal spp;

    @ColumnTransformer(write = "?::money")
    @Column(name = "\"potonganSpp\"", nullable = false, columnDefinition = "money")
    private BigDecimal potonganSpp;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
