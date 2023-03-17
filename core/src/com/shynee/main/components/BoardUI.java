package com.shynee.main.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.shynee.main.*;
import com.shynee.main.abstracts.Component;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Piece;
import com.shynee.main.chess.Square;
import com.shynee.main.utils.*;
import space.earlygrey.shapedrawer.ShapeDrawer;


/*
  BoardUI class responsible for drawing the board and its pieces
 */
public class BoardUI extends Component {

    private final OrthographicCamera camera;
    private final ChessBoard board;
    private final Spritesheet sheet;

    private Square[] squares;

    public BoardUI(ChessBoard board){
        this.camera = Main.currentScene().camera;
        this.sheet = AssetPool.getSpritesheet("Pieces.png");
        this.board = board;
    }

    @Override
    public void start() {
        //Update camera to center board
        this.camera.position.x = gameObject.transform.position.x + gameObject.transform.scale.x/2;
        this.camera.position.y = gameObject.transform.position.y + gameObject.transform.scale.y/2;
    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        ShapeDrawer drawer = AssetPool.getShapeDrawer(batch, new ShapeDrawer(batch, new TextureRegion(AssetPool.getTexture("images/Solid_white.png"))));
        squares = board.getSquares();

        if (squares == null || squares.length == 0) return;

        for (Square s : squares){
            Transform t = s.getTransform();

            if (s.hasColor()){
                drawer.filledRectangle(t.position.x, t.position.y, t.scale.x, t.scale.y, s.getColor());
            }

            if (s.hasPiece()){
                Piece p = s.getPiece();
                int textureIndex = p.color ? p.type : p.type + 6;
                batch.draw(sheet.getSprite(textureIndex), t.position.x, t.position.y, t.scale.x, t.scale.y);
            }

            if (s.isPossibleMove()){
                drawer.filledCircle(new Vector2(t.position.x+t.scale.x/2, t.position.y + t.scale.y/2), 12, new Color(0, 0, 0, 0.3f));
            }
        }
    }
}
