package com.shynee.main.chess;

/**
 * Piece -- Represents one piece in a chess game.
 * Contains an int to determine the type of piece
 * Contains a color to determine the color of the piece
 */
public class Piece {

    public int type;
    public boolean color;
    public boolean hasMoved;

    public Piece(int type, boolean color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }

    /**
     * Copies the values of one piece into a new piece
     * @param piece Piece to copy values from
     * @return New copied piece
     */
    public static Piece copy(Piece piece){
        Piece newPiece = new Piece(piece.type, piece.color);
        newPiece.hasMoved = piece.hasMoved;
        return newPiece;
    }

    public final static int KING = 0;
    public final static int QUEEN = 1;
    public final static int BISHOP = 2;
    public final static int KNIGHT = 3;
    public final static int ROOK = 4;
    public final static int PAWN = 5;
}
