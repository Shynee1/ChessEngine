package com.shynee.main.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Spritesheet -- Represents a sheet contain multiple sprites
 */
public class Spritesheet {

    private Sprite[] sprites;

    /**
     * Fills a Sprite[] with all sprites in the spritesheet
     * @param texture Texture containing the full spritesheet
     * @param numSprites Number of sprites in the spritesheet
     * @param rows Number of rows in the spritesheet
     */
    public Spritesheet(Texture texture, int numSprites, int rows){
        sprites = new Sprite[numSprites];

        int spritesPerRow = numSprites/rows;

        int spriteWidth = texture.getWidth()/spritesPerRow;
        int spriteHeight = texture.getHeight()/rows;

        int currentX = 0;
        int currentY = 0;

        for (int i = 0; i < numSprites; i++){
            sprites[i] = new Sprite(texture, currentX, currentY, spriteWidth, spriteHeight);
            currentX += spriteWidth;

            if (currentX > texture.getWidth() - spriteWidth){
                currentX = 0;
                currentY += spriteHeight;
            }
        }
    }

    public Sprite getSprite(int index){
        return sprites[index];
    }
}
