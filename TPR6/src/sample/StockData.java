package sample;

public class StockData {
    private String date, time;
    private double open, high, low, close;
    private int vol;

    public StockData(String date, String time, double open, double high, double low, double close, int vol) {
        this.date = date;
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.vol = vol;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public int getVol() {
        return vol;
    }
}
