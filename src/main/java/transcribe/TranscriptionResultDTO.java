package transcribe;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class TranscriptionResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<TranscriptionTextDTO> transcripts;

    private List<TranscriptionItemDTO> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranscriptionResultDTO transcriptionResultDTO = (TranscriptionResultDTO) o;
        if (transcriptionResultDTO.getTranscripts() == null) {
            return false;
        }
        return Objects.equals(getTranscripts(), transcriptionResultDTO.getTranscripts());
    }

    public List<TranscriptionTextDTO> getTranscripts() { return transcripts; }

    public void setTranscripts(List<TranscriptionTextDTO> transcripts) { this.transcripts = transcripts; }

    public List<TranscriptionItemDTO> getItems() { return items; }

    public void setItems(List<TranscriptionItemDTO> items) { this.items = items; }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTranscripts());
    }

    @Override
    public String toString() {
        return "TranscriptionResultDTO{" +
            "transcripts=" + getTranscripts() +
            ", items='" + getItems() + "'" +
            "}";
    }
}