package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "asset_type")
@Getter
@Setter
@NoArgsConstructor
public class AssetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * JSON array descrevendo os atributos customizados que ativos deste tipo podem ter, ex:
     * [{"name":"tensao","label":"Tensao (V)","type":"NUMBER","required":true}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes_schema", columnDefinition = "jsonb")
    private String customAttributesSchema;
}
