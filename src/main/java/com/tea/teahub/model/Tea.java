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
@Table(name = "tea")
public class Tea {

    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String originCountry;

    @Column(nullable = false)
    private String originRegion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TeaType type;

    @Column(nullable = false, columnDefinition = "text")
    private String notes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
