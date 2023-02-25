package com.shynee.main.abstracts;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.utils.GameObject;

public abstract class Component {

    public GameObject gameObject = null;

    public abstract void start();

    public abstract void update(float dt, SpriteBatch batch);

}
