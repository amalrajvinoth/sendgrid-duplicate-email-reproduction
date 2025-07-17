import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import java.util.UUID;

public class SendGridDuplicateEmailReproduction {

  private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
  private static final String SENDER_EMAIL = System.getenv("SENDER_EMAIL");
  private static final String SENDER_NAME = System.getenv("SENDER_NAME");
  private static final String SUPPORT_EMAIL = System.getenv("SUPPORT_EMAIL");
  private static final String RECIPIENT_EMAIL = System.getenv("RECIPIENT_EMAIL");

  public static void main(String[] args) {
    if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isEmpty()) {
      System.err.println("Please set SENDGRID_API_KEY environment variable");
      System.exit(1);
    }

    try {
      sendSingleEmail(RECIPIENT_EMAIL);
      System.out.println("Email sent successfully. Check if recipient received 2 emails instead of 1.");
    } catch (Exception e) {
      System.err.println("Error sending email: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void sendSingleEmail(String recipientEmail) throws Exception {
    String requestId = UUID.randomUUID().toString();
    String subject = "Test Email - Single Send " + requestId;

    System.out.println("Sending single email with request ID: " + requestId);
    System.out.println("Recipient: " + recipientEmail);
    System.out.println("Subject: " + subject);

    // Create personalization
    Personalization personalization = new Personalization();
    personalization.addCustomArg("X-TG-Request-ID", requestId);
    personalization.addHeader("Message-ID", requestId);

    // Add recipient
    Email to = new Email(recipientEmail);
    personalization.addTo(to);

    // Add BCC to support email
    personalization.addBcc(new Email(SUPPORT_EMAIL));

    // Set subject
    personalization.setSubject(subject);

    // Set sender
    Email from = new Email(SENDER_EMAIL, SENDER_NAME);
    personalization.setFrom(from);

    // Create content
    Content content = new Content("text/plain",
        "This is a test email to reproduce the SendGrid duplicate email issue.\n\n" +
            "Request ID: " + requestId + "\n" +
            "Timestamp: " + System.currentTimeMillis() + "\n\n" +
            "If you receive this email twice, it confirms the duplicate email issue."
    );

    // Create mail object
    Mail mail = new Mail(from, subject, to, content);
    mail.addHeader("X-TG-Request-ID", requestId);
    mail.addPersonalization(personalization);

    // Build request
    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    // Send via SendGrid
    SendGrid sendGrid = new SendGrid(SENDGRID_API_KEY);
    var response = sendGrid.api(request);

    System.out.println("SendGrid Response Status: " + response.getStatusCode());
    System.out.println("SendGrid Response Body: " + response.getBody());

    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
      System.out.println("Email sent successfully to SendGrid");
    } else {
      System.err.println("SendGrid API error: " + response.getStatusCode() + " - " + response.getBody());
    }
  }
} 