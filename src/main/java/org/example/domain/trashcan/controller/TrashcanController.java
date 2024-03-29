package org.example.domain.trashcan.controller;

import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.service.TrashcanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/trashcans")
public class TrashcanController {

    private final TrashcanService trashcanService;

    @Autowired
    public TrashcanController(TrashcanService trashcanService) {
        this.trashcanService = trashcanService;
    }

    @GetMapping("/trashcan/{id}")
    public ResponseEntity<Trashcan> getTrashcanById(@PathVariable("id") Long id) {
        return trashcanService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Trashcan>> getAllTrashcans() {
        List<Trashcan> trashcans = trashcanService.findAll();
        return ResponseEntity.ok(trashcans);
    }
}
