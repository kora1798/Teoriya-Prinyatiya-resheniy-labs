package sample;

import java.io.Serializable;

class FileUnit implements Serializable {
    private double[][] strategyMatrix, resultMatrix;
    private String P,Q,valueOfGame,choiceBoxValue,textCond;

    public FileUnit(double[][] strategyMatrix, double[][] resultMatrix, String p, String q, String valueOfGame, String choiceBoxValue, String textCond) {
        this.strategyMatrix = strategyMatrix;
        this.resultMatrix = resultMatrix;
        P = p;
        Q = q;
        this.valueOfGame = valueOfGame;
        this.choiceBoxValue = choiceBoxValue;
        this.textCond = textCond;
    }

    public double[][] getResultMatrix() {
        return resultMatrix;
    }

    public String getP() {
        return P;
    }

    public String getQ() {
        return Q;
    }

    public String getValueOfGame() {
        return valueOfGame;
    }

    public String getChoiceBoxValue() {
        return choiceBoxValue;
    }

    public String getTextCond() {
        return textCond;
    }

    public double[][] getStrategyMatrix() {
        return strategyMatrix;
    }
}
