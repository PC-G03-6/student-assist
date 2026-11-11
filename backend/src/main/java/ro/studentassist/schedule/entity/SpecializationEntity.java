package ro.studentassist.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entitate JPA pentru o linie de studiu / specializare UBB FMI (ex: I3, IE2, MI1).
 */
@Entity
@Table(name = "specializations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecializationEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "field", nullable = false)
    private String field;

    @Column(name = "language", nullable = false, length = 32)
    private String language;

    @Column(name = "degree", nullable = false, length = 32)
    private String degree;

    @Column(name = "study_year", nullable = false)
    private Integer year;

    @Column(name = "configuration_id", length = 32)
    private String configurationId;
}
