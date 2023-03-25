package com.shynee.main.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.shynee.main.chess.*;
import com.shynee.main.Main;
import com.shynee.main.abstracts.Component;
import com.shynee.main.utils.Constants;

/**
 * InputHandler -- Responsible for reading/processing all keyboard/mouse input
 */
public class InputHandler extends Component {

    private final ChessBoard board;
    private final Book book;
    private final int squareSize = Constants.WORLD_HEIGHT/8;

    private float mouseOffset;
    private boolean hasClicked;
    private int previousClickedSquarePos;
    public InputHandler(ChessBoard board, Book book){
        this.board = board;
        this.book = book;
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

    /**
     * Handles the initial click to select piece to move.
     * Updates 'hasClicked' boolean notify that a piece has been selected
     * @param clickedSquare The square originally clicked
     */
    private void handleFirstClick(int clickedSquare){
        // Check if the clicked square contains a piece of the correct color
        if (!board.getSquare(clickedSquare).hasPiece() || board.getSquare(clickedSquare).getPiece().color != board.colorToMove() || !board.gameRunning) return;

        this.previousClickedSquarePos = clickedSquare;

        board.highlightSquare(clickedSquare, Constants.MOVE_COLOR);
        board.generatePossibleMoves(clickedSquare);

        this.hasClicked = true;
    }

    /**
     * Handles second click to move previously clicked square to new square position.
     * This method must be called after handleFirstClick() or an error will occur.
     * @param clickedSquare The square for the piece to move to.
     */
    private void handleMoveClick(int clickedSquare){
        this.hasClicked = false;

        // Check if the previously clicked square is able to move to the new square
        if (!board.validateMove(previousClickedSquarePos, clickedSquare)) {
            board.clearUI();
            return;
        }

        board.makeMove(board.getMove(previousClickedSquarePos, clickedSquare), false);
        book.updateMoves(board.zobristKey);
    }

    private int worldToArray(Vector2 worldCoords){
        worldCoords.x += mouseOffset;
        worldCoords.y = Constants.WORLD_HEIGHT - worldCoords.y;

        // Convert screen coordinates to board position by dividing the coordinates by the size of each square
        return BoardUtility.getArrayIndex((int) worldCoords.y/squareSize, (int) worldCoords.x/squareSize);
    }

}
