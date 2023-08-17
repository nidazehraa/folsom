package com.spotify.folsom.client.ascii;

import static com.spotify.folsom.MemcacheStatus.UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.US_ASCII;

import com.spotify.folsom.MemcacheStatus;
import com.spotify.folsom.guava.HostAndPort;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AsciiAuthenticateRequest extends SetRequest {

  private static final byte[] KEY = "ASCII_AUTH".getBytes(StandardCharsets.US_ASCII);

  public AsciiAuthenticateRequest(final String username, final String password) {
    super(Operation.SET, KEY, (username + " " + password).getBytes(US_ASCII), 0, 0, 0);
  }

  @Override
  public void handle(final AsciiResponse response, final HostAndPort server) throws IOException {
    switch (response.type) {
      case STORED:
        succeed(MemcacheStatus.OK);
        return;
      case CLIENT_ERROR:
        succeed(UNAUTHORIZED);
        return;
      case VALUE_TOO_LARGE:
      case OUT_OF_MEMORY:
        fail(new AsciiResponseException(response.type.name()), server);
        return;
      default:
        final IOException exception =
            new IOException(
                String.format(
                    "Invalid response %s, expected STORED or CLIENT_ERROR.", response.type));
        fail(exception, server);
    }
  }
}
