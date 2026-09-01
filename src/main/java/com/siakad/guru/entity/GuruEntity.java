package com.siakad.guru.entity;

import com.siakad.common.enums.GuruStatus;
import com.siakad.common.enums.JK;
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
 * Pemetaan JPA ke tabel legacy {@code "Guru"}.
 */
@Entity
@Table(name = "\"Guru\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuruEntity {

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

    @Column(name = "nip", length = 45, nullable = false)
    private String nip;

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
    @Column(name = "status", nullable = false, columnDefinition = "\"GuruStatus\"")
    private GuruStatus status;

    @Column(name = "\"pendidikanTerakhir\"", length = 45)
    private String pendidikanTerakhir;

    @Column(name = "\"tglLahir\"")
    private OffsetDateTime tglLahir;

    @Column(name = "\"tmptLahir\"", length = 45)
    private String tmptLahir;

    @Column(name = "jabatan", length = 45)
    private String jabatan;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;

    @Column(name = "\"createdBy\"", length = 45)
    private String createdBy;

    @Column(name = "\"updatedBy\"", length = 45)
    private String updatedBy;
}
