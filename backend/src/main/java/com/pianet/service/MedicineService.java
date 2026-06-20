package com.pianet.service;

import com.pianet.dto.MedicineRequest;
import com.pianet.entity.Medicine;
import com.pianet.repository.MedicineRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicineService {

  private final MedicineRepository medicineRepository;
  private final AccessService accessService;

  public List<Medicine> list(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return medicineRepository.findAll();
    }
    return medicineRepository.findByNameContainingIgnoreCaseOrderByName(keyword.trim());
  }

  @Transactional(readOnly = true)
  public Medicine get(Long id) {
    return medicineRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("药品不存在"));
  }

  @Transactional
  public Medicine create(MedicineRequest req, Long uid) {
    accessService.requireAdmin(uid);
    Medicine m = new Medicine();
    apply(m, req);
    return medicineRepository.save(m);
  }

  @Transactional
  public Medicine update(Long id, MedicineRequest req, Long uid) {
    accessService.requireAdmin(uid);
    Medicine m = get(id);
    apply(m, req);
    return medicineRepository.save(m);
  }

  @Transactional
  public void delete(Long id, Long uid) {
    accessService.requireAdmin(uid);
    medicineRepository.deleteById(id);
  }

  private static void apply(Medicine m, MedicineRequest req) {
    m.setName(req.getName().trim());
    m.setCategory(req.getCategory());
    m.setSpecification(req.getSpecification());
    m.setUsageText(req.getUsageText());
    m.setTaboo(req.getTaboo());
    if (req.getStock() != null) {
      m.setStock(req.getStock());
    }
  }
}
