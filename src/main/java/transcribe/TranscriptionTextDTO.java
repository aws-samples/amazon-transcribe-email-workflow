package transcribe;

import java.io.Serializable;
import java.util.Objects;

public class TranscriptionTextDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String transcript;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranscriptionTextDTO transcriptionTextDTO = (TranscriptionTextDTO) o;
        if (transcriptionTextDTO.getTranscript() == null) {
            return false;
        }
        return Objects.equals(getTranscript(), transcriptionTextDTO.getTranscript());
    }

    public String getTranscript() { return transcript; }

    public void setTranscript(String transcript) { this.transcript = transcript; }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTranscript());
    }

    @Override
    public String toString() {
        return "TranscriptionTextDTO{" +
            "transcript=" + getTranscript() +
            "}";
    }
}
