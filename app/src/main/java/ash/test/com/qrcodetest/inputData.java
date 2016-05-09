package ash.test.com.qrcodetest;

public class inputData {
    private String name;
    private String freshness;
    private String stock;

    inputData(String name, String freshness, String stock){
        this.name = name;
        this.freshness = freshness;
        this.stock = stock;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setFreshness(String freshness){
        this.freshness = freshness;
    }

    public void setStock(String stock){
        this.stock = stock;
    }

    public String getName(){
        return name;
    }

    public String getFreshness(){
        return freshness;
    }

    public String getStock(){
        return stock;
    }
}
