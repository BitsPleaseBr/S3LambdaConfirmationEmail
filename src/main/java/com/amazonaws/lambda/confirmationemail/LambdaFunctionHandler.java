package com.amazonaws.lambda.confirmationemail;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

public class LambdaFunctionHandler implements RequestHandler<SNSEvent, String> {

  @Override
  public String handleRequest(SNSEvent event, Context context) {
    try {
      context.getLogger().log("Received event: " + event);
      String message = event.getRecords().get(0).getSNS().getMessage();
      context.getLogger().log("From SNS: " + message);
      ConfirmationEmail.build(message).send();
      return "200";
    } catch (Exception e) {
      context.getLogger().log("Erro ao enviar email de confirmação.");
      e.printStackTrace();
      return "500";
    }
  }
}
