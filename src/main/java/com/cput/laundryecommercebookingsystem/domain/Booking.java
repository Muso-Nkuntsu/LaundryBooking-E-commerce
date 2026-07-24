package com.cput.laundryecommercebookingsystem.domain;

import jakarta.persistence.*;

// Still not done with my Code, waitin gon the UML to know the relationship.
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
