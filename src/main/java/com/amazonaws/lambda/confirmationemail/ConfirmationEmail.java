package com.amazonaws.lambda.confirmationemail;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

public class ConfirmationEmail {
  private final String fromAdress = System.getenv("fromAdress");
  private final String emailUser = System.getenv("emailUser");
  private final String emailPassword = System.getenv("emailPassword");
  private final String smtpHost = System.getenv("smtpHost");
  private final Integer smtpPort = Integer.valueOf(System.getenv("smtpPort"));
  private final String subject = System.getenv("emailSubject");
  private final String baseURL = System.getenv("confirmationURL");
  private final String URLPattern = baseURL + "?token=%s";
  private MessageParser parser;
  private HtmlEmail HtmlEmail;

  public void send() {
    // Envia o e-mail
    try {
      System.out.println("Enviando o email");
      HtmlEmail.send();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static ConfirmationEmail build(String message) {
    System.out.println("mensagem: " + message);
    ConfirmationEmail email = new ConfirmationEmail();
    email.parser = new MessageParser(message);
    email.build();
    return email;
  }

  private void build() {
    HtmlEmail = new HtmlEmail();
    HtmlEmail.setHostName(smtpHost);
    HtmlEmail.setSmtpPort(smtpPort);
    HtmlEmail.setSSLOnConnect(true);
    HtmlEmail.setAuthenticator(new DefaultAuthenticator(emailUser, emailPassword));
    try {
      HtmlEmail.addTo(parser.getEmail());
      HtmlEmail.setFrom(fromAdress);
    } catch (EmailException e) {
      e.printStackTrace();
    }
    HtmlEmail.setSubject(subject);

    try {
      // Configura o html do email
      File template = getTemplateFromS3();
      Document doc = Jsoup.parse(template, "UTF-8", "");

      // Gera link de confirmação
      String confirmationUrl = String.format(URLPattern, parser.getToken());

      // Edita o template
      doc.select("name").first().text(parser.getNome());
      doc.select("a").first().attr("href", confirmationUrl);

      HtmlEmail.setHtmlMsg(doc.toString());
      System.out.println("Email configurado com sucesso");
    } catch (Exception e) {
      System.out.println("Erro ao configurar email");
      e.printStackTrace();
    }
  }

  private File getTemplateFromS3() {
    
    S3Object templateFile = AmazonS3ClientBuilder.standard().withRegion(Regions.SA_EAST_1).build().getObject(System.getenv("templateBucket"), System.getenv("templateFile"));
    File template = null;
    
    try {
      
      template = File.createTempFile("template", "html");
      template.setWritable(true);
      FileUtils.copyInputStreamToFile(templateFile.getObjectContent(), template);
    } catch (IOException e) {
      
      System.out.println("Não foi possível obter o template pelo S3");
      e.printStackTrace();
    }
    
    return template;
  }
  
  private ConfirmationEmail() {}
}
