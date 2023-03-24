package com.shynee.main.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.shynee.main.abstracts.Component;
import com.shynee.main.chess.AI.Search;
import com.shynee.main.chess.Book;
import com.shynee.main.chess.ChessBoard;
import com.shynee.main.chess.Move;

import java.util.List;
import java.util.Random;

public class AIController extends Component {

    private final boolean color;
    private final ChessBoard board;
    private final Search search;
    private final Book book;

    public AIController(boolean aiColor, ChessBoard board, Book book){
        this.color = aiColor;
        this.board = board;
        this.search = new Search(board);
        this.book = book;
    }

    @Override
    public void start() {

    }

    @Override
    public void update(float dt, SpriteBatch batch) {
        if (board.colorToMove() != color || !board.gameRunning) return;

        Move bestMove = null;

        bestMove = book.getRandomMove();
        if (bestMove == null) {
            startSearchTimer();
            bestMove = search.startSearch(30);
        }
        if (bestMove != null) board.makeMove(bestMove, false);

        book.updateMoves(board.zobristKey);
    }

    public void startSearchTimer(){
        Thread thread = new Thread(() -> {
            boolean abort = false;
            long startTime = System.currentTimeMillis();
            while (!abort){
                long timeNow = System.currentTimeMillis();
                float elapsedSeconds = (float)((timeNow - startTime)/1000);

                if (elapsedSeconds >= 1.5f) {
                    search.abortSearch();
                    abort = true;
                }
            }
        });
        thread.start();
    }

    private Move getRandomMove(){
        Random random = new Random();

        List<Move> legals = board.getMoveCalculator().getLegalMoves(board, board.colorToMove());
        if (legals.isEmpty()) return null;

        return legals.get(random.nextInt(legals.size()));
    }
}
