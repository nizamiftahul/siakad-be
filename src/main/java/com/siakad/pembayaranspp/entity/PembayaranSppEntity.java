package com.siakad.pembayaranspp.entity;

import com.siakad.common.enums.PembayaranStatus;
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
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Pemetaan JPA ke tabel legacy {@code "PembayaranSPP"}.
 *
 * <p>Kolom {@code pembayaranId} dan {@code semesterId} sengaja tidak dipetakan
 * karena belum ada entity Java untuk tabel yang direferensikan (di luar cakupan
 * fitur ini).
 */
@Entity
@Table(name = "\"PembayaranSPP\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PembayaranSppEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"kelasSiswaId\"", nullable = false)
    private Integer kelasSiswaId;

    @Column(name = "\"jenisPembayaranId\"")
    private Integer jenisPembayaranId;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private OffsetDateTime updatedAt;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ColumnTransformer(write = "?::money")
    @Column(name = "spp", nullable = false, columnDefinition = "money")
    private BigDecimal spp;

    @ColumnTransformer(write = "?::money")
    @Column(name = "\"potonganSpp\"", nullable = false, columnDefinition = "money")
    private BigDecimal potonganSpp;

    @Column(name = "bulan", nullable = false)
    private Integer bulan;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "\"tglPembayaran\"")
    private OffsetDateTime tglPembayaran;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "\"PembayaranStatus\"")
    private PembayaranStatus status;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
