package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_order_checklist_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"work_order_id", "checklist_item_id"}))
@Getter
@Setter
@NoArgsConstructor
public class WorkOrderChecklistResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_item_id", nullable = false)
    private ChecklistItem checklistItem;

    @Column(nullable = false)
    private boolean completed = false;

    /** Valor respondido: "true"/"false" para YES_NO, texto livre, numero como string, ou opcao escolhida. */
    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
