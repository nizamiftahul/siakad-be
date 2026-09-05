package com.siakad.pembayaranlainnya.entity;

import com.siakad.common.enums.PembayaranStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Maps public."PembayaranLainnyaDetail". "pembayaranLainnyaId" and "pembayaranId"
 * are legacy/out-of-scope FK columns and are deliberately left unmapped, same
 * precedent as PembayaranSppEntity's own unmapped pembayaranId/semesterId.
 */
@Entity
@Table(name = "\"PembayaranLainnyaDetail\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PembayaranLainnyaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"kelasSiswaId\"", nullable = false)
    private Integer kelasSiswaId;

    @Column(name = "\"jenisPembayaranId\"")
    private Integer jenisPembayaranId;

    @Column(name = "jenis", nullable = false, length = 45)
    private String jenis;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private OffsetDateTime updatedAt;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ColumnTransformer(write = "?::money")
    @Column(name = "jumlah", nullable = false, columnDefinition = "money")
    private BigDecimal jumlah;

    @ColumnTransformer(write = "?::money")
    @Column(name = "potongan", nullable = false, columnDefinition = "money")
    private BigDecimal potongan;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "\"PembayaranStatus\"")
    private PembayaranStatus status;

    @Column(name = "\"tglPembayaran\"")
    private OffsetDateTime tglPembayaran;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
