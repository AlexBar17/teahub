package com.tea.teahub.model;

import com.tea.teahub.model.enums.TeaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "teas")
public class Tea{

    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    private String originCountry;

    private String originRegion;

    @Enumerated(EnumType.STRING)
    private TeaType type;

    private String notes;

    private BigDecimal rating;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
