package in.udacity.learning.model;
/**
 * Created by Lokesh on 05-09-2015.
 */
public class Item {
    int id;
    String Name;
    String Description;

    public Item(int id, String name, String description) {
        this.id = id;
        Name = name;
        Description = description;
    }

    public Item(int id, String name) {
        this.id = id;
        Name = name;
    }

    public Item(String name, String description) {
        Name = name;
        Description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @Override
    public String toString() {

        return Description==null ?Name:Name+" "+Description;
    }
}
