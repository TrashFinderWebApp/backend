package org.example.domain.trashcan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.TrashcanLocationDto;
import org.example.domain.trashcan.dto.request.TrashcanRegistrationRequest;
import org.example.domain.trashcan.dto.response.TrashcanRegistrationResponse;
import org.example.domain.trashcan.service.TrashcanService;
import org.example.global.security.jwt.JwtProvider;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "trashcan", description = "쓰레기통 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trashcan")
public class TrashcanController {

    private final TrashcanService trashcanService;
    private final JwtProvider jwtProvider;
    @GetMapping("/locations")
    @Operation(summary = "쓰레기통 찾기", description = "반경 내 쓰레기통 찾기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 쓰레기통 위치를 찾았습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 파라미터를 확인해주세요."),
            @ApiResponse(responseCode = "404", description = "지정된 조건에 맞는 쓰레기통 위치를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다.")
    })
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
    @Operation(summary = "쓰레기통 정보", description = "쓰레기통 상세정보")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 상세정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "쓰레기통 정보를 찾을 수 없음"),
    })
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

    @PostMapping("/registrations")
    @Operation(summary = "쓰레기통 등록", description = "쓰레기통 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    })
    public ResponseEntity<?> registerTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("address_detail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("image_object") List<MultipartFile> imageObjects){
        String encryptedRefreshToken = jwtProvider.resolveRefreshToken(request);
        if (encryptedRefreshToken == null) {
            return new ResponseEntity<>("헤더에 refresh token이 없습니다. 다시 로그인해주세요.",HttpStatus.UNAUTHORIZED);
        }
        try {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            Trashcan trashcan = new Trashcan();
            trashcan.setLocation(location);
            trashcan.setAddressDetail(addressDetail);
            trashcan.setStatus("registered");

            Trashcan registeredTrashcan = trashcanService.registerTrashcan(trashcan, imageObjects, description, encryptedRefreshToken);
            return new ResponseEntity<>(new TrashcanRegistrationResponse("Location registered successfully."), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new TrashcanRegistrationResponse("Invalid request data."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/suggestions")
    @Operation(summary = "쓰레기통 위치 제안", description = "쓰레기통 위치 제안")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 제안 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    })
    public ResponseEntity<?> suggestTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("address_detail") String addressDetail,
            @RequestParam("description") String description,
            @RequestParam("image_object") List<MultipartFile> imageObjects) {
        String encryptedRefreshToken = jwtProvider.resolveRefreshToken(request);
        if (encryptedRefreshToken == null) {
            return new ResponseEntity<>("헤더에 refresh token이 없습니다. 다시 로그인해주세요.",HttpStatus.UNAUTHORIZED);
        }
        try {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            Trashcan trashcan = new Trashcan();
            trashcan.setLocation(location);
            trashcan.setAddressDetail(addressDetail);
            trashcan.setStatus("suggested");

            Trashcan suggestedTrashcan = trashcanService.suggestTrashcan(trashcan, imageObjects, description, encryptedRefreshToken);
            return new ResponseEntity<>(new TrashcanRegistrationResponse("Location registered successfully."), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new TrashcanRegistrationResponse("Invalid request data."), HttpStatus.BAD_REQUEST);
        }
    }
}
