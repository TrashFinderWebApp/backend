package org.example.domain.trashcan.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.Member;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.service.MemberService;
import org.example.domain.rank.domain.ScoreDescription;
import org.example.domain.rank.repository.ScoreRepository;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Report;
import org.example.domain.trashcan.domain.Suggestion;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.request.TrashcanLocationRequest;
import org.example.domain.trashcan.dto.response.TrashcansPageResponse;
import org.example.domain.trashcan.dto.response.TrashcansResponse;
import org.example.domain.trashcan.dto.response.ReportPageResponse;
import org.example.domain.trashcan.dto.response.ReportResponse;
import org.example.domain.trashcan.dto.response.TrashcanListPageResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponse;
import org.example.domain.trashcan.dto.response.TrashcanListResponse;
import org.example.domain.trashcan.dto.response.TrashcanLocationResponse;
import org.example.domain.trashcan.dto.response.TrashcanMessageResponse;
import org.example.domain.trashcan.exception.ImageException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.example.domain.trashcan.repository.DescriptionRepository;
import org.example.domain.trashcan.repository.ImageRepository;
import org.example.domain.trashcan.repository.RegistrationRepository;
import org.example.domain.trashcan.repository.ReportRepository;
import org.example.domain.trashcan.repository.SuggestionRepository;
import org.example.domain.trashcan.repository.TrashcanRepository;
import org.example.global.security.jwt.JwtProvider;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.example.domain.rank.domain.Score;
import org.webjars.NotFoundException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TrashcanService{
    private final TrashcanRepository trashcanRepository;
    private final ImageRepository imageRepository;
    private final DescriptionRepository descriptionRepository;
    private final RegistrationRepository registrationRepository;
    private final SuggestionRepository suggestionRepository;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final ScoreRepository scoreRepository;
    private final ReportRepository reportRepository;

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileName;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public List<Trashcan> findTrashcansNear(double latitude, double longitude, double radius, String status) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return trashcanRepository.findWithinDistance(location, radius, status);
    }

    public List<TrashcanLocationResponse> findTrashcanLocations(TrashcanLocationRequest requestDto) {
        List<Trashcan> trashcans = findTrashcansNear(requestDto.getLatitude(), requestDto.getLongitude(), requestDto.getRadius(), requestDto.getStatus());

        List<TrashcanLocationResponse> responseList = new ArrayList<>();

        for (Trashcan trashcan : trashcans) {
            Integer count = 0;

            if ("REGISTERED".equals(requestDto.getStatus())) {
                count = getRegistrationCountForTrashcan(trashcan.getId());
            } else if ("SUGGESTED".equals(requestDto.getStatus())) {
                count = getSuggestionCountForTrashcan(trashcan.getId());
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

        return responseList;
    }

    public Optional<Trashcan> getTrashcanDetails(Long id) {
        return trashcanRepository.findById(id);
    }

    public List<Image> getImagesByTrashcanId(Long id) {
        return imageRepository.findByTrashcanId(id);
    }

    public List<Description> getDescriptionsByTrashcanId(Long id) {
        return descriptionRepository.findByTrashcanId(id);
    }

    public int getRegistrationCountForTrashcan(Long trashcanId) {
        return registrationRepository.countByTrashcanId(trashcanId);
    }

    public int getSuggestionCountForTrashcan(Long trashcanId) {
        return suggestionRepository.countByTrashcanId(trashcanId);
    }

    public void increaseTrashcanViews(Long id){
        Optional<Trashcan> trashcanOptional = trashcanRepository.findById(id);
        trashcanOptional.ifPresent(trashcan -> {
            trashcan.increaseViews();
            trashcanRepository.save(trashcan);
        });
    }

    public TrashcanDetailsResponse getTrashcanDetailsResponse(Long id) {
        Trashcan trashcan = getTrashcanDetails(id)
                .orElseThrow(() -> new TrashcanNotFoundException("쓰레기통 정보를 찾을 수 없음"));

        increaseTrashcanViews(id);

        List<String> images = getImagesByTrashcanId(id).stream()
                .map(Image::getImage)
                .collect(Collectors.toList());
        List<String> descriptions = getDescriptionsByTrashcanId(id).stream()
                .map(Description::getDescription)
                .collect(Collectors.toList());
        Integer count = 0;

        if ("REGISTERED".equals(trashcan.getStatus())) {
            count = getRegistrationCountForTrashcan(trashcan.getId());
        } else if ("SUGGESTED".equals(trashcan.getStatus())) {
            count = getSuggestionCountForTrashcan(trashcan.getId());
        }

        return new TrashcanDetailsResponse(
                trashcan.getId(),
                trashcan.getAddress(),
                trashcan.getAddressDetail(),
                images, // 이미지 URL 리스트
                descriptions, // 설명 텍스트 리스트
                trashcan.getViews(),
                trashcan.getStatus(),
                count
        );
    }

    private void saveImages(List<MultipartFile> imageFiles, Trashcan savedTrashcan) throws IOException {
        for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                String uuid = UUID.randomUUID().toString();
                String ext = file.getContentType();

                InputStream keyFile = ResourceUtils.getURL(keyFileName).openStream();
                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(keyFile))
                        .build()
                        .getService();

                BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uuid)
                        .setContentType(ext).build();

                storage.create(blobInfo, file.getInputStream());
                String imgUrl = "https://storage.googleapis.com/" + bucketName + "/" + uuid;

                Image image = new Image();
                image.setTrashcan(savedTrashcan);
                image.setImage(imgUrl);
                imageRepository.save(image);
            }
        }
    }
    private void saveDescription(String description, Trashcan savedTrashcan) {
        Description descriptionObject = new Description();
        descriptionObject.setDescription(description);
        descriptionObject.setTrashcan(savedTrashcan);
        descriptionRepository.save(descriptionObject);
    }

    public boolean isCoordinateDuplicate(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Long count = trashcanRepository.existsByLocation(location);
        log.info("중복 count: " + count);
        return count != null && count > 0;
    }

    public TrashcanMessageResponse registerTrashcan(double latitude, double longitude, String addressDetail, String address, String description, List<MultipartFile> imageObjects, String accessToken) {
        if (isCoordinateDuplicate(latitude, longitude)) {
            throw new IllegalArgumentException("이미 해당 위치에 쓰레기통이 등록되어 있습니다.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);

        Claims claims = jwtProvider.parseClaims(accessToken);
        Member member = memberRepository.findById(Long.parseLong(claims.getSubject())).
                orElseThrow(()-> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        int registrationCount = registrationRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);
        int suggestionCount = suggestionRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);

        if (registrationCount + suggestionCount >= 3) {
            throw new IllegalStateException("하루 제한 횟수를 초과하였습니다.");
        }

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Trashcan trashcan = new Trashcan();
        trashcan.setLocation(location);
        trashcan.setAddressDetail(addressDetail);
        trashcan.setAddress(address);
        trashcan.setStatus("REGISTERED");

        Trashcan savedTrashcan = trashcanRepository.save(trashcan);

        int totalScore = 100;

        if (imageObjects != null && !imageObjects.isEmpty()) {
            try {
                saveImages(imageObjects, trashcan);
                totalScore += 100;
            } catch (IOException e) {
                throw new ImageException("이미지 저장 중 오류가 발생하였습니다.");
            }
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, savedTrashcan);
            totalScore += 100;
        }

        Registration registration = new Registration();
        registration.setMember(member);
        registration.setTrashcan(savedTrashcan);
        registrationRepository.save(registration);

        Score score = Score.builder()
                .eachScore(totalScore)
                .scoreDescription(ScoreDescription.REGISTRATION)
                .member(member)
                .build();
        scoreRepository.save(score);

        return new TrashcanMessageResponse("쓰레기통 위치가 성공적으로 등록되었습니다.");
    }

    public void registerTrashcanId(Long trashcanId, List<MultipartFile> imageFiles, String description, String accessToken) {
        // 쓰레기통 ID로 쓰레기통 엔티티 조회
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        // AccessToken에서 회원 정보 추출
        Claims claims = jwtProvider.parseClaims(accessToken);

        Long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));
        // 중복 등록 검사
        List<Registration> registrations = registrationRepository.findByMemberAndTrashcan(member, trashcan);
        if (!registrations.isEmpty()) {
            throw new IllegalStateException("이미 해당 쓰레기통에 등록된 정보가 있습니다.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);
        int registrationCount = registrationRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);
        int suggestionCount = suggestionRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);

        if (registrationCount + suggestionCount >= 3) {
            throw new IllegalStateException("하루 제한 횟수를 초과하였습니다.");
        }

        int totalScore = 100;

        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                saveImages(imageFiles, trashcan);
                totalScore += 100;
            } catch (IOException e) {
                throw new ImageException("이미지 저장 중 오류가 발생하였습니다.");
            }
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
            totalScore += 100;
        }

        Score score = Score.builder()
                .eachScore(totalScore)
                .scoreDescription(ScoreDescription.REGISTRATION)
                .member(member)
                .build();
        scoreRepository.save(score);

        // 중복이 아니라면 registration 정보 저장
        Registration registration = new Registration();
        registration.setMember(member);
        registration.setTrashcan(trashcan);
        registrationRepository.save(registration);

        // 상태 업데이트
        long registrationTotalCount = registrationRepository.countByTrashcan(trashcan);
        if (registrationTotalCount >= 5) {
            trashcan.setStatus("ADDED");
            trashcanRepository.save(trashcan);
        }
    }

    public TrashcanMessageResponse suggestTrashcan(double latitude, double longitude, String addressDetail, String address, String description, List<MultipartFile> imageObjects, String accessToken) {
        if (isCoordinateDuplicate(latitude, longitude)) {
            throw new IllegalArgumentException("이미 해당 위치에 쓰레기통이 등록되어 있습니다.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);

        Claims claims = jwtProvider.parseClaims(accessToken);
        Member member = memberRepository.findById(Long.parseLong(claims.getSubject())).
                orElseThrow(()-> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        int registrationCount = registrationRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);
        int suggestionCount = suggestionRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);

        if (registrationCount + suggestionCount >= 3) {
            throw new IllegalStateException("하루 제한 횟수를 초과하였습니다.");
        }

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Trashcan trashcan = new Trashcan();
        trashcan.setLocation(location);
        trashcan.setAddressDetail(addressDetail);
        trashcan.setAddress(address);
        trashcan.setStatus("SUGGESTED");

        Trashcan savedTrashcan = trashcanRepository.save(trashcan);

        int totalScore = 100;

        if (imageObjects != null && !imageObjects.isEmpty()) {
            try {
                saveImages(imageObjects, savedTrashcan);
                totalScore += 100;
            } catch (IOException e) {
                throw new ImageException("이미지 저장 중 오류가 발생하였습니다.");
            }
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, savedTrashcan);
            totalScore += 100;
        }

        Suggestion suggestion = new Suggestion();
        suggestion.setMember(member);
        suggestion.setTrashcan(trashcan);
        suggestionRepository.save(suggestion);

        Score score = Score.builder()
                .eachScore(totalScore)
                .scoreDescription(ScoreDescription.SUGGESTION)
                .member(member)
                .build();
        scoreRepository.save(score);

        return new TrashcanMessageResponse("쓰레기통 위치가 성공적으로 제안되었습니다.");
    }

    public void suggestTrashcanId(Long trashcanId, List<MultipartFile> imageFiles, String description, String accessToken) {
        // 쓰레기통 ID로 쓰레기통 엔티티 조회
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        // AccessToken에서 회원 정보 추출
        Claims claims = jwtProvider.parseClaims(accessToken);
        Long memberId = Long.parseLong(claims.getSubject());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        // 제안 중복 등록 검사
        List<Suggestion> suggestions = suggestionRepository.findByMemberAndTrashcan(member, trashcan);

        if (!suggestions.isEmpty()) {
            throw new IllegalStateException("이미 해당 쓰레기통에 대한 제안이 있습니다.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);
        int registrationCount = registrationRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);
        int suggestionCount = suggestionRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);

        if (registrationCount + suggestionCount >= 3) {
            throw new IllegalStateException("하루 제한 횟수를 초과하였습니다.");
        }

        int totalScore = 100;

        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                saveImages(imageFiles, trashcan);
                totalScore += 100;
            } catch (IOException e) {
                throw new ImageException("이미지 저장 중 오류가 발생하였습니다.");
            }
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
            totalScore += 100;
        }

        Score score = Score.builder()
                .eachScore(totalScore)
                .scoreDescription(ScoreDescription.SUGGESTION)
                .member(member)
                .build();
        scoreRepository.save(score);

        // 중복이 아니라면 Suggestion 정보 저장
        Suggestion suggestion = new Suggestion();
        suggestion.setMember(member);
        suggestion.setTrashcan(trashcan);
        suggestionRepository.save(suggestion);
    }


    public TrashcansPageResponse getTrashcanDetailsByMemberId(Long memberId, String status, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        List<TrashcansResponse> responses = new ArrayList<>();
        TrashcansPageResponse trashcansPageResponse;

        if (status.equals("REGISTRATION")) {
            Page<Registration> registrations = registrationRepository.findByMemberId(memberId, pageable);
            for (Registration registration : registrations) {
                Trashcan trashcan = registration.getTrashcan();
                List<String> images = getImagesByTrashcanId(trashcan.getId()).stream().map(Image::getImage).collect(Collectors.toList());
                List<String> descriptions = getDescriptionsByTrashcanId(trashcan.getId()).stream().map(Description::getDescription).collect(Collectors.toList());
                Integer count = getRegistrationCountForTrashcan(trashcan.getId());

                TrashcansResponse response = new TrashcansResponse(trashcan.getLocation().getY(), trashcan.getLocation().getX(), trashcan.getId(), trashcan.getAddress(), trashcan.getAddressDetail(), images, descriptions, trashcan.getViews(), trashcan.getStatus(), registration.getCreatedAt(), count);
                responses.add(response);
            }
            trashcansPageResponse = new TrashcansPageResponse(registrations.getTotalPages(), responses);

        } else if (status.equals("SUGGESTION")) {
            Page<Suggestion> suggestions = suggestionRepository.findByMemberId(memberId, pageable);
            for (Suggestion suggestion : suggestions) {
                Trashcan trashcan = suggestion.getTrashcan();
                List<String> images = getImagesByTrashcanId(trashcan.getId()).stream().map(Image::getImage).collect(Collectors.toList());
                List<String> descriptions = getDescriptionsByTrashcanId(trashcan.getId()).stream().map(Description::getDescription).collect(Collectors.toList());
                Integer count = getSuggestionCountForTrashcan(trashcan.getId());

                TrashcansResponse response = new TrashcansResponse(trashcan.getLocation().getY(), trashcan.getLocation().getX(), trashcan.getId(), trashcan.getAddress(), trashcan.getAddressDetail(), images, descriptions, trashcan.getViews(), trashcan.getStatus(), suggestion.getCreatedAt(), count);
                responses.add(response);
            }
            trashcansPageResponse = new TrashcansPageResponse(suggestions.getTotalPages(), responses);

        } else {
            throw new IllegalArgumentException("잘못된 타입입니다.");
        }


        return trashcansPageResponse;
    }

    public void updateTrashcanStatus(Long id, String status){
        Trashcan trashcan = trashcanRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("쓰레기통을 찾을 수 없습니다."));
        trashcan.setStatus(status);
        trashcanRepository.save(trashcan);
    }

    public TrashcanListPageResponse getTrashcanDetailsByStatus(String status, Pageable pageable, String sort) {
        Page<Trashcan> trashcans;

        if (status.equals("REMOVED")) {
            if (sort.equals("DESC")) {
                trashcans = trashcanRepository.findByStatusOrderByReportCountDesc(status, pageable);
            } else if (sort.equals("ASC")) {
                trashcans = trashcanRepository.findByStatusOrderByReportCountAsc(status, pageable);
            } else {
                throw new IllegalArgumentException("잘못된 정렬 요청입니다.");
            }
        } else if (status.equals("ADDED") || status.equals("REGISTERED") || status.equals("SUGGESTED")) {
            if (sort.equals("DESC")) {
                trashcans = trashcanRepository.findByStatusOrderByViewsDesc(status, pageable);
            } else if (sort.equals("ASC")) {
                trashcans = trashcanRepository.findByStatusOrderByViewsAsc(status, pageable);
            } else {
                throw new IllegalArgumentException("잘못된 정렬 요청입니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 status 값입니다.");
        }

        List<TrashcanListResponse> responses = trashcans.getContent().stream().map(trashcan -> {
            Integer registrationCount = getRegistrationCountForTrashcan(trashcan.getId());
            Integer suggestionCount = getSuggestionCountForTrashcan(trashcan.getId());
            Integer reportCount = reportRepository.countByTrashcanId(trashcan.getId());
            List<String> images = getImagesByTrashcanId(trashcan.getId()).stream()
                    .map(Image::getImage)
                    .collect(Collectors.toList());
            List<String> descriptions = getDescriptionsByTrashcanId(trashcan.getId()).stream()
                    .map(Description::getDescription)
                    .collect(Collectors.toList());

            return new TrashcanListResponse(
                    trashcan.getId(),
                    trashcan.getAddress(),
                    trashcan.getAddressDetail(),
                    images,
                    descriptions,
                    trashcan.getViews(),
                    trashcan.getStatus(),
                    Math.max(registrationCount, suggestionCount),
                    reportCount
            );
        }).collect(Collectors.toList());

        TrashcanListPageResponse trashcanDetailsPageResponse = new TrashcanListPageResponse(trashcans.getTotalPages(), responses);

        return trashcanDetailsPageResponse;
    }

    public void deleteTrashcanById(Long id) {
        if (!trashcanRepository.existsById(id)) {
            throw new TrashcanNotFoundException("쓰레기통을 찾을 수 없음: " + id);
        }
        trashcanRepository.deleteById(id);
    }

    public void reportTrashcan(Long trashcanId, String description, String accessToken) {
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        Claims claims = jwtProvider.parseClaims(accessToken);
        Long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        boolean existsReport = reportRepository.existsByMemberAndTrashcan(member, trashcan);
        if (existsReport) {
            throw new IllegalStateException("이미 신고된 쓰레기통입니다.");
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);

        long todayReportsCount = reportRepository.countByMemberAndCreatedAtBetween(member, startOfDay, endOfDay);
        if (todayReportsCount >= 3) {
            throw new IllegalStateException("하루 신고 횟수를 초과하였습니다.");
        }

        Report report = Report.builder()
                .member(member)
                .trashcan(trashcan)
                .description(description)
                .build();
        reportRepository.save(report);

        long reportCount = reportRepository.countByTrashcan(trashcan);
        if (reportCount >= 5) {
            trashcan.setStatus("REMOVED");
            trashcanRepository.save(trashcan);
        }
    }

    public ReportPageResponse getReportsByTrashcanId(Long trashcanId, Pageable pageable) {
        Page<Report> reports = reportRepository.findByTrashcanId(trashcanId, pageable);
        if (reports.isEmpty()) {
            throw new IllegalArgumentException("신고 요청이 없습니다.");
        }
        List<ReportResponse> reportResponses = reports.stream()
                .map(ReportResponse::new)
                .collect(Collectors.toList());

        return new ReportPageResponse(reports.getTotalPages(), reportResponses);
    }

    public ReportPageResponse getReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        if (reports.isEmpty()) {
            throw new NotFoundException("신고 내용이 없습니다.");
        }

        List<ReportResponse> reportResponses = reports.stream()
                .map(ReportResponse::new)
                .collect(Collectors.toList());

        return new ReportPageResponse(reports.getTotalPages(), reportResponses);
    }

    public void deleteReportById(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new NotFoundException("신고 내역을 찾을 수 없음: " + id);
        }
        reportRepository.deleteById(id);
    }
}
