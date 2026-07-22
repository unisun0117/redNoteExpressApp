package cn.qdm.tob.client.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmailRecipientDTO implements Serializable {
    @JsonProperty("Email")
    private String email;

    @JsonProperty("DisplayName")
    private String displayName;

    public EmailRecipientDTO(String email) {
        this.email = email;
    }

    public EmailRecipientDTO(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EmailRecipientDTO anther){
            return this.email.equals(anther.getEmail());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.email.hashCode();
    }
}
