package com.shynee.main.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.utils.Transform;
import com.shynee.main.abstracts.Component;

import java.util.HashMap;

public class GameObject {

    public String name;
    public Transform transform;

    private HashMap<Class<?>, Component> components;
    private boolean started = false;

    public GameObject(String name){
        this.name = name;
        this.transform = new Transform();
        this.components = new HashMap<>();
    }

    public GameObject(String name, Transform transform){
        this.name = name;
        this.transform = transform;
        this.components = new HashMap<>();
    }

    public void start(){
        // Start every component in components
        components.forEach((name, component) -> component.start());
        started = true;
    }

    public void update(float dt, SpriteBatch  batch){
        // Update every component in components
        components.forEach((name, component) -> component.update(dt, batch));
    }

    public void addComponent(Component component){
        this.components.put(component.getClass(), component);
        component.gameObject = this;

        if (started) component.start();
    }

    public void removeComponent(Component component){
        this.components.remove(component.getClass());
        component.gameObject = null;
    }

    public <T extends Component> T getComponent(Class<T> componentClass){
        Component c = this.components.get(componentClass);
        return componentClass.isAssignableFrom(c.getClass()) ? componentClass.cast(c) : null;
    }
}
