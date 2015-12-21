package org.familysearch.qa.util;

import org.familysearch.sas.client.Base64;
import org.familysearch.sas.client.ObjectRequester;
import org.familysearch.sas.schema.Attribute;
import org.familysearch.sas.schema.Sas;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.util.Map;

/**
 * Created by rbirch on 10/30/2015.
 */
public class MessagingDecrypter {
  private String sasObjectName;
  private String filePath;

  public MessagingDecrypter(String sasObjectName) {
    this.sasObjectName = sasObjectName;
  }

  public byte[] decrypt(String fileName) throws UnsupportedEncodingException {
    String pathName;

    try {
      ClassLoader loader = this.getClass().getClassLoader();
      URL url = loader.getResource(fileName);
      pathName = url.getPath();
    }
    catch(Exception ex) {
      throw new IllegalArgumentException("Unable to get path for credentials file: " + fileName, ex);
    }

    this.filePath = pathName;

    FileInputStream iStream = null;
    ByteArrayOutputStream oStream = new ByteArrayOutputStream();

    try {
      iStream = new FileInputStream(pathName);

      byte[] encryptedData = Base64.decode(getStringFromInputStream(iStream));
      ByteArrayInputStream bStream = new ByteArrayInputStream(encryptedData);
      SecretKeySpec spec = new SecretKeySpec(getKeyFromSas(), "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(2, spec);

      byte[] inputBuffer = new byte[64];

      while(true) {
        int bytesRead;
        byte[] outputBuffer;
        if((bytesRead = bStream.read(inputBuffer)) == -1) {
          outputBuffer = cipher.doFinal();
          if(outputBuffer != null) {
            oStream.write(outputBuffer);
          }
          break;
        }

        outputBuffer = cipher.update(inputBuffer, 0, bytesRead);
        if(outputBuffer != null) {
          oStream.write(outputBuffer);
        }
      }
    } catch(Exception ex) {
      throw new IllegalStateException("Decryption Error", ex);
    }
    finally {
      try {
        iStream.close();
        oStream.flush();
        oStream.close();
      }
      catch(Exception ignore){}
    }

    return oStream.toByteArray();
  }

  public String getStringFromInputStream(InputStream iStream) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(iStream, "UTF-8"));

    for(String line = br.readLine(); line != null; line = br.readLine()) {
      builder.append(line);
    }

    return  builder.toString();
  }

  private byte[] getKeyFromSas() {
    ObjectRequester requester = new ObjectRequester();

    try {
      Sas sas = requester.getSecureObject(this.sasObjectName);
      Map attributes = sas.getAttributes();
      return ((Attribute)attributes.get("fchKeystoreData")).getSingleValue();
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to get encryption key from SAS.", ex);
    }
  }
}
