package com.tjudream.designpattern.composite.arch;

import java.util.LinkedList;
import java.util.List;

public class Composite extends Component {
    private List<Component> children = new LinkedList<>();
    public Composite(String name) {
        super(name);
    }

    @Override
    public void add(Component component) {
        this.children.add(component);
    }

    @Override
    public void remove(Component component) {
        this.children.remove(component);
    }

    @Override
    public void display() {
        System.out.println("print " + this.getName());
        for (Component comp : this.children) {
            comp.display();
        }
    }
}
