package board;

import pieces.Piece;
import pieces.PieceAttributes;
import pieces.Pos;


public class PositionEvaluationController{
    // white - array[row * 8 + col]
    // black - array[63 - (row * 8 + col)]

    private final static int[] knight_scores = {
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -50, -40, -20, -30, -30, -20, -40, -50,
    };

    private final static int[] bishop_scored = {
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -20, -10, -40, -10, -10, -40, -10, -20,
    };

    private final static int[] king_table_midgame = {
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            20, 20, 0, 0, 0, 0, 20, 20,
            20, 30, 10, 0, 0, 10, 30, 20
    };

    private final static int[] king_table_endgame = {
            -50, -40, -30, -20, -20, -30, -40, -50,
            -30, -20, -10, 0, 0, -10, -20, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 30, 40, 40, 30, -10, -30,
            -30, -10, 20, 30, 30, 20, -10, -30,
            -30, -30, 0, 0, 0, 0, -30, -30,
            -50, -30, -30, -30, -30, -30, -30, -50
    };

    private final static int[] pawn_table_early = {
            0, 0, 0, 0, 0, 0, 0, 0,
            -10, -10, -20, -30, -30, -20, -10, -10,
            -10, -10, -20, -30, -30, -20, -10, -10,
            -10, -5, -5, 10, 10, -5, -10, -10,
            0, 5, 5, 40, 40, 5, 5, 0,
            10, 10, 30, 30, 30, 30, 10, 10,
            15, 15, 15, 15, 15, 15, 15, 15,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private final static int[] pawn_table_midgame = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5, 5, 10, 27, 27, 10, 5, 5,
            0, 0, 0, 25, 25, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5,
            5, 10, 10, -25, -25, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    // Element at index "i" indicates how many pawns are at column "i"
    private static int[] white_pawn_cols = new int[8];
    private static int[] black_pawn_cols = new int[8];

    private static int white_pieces_count = 0;
    private static int black_pieces_count = 0;

    private static int black_knight_count = 0;
    private static int white_knight_count = 0;

    private static int white_bishop_count = 0;
    private static int black_bishop_count = 0;

    private static int white_rook_count = 0;
    private static int black_rook_count = 0;

    private static int black_pawns_on_black_squares = 0;
    private static int black_pawns_on_white_squares = 0;

    private static int white_pawns_on_black_squares = 0;
    private static int white_pawns_on_white_squares = 0;

    private static PieceAttributes.Color dominating_black_bishop = PieceAttributes.Color.NONE;
    private static PieceAttributes.Color dominating_white_bishop = PieceAttributes.Color.NONE;


    private static void clear(){
        white_pawn_cols = new int[8];
        black_pawn_cols = new int[8];

        white_pieces_count = 0;
        black_pieces_count = 0;

        black_knight_count = 0;
        white_knight_count = 0;

        white_bishop_count = 0;
        black_bishop_count = 0;

        white_rook_count = 0;
        black_rook_count = 0;

        black_pawns_on_black_squares = 0;
        black_pawns_on_white_squares = 0;

        white_pawns_on_black_squares = 0;
        white_pawns_on_white_squares = 0;

        dominating_black_bishop = PieceAttributes.Color.NONE;
        dominating_white_bishop = PieceAttributes.Color.NONE;
    }

    public static double getRating(Board board, GameLogicController.GameState state, PieceAttributes.Color colorToMove){
        if(state == GameLogicController.GameState.MATE){
            return colorToMove == PieceAttributes.Color.WHITE ? Double.MAX_VALUE : -Double.MIN_VALUE;
        }

        if(state == GameLogicController.GameState.STALEMATE){
            return 0.0;
        }

        clear();
        double score = 0.0;
        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = board.m_boardSquares[i][j].getPiece();
                if(piece == null)
                    continue;
                if(piece.getType() == PieceAttributes.Type.PAWN){
                    if(piece.getColor() == PieceAttributes.Color.WHITE){
                        ++white_pawn_cols[j];
                        // Needed for evaluating same color bishop with pawn pos
                        boolean isWhite = ((i + j) % 2) != 1;
                        if(isWhite) ++white_pawns_on_white_squares;
                        else ++white_pawns_on_black_squares;
                    }else{
                        ++black_pawn_cols[j];
                        // Needed for evaluating same color bishop with pawn pos
                        boolean isWhite = ((i + j) % 2) != 1;
                        if(isWhite) ++black_pawns_on_white_squares;
                        else ++black_pawns_on_black_squares;
                    }
                }
                // Count white pieces
                if(piece.getColor() == PieceAttributes.Color.WHITE){
                    ++white_pieces_count;
                }else{
                    ++black_pieces_count;
                }
            }
        }


