package com.shynee.main.chess;

import com.badlogic.gdx.graphics.Color;
import com.shynee.main.utils.Transform;

public class Square {

    private Transform transform;
    private int arrayPosition;
    private Piece piece;
    private Color color;
    private boolean isPossibleMove;

    public Square(Transform transform, int arrayPosition) {
        this.transform = transform;
        this.arrayPosition = arrayPosition;
        this.piece = null;
        this.color = null;
        this.isPossibleMove = false;
    }

    public Square setPiece(Piece piece) {
        this.piece = piece;
        return this;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Transform getTransform() {
        return transform;
    }

    public Piece getPiece() {
        return piece;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPossibleMove() {
        return isPossibleMove;
    }

    public boolean hasColor(){
        return color != null;
    }

    public boolean hasPiece(){
        return piece != null;
    }

    public boolean hasKing(){
        return hasPiece() && piece.type == Piece.KING;
    }

    public void setArrayPosition(int rank, int file){
        this.arrayPosition = ChessBoard.getArrayIndex(rank, file);
    }

    public int getArrayPosition(){
        return arrayPosition;
    }

    public void setPossibleMove(boolean isPossibleMove){
        this.isPossibleMove = isPossibleMove;
    }

    public void promotePiece(int newPieceType, boolean color){
        this.setPiece(new Piece(newPieceType, color));
    }

    public boolean sharesPieceColor(Square square){
        if (!square.hasPiece() || !hasPiece()) return false;

        return square.getPiece().color == this.getPiece().color;
    }

}
