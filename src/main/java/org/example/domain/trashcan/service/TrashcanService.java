package org.example.domain.trashcan.service;

import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.repository.TrashcanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TrashcanService{

    private final TrashcanRepository trashcanRepository;

    @Autowired
    public TrashcanService(TrashcanRepository trashcanRepository) {
        this.trashcanRepository = trashcanRepository;
    }

    public Optional<Trashcan> findById(Long id) {
        return trashcanRepository.findById(id);
    }

    public List<Trashcan> findAll() {
        return trashcanRepository.findAll();
    }
}
