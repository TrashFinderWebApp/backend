package org.example.domain.trashcan.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.domain.Member;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.service.MemberService;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Registration;
import org.example.domain.trashcan.domain.Suggestion;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.repository.DescriptionRepository;
import org.example.domain.trashcan.repository.ImageRepository;
import org.example.domain.trashcan.repository.RegistrationRepository;
import org.example.domain.trashcan.repository.SuggestionRepository;
import org.example.domain.trashcan.repository.TrashcanRepository;
import org.example.global.security.jwt.JwtProvider;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

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

        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, trashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
        }

        //registration 정보 저장
        Claims claims = jwtProvider.parseClaims(accessToken);

        memberRepository.findById(Long.parseLong(claims.getSubject()))
                .map(member -> {
                    Registration registration = new Registration();
                    registration.setMember(member);
                    registration.setTrashcan(trashcan);
                    registrationRepository.save(registration);
                    return registration;
                }).orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

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
        Member member = memberRepository.findById(Long.parseLong(claims.getSubject())).get();

        Suggestion suggestion = new Suggestion();
        suggestion.setMember(member);
        suggestion.setTrashcan(trashcan);
        suggestionRepository.save(suggestion);
        return savedTrashcan;
    }

    @Transactional
    public void suggestTrashcanId(Long trashcanId, List<MultipartFile> imageFiles, String description, String accessToken) throws IOException {
        // 쓰레기통 ID로 쓰레기통 엔티티 조회
        Trashcan trashcan = trashcanRepository.findById(trashcanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쓰레기통을 찾을 수 없습니다. ID: " + trashcanId));

        if (imageFiles != null && !imageFiles.isEmpty()) {
            saveImages(imageFiles, trashcan);
        }

        if (description != null && !description.isEmpty()) {
            saveDescription(description, trashcan);
        }

        //registration 정보 저장
        Claims claims = jwtProvider.parseClaims(accessToken);

        memberRepository.findById(Long.parseLong(claims.getSubject()))
                .map(member -> {
                    Suggestion suggestion = new Suggestion();
                    suggestion.setMember(member);
                    suggestion.setTrashcan(trashcan);
                    suggestionRepository.save(suggestion);
                    return suggestion;
                }).orElseThrow(() -> new NoSuchElementException("해당 ID의 회원을 찾을 수 없습니다."));

    }
}
