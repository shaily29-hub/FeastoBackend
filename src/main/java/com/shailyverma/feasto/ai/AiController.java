package com.shailyverma.feasto.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public AiResponse chat(
            @RequestBody AiRequest request
    ) {

        String reply =
                aiService.getAiReply(request);

        return new AiResponse(reply);
    }
}
