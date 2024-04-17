package org.example.domain.trashcan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.request.TrashcanLocationRequest;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponse;
import org.example.domain.trashcan.dto.response.TrashcanLocationResponse;
import org.example.domain.trashcan.dto.response.TrashcanMessageResponse;
import org.example.domain.trashcan.service.TrashcanService;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.jwt.JwtProvider;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "trashcan", description = "쓰레기통 api")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trashcan")
public class TrashcanController {

    private final TrashcanService trashcanService;
    private final JwtProvider jwtProvider;
    @GetMapping("/locations")
    @Operation(summary = "쓰레기통 찾기", description = "반경 내 쓰레기통 찾기",
            parameters = {
                    @Parameter(name = "radius", description = "반경(m)", required = true)
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = {
                            @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TrashcanLocationResponse.class))
                            )
                    }
            ),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 status 값이거나, 해당 조건에 맞는 쓰레기통이 존재하지 않습니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getTrashcanLocations(@ModelAttribute TrashcanLocationRequest requestDto) {
        List<String> validStatuses = Arrays.asList("added", "registered", "suggested", "removed");
        if (!validStatuses.contains(requestDto.getStatus())) {
            ErrorMessage errorMessage = new ErrorMessage("유효하지 않은 status 값입니다.");
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST); // BAD_REQUEST와 함께 ErrorMessage 반환
        }

        List<Trashcan> trashcans = trashcanService.findTrashcansNear(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius(), requestDto.getStatus());
        if (trashcans.isEmpty()) {
            ErrorMessage errorMessage = new ErrorMessage("해당 조건에 맞는 쓰레기통이 존재하지 않습니다.");
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND); // NOT_FOUND와 함께 ErrorMessage 반환
        }

        List<TrashcanLocationResponse> responseList = new ArrayList<>();

        for (Trashcan trashcan : trashcans) {
            Integer count = 0;

            if ("registered".equals(requestDto.getStatus())) {
                count = trashcanService.getRegistrationCountForTrashcan(trashcan.getId());
            } else if ("suggested".equals(requestDto.getStatus())) {
                count = trashcanService.getSuggestionCountForTrashcan(trashcan.getId());
            }

            TrashcanLocationResponse response = new TrashcanLocationResponse(
                    trashcan.getId(),
                    trashcan.getLocation().getY(),
                    trashcan.getLocation().getX(),
                    trashcan.getAddressDetail(),
                    trashcan.getViews(),
                    count
            );

            responseList.add(response);
        }

        return ResponseEntity.ok().body(responseList);
    }

    @GetMapping("/locations/details/{id}")
    @Operation(summary = "쓰레기통 정보", description = "쓰레기통 상세정보")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 상세정보 가져오기 성공",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrashcanDetailsResponse.class))}),
            @ApiResponse(responseCode = "404", description = "쓰레기통 정보를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> getTrashcanDetails(@PathVariable("id") Long id) {
        Optional<Trashcan> trashcanOptional = trashcanService.getTrashcanDetails(id);

        if (!trashcanOptional.isPresent()) {
            ErrorMessage errorMessage = new ErrorMessage("쓰레기통 정보를 찾을 수 없음");
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND); // 직접 ResponseEntity를 반환
        }

        trashcanService.increaseTrashcanViews(id);

        Trashcan trashcan = trashcanOptional.get();
        List<String> images = trashcanService.getImagesByTrashcanId(id).stream()
                .map(Image::getImage)
                .collect(Collectors.toList());
        List<String> descriptions = trashcanService.getDescriptionsByTrashcanId(id).stream()
                .map(Description::getDescription)
                .collect(Collectors.toList());
        int count = 0;

        if (trashcan.getStatus().equals("registered")) {
            count = trashcanService.getRegistrationCountForTrashcan(trashcan.getId());
        } else if (trashcan.getStatus().equals("suggested")) {
            count = trashcanService.getSuggestionCountForTrashcan(trashcan.getId());
        }

        TrashcanDetailsResponse response = new TrashcanDetailsResponse(
                trashcan.getId(),
                trashcan.getAddress(),
                trashcan.getAddressDetail(),
                images, // 이미지 URL 리스트
                descriptions, // 설명 텍스트 리스트
                trashcan.getViews(),
                trashcan.getStatus(),
                count
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/registrations")
    @Operation(summary = "쓰레기통 등록", description = "새로운 쓰레기통 위치를 등록합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 위치 등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrashcanMessageResponse.class))),
    })
    public ResponseEntity<?> registerTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("address_detail") String addressDetail,
            @RequestParam("address") String address,
            @RequestParam("description") String description,
            @RequestParam("image_object") List<MultipartFile> imageObjects){
        try {
            String token = jwtProvider.resolveAccessToken(request);

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            Trashcan trashcan = new Trashcan();
            trashcan.setLocation(location);
            trashcan.setAddressDetail(addressDetail);
            trashcan.setAddress(address);
            trashcan.setStatus("registered");

            Trashcan registeredTrashcan = trashcanService.registerTrashcan(trashcan, imageObjects, description, token);
            return new ResponseEntity<>(new TrashcanMessageResponse("Location registered successfully."), HttpStatus.CREATED);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage("Invalid request data.");
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage("An error occurred.");
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/registrations/{id}")
    @Operation(summary = "등록 쓰레기통 정보 추가", description = "기존 쓰레기통의 정보를 추가합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 정보 추가 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> registerTrashcanId(
            @PathVariable("id") Long trashcanId,
            HttpServletRequest request,
            @RequestParam("image_object") List<MultipartFile> imageObjects,
            @RequestParam("description") String description) {
        try {
            String token = jwtProvider.resolveAccessToken(request);
            trashcanService.registerTrashcanId(trashcanId, imageObjects, description, token);
            return ResponseEntity.ok().body(new TrashcanMessageResponse("정보 추가 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage("잘못된 요청 데이터: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage("서버 내부 오류: " + e.getMessage()));
        }
    }

    @PostMapping("/suggestions")
    @Operation(summary = "쓰레기통 위치 제안", description = "쓰레기통이 설치되길 원하는 위치를 제안합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "쓰레기통 위치 제안 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrashcanMessageResponse.class))),
    })
    public ResponseEntity<?> suggestTrashcan(
            HttpServletRequest request,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("address_detail") String addressDetail,
            @RequestParam("address") String address,
            @RequestParam("description") String description,
            @RequestParam("image_object") List<MultipartFile> imageObjects) {
        try {
            String token = jwtProvider.resolveAccessToken((HttpServletRequest) request);

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            Trashcan trashcan = new Trashcan();
            trashcan.setLocation(location);
            trashcan.setAddressDetail(addressDetail);
            trashcan.setAddress(address);
            trashcan.setStatus("suggested");

            Trashcan suggestedTrashcan = trashcanService.suggestTrashcan(trashcan, imageObjects, description, token);
            return new ResponseEntity<>(new TrashcanMessageResponse("Location registered successfully."), HttpStatus.CREATED);
        } catch (IOException e) {
            ErrorMessage errorMessage = new ErrorMessage("Invalid request data.");
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ErrorMessage errorMessage = new ErrorMessage("An error occurred.");
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/suggestions/{id}")
    @Operation(summary = "제안 쓰레기통 정보 추가", description = "기존 쓰레기통의 정보를 추가합니다. 이 작업은 인증된 사용자만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓰레기통 정보 추가 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> suggestTrashcanId(
            @PathVariable("id") Long trashcanId,
            HttpServletRequest request,
            @RequestParam("image_object") List<MultipartFile> imageObjects,
            @RequestParam("description") String description) {
        try {
            String token = jwtProvider.resolveAccessToken(request);
            trashcanService.suggestTrashcanId(trashcanId, imageObjects, description, token);
            return ResponseEntity.ok().body(new TrashcanMessageResponse("정보 추가 성공"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage("잘못된 요청 데이터: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage("서버 내부 오류: " + e.getMessage()));
        }
    }
}
