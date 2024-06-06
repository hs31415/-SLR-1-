import java.util.ArrayList;

public class Identify {
    private String type;
    private String name;
    private final ArrayList<String> lens;

    public Identify() {
        type = "";
        name = "";
        lens = new ArrayList<String>();
    }
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public Integer getSize(){
        return lens.size();
    }
    public String getLen(Integer i) {
        return lens.get(i);
    }
    public void pushLen(String s) {
        lens.add(s);
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setName(String name) {
        this.name = name;
    }
}
