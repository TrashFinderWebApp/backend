package org.example.domain.trashcan.service;

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
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;

@Service
public class TrashcanService{
    @Autowired
    private TrashcanRepository trashcanRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private DescriptionRepository descriptionRepository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private SuggestionRepository suggestionRepository;
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

    public List<Trashcan> findTrashcansNear(double latitude, double longitude, double radius, String status) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return trashcanRepository.findWithinDistance(location, radius, status);
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
}
