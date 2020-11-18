package com.wisaterhunep.stock;

public class Stock implements Comparable<Stock> {

    private String symbol;
    private String companyName;
    private String latestPrice;
    private String change;
    private String changePercent;

    Stock(){
    }

    Stock(String symbol, String companyName){
        this.symbol = symbol;
        this.companyName = companyName;
        this.latestPrice = "0.0";
        this.change = "0.0";
        this.changePercent = "0.0";
    }

    public Stock(String symbol, String companyName, String latestPrice, String change, String changePercent){
        this.symbol = symbol;
        this.companyName = companyName;

        if(latestPrice == null || latestPrice.equals("null")){
            this.latestPrice = "0.0";
        }else {
            this.latestPrice = latestPrice;
        }

        if(change == null || change.equals("null")){
            this.change = "0.0";
        }else{
            this.change = change;
        }

        if(changePercent == null || changePercent.equals("null")){
            this.changePercent = "0.0";
        }else {
            this.changePercent = changePercent;
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(String latestPrice) {
        this.latestPrice = latestPrice;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public int compareTo(Stock stock) {
        return symbol.compareTo(stock.symbol);
    }

    public String getRoundedPrice(){

        Double doublePrice = Double.parseDouble(latestPrice);
        String roundedPrice = ((double)Math.round(doublePrice * 100) / 100) + "";

        return roundedPrice;
    }

    public String getRoundedChange(){

        Double doubleChange = Double.parseDouble(change);
        String roundedChange = ((double)Math.round(doubleChange * 100) / 100) + "";

        return roundedChange;
    }

    public String getRoundedChangePercent(){

        Double doubleChangePercent = Double.parseDouble(changePercent) * 100;
        String roundedChangePercent = ((double)Math.round(doubleChangePercent * 100) / 100) + "";

        return roundedChangePercent;
    }

}
