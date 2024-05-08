package org.example.domain.notification.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.notification.controller.dto.request.UpdateNotificationRequest;
import org.example.global.domain.BaseTimeEntity;

@Entity
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "state", nullable = false)
    private String state;

    public Notification(String title, String description, String type) {
        this.title = title;
        this.description = description;
        this.state = type;
    }

    public void update(UpdateNotificationRequest updateRequest) {
        if (updateRequest.getTitle() != null) {
            this.title = updateRequest.getTitle();
        }
        if (updateRequest.getDescription() != null) {
            this.description = updateRequest.getDescription();
        }
        if (updateRequest.getState() != null) {
            this.state = updateRequest.getState();
        }
    }

}
