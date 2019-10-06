/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mulinlab.varnote.utils.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mxli
 */
public final class LimitInputStream extends FilterInputStream {

  private long left;
  private long mark = -1;

  /**
   * Wraps another input stream, limiting the number of bytes which can be read.
   *
   * @param in the input stream to be wrapped
   * @param limit the maximum number of bytes to be read
   */
  public LimitInputStream(InputStream in, long limit) {
    super(in);
   // Preconditions.checkNotNull(in);
  //  Preconditions.checkArgument(limit >= 0, "limit must be non-negative");
    left = limit;
  }

  @Override public int available() throws IOException {
    return (int) Math.min(in.available(), left);
  }

  @Override public synchronized void mark(int readlimit) {
    in.mark(readlimit);
    mark = left;
    // it's okay to mark even if mark isn't supported, as reset won't work
  }

  @Override public int read() throws IOException {
    if (left == 0) {
      return -1;
    }

    int result = in.read();
    if (result != -1) {
      --left;
    }
    return result;
  }

  @Override public int read(byte[] b, int off, int len) throws IOException {
    if (left == 0) {
      return -1;
    }

    len = (int) Math.min(len, left);
    int result = in.read(b, off, len);
    if (result != -1) {
      left -= result;
    }
    return result;
  }

  @Override public synchronized void reset() throws IOException {
    if (!in.markSupported()) {
      throw new IOException("Mark not supported");
    }
    if (mark == -1) {
      throw new IOException("Mark not set");
    }

    in.reset();
    left = mark;
  }

  @Override public long skip(long n) throws IOException {
    n = Math.min(n, left);
    long skipped = in.skip(n);
    left -= skipped;
    return skipped;
  }
}
