package com.pianet.repository;

import com.pianet.entity.Medicine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

  List<Medicine> findByNameContainingIgnoreCaseOrderByName(String q);
}
