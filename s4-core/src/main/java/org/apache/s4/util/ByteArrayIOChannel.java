/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.s4.util;

import org.apache.s4.client.IOChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ByteArrayIOChannel implements IOChannel {
    private InputStream in;
    private OutputStream out;

    public ByteArrayIOChannel(Socket socket) throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    private void readBytes(byte[] s, int n) throws IOException {
        int r = 0; // bytes read so far

        do {
            // keep reading bytes till the required "n" are read
            int p = in.read(s, r, (n - r));

            if (p == -1) {
                throw new IOException("reached end of stream after reading "
                        + r + " bytes. expected " + n + " bytes");
            }

            r += p;

        } while (r < n);
    }

    public byte[] recv() throws IOException {
        // first read size of byte array.
        // unsigned int, big endian: 0A0B0C0D -> {0A, 0B, 0C, 0D}
        byte[] s = { 0, 0, 0, 0 };
        readBytes(s, 4);

        // to allow full range of int, using long for size
        int size = (int) ( // NOTE: type cast not necessary for int
        (0xff & s[0]) << 24 | (0xff & s[1]) << 16 | (0xff & s[2]) << 8 | (0xff & s[3]) << 0);

        if (size == 0)
            return null;
        
        // ignore ridiculous sizes
        // TODO: come up with a better solution than this
        if (size < 0 || size > (10*1024*1024)) {
            throw new IOException("Bizarre size "  + size);
        }

        byte[] v = new byte[size];

        // read the message
        readBytes(v, size);

        return v;
    }

    public void send(byte[] v) throws IOException {
        byte[] s = { 0, 0, 0, 0 };
        int size = v.length;

        s[3] = (byte) (size & 0xff);
        size >>= 8;
        s[2] = (byte) (size & 0xff);
        size >>= 8;
        s[1] = (byte) (size & 0xff);
        size >>= 8;
        s[0] = (byte) (size & 0xff);

        out.write(s);
        out.write(v);
        out.flush();
    }
}
