package com.shynee.main.chess;

public class Move{

    public int piecePos;
    public int squarePos;
    public int directionOffset;
    public boolean isCastle;
    public boolean isPromotion;

    private int sortIndex;

    public Move(int oldSquarePos, int newSquarePos, int directionOffset){
        this.piecePos = oldSquarePos;
        this.squarePos = newSquarePos;
        this.directionOffset = directionOffset;
    }

    public Move setCastle(){
        this.isCastle = true;
        return this;
    }

    public Move setPromotion() {
        this.isPromotion = true;
        return this;
    }

    public void setSortIndex(int index){
        this.sortIndex = index;
    }

    public int getSortIndex(){
        return sortIndex;
    }

}
