package chess.spring.controller;


import chess.spring.controller.dto.response.ChessGameDtoNew;
import chess.spring.controller.dto.response.GameStatusDto;
import chess.spring.service.ChessGameService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ChessGameController {

    private final ChessGameService chessGameService;

    public ChessGameController(ChessGameService chessGameService) {
        this.chessGameService = chessGameService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ChessGameDtoNew> allChessGames = chessGameService.getAllGames();
        model.addAttribute("allChessGames", allChessGames);
        return "index";
    }

    @PostMapping("/games")
    public String createChessGame(@RequestParam String roomTitle) {
        Long createdChessGameId = chessGameService.createNewChessGame(roomTitle);
        return "redirect:/games/" + createdChessGameId;
    }

    @GetMapping("/games/{gameId}")
    public String chessBoard(@PathVariable Long gameId, Model model) {
        GameStatusDto gameStatusDto = chessGameService.getGameStatus(gameId);
        model.addAttribute("gameStatusDto", gameStatusDto);
        return "chess-board";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long gameId) {
        chessGameService.deleteGame(gameId);
        return "redirect:/";
    }
}
