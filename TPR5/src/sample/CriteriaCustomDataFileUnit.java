package sample;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class CriteriaCustomDataFileUnit implements Serializable {
    private String name,
            functionType,
            parameters;
    private double weight;
    private boolean negativeDirection;

    public CriteriaCustomDataFileUnit(String name, String functionType, String parameters, double weight, boolean negativeDirection) {
        this.name = name;
        this.functionType = functionType;
        this.parameters = parameters;
        this.weight = weight;
        this.negativeDirection = negativeDirection;
    }

    public String getName() {
        return name;
    }

    public String getFunctionType() {
        return functionType;
    }

    public String getParameters() {
        return parameters;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isNegativeDirection() {
        return negativeDirection;
    }

}
