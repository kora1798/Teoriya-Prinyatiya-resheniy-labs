package sample;

import java.io.Serializable;
import java.util.ArrayList;

public class FileUnit implements Serializable {
    ArrayList<String> namesOfAlternativesList, namesOfCriteriaList;
    CriteriaCustomDataFileUnit[] criteriaCustomDataFileUnits;
    double[][] valuesMatrix;
    ArrayList<String> resultList;


    public FileUnit(ArrayList<String> namesOfAlternativesList, ArrayList<String> namesOfCriteriaList, CriteriaCustomDataFileUnit[] criteriaCustomDataFileUnits, double[][] valuesMatrix, ArrayList<String> resultList) {
        this.namesOfAlternativesList = namesOfAlternativesList;
        this.namesOfCriteriaList = namesOfCriteriaList;
        this.criteriaCustomDataFileUnits = criteriaCustomDataFileUnits;
        this.valuesMatrix = valuesMatrix;
        this.resultList = resultList;
    }

    public void setNamesOfAlternativesList(ArrayList<String> namesOfAlternativesList) {
        this.namesOfAlternativesList = namesOfAlternativesList;
    }

    public void setNamesOfCriteriaList(ArrayList<String> namesOfCriteriaList) {
        this.namesOfCriteriaList = namesOfCriteriaList;
    }

    public void setCriteriaCustomDataFileUnits(CriteriaCustomDataFileUnit[] criteriaCustomDataFileUnits) {
        this.criteriaCustomDataFileUnits = criteriaCustomDataFileUnits;
    }

    public void setValuesMatrix(double[][] valuesMatrix) {
        this.valuesMatrix = valuesMatrix;
    }

    public void setResultList(ArrayList<String> resultList) {
        this.resultList = resultList;
    }

    public ArrayList<String> getNamesOfAlternativesList() {
        return namesOfAlternativesList;
    }

    public ArrayList<String> getNamesOfCriteriaList() {
        return namesOfCriteriaList;
    }

    public CriteriaCustomDataFileUnit[] getCriteriaCustomDataFileUnits() {
        return criteriaCustomDataFileUnits;
    }

    public double[][] getValuesMatrix() {
        return valuesMatrix;
    }

    public ArrayList<String> getResultList() {
        return resultList;
    }
}
