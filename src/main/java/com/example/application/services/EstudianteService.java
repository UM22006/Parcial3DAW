package com.example.application.services;

import com.example.application.data.Estudiante;
import com.example.application.data.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstudianteService {

    private final EstudianteRepository repository;

    public EstudianteService(EstudianteRepository repository) {
        this.repository = repository;
    }

    // Buscar estudiante por carnet
    public Optional<Estudiante> findByCarnet(String carnet) {
        return repository.findByCarnet(carnet);
    }

    public List<Estudiante> findAll() {
        return repository.findAll();
    }

    public void save(Estudiante estudiante) {
        repository.save(estudiante);
    }

    public void delete(Estudiante estudiante) {
        repository.delete(estudiante);
    }
}
