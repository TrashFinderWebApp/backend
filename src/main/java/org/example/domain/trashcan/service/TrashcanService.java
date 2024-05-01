package org.example.domain.trashcan.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.Member;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.service.MemberService;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Suggestion;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.dto.response.TrashcanAllResponse;
import org.example.domain.trashcan.dto.response.TrashcanDetailsResponse;
import org.example.domain.trashcan.repository.DescriptionRepository;
import org.example.domain.trashcan.repository.ImageRepository;
import org.example.domain.trashcan.repository.RegistrationRepository;
import org.example.domain.trashcan.repository.SuggestionRepository;
import org.example.domain.trashcan.repository.TrashcanRepository;
import org.example.global.security.jwt.JwtProvider;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
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

    @Transactional
    public List<Trashcan> findTrashcansNear(double latitude, double longitude, double radius, String status) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return trashcanRepository.findWithinDistance(location, radius, status);
    }

    @Transactional
    public Optional<Trashcan> getTrashcanDetails(Long id) {
        return trashcanRepository.findById(id);
    }

    @Transactional
    public List<Image> getImagesByTrashcanId(Long id) {
        return imageRepository.findByTrashcanId(id);
    }

    @Transactional
    public List<Description> getDescriptionsByTrashcanId(Long id) {
        return descriptionRepository.findByTrashcanId(id);
    }

    @Transactional
    public int getRegistrationCountForTrashcan(Long trashcanId) {
        return registrationRepository.countByTrashcanId(trashcanId);
    }

    @Transactional
    public int getSuggestionCountForTrashcan(Long trashcanId) {
        return suggestionRepository.countByTrashcanId(trashcanId);
    }

    @Transactional
    public void increaseTrashcanViews(Long id){
        Optional<Trashcan> trashcanOptional = trashcanRepository.findById(id);
        trashcanOptional.ifPresent(trashcan -> {
            trashcan.increaseViews();
            trashcanRepository.save(trashcan);
        });
    }

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String keyFileName;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

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

    @Transactional
    public Trashcan registerTrashcan(Trashcan trashcan, List<MultipartFile> imageFiles, String description, String accessToken) throws IOException {
        Trashcan savedTrashcan = trashcanRepository.save(trashcan);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, savedTrashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, savedTrashcan);
        }


        Claims claims = jwtProvider.parseClaims(accessToken);

        memberRepository.findById(Long.parseLong(claims.getSubject()))
                .map(member -> {
                    Registration registration = new Registration();
                    registration.setMember(member);
                    registration.setTrashcan(trashcan);
                    registrationRepository.save(registration);
                    return registration;
                }).orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        return savedTrashcan;
    }

    @Transactional
    public void registerTrashcanId(Long trashcanId, List<MultipartFile> imageFiles, String description, String accessToken) throws IOException {
        // 쓰레기통 ID로 쓰레기통 엔티티 조회
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        // AccessToken에서 회원 정보 추출
        Claims claims = jwtProvider.parseClaims(accessToken);

        Long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));
        log.info("중복 등록 검사: 회원 ID = {}, 쓰레기통 ID = {}", memberId, trashcanId);
        // 중복 등록 검사
        List<Registration> registrations = registrationRepository.findByMemberAndTrashcan(member, trashcan);
        log.info("findByMemberAndTrashcan 결과 개수: {}", registrations.size());
        boolean isAlreadyRegistered = !registrations.isEmpty();
        if (isAlreadyRegistered) {
            throw new IllegalStateException("이미 해당 쓰레기통에 등록된 정보가 있습니다.");
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, trashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
        }

        // 중복이 아니라면 registration 정보 저장
        Registration registration = new Registration();
        registration.setMember(member);
        registration.setTrashcan(trashcan);
        registrationRepository.save(registration);
    }

    @Transactional
    public Trashcan suggestTrashcan(Trashcan trashcan, List<MultipartFile> imageFiles, String description, String accessToken) throws IOException {
        Trashcan savedTrashcan = trashcanRepository.save(trashcan);
        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, savedTrashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, savedTrashcan);
        }

        Claims claims = jwtProvider.parseClaims(accessToken);
        memberRepository.findById(Long.parseLong(claims.getSubject()))
                .map(member -> {
                    Suggestion suggestion = new Suggestion();
                    suggestion.setMember(member);
                    suggestion.setTrashcan(trashcan);
                    suggestionRepository.save(suggestion);
                    return suggestion;
                }).orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        return savedTrashcan;
    }

    @Transactional
    public void suggestTrashcanId(Long trashcanId, List<MultipartFile> imageFiles, String description, String accessToken) throws IOException {
        // 쓰레기통 ID로 쓰레기통 엔티티 조회
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        // AccessToken에서 회원 정보 추출
        Claims claims = jwtProvider.parseClaims(accessToken);
        Long memberId = Long.parseLong(claims.getSubject());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        log.info("제안 중복 검사: 회원 ID = {}, 쓰레기통 ID = {}", memberId, trashcanId);
        // 제안 중복 등록 검사
        List<Suggestion> suggestions = suggestionRepository.findByMemberAndTrashcan(member, trashcan);
        log.info("findByMemberAndTrashcan 결과 개수: {}", suggestions.size());

        if (!suggestions.isEmpty()) {
            throw new IllegalStateException("이미 해당 쓰레기통에 대한 제안이 있습니다.");
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, trashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
        }

        // 중복이 아니라면 Suggestion 정보 저장
        Suggestion suggestion = new Suggestion();
        suggestion.setMember(member);
        suggestion.setTrashcan(trashcan);
        suggestionRepository.save(suggestion);
    }


    public List<TrashcanAllResponse> getTrashcanDetailsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

        List<Trashcan> registeredTrashcans = registrationRepository.findByMemberId(memberId).stream().map(Registration::getTrashcan).collect(Collectors.toList());
        List<Trashcan> suggestedTrashcans = suggestionRepository.findByMemberId(memberId).stream().map(Suggestion::getTrashcan).collect(Collectors.toList());
        List<TrashcanAllResponse> responses = new ArrayList<>();

        for (Trashcan trashcan : registeredTrashcans) {
            List<String> images = getImagesByTrashcanId(trashcan.getId()).stream().map(Image::getImage).collect(Collectors.toList());
            List<String> descriptions = getDescriptionsByTrashcanId(trashcan.getId()).stream().map(Description::getDescription).collect(Collectors.toList());
            Integer count = 0;
            count = getRegistrationCountForTrashcan(trashcan.getId());

            TrashcanAllResponse response = new TrashcanAllResponse(trashcan.getLocation().getY(), trashcan.getLocation().getX(), trashcan.getId(), trashcan.getAddress(), trashcan.getAddressDetail(), images, descriptions, trashcan.getViews(), trashcan.getStatus(), count);
            responses.add(response);
        }
        for (Trashcan trashcan : suggestedTrashcans) {
            List<String> images = getImagesByTrashcanId(trashcan.getId()).stream().map(Image::getImage).collect(Collectors.toList());
            List<String> descriptions = getDescriptionsByTrashcanId(trashcan.getId()).stream().map(Description::getDescription).collect(Collectors.toList());
            Integer count = 0;
            count = getSuggestionCountForTrashcan(trashcan.getId());
            TrashcanAllResponse response = new TrashcanAllResponse(trashcan.getLocation().getY(), trashcan.getLocation().getX(), trashcan.getId(), trashcan.getAddress(), trashcan.getAddressDetail(), images, descriptions, trashcan.getViews(), trashcan.getStatus(), count);
            responses.add(response);
        }
        return responses;
    }
}
