package org.example.domain.trashcan.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "description")
public class Description {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "trashcan_id", nullable = false)
    private Trashcan trashcan;

    @Lob
    private String description;
}
