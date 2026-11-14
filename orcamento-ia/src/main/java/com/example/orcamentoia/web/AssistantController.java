package com.example.orcamentoia.web;

import com.example.orcamentoia.service.AssistantService;
import com.example.orcamentoia.service.SpeechService;
import com.example.orcamentoia.service.TranscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AssistantController {

    public record ChatRequest(@NotBlank String message) {}
    public record AssistantResponse(String transcription, String answer) {}

    private final AssistantService assistant;
    private final TranscriptionService transcription;
    private final SpeechService speech;

    public AssistantController(AssistantService assistant, TranscriptionService transcription, SpeechService speech) {
        this.assistant = assistant;
        this.transcription = transcription;
        this.speech = speech;
    }

    /** Fluxo de texto (bom para testar sem áudio). */
    @PostMapping("/chat")
    public AssistantResponse chat(@Valid @RequestBody ChatRequest request) {
        return new AssistantResponse(request.message(), assistant.ask(request.message()));
    }

    /** Fluxo principal: áudio -> texto -> IA -> função real -> resposta em texto. */
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistantResponse voice(@RequestParam("audio") MultipartFile audio) throws IOException {
        String text = transcription.transcribe(audio.getBytes(), filenameOf(audio));
        return new AssistantResponse(text, assistant.ask(text));
    }

    /** Mesmo fluxo, mas a resposta volta falada (MP3). */
    @PostMapping(value = "/voice/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mpeg")
    public ResponseEntity<byte[]> voiceAudio(@RequestParam("audio") MultipartFile audio) throws IOException {
        String text = transcription.transcribe(audio.getBytes(), filenameOf(audio));
        byte[] mp3 = speech.speak(assistant.ask(text));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/mpeg")).body(mp3);
    }

    private String filenameOf(MultipartFile f) {
        String n = f.getOriginalFilename();
        return (n == null || n.isBlank()) ? "audio.mp3" : n;
    }
}
