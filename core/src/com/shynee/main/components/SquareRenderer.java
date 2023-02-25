package com.shynee.main.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.abstracts.Component;
import com.shynee.main.chess.Square;
import com.shynee.main.utils.AssetPool;
import com.shynee.main.utils.Spritesheet;

public class SquareRenderer extends Component {

    private final Spritesheet sheet;

    private Square[][] board;

    public SquareRenderer(Square[][] board){
        this.board = board;
        this.sheet = AssetPool.getSpritesheet("Pieces.png");
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt, SpriteBatch batch) {

    }
}
