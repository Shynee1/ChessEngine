package com.shynee.main.utils;

import com.badlogic.gdx.math.Vector2;

public class Transform {

    public Vector2 position;
    public Vector2 scale;

    public Transform(){
        init(new Vector2(), new Vector2());
    }

    public Transform(Vector2 position) {
        init(position, new Vector2());
    }

    public Transform(Vector2 position, Vector2 scale) {
        init(position, scale);
    }

    public void init(Vector2 position, Vector2 scale){
        this.position = position;
        this.scale = scale;
    }

    public Transform copy(){
        return new Transform(new Vector2(this.position), new Vector2(this.scale));
    }

    public void copy(Transform to){
        to.position.set(this.position);
        to.scale.set(this.scale);
    }

    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (!(o instanceof Transform)) return false;

        Transform t = (Transform) o;
        return t.position.equals(this.position) && t.scale.equals(this.scale);
    }
}
