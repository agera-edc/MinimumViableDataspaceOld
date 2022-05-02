# MVD - RegistrationService

_This document serves database for ideas, discussions, technical details and specifications regarding the RegistrationService. It will eventually flow into several stand-alone final documents._

## Necessary features
1. Onboarding
2. Offboarding
3. Banning/Blacklisting
4. Updating participants
5. "Phonebook" of the dataspace: this central participant registry maps connector URL to IDs to DID keys etc. Necessary operations may include various permutations of `getRecordFor[Url|DidKey|ParticipantAgentId]`: looks up a participant record for the given search criterion. A `ParticipantRecord` could roughly look like this:
  ```java
    public class ParticipantRecord {
        private final participantUrl; // = connector URL
        private final participantAgentId; // = connector ID
        private final didKey;
        private final fcnAddress; 
    }
  ```
   
## Data Models

#### Identifying properties for a participant agent:
- participant ID (e.g. root web did key), could be equal to connector ID if participant == participant agent, e.g. when a company has just one connector.
- <a name="connectorId">connector ID</a> (=participant agent ID): human-readable name identifying the connector.
- <a name="connectorUrl">connector URL</a> (no ports!): root URL of the connector to which the various context paths (e.g. IDS) are appended.
- `did:web` key
- Company Description, free text. Optional.
- Tax-ID number. Optional.
- (GAIA-X) Self Description (embedded, JSON)
- <a name="fcnAddress">Federated Cache Node address (for FCC): fully qualified URL+port, no context path, e.g. `https://connector.spacelasertec.com:8181`

