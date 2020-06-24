package com.tjudream.designpattern.composite.arch;

public abstract class Component {
    private String name;

    public Component(String name) {
        this.name = name;
    }

    public abstract void add(Component component);
    public abstract void remove(Component component);
    public abstract void display();

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}
