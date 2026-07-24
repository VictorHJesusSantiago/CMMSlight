package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "checklist_item")
@Getter
@Setter
@NoArgsConstructor
public class ChecklistItem {

    public enum ItemType { YES_NO, TEXT, NUMBER, MULTIPLE_CHOICE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_template_id", nullable = false)
    private ChecklistTemplate checklistTemplate;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType = ItemType.YES_NO;

    /** Lista JSON de opcoes, usada apenas quando itemType = MULTIPLE_CHOICE, ex: ["OK","Ajustar","Trocar"]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String options;

    @Column(nullable = false)
    private boolean required = true;
}
