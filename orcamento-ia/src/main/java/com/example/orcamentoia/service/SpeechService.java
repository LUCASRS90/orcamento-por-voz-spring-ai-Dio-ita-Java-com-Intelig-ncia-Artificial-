package com.example.orcamentoia.service;

import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.stereotype.Service;

/** Texto -> áudio (TTS). Retorna bytes MP3. */
@Service
public class SpeechService {

    private final OpenAiAudioSpeechModel model;

    public SpeechService(OpenAiAudioSpeechModel model) {
        this.model = model;
    }

    public byte[] speak(String text) {
        return model.call(text);
    }
}
