package transcribe;

import java.io.Serializable;
import java.util.Objects;

public class TranscriptionResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String jobName;

    private String accountId;

    private TranscriptionResultDTO results;

    private String status;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranscriptionResponseDTO transcriptionResponseDTO = (TranscriptionResponseDTO) o;
        if (transcriptionResponseDTO.getStatus() == null) {
            return false;
        }
        return Objects.equals(getStatus(), transcriptionResponseDTO.getStatus());
    }

    public String getJobName() { return jobName; }

    public void setJobName(String jobName) { this.jobName = jobName; }

    public String getAccountId() { return accountId; }

    public void setAccountId(String accountId) { this.accountId = accountId; }

    public TranscriptionResultDTO getResults() { return results; }

    public void setResults(TranscriptionResultDTO results) { this.results = results; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    @Override
    public int hashCode() {
        return Objects.hashCode(getStatus());
    }

    @Override
    public String toString() {
        return "TranscriptionResponseDTO{" +
            "jobName=" + getJobName() +
            ", accountId='" + getAccountId() + "'" +
            ", status='" + getStatus() + "'" +
            ", results='" + getResults() + "'" +
            "}";
    }
}

