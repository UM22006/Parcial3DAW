package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlHorasRepository extends JpaRepository<ControlHoras, Long> {
    List<ControlHoras> findByEstudiante_Carnet(String carnet);
}