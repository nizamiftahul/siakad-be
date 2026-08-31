package com.siakad.guru.entity;

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

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pemetaan JPA minimal ke tabel legacy {@code "Guru"}, dipakai saat ini hanya untuk
 * validasi referensi (mis. {@code waliKelasId} pada {@code KelasGrup}). Belum ada
 * fitur CRUD Guru sehingga kolom lain di luar id/nama/jenjang sengaja tidak dipetakan.
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

    @Column(name = "nama", length = 45, nullable = false)
    private String nama;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "jenjang", nullable = false, columnDefinition = "\"Jenjang\"")
    private Jenjang jenjang;
}
