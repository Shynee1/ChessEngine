package com.shynee.main.chess.AI;

import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

public class TranspositionTable {
    public class HashData{

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

    public final int EXACT = 0;
    public final int LOWER = 1;
    public final int UPPER = 2;

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
        if (data == null || data.depth < depth || data.hash != board.zobristKey) return Integer.MIN_VALUE;

        if (data.flag == UPPER && data.bestEval > alpha)
            return alpha;
        if (data.flag == LOWER && data.bestEval < beta)
            return beta;
        if (data.flag == EXACT)
            return data.bestEval;

        return Integer.MIN_VALUE;
    }

    private int getIndex(){
        return (int) (board.zobristKey % size);
    }

    public Move getCurrentMove(){
        return hashTable[getIndex()].bestMove;
    }



}
