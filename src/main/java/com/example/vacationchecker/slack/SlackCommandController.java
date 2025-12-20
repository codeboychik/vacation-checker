package com.example.vacationchecker.slack;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack")
public class SlackCommandController {

    private final SlackCommandService commandService;

    public SlackCommandController(SlackCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping(path = "/command", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<SlackResponse> handleCommand(@RequestParam("text") String text,
                                                       @RequestParam(value = "command", required = false) String command) {
        SlackResponse response = commandService.handleVacationCommand(text);
        return ResponseEntity.ok(response);
    }
}
