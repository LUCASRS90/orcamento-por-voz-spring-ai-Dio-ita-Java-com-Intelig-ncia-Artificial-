package com.example.orcamentoia.service;

import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

/** Áudio -> texto (Whisper). */
@Service
public class TranscriptionService {

    private final OpenAiAudioTranscriptionModel model;

    public TranscriptionService(OpenAiAudioTranscriptionModel model) {
        this.model = model;
    }

    public String transcribe(byte[] audio, String filename) {
        // A API da OpenAI precisa do nome do arquivo (com extensão) para identificar o formato.
        var resource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        return model.transcribe(resource);
    }
}
