package com.siakad.deposito.entity;

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

@Entity
@Table(name = "\"Deposito\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "\"siswaId\"", nullable = false)
    private Integer siswaId;

    @Column(name = "\"jenisPembayaranId\"", nullable = false)
    private Integer jenisPembayaranId;

    @ColumnTransformer(write = "?::money")
    @Column(name = "deposito", nullable = false, columnDefinition = "money")
    private BigDecimal deposito;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "\"updatedBy\"", nullable = false)
    private String updatedBy;
}
