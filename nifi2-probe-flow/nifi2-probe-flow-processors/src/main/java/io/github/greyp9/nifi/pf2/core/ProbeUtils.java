/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.greyp9.nifi.pf2.core;

import at.favre.lib.bytes.Bytes;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public final class ProbeUtils {

    /**
     * <a href="https://checkstyle.sourceforge.io/config_design.html#FinalClass">Constructor</a>
     */
    private ProbeUtils() {
    }

    public static byte[] toBytes(final Class<?> c, final String resource) throws IOException {
        final URL url = Objects.requireNonNull(c.getClassLoader().getResource(resource));
        return toBytes(Objects.requireNonNull(url.openStream()));
    }

    public static byte[] toBytes(final InputStream is) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            return toBytes(bis);
        }
    }

    public static byte[] toBytes(final BufferedInputStream is) throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) >= 0) {
            os.write(b);
        }
        return os.toByteArray();
    }

    public static String fromBytesUTF8(final byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] toBytesUTF8(final String string) {
        return (string == null) ? null : string.getBytes(StandardCharsets.UTF_8);
    }

    public static String onNull(final String string, final String sDefault) {
        return (string == null) ? sDefault : string;
    }

    public static int toInt(final String string, final int iDefault) {
        try {
            return Integer.decode(string);
        } catch (final NumberFormatException e) {
            return iDefault;
        }
    }

    public static String toStringZ(final Date date) {
        return date.toInstant().toString();
    }

    public static String sha256(final byte[] input) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
            //return Hex.encodeHexString(messageDigest.digest(input));  // commons-codec
            return Bytes.wrap(messageDigest.digest(input)).encodeHex();  // bytes-java
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T as(final Object o, final Class<T> clazz) {
        return Optional.of(o).filter(clazz::isInstance).map(clazz::cast).orElse(null);
    }

    private static final String SHA_256 = "SHA-256";
}
