package org.example.domain.trashcan.controller;

import java.util.stream.Collectors;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.TrashcanLocationDto;
import org.example.domain.trashcan.service.TrashcanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trashcan")
public class TrashcanController {

    @Autowired
    private TrashcanService trashcanService;

    @GetMapping("/{id}")
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
    @GetMapping("/locations")
    public ResponseEntity<?> getTrashcanLocations(@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude, @RequestParam("radius") double radius) {
        try {
            List<Trashcan> trashcans = trashcanService.findTrashcansNear(latitude, longitude, radius);

            List<TrashcanLocationDto> trashcanLocationDtos = trashcans.stream().map(trashcan -> {
                TrashcanLocationDto dto = new TrashcanLocationDto();
                dto.setTrashcanId(trashcan.getId());
                dto.setLatitude(trashcan.getLocation().getY());
                dto.setLongitude(trashcan.getLocation().getX());
                dto.setAddressDetail(trashcan.getAddressDetail());
                dto.setViews(trashcan.getViews());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok().body(trashcanLocationDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Locations not found.\"}");
        }
    }

}
