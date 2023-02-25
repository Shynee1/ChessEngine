package com.shynee.main.chess;

public class Move {

    public int piecePos;
    public int squarePos;
    public int directionOffset;
    public boolean isCastle;
    public boolean isFirstMove;

    public Move(int oldSquarePos, int newSquarePos, int directionOffset){
        this.piecePos = oldSquarePos;
        this.squarePos = newSquarePos;
        this.directionOffset = directionOffset;
    }

    public Move setCastle(boolean canCastle){
        this.isCastle = canCastle;
        return this;
    }

    public Move setFirstMove(){
        this.isFirstMove = true;
        return this;
    }
}
