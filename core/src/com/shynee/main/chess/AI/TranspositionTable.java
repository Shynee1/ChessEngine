package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.Arrays;
import java.util.Collections;

public class TranspositionTable {

    public static final int EXACT = 0;
    public static final int LOWER = 1;
    public static final int UPPER = 2;

    private final int size;
    private final HashData[] hashTable;
    private final ChessBoard board;

    public TranspositionTable(ChessBoard board, int size){
        this.board = board;
        this.hashTable = new HashData[size];
        this.size = size;
    }

    public void storeEvaluation(int depth, int flag, int eval, Move move){
        HashData data = new HashData(board.zobristKey, depth, flag, eval, move);
        hashTable[getIndex()] = data;
    }

    public int lookupEvaluation(int depth, int alpha, int beta){
        HashData data = hashTable[getIndex()];

        if (data == null || data.depth <= depth || data.hash != board.zobristKey) return Integer.MIN_VALUE;

        if (data.flag == EXACT)
            return data.bestEval;
        if (data.flag == UPPER && data.bestEval <= alpha)
            return data.bestEval;
        if (data.flag == LOWER && data.bestEval >= beta)
            return data.bestEval;

        return Integer.MIN_VALUE;
    }

    public void clear(){
        for (int i = 0; i < size; i++){
            hashTable[i] = null;
        }
    }

    private int getIndex(){
        return (int) (board.zobristKey % size);
    }

    public Move getCurrentMove(){
        return hashTable[getIndex()].bestMove;
    }

    public int getCurrentEval(){
        return hashTable[getIndex()].bestEval;
    }

    class HashData{

        public long hash;
        public int depth;
        public int flag;
        public int bestEval;
        public Move bestMove;
        HashData(long hash, int depth, int flag, int bestEval, Move bestMove){
            this.hash = hash;
            this.depth = depth;
            this.flag = flag;
            this.bestEval = bestEval;
            this.bestMove = bestMove;
        }
    }

}
