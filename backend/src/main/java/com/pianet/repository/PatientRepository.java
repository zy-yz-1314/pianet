package com.pianet.repository;

import com.pianet.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

  @Query(
      "SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.visits WHERE p.id = :id")
  java.util.Optional<Patient> findByIdWithVisits(@Param("id") Long id);

  java.util.Optional<Patient> findByLinkedUser_Id(Long userId);

  Page<Patient> findByArchivedFalseOrderByIdDesc(Pageable pageable);

  Page<Patient> findAllByOrderByIdDesc(Pageable pageable);

  @Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR p.phone LIKE CONCAT('%',:kw,'%')")
  Page<Patient> searchByKeyword(@Param("kw") String kw, Pageable pageable);

  @Query("SELECT p FROM Patient p WHERE p.archived = false AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR p.phone LIKE CONCAT('%',:kw,'%'))")
  Page<Patient> searchByKeywordExcludeArchived(@Param("kw") String kw, Pageable pageable);
}
