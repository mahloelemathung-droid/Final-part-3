/**
 * One message as it is written to / read from the JSON file.
 * Only stored messages are saved to JSON.
 */
public class Message {

    private final String messageID;
    private final String messageHash;
    private final String sender;
    private final String recipient;
    private final String messageText;
    private final String flag;

    public Message(String messageID, String messageHash, String sender,
                   String recipient, String messageText, String flag) {
        this.messageID = messageID;
        this.messageHash = messageHash;
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.flag = flag;
    }

    public String getMessageID() { return messageID; }
    public String getMessageHash() { return messageHash; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getFlag() { return flag; }
}
