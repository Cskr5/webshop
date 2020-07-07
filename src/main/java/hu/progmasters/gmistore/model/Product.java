package hu.progmasters.gmistore.model;

import hu.progmasters.gmistore.enums.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Basic(optional = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String pictureUrl;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "pictures")
    @Column(name = "product_pictures")
    private Set<String> pictures = new HashSet<>();

    @NotNull
    private double price;

    @Column(columnDefinition = "int default 0")
    private int discount;

    @Column(columnDefinition = "int default 0")
    private int warrantyMonths;

    @Column(columnDefinition = "int default 0")
    private int quantityAvailable;

    @ElementCollection(targetClass = Integer.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "ratings")
    @Column(name = "product_ratings")
    private List<Integer> ratings = new ArrayList<>();

    @Column(columnDefinition = "double default 0.0")
    private double averageRating;

    @Column(columnDefinition = "boolean default true")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "product_order_id", referencedColumnName = "id")
    private Order productOrder;
}
