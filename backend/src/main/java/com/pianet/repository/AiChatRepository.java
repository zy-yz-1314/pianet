package com.pianet.repository;

import com.pianet.entity.AiChat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {

  List<AiChat> findByPatientIdOrderByChatTimeDesc(Long patientId);
}
