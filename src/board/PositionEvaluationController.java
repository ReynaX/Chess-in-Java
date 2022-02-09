package board;

import pieces.Piece;

import java.util.HashMap;
import java.util.Random;

public class PositionEvaluationController{
    public static void initTable(){
        // Generate random numbers
        Random rand = new Random();
        for(int i = 0; i < 64; ++i){
            for(int j = 0; j < 12; ++j){
                randomNumbers[i][j] = rand.nextLong(Long.MAX_VALUE);
            }
        }
    }

    private enum Term{
        MATERIAL, PAWN_STRUCTURE, EVALUATION_PATTERNS, MOBILITY, CENTER_CONTROL, CONNECTIVITY, TRAPPED_PIECES,
        KING_SAFETY, SPACE, TEMPO
    }

    public static double getRating(Board board){
        return getMaterialEvaluation(board);
    }

    public static long generateHashKey(Board board){
        long hashKey = 0L;

        // Iterate through board and create
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = board.m_boardSquares[i][j].getPiece();
                if(piece != null){
                    Integer value = pieceMap.get(piece.getType().getValue());
                    hashKey ^= randomNumbers[i][j];
                }
            }
        }
        System.out.printf("hash key: %d\n", hashKey);
        return hashKey;
    }

    public static double getMaterialEvaluation(Board board){
        double value = 0.0;
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = board.m_boardSquares[i][j].getPiece();
                if(piece != null){
                    value += (piece.getType().getValue() * piece.getColor().getValue());
                }
            }
        }
        return value;
    }

    public static double getPawnStructureMaterial(Board board){
        double value = 0.0;

        return value;
    }


    public static HashMap<Character, Integer> pieceMap = new HashMap<>(){{
        put('P', 0);
        put('N', 1);
        put('B', 2);
        put('R', 3);
        put('Q', 4);
        put('K', 5);
        put('p', 6);
        put('n', 7);
        put('b', 8);
        put('r', 9);
        put('q', 10);
        put('k', 11);
    }};

    public static long[][] randomNumbers = new long[64][12];
}
