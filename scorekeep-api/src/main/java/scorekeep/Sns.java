package scorekeep;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sns {
  private static final Logger logger = LoggerFactory.getLogger(Sns.class);
  private static AmazonSNS snsclient = AmazonSNSClientBuilder.standard()
        .build();

  private static AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().build();


  /*
   * Send a notification email.
   */
  public static void sendNotification(String subject, String body) {
    String topicarn = getNotificationTopicArn();
    PublishRequest publishRequest = new PublishRequest(topicarn, body, subject);
    PublishResult publishResult = snsclient.publish(publishRequest);
    logger.info("Email sent: " + publishResult.getMessageId());
  }

  /*
   * Create an SNS subscription.
   */
  public static void createSubscription() {
    String topicarn = getNotificationTopicArn();
    String emailaddress = System.getenv("NOTIFICATION_EMAIL");
    SubscribeRequest subRequest = new SubscribeRequest(topicarn, "email", emailaddress);
    snsclient.subscribe(subRequest);
  }

  private static String getNotificationTopicArn() {
    String region = System.getenv("AWS_REGION");

    GetCallerIdentityRequest request = new GetCallerIdentityRequest();
    GetCallerIdentityResult response = stsClient.getCallerIdentity(request);

    String accountId = response.getAccount();

    return "arn:aws:sns:" + region + ":" + accountId + ":scorekeep-notifications";
  }
}
