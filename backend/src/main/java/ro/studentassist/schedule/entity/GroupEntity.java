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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Entitate JPA pentru o grupă academică (ex: 231, 921, 311).
 */
@Entity
@Table(name = "study_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "specialization_id", nullable = false, length = 64)
    private String specializationId;

    @Column(name = "study_year", nullable = false)
    private Integer year;

    @Column(name = "subgroups_csv")
    private String subgroupsCsv;

    @Column(name = "configuration_id", length = 32)
    private String configurationId;

    public List<String> getSubgroupsList() {
        if (subgroupsCsv == null || subgroupsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(subgroupsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void setSubgroupsList(List<String> subgroups) {
        if (subgroups == null || subgroups.isEmpty()) {
            this.subgroupsCsv = "";
        } else {
            this.subgroupsCsv = String.join(",", subgroups);
        }
    }
}
