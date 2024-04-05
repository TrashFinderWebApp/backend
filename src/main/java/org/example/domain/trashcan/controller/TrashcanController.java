package org.example.domain.trashcan.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trashcan")
public class TrashcanController {

    @Autowired
    private TrashcanService trashcanService;

    @GetMapping("/locations")
    public ResponseEntity<?> getTrashcanLocations(@RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("radius") double radius,
            @RequestParam("status") String status) {
        try {
            List<Map<String, Object>> responseList = new ArrayList<>();
            List<Trashcan> trashcans = trashcanService.findTrashcansNear(latitude, longitude, radius, status);

            for (Trashcan trashcan : trashcans) {
                Map<String, Object> trashcanData = new HashMap<>();
                trashcanData.put("id", trashcan.getId());
                trashcanData.put("latitude", trashcan.getLocation().getY());
                trashcanData.put("longitude", trashcan.getLocation().getX());
                trashcanData.put("addressDetail", trashcan.getAddressDetail());
                trashcanData.put("views", trashcan.getViews());

                if ("registered".equals(status)) {
                    int registrationCount = trashcanService.getRegistrationCountForTrashcan(trashcan.getId());
                    trashcanData.put("registrationCount", registrationCount);
                } else if ("suggested".equals(status)) {
                    int suggestionCount = trashcanService.getSuggestionCountForTrashcan(trashcan.getId());
                    trashcanData.put("suggestionCount", suggestionCount);
                }
                responseList.add(trashcanData);
            }

            return ResponseEntity.ok().body(responseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\": \"Locations not found.\"}");
        }
    }

    @GetMapping("/locations/details/{id}")
    public ResponseEntity<?> getTrashcanDetails(@PathVariable("id") Long id) {
        Optional<Trashcan> trashcanOptional = trashcanService.getTrashcanDetails(id);
        if (!trashcanOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Location not found."));
        }

        Trashcan trashcan = trashcanOptional.get();
        List<Image> images = trashcanService.getImagesByTrashcanId(id);
        List<Description> descriptions = trashcanService.getDescriptionsByTrashcanId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("trashcan_id", trashcan.getId());
        response.put("address", trashcan.getAddress());
        response.put("address_detail", trashcan.getAddressDetail());
        response.put("image_urls", images);
        response.put("description", descriptions);
        response.put("views", trashcan.getViews());

        if (trashcan.getStatus().equals("registered")){
            int registrationCount = trashcanService.getRegistrationCountForTrashcan(trashcan.getId());
            response.put("registrationCount", registrationCount);
        } else if (trashcan.getStatus().equals("suggested")){
            int suggestionCount = trashcanService.getSuggestionCountForTrashcan(trashcan.getId());
            response.put("suggestionCount", suggestionCount);
        }

        return ResponseEntity.ok(response);
    }
}
