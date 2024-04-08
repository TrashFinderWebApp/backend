package org.example.domain.trashcan.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.example.domain.trashcan.domain.Description;
import org.example.domain.trashcan.domain.Image;
import org.example.domain.trashcan.domain.Trashcan;
import org.example.domain.trashcan.repository.DescriptionRepository;
import org.example.domain.trashcan.repository.ImageRepository;
import org.example.domain.trashcan.repository.RegistrationRepository;
import org.example.domain.trashcan.repository.SuggestionRepository;
import org.example.domain.trashcan.repository.TrashcanRepository;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrashcanService{
    private final TrashcanRepository trashcanRepository;
    private final ImageRepository imageRepository;
    private final DescriptionRepository descriptionRepository;
    private final RegistrationRepository registrationRepository;
    private final SuggestionRepository suggestionRepository;

    public TrashcanService(TrashcanRepository trashcanRepository,
            ImageRepository imageRepository,
            DescriptionRepository descriptionRepository,
            RegistrationRepository registrationRepository,
            SuggestionRepository suggestionRepository) {
        this.trashcanRepository = trashcanRepository;
        this.imageRepository = imageRepository;
        this.descriptionRepository = descriptionRepository;
        this.registrationRepository = registrationRepository;
        this.suggestionRepository = suggestionRepository;
    }

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
    public Trashcan registerTrashcan(Trashcan trashcan, List<MultipartFile> imageFiles, String description) throws IOException {
        Trashcan savedTrashcan = trashcanRepository.save(trashcan);
        saveImages(imageFiles, savedTrashcan);
        saveDescription(description, savedTrashcan);
        //유저 id 찾아서 registtaion 테이블도 넣어야함
        return savedTrashcan;
    }

    @Transactional
    public Trashcan suggestTrashcan(Trashcan trashcan, List<MultipartFile> imageFiles, String description) throws IOException {
        Trashcan savedTrashcan = trashcanRepository.save(trashcan);
        saveImages(imageFiles, savedTrashcan);
        saveDescription(description, savedTrashcan);
        //유저 id 찾아서 suggestion 테이블도 넣어야함
        return savedTrashcan;
    }
}
