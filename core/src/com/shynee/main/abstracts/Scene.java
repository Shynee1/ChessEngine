package com.shynee.main.abstracts;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.utils.GameObject;

import java.util.HashMap;

public abstract class Scene {

    public OrthographicCamera camera;

    protected HashMap<String, GameObject> gameObjects;
    protected boolean isRunning = false;

    public Scene(){
        gameObjects = new HashMap<>();
    }

    public void start(){
        gameObjects.forEach((name, go) -> go.start());
        this.isRunning = true;
    }

    public void update(float dt, SpriteBatch batch){
        gameObjects.forEach((name, go) -> go.update(dt, batch));
    }

    public void dispose(){
        gameObjects = new HashMap<>();
    }

    public void addGameObject(GameObject go){

        if (gameObjects.containsKey(go.name)) throw new RuntimeException("Cannot have 2 GameObjects with the name '" + go.name + "'");

        this.gameObjects.put(go.name, go);

        if (isRunning) go.start();

    }

    public GameObject getGameObject(GameObject go){
        return gameObjects.get(go.name);
    }

    public GameObject getGameObject(String gameObjectName){
        return gameObjects.get(gameObjectName);
    }
}
