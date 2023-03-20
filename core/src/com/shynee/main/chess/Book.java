package com.shynee.main.chess;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;

public class Book {

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
    private final Random random = new Random();
    private final ChessBoard board;
    private final Gson gson;

    public Book(ChessBoard board){
        this.board = board;
        this.gson = new Gson();

        FileHandle file = Gdx.files.internal("games/book.txt");
        if (file.exists()) loadBook(file);
        else createBook(file);
    }

    private void createBook(FileHandle file){
        String[] book = Gdx.files.internal("games/games.txt").readString().split("\n");
        StringBuilder zobristString = new StringBuilder();

        for (String line : book){
            String[] moves = line.split(" ");
            Queue<BookElement> bookQueue = new LinkedList<>();

            int iterations = 0;
            for (String move : moves){
                if (iterations > 9) break;

                Move boardMove = PGNUtility.pgnToMove(board, move);
                if (boardMove == null) continue;
                board.makeMove(boardMove, true);
                lastMoves.push(boardMove);

                BookElement element = new BookElement(board.zobristKey, move, boardMove);
                bookQueue.add(element);

                iterations++;

            }

            for (int i = 0; i < iterations; i++){
                Move m = lastMoves.pop();
                board.unmakeMove(m, true);
            }

            zobristBook.add(bookQueue);
        }

        zobristString.append(gson.toJson(zobristBook));

        try {
            file.file().createNewFile();
            FileWriter writer = new FileWriter(file.file());
            writer.write(zobristString.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadBook(FileHandle file){
        String json = file.readString();
        BookElement[][] bookElements = gson.fromJson(json, BookElement[][].class);

        for (BookElement[] bookQueue : bookElements){
            Queue<BookElement> queue = new LinkedList<>();
            Collections.addAll(queue, bookQueue);
            zobristBook.add(queue);
        }
    }

    public void updateMoves(long newZobrist){
        zobristBook.removeIf(q -> q.peek() == null || q.poll().zobristKey != newZobrist);
    }

    public Move getRandomMove(boolean color){
        if (zobristBook.size() == 0) return null;

        int pos = zobristBook.size() == 1 ? 0 : random.nextInt(zobristBook.size()-1);
        BookElement element = zobristBook.get(pos).poll();
        if (element == null) return null;

        Move move = element.move;

        if (color){
            move.piecePos = 63 - move.piecePos;
            move.squarePos = 63 - move.squarePos;
        }


        return move;
    }
}
