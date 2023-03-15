package com.shynee.main.scenes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.shynee.main.chess.Book;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.abstracts.Scene;
import com.shynee.main.chess.PGNUtility;
import com.shynee.main.components.*;
import com.shynee.main.utils.GameObject;
import com.shynee.main.utils.AssetPool;
import com.shynee.main.utils.Constants;
import com.shynee.main.utils.Spritesheet;
import com.shynee.main.utils.Transform;

public class ChessScene extends Scene {

    private final String FEN;
    private final boolean playerColor;

    public ChessScene(String FEN, boolean color){
        super();
        this.FEN = FEN;
        this.playerColor = color;
    }

    @Override
    public void start(){
        loadResources();
        this.camera = new OrthographicCamera(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        ChessBoard board = new ChessBoard("3r4/8/3k4/8/8/3K4/8/8 w - - 1", playerColor);
        Book book = new Book(board);

        super.start();

        GameObject boardSprite = new GameObject("BoardSprite", new Transform(new Vector2(), new Vector2(Constants.WORLD_HEIGHT, Constants.WORLD_HEIGHT)));
        boardSprite.addComponent(new SpriteRenderer("images/Board.png"));
        addGameObject(boardSprite);

        GameObject boardUI = new GameObject("BoardUI", new Transform(new Vector2(), new Vector2(Constants.WORLD_HEIGHT, Constants.WORLD_HEIGHT)));
        boardUI.addComponent(new BoardUI(board));
        addGameObject(boardUI);

        GameObject mouseInput = new GameObject("MouseInputHandler");
        mouseInput.addComponent(new InputHandler(board, book));
        addGameObject(mouseInput);

        GameObject aiPlayer = new GameObject("AiPLayer");
        aiPlayer.addComponent(new AIController(!playerColor, board, book));
        addGameObject(aiPlayer);

        GameObject aiPlayer2 = new GameObject("AiPLayer2");
        aiPlayer2.addComponent(new AIController(playerColor, board, book));
        //addGameObject(aiPlayer2);


    }

    @Override
    public void update(float dt, SpriteBatch batch){
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        super.update(dt, batch);
        batch.end();
    }


    @Override
    public void dispose() {
        super.dispose();
    }

    private void loadResources(){
        AssetPool.addSpritesheet("Pieces.png", new Spritesheet(new Texture("spritesheets/Pieces.png"), 12, 2));
        AssetPool.getTexture("images/Solid_white.png");
    }
}
