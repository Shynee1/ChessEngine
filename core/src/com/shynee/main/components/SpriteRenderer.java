package com.shynee.main.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.utils.Transform;
import com.shynee.main.abstracts.Component;

public class SpriteRenderer extends Component {

    private Sprite sprite;

    public SpriteRenderer(String resourceName){
        this.sprite = new Sprite(new Texture(resourceName));
    }

    public SpriteRenderer(Texture texture){
        this.sprite = new Sprite(texture);
    }

    public SpriteRenderer(Sprite sprite){
        this.sprite = sprite;

    }

    @Override
    public void start() {
        Transform t = gameObject.transform;
        sprite.setPosition(t.position.x, t.position.y);
        sprite.setSize(t.scale.x, t.scale.y);
    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        sprite.draw(batch);
    }
}
