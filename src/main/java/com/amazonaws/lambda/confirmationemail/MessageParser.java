package com.amazonaws.lambda.confirmationemail;

import org.mindrot.jbcrypt.BCrypt;

public class MessageParser {
  private final String key = System.getenv("secretKey");
  private String email;
  private String nome;
  private String hash;
  private String token;

  public String getEmail() {
    return email;
  }

  public String getNome() {
    return nome;
  }

  public String getHash() {
    return hash;
  }
  
  public String getToken() {
    return token;
  }

  MessageParser(String message) {
    email = message.split(":")[0];
    nome = message.split(":")[1];
    hash = BCrypt.hashpw(email + key, BCrypt.gensalt());
    token = email + ":" + hash;
  }
}
