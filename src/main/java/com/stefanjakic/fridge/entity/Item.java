package com.stefanjakic.fridge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name must not be blank")
    private String name;
    private String category;
    @Min(value = 1, message = "Item volume must be greater than zero")
    private double volume;
    private LocalDateTime storedAt;
    private LocalDate bestBefore;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "fridge_id")
    private Fridge fridge;


    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id != null && id.equals(item.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", volume=" + volume +
                ", storedAt=" + storedAt +
                ", bestBefore=" + bestBefore +
                ", fridge=" + fridge.getName()+
                ", owner=" + owner +
                 '}';
    }
}
