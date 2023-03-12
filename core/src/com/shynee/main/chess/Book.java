package com.shynee.main.chess;

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

        File file = new File("games/book.txt");
        if (file.exists()) loadBook(file);
        else createBook(file);
    }

    private void createBook(File file){
        String[] book = readBook(new File("games/games.txt")).split("\n");
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
                board.unmakeMove(m);
            }

            zobristBook.add(bookQueue);
        }

        zobristString.append(gson.toJson(zobristBook));

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(zobristString.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadBook(File file){
        String json = readBook(file);
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

    public Move getRandomMove(){
        if (zobristBook.size() == 0) return null;

        int pos = zobristBook.size() == 1 ? 0 : random.nextInt(zobristBook.size()-1);
        return zobristBook.get(pos).poll().move;
    }

    private String readBook(File file){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder string = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null){
                string.append(line);
                string.append("\n");
            }

            return string.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