        for(int i = 0; i < 8; ++i){
            for(int j = 0; j < 8; ++j){
                Piece piece = board.m_boardSquares[i][j].getPiece();
                if(piece == null)
                    continue;
                score += evaluatePiece(piece);
            }
        }

        if(black_bishop_count < 2){
            boolean isWhiteBishopDominating = dominating_black_bishop == PieceAttributes.Color.WHITE;
            if(isWhiteBishopDominating)
                score -= (black_pawns_on_white_squares - black_pawns_on_black_squares) * 12;
            else score += (black_pawns_on_white_squares - black_pawns_on_black_squares) * 12;
        }

        if(white_bishop_count < 2){
            boolean isWhiteBishopDominating = dominating_white_bishop == PieceAttributes.Color.WHITE;
            if(isWhiteBishopDominating)
                score -= (white_pawns_on_white_squares - white_pawns_on_black_squares) * 12;
            else score += (white_pawns_on_white_squares - white_pawns_on_black_squares) * 12;
        }
        return score;
    }

    public static double evaluatePiece(Piece piece){
        double score = 0.0;

        Pos pos = piece.getPos();
        int index = pos.row() * 8 + pos.col();
        if(piece.getColor() == PieceAttributes.Color.BLACK)
            index = 63 - index;

        if(piece.getType() == PieceAttributes.Type.PAWN){
            if(piece.getColor() == PieceAttributes.Color.WHITE){ // white pawns
                if(white_pawn_cols[pos.col()] > 0) // double pawns
                    score -= 20;

                if(black_pawn_cols[pos.col()] == 0) // passed pawns
                    score += 25;

                if(pos.col() - 1 > 0){
                    if(white_pawn_cols[pos.col() - 1] > 0) // connected pawns
                        score += 10;
                    else score -= 10; // isolated pawns
                }

                if(pos.col() + 1 < 8){
                    if(white_pawn_cols[pos.col() + 1] > 0) // connected pawns
                        score += 10;
                    else score -= 10; // isolated pawns
                }
            }else{ // black pawns
                if(black_pawn_cols[pos.col()] > 0) // double pawns
                    score += 20;

                if(black_pawn_cols[pos.col()] == 0) // passed pawns
                    score -= 25;

                if(pos.col() - 1 > 0){
                    if(black_pawn_cols[pos.col() - 1] > 0) // connected pawns
                        score -= 10;
                    else score += 10; // isolated pawns
                }

                if(pos.col() + 1 < 8){
                    if(black_pawn_cols[pos.col() + 1] > 0) // connected pawns
                        score -= 10;
                    else score += 10; // isolated pawns
                }
            }
            if(white_pieces_count + black_pieces_count >= 25)
                score += (pawn_table_early[index] * piece.getColor().getValue());
            else score += (pawn_table_midgame[index] * piece.getColor().getValue());

        }else if(piece.getType() == PieceAttributes.Type.KNIGHT){
            score += (knight_scores[index] * piece.getColor().getValue());
            if(piece.getColor() == PieceAttributes.Color.WHITE){
                ++white_knight_count;
                if(white_pieces_count + black_pieces_count < 12) // knights are worth less in endgame
                    score -= 12;
            }else{
                ++black_knight_count;
                if(white_pieces_count + black_pieces_count < 12) // knights are worth less in endgame
                    score += 12;
            }
        }else if(piece.getType() == PieceAttributes.Type.BISHOP){
            score += (bishop_scored[index] * piece.getColor().getValue());
            if(piece.getColor() == PieceAttributes.Color.WHITE){
                ++white_bishop_count;
                if(white_bishop_count >= 2)
                    score += 15;

                if(white_bishop_count >= 2 && white_pieces_count + black_pieces_count < 12) // bishops are worth more
                    // in endgame
                    score += 15;
            }else{
                ++black_bishop_count;
                if(black_bishop_count >= 2)
                    score -= 15;

                if(black_bishop_count >= 2 && white_pieces_count + black_pieces_count < 12) // bishops are worth more
                    // in endgame
                    score -= 15;
            }

        }else if(piece.getType() == PieceAttributes.Type.ROOK){
            if(piece.getColor() == PieceAttributes.Color.WHITE){
                ++white_rook_count;
                if(white_rook_count >= 2)
                    score += 25;
            }else{
                ++black_rook_count;
                if(black_rook_count >= 2)
                    score -= 25;
            }

        }else if(piece.getType() == PieceAttributes.Type.KING){
            if(white_pieces_count + black_pieces_count < 12)
                score += king_table_endgame[index];
            else score += king_table_midgame[index];
        }
        score += piece.getType().getValue() * piece.getColor().getValue();
        return score;
    }
}
