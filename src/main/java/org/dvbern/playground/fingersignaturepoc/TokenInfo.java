package org.dvbern.playground.fingersignaturepoc;
import java.time.Instant;

public class TokenInfo {
    public String token;
    public String orgId;
    public String userId;
    public Instant expiry;
    public boolean validated = false;   // default value
    public String signatureImage;
}