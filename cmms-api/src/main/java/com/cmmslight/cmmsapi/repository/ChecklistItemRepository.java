package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    List<ChecklistItem> findByChecklistTemplateIdOrderBySortOrderAsc(Long checklistTemplateId);
}
