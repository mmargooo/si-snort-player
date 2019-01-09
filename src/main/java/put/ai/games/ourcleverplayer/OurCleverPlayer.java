/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.ourcleverplayer;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Comparator;

    class HeuristicMove {
        int heuristic;
        Move move;

        public HeuristicMove(int heuristic, Move move) {
            this.heuristic = heuristic;
            this.move = move;
        }
    }

    class HeuristicMoveComparator implements Comparator<HeuristicMove> {

        @Override
        public int compare(HeuristicMove o1, HeuristicMove o2) {
            return o1.heuristic-o2.heuristic;
        }
    }


    class NoMoreTimeException extends Exception {

        public NoMoreTimeException() {
            super();
            // TODO Automatycznie generowany szkielet konstruktora
        }

        public NoMoreTimeException(String message, Throwable cause,
                                   boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
            // TODO Automatycznie generowany szkielet konstruktora
        }

        public NoMoreTimeException(String message, Throwable cause) {
            super(message, cause);
            // TODO Automatycznie generowany szkielet konstruktora
        }

        public NoMoreTimeException(String message) {
            super(message);
            // TODO Automatycznie generowany szkielet konstruktora
        }

        public NoMoreTimeException(Throwable cause) {
            super(cause);
            // TODO Automatycznie generowany szkielet konstruktora
        }
    }


        public class OurCleverPlayer extends Player {

    private Random random = new Random(0xdeadbeef);
    int boardSize;
    long startTime;
    double late;


    @Override
    public String getName() {
        return "Robert Banaszak 132189 Małgorzata Brzuchalska 132195";
    }


    @Override
    public Move nextMove(Board board) {

        startTime = System.currentTimeMillis();

        long appStartTime = System.nanoTime();
        int depth;
        int heuristic = 0;
        long appTime = 0;
        List<Move> moves = board.getMovesFor(getColor());

        if (getTime() >= 200) {
            heuristic = heuristic(board, getColor());
            board.doMove(moves.get(moves.size() / 2));
            board.undoMove(moves.get(moves.size() / 2));
            board.doMove(moves.get(moves.size() - 1));
            //heuristic = heuristic(board, getColor()); do analizy
            board.undoMove(moves.get(moves.size() - 1));
            appTime = (System.nanoTime() - appStartTime) / 2;
            depth = (int) Math.floor(3.5 * Math.log((getTime() - 100) * 1000000 / appTime) / Math.log(moves.size() * (moves.size() - heuristic)));
        } else
            depth = 1;

        if (late * 4 > 1)
            depth--;
        System.out.println(depth + " " + appTime + " " + late * 4);

        boardSize = board.getSize() * board.getSize();
        int max = -boardSize - 1;
        Move best = null;

        int alfa = -boardSize;
        int beta = boardSize;
        int newHeuristic;
        PriorityQueue<HeuristicMove> movesQueue = new PriorityQueue<HeuristicMove>(moves.size(), new HeuristicMoveComparator());
        for (Move move : moves) {
            board.doMove(move);
            newHeuristic = heuristic(board, getColor());
            if (newHeuristic - heuristic != -1)
                movesQueue.add(new HeuristicMove(-newHeuristic, move));
            board.undoMove(move);
        }
        if (!movesQueue.isEmpty())
            best = movesQueue.peek().move;
        else
            best = moves.get(0);
        Move move;
        Color opColor = getOpponent(getColor());
        try {
            if (depth > 1)
                while (!movesQueue.isEmpty()) {
                    move = movesQueue.poll().move;
                    board.doMove(move);
                    alfa = alphabeta(board, depth - 1, alfa, beta, opColor);
                    if (max < alfa) {
                        max = alfa;
                        best = move;
                    }

                    board.undoMove(move);
                }
            long end_time = System.currentTimeMillis();
            double difference = (end_time - startTime);
            System.out.println(difference);
            late = 0;
        } catch (NoMoreTimeException e) {
            late = movesQueue.size() / (double) moves.size();
            System.out.println("I was too slow... I left " + movesQueue.size() + "/" + moves.size() + " nodes alone :( ");
        }
        return best;
    }

    public int alphabeta(Board board, int depth, int alfa, int beta, Color color) throws NoMoreTimeException {
        if (getTime() <= System.currentTimeMillis() - startTime + 100)
            throw new NoMoreTimeException();
        if (board.getMovesFor(color).size() == 0) {
            if (color == getColor())
                return -boardSize;
            else
                return boardSize;
        } else if (depth == 0) {
            return heuristic(board, getOpponent(color));
        } else if (color == getColor()) {
            List<Move> moves = board.getMovesFor(color);
            PriorityQueue<HeuristicMove> movesQueue = new PriorityQueue<HeuristicMove>(moves.size(), new HeuristicMoveComparator());
            for (Move move : moves) {
                board.doMove(move);
                movesQueue.add(new HeuristicMove(-heuristic(board, color), move));
                board.undoMove(move);
            }
            Move move;
            while (!movesQueue.isEmpty()) {
                move = movesQueue.poll().move;
                board.doMove(move);
                alfa = Math.max(alfa, alphabeta(board, depth - 1, alfa, beta, getOpponent(color)));
                board.undoMove(move);
                if (beta <= alfa) {
                    movesQueue.clear();
                    break;
                }
            }
            return alfa;
        } else {
            List<Move> moves = board.getMovesFor(color);
            PriorityQueue<HeuristicMove> movesQueue = new PriorityQueue<HeuristicMove>(moves.size(), new HeuristicMoveComparator());
            for (Move move : moves) {
                board.doMove(move);
                movesQueue.add(new HeuristicMove(heuristic(board, color), move));
                board.undoMove(move);
            }
            Move move;
            while (!movesQueue.isEmpty()) {
                move = movesQueue.poll().move;
                board.doMove(move);
                beta = Math.min(beta, alphabeta(board, depth - 1, alfa, beta, getOpponent(color)));
                board.undoMove(move);
                if (beta <= alfa) {
                    break;
                }
            }
            return beta;
        }
    }

    public int heuristic(Board board, Color color) {
        List<Move> myMoves = board.getMovesFor(color);
        List<Move> opMoves = board.getMovesFor(getOpponent(color));
        if (color == getColor())
            return myMoves.size() - opMoves.size();
        else
            return opMoves.size() - myMoves.size();
    }
}
