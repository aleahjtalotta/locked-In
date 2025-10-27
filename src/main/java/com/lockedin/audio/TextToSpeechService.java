package com.lockedin.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Lightweight text-to-speech bridge that shells out to the host platform.
 */
public final class TextToSpeechService {
    private TextToSpeechService() {
    }

    /**
     * Queues a narration task to run asynchronously on the common pool.
     *
     * @param text raw narration text to speak aloud
     */
    public static void speakAsync(String text) {
        String sanitized = sanitize(text);
        if (sanitized.isEmpty()) {
            return;
        }
        CompletableFuture.runAsync(() -> speak(sanitized));
    }

    /**
     * Performs the platform-specific speech synthesis synchronously.
     *
     * @param text narration text that has already been sanitized
     */
    private static void speak(String text) {
        List<String> command = buildCommand(text);
        if (command.isEmpty()) {
            return;
        }
        try {
            new ProcessBuilder(command).start();
        } catch (IOException ignored) {
            // If the command is unavailable on the host system, we silently fall back to text-only output.
        }
    }

    /**
     * Builds an operating-system-specific command invocation for text-to-speech.
     *
     * @param text narration to pass to the command
     * @return command tokens ready for {@link ProcessBuilder}, or an empty list when unsupported
     */
    private static List<String> buildCommand(String text) {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> command = new ArrayList<>();
        if (os.contains("mac")) {
            command.add("say");
            command.add(text);
        } else if (os.contains("win")) {
            command.add("powershell");
            command.add("-Command");
            command.add(buildWindowsCommand(text));
        } else if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
            command.add("espeak");
            command.add(text);
        }
        return command;
    }

    /**
     * Creates a PowerShell script that speaks the supplied text on Windows.
     *
     * @param text narration text to embed in the script
     * @return PowerShell command string that triggers the synthesizer
     */
    private static String buildWindowsCommand(String text) {
        String escaped = text.replace("'", "''");
        return "Add-Type -AssemblyName System.Speech; "
                + "$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                + "$synth.Speak('" + escaped + "');";
    }

    /**
     * Normalizes whitespace and trims the supplied narration text.
     *
     * @param text raw narration input
     * @return sanitized narration or an empty string when the input is {@code null}
     */
    private static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
