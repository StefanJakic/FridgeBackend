package com.hylastix.fridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fridge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Fridge name must not be blank")
    private String name;

    @Min(value = 1, message = "Fridge capacity must be greater than zero")
    private double capacity;
    private double currentVolume = 0.0;
    private double currentNumberOfItems = 0.0;

    @Version
    private Long version;

    @ManyToMany(mappedBy = "fridges")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "fridge", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> items = new HashSet<>();

    public void incrementNumberOfItems() {
        this.currentNumberOfItems++;
    }

    public void decrementNumberOfItems() {
        this.currentNumberOfItems--;
    }

    public void incrementVolume(double volume) {
        this.currentVolume += volume;
    }

    public void decrementVolume(double volume) {
        this.currentVolume -= volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fridge fridge = (Fridge) o;
        return id != null && id.equals(fridge.id);
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Fridge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", currentVolume=" + currentVolume +
                ", currentNumberOfItems=" + currentNumberOfItems +
                '}';
    }
}
