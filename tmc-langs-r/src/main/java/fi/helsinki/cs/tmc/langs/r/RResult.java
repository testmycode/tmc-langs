
package fi.helsinki.cs.tmc.langs.r;

import java.util.List;


public class RResult {
    
    private String name;
    private List<String> points;
    
    public RResult(){
      
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPoints() {
        return points;
    }

    public void setPoints(List<String> points) {
        this.points = points;
    }
    
    
}
