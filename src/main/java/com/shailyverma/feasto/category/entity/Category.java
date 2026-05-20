package com.shailyverma.feasto.category.entity;

import com.shailyverma.feasto.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Table(name = "categories")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    @Builder.Default
    @Column(name = "is_html", nullable = false)
    private Boolean isHtml = false;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Menu> menus;
}
