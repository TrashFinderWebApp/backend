package org.example.domain.trashcan.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.trashcan.domain.Trashcan;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TrashcanRegistrationRequest {
    private double latitude;
    private double longitude;
    private String addressDetail;
    private List<MultipartFile> imageObjects;
    private String description;
    public Trashcan toTrashcan() {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Trashcan trashcan = new Trashcan();
        trashcan.setLocation(location);
        trashcan.setAddressDetail(addressDetail);
        trashcan.setStatus("registered");
        return trashcan;
    }
}
