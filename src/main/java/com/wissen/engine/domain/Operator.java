package com.wissen.engine.domain;

import lombok.*;
import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "operator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operator {
    @Id
    @Column(name = "id")
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String skills;

    @Column(nullable = false)
    private Integer load = 0;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(nullable = false)
    private Integer maxCapacity = 5;

    @Transient
    public List<String> getSkillsList() {
        return Arrays.asList(skills.split(",\\s*"));
    }

    @Transient
    public boolean hasSkill(String skill) {
        return getSkillsList().contains(skill);
    }

    @Transient
    public boolean canAcceptMoreWork(Integer maxCapacity) {
        return load < maxCapacity && available;
    }
}
