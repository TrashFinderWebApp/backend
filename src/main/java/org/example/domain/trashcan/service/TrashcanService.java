package org.example.domain.trashcan.service;

import org.example.domain.trashcan.domain.Trashcan;
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
    private final TrashcanRepository trashcanRepository;

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

    public List<Trashcan> findTrashcansNear(double latitude, double longitude, double radius) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return trashcanRepository.findWithinDistance(location, radius);
    }
}
