package models;

import enums.VehicleType;

/**
 * Created by taras.fihurnyak on 2/2/2017.
 */
public class Vehicle {
    private String model;
    private Owner owner;
    private String number;
    private VehicleType carType;

    public Vehicle() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public VehicleType getCarType() {
        return carType;
    }

    public void setCarType(VehicleType carType) {
        this.carType = carType;
    }
}
