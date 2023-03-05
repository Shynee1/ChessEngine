package com.shynee.main.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.abstracts.Component;
import com.shynee.main.chess.AI.Search;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;
import java.util.Random;

public class AIController extends Component {

    private final boolean color;
    private final ChessBoard board;
    private final Search search;


    public AIController(boolean aiColor, ChessBoard board){
        this.color = aiColor;
        this.board = board;
        this.search = new Search(board);
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        if (board.colorToMove() != color || !board.gameRunning) return;

        Move bestMove = search.startSearch(5);
        if (bestMove != null) board.makeMove(bestMove, false);

    }

    private Move getRandomMove(){
        Random random = new Random();

        List<Move> legals = board.getMoveCalculator().getLegalMoves(board, board.colorToMove());
        if (legals.isEmpty()) return null;

        return legals.get(random.nextInt(legals.size()));
    }











}
