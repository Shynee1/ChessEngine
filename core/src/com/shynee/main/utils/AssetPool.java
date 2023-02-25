package com.shynee.main.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashMap;

public class AssetPool {

    private static HashMap<String, Spritesheet> spritesheetPool = new HashMap<>();
    private static HashMap<String, Texture> texturePool = new HashMap<>();
    private static HashMap<SpriteBatch, ShapeDrawer> drawerPool = new HashMap<>();

    public static Texture getTexture(String resourceName){
        if (texturePool.containsKey(resourceName)) return texturePool.get(resourceName);

        texturePool.put(resourceName, new Texture(resourceName));
        return texturePool.getOrDefault(resourceName, null);
    }

    public static void addSpritesheet(String resourceName, Spritesheet spritesheet){
        if (spritesheetPool.containsKey(resourceName)) return;

        spritesheetPool.put(resourceName, spritesheet);
    }

    public static Spritesheet getSpritesheet(String resourceName){
        if (!spritesheetPool.containsKey(resourceName)) throw new RuntimeException("Tried accessing spritesheet that has not been added to the AssetPool '" + resourceName + "'");
        return spritesheetPool.getOrDefault(resourceName, null);
    }

    public static ShapeDrawer getShapeDrawer(SpriteBatch batch, ShapeDrawer drawer){
        if (drawerPool.containsKey(batch)) return drawerPool.get(batch);
        drawerPool.put(batch, drawer);
        return drawerPool.get(batch);
    }

}
