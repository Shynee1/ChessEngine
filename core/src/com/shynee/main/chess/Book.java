package com.shynee.main.chess;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;

/**
 * Book: Used to get a random book move at the start of the game - All book moves capped at 9 moves
 */
public class Book {

    /**
     * BookElement: Struct used to store zobrist keys and their respective moves
     */
    class BookElement{
        public long zobristKey;
        public Move move;
        public String pgn;

        public BookElement(long zobristKey, String pgn, Move move){
            this.zobristKey = zobristKey;
            this.move = move;
            this.pgn = pgn;
        }
    }

    private final List<Queue<BookElement>> zobristBook = new ArrayList<>();
    private final Stack<Move> lastMoves = new Stack<>();
    private final ChessBoard board;
    private final Gson gson;

    public Book(ChessBoard board){
        this.board = board;
        this.gson = new Gson();

        FileHandle file = Gdx.files.internal("games/book.txt");
        if (file.exists()) loadBook(file);
        // Create book is only called if book.txt gets deleted
        else createBook(file);
    }

    /**
     * Converts algebraic notation in games.txt to BookElement json.
     * Each BookElement is a representation of the board after the algebraic notation has been made.
     * The final json is stored in a new file named book.txt.
     * @param file The file used to store the final json.
     */
    private void createBook(FileHandle file){
        // Split games.txt into each individual game
        String[] book = Gdx.files.internal("games/games.txt").readString().split("\n");
        StringBuilder zobristString = new StringBuilder();

        for (String line : book){
            // Split each game into a list of moves
            String[] moves = line.split(" ");
            // Each queue represents one game - Each element is a move in that game
            Queue<BookElement> bookQueue = new LinkedList<>();

            int iterations = 0;
            for (String move : moves){
                if (iterations > 9) break;

                // Convert algebraic notation into a move
                Move boardMove = PGNUtility.pgnToMove(board, move);
                if (boardMove == null) continue;

                board.makeMove(boardMove, true);
                lastMoves.push(boardMove);

                // Store board position after move has been made
                BookElement element = new BookElement(board.zobristKey, move, boardMove);
                bookQueue.add(element);

                iterations++;
            }

            // Unmake every move
            for (int i = 0; i < iterations; i++){
                Move m = lastMoves.pop();
                board.unmakeMove(m, true);
            }

            zobristBook.add(bookQueue);
        }

        zobristString.append(gson.toJson(zobristBook));

        // Create new file and store json
        try {
            file.file().createNewFile();
            FileWriter writer = new FileWriter(file.file());
            writer.write(zobristString.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads from previously created book.txt and stores all BookElements
     * @param file Reference to book.txt file
     */
    private void loadBook(FileHandle file){
        String json = file.readString();
        BookElement[][] bookElements = gson.fromJson(json, BookElement[][].class);

        for (BookElement[] bookQueue : bookElements){
            Queue<BookElement> queue = new LinkedList<>();
            Collections.addAll(queue, bookQueue);
            zobristBook.add(queue);
        }
    }

    /**
     * Updates possible book moves after a move has been made.
     * Updated after both white and black moves.
     * @param newZobrist Zobrist key of board position after a move has been made
     */
    public void updateMoves(long newZobrist){
        zobristBook.removeIf(q -> q.peek() == null || q.poll().zobristKey != newZobrist);
    }

    /**
     * Gets a random move from possible book moves
     * @return The random move
     */
    public Move getRandomMove(){
        if (zobristBook.size() == 0) return null;

        int pos = zobristBook.size() == 1 ? 0 : (int) (Math.random()*zobristBook.size());
        BookElement element = zobristBook.get(pos).poll();
        if (element == null) return null;

        return element.move;
    }
}
