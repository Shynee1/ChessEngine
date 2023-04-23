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

    public static boolean isSameMove(Move m1, Move m2){
        if (m1 == null || m2 == null)
            return false;

        return m1.squarePos == m2.squarePos && m1.piecePos == m2.piecePos && m1.isCastle == m2.isCastle && m1.isPromotion == m2.isPromotion;
    }

}
