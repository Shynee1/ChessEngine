package com.shynee.main.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.shynee.main.chess.BoardUtility;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.Main;
import com.shynee.main.abstracts.Component;
import com.shynee.main.utils.Constants;

public class InputHandler extends Component {

    private final ChessBoard board;
    private final int squareSize = Constants.WORLD_HEIGHT/8;

    private float mouseOffset;
    private boolean hasClicked;
    private int previousClickedSquarePos;
    public InputHandler(ChessBoard board){
        this.board = board;
        this.hasClicked = false;
    }

    @Override
    public void start() {
        this.mouseOffset = Main.currentScene().camera.position.x - Constants.WORLD_WIDTH/2f;
    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        if (!Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) return;

        Vector2 mouseInput = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        int boardPos = worldToArray(mouseInput);

        if (!(boardPos >= 0 && boardPos < 64)) return;

        if (!hasClicked) handleFirstClick(boardPos);
        else handleMoveClick(boardPos);
    }

    private void handleFirstClick(int clickedSquare){
        if (!board.getSquare(clickedSquare).hasPiece() || board.getSquare(clickedSquare).getPiece().color != board.colorToMove() || !board.gameRunning) return;

        this.previousClickedSquarePos = clickedSquare;

        board.highlightSquare(clickedSquare, Constants.MOVE_COLOR);
        board.generatePossibleMoves(clickedSquare);

        this.hasClicked = true;
    }

    private void handleMoveClick(int clickedSquare){
        this.hasClicked = false;

        if (!board.validateMove(previousClickedSquarePos, clickedSquare)) {
            board.clearUI();
            return;
        }

        board.makeMove(board.getMove(previousClickedSquarePos, clickedSquare), false);
    }

    private int worldToArray(Vector2 worldCoords){
        worldCoords.x += mouseOffset;
        worldCoords.y = Constants.WORLD_HEIGHT - worldCoords.y;

        return BoardUtility.getArrayIndex((int) worldCoords.y/squareSize, (int) worldCoords.x/squareSize);
    }

}
