package com.shynee.main.chess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public Book(ChessBoard board){
        String[] book = readBook().split("\n");
        this.board = board;

        for (String line : book){
            Queue<BookElement> zobristQueue = new LinkedList<>();

            String[] moves = line.split(" ");

            int iterations = 0;
            for (String move : moves){
                if (iterations > 7) break;

                Move boardMove = PGNUtility.pgnToMove(board, move);
                if (boardMove == null){
                    System.out.println(FenUtility.savePosition(board));
                    throw new RuntimeException();
                }
                board.makeMove(boardMove, true);
                lastMoves.push(boardMove);
                zobristQueue.add(new BookElement(board.zobristKey, move, boardMove));

                iterations++;

            }

            for (int i = 0; i < iterations; i++){
                Move m = lastMoves.pop();
                board.unmakeMove(m);
            }

            zobristBook.add(zobristQueue);
        }
    }

    public void updateMoves(long newZobrist){
        zobristBook.removeIf(q -> q.peek() == null || q.peek().zobristKey != newZobrist);

        for (Queue<BookElement> qu : zobristBook){
            qu.remove();
        }
    }

    public Move getRandomMove(){
        if (zobristBook.size() == 0) return null;

        int pos = zobristBook.size() == 1 ? 0 : random.nextInt(zobristBook.size()-1);
        return zobristBook.get(pos).poll().move;
    }

    private String readBook(){
        try {
            File file = new File("games/games.txt");
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
