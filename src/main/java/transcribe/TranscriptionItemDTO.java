package transcribe;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


public class TranscriptionItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String start_time;

    private String end_time;

    private List<TranscriptionItemAlternativesDTO> alternatives;

    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranscriptionItemDTO transcriptionItemDTO = (TranscriptionItemDTO) o;
        if (transcriptionItemDTO.getType() == null) {
            return false;
        }
        return Objects.equals(getType(), transcriptionItemDTO.getType());
    }

    public List<TranscriptionItemAlternativesDTO> getAlternatives() { return alternatives; }

    public void setAlternatives(List<TranscriptionItemAlternativesDTO> alternatives) { this.alternatives = alternatives; }

    public String getEnd_time() { return end_time; }

    public void setEnd_time(String end_time) { this.end_time = end_time; }

    public String getStart_time() { return start_time; }

    public void setStart_time(String start_time) { this.start_time = start_time; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    @Override
    public int hashCode() {
        return Objects.hashCode(getType());
    }

    @Override
    public String toString() {
        return "TranscriptionItemDTO{" +
            "type=" + getType() +
            ", start_time='" + getStart_time() + "'" +
            ", end_time='" + getEnd_time() + "'" +
            ", alternatives='" + getAlternatives() + "'" +
            "}";
    }
}
