package com.viewstar.dualauth.jpa.api;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "Invitecode")
public class Invitecode implements Serializable{
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;
    @Column(length = 6)
    @Length(min = 0,max=6)
    private String code;
    @Column(length = 32)
    @Length(min = 0,max=32)
    private String userid;
}
