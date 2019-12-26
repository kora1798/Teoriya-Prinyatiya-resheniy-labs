package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class CriteriaCustomData implements Serializable {
    private SimpleStringProperty name = new SimpleStringProperty(),
            functionType = new SimpleStringProperty(),
            parameters = new SimpleStringProperty();
    private SimpleDoubleProperty weight = new SimpleDoubleProperty();
    private SimpleBooleanProperty negativeDirection = new SimpleBooleanProperty();

    public CriteriaCustomData(String name, String functionType, Double weight, Boolean negativeDirection, String parameters) {
        this.name.set(name);
        this.functionType.set(functionType);
        this.weight.set(weight);
        this.negativeDirection.set(negativeDirection);
        this.parameters.set(parameters);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setFunctionType(String functionType) {
        this.functionType.set(functionType);
    }

    public boolean isNegativeDirection() {
        return negativeDirection.get();
    }

    public void setParameters(String parameters) {
        this.parameters.set(parameters);
    }

    public void setWeight(double weight) {
        this.weight.set(weight);
    }

    public void setNegativeDirection(boolean negativeDirection) {
        this.negativeDirection.set(negativeDirection);
    }

    public String getName() {
        return name.get();
    }


    public String getFunctionType() {
        return functionType.get();
    }


    public String getParameters() {
        return parameters.get();
    }


    public double getWeight() {
        return weight.get();
    }

    public BooleanProperty getNegativeDirection(){
        return this.negativeDirection;
    }
}
