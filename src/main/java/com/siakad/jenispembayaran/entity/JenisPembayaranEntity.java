package com.siakad.jenispembayaran.entity;

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

@Entity
@Table(name = "\"JenisPembayaran\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JenisPembayaranEntity {

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

    @Column(name = "jenis", length = 45, nullable = false)
    private String jenis;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;

    @Column(name = "\"kodePembayaran\"", length = 3)
    private String kodePembayaran;

    @Column(name = "\"order\"")
    private Integer order;

    @Column(name = "kode", length = 5)
    private String kode;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
