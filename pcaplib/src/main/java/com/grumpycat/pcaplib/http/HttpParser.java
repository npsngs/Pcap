package com.grumpycat.pcaplib.http;

import android.text.TextUtils;
import android.util.Log;

import com.grumpycat.pcaplib.protocol.HttpHeader;
import com.grumpycat.pcaplib.util.CommonMethods;
import com.grumpycat.pcaplib.util.Const;

import java.util.Locale;

/**
 * Created by cc.he on 2018/11/14
 */
public class HttpParser {
    public static HttpHeader parseHttpRequestHeader(byte[] buffer, int offset, int count) {
        try {
            switch (buffer[offset]) {
                //GET
                case 'G':
                    //HEAD
                case 'H':
                    //POST, PUT
                case 'P':
                    //DELETE
                case 'D':
                    //OPTIONS
                case 'O':
                    //TRACE
                case 'T':
                    //CONNECT
                case 'C':
                    return parseHttpHeader(buffer, offset, count);
                //SSL
                case 0x16:
                    return parseHttpsHeader(buffer, offset, count);
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpHeader parseHttpHeader(byte[] buffer, int offset, int count) {
        HttpHeader httpHeader = new HttpHeader();
        httpHeader.isHttps = false;
        String headerString = new String(buffer, offset, count);
        String[] headerLines = headerString.split("\\r\\n");
        String host = getHttpHost(headerLines);
        if (!TextUtils.isEmpty(host)) {
            httpHeader.host = host;
        }
        parseRequestLine(httpHeader, headerLines[0]);
        return httpHeader;
    }

    public static String getRemoteHost(byte[] buffer, int offset, int count) {
        String headerString = new String(buffer, offset, count);
        String[] headerLines = headerString.split("\\r\\n");
        return getHttpHost(headerLines);
    }

    public static String getHttpHost(String[] headerLines) {
        for (int i = 1; i < headerLines.length; i++) {
            String[] nameValueStrings = headerLines[i].split(":");
            if (nameValueStrings.length == 2 || nameValueStrings.length == 3) {
                String name = nameValueStrings[0].toLowerCase(Locale.ENGLISH).trim();
                String value = nameValueStrings[1].trim();
                if ("host".equals(name)) {
                    return value;
                }
            }
        }
        return null;
    }

    private static void parseRequestLine(HttpHeader httpHeader, String requestLine) {
        String[] parts = requestLine.trim().split(" ");
        if (parts.length == 3) {
            httpHeader.method = parts[0];
            String path = parts[1];
            httpHeader.path = path;
            if (path.startsWith("/")) {
                if (httpHeader.host != null) {
                    httpHeader.url = "http://" + httpHeader.host + path;
                }
            } else {
                if (httpHeader.url != null && httpHeader.url.startsWith("http")) {
                    httpHeader.url = path;
                } else {
                    httpHeader.url = "http://" + path;
                }
            }
        }
    }

    private static HttpHeader parseHttpsHeader(byte[] buffer, int offset, int count) {
        int limit = offset + count;
        //TLS Client Hello
        if (count > 43 && buffer[offset] == 0x16) {
            //Skip 43 byte header
            offset += 43;

            //read sessionID
            if (offset + 1 > limit) {
                return null;
            }
            int sessionIDLength = buffer[offset++] & 0xFF;
            offset += sessionIDLength;

            //read cipher suites
            if (offset + 2 > limit) {
                return null;
            }

            int cipherSuitesLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;
            offset += cipherSuitesLength;

            //read Compression method.
            if (offset + 1 > limit) {
                return null;
            }
            int compressionMethodLength = buffer[offset++] & 0xFF;
            offset += compressionMethodLength;
            if (offset == limit) {
                Log.w("HttpParser","TLS Client Hello packet doesn't contains SNI info.(offset == limit)");
                return null;
            }

            //read Extensions
            if (offset + 2 > limit) {
                return null;
            }
            int extensionsLength = CommonMethods.readShort(buffer, offset) & 0xFFFF;
            offset += 2;

            if (offset + extensionsLength > limit) {
                Log.w("HttpParser","TLS Client Hello packet is incomplete.");
                return null;
            }

            while (offset + 4 <= limit) {
                int type0 = buffer[offset++] & 0xFF;
                int type1 = buffer[offset++] & 0xFF;
                int length = CommonMethods.readShort(buffer, offset) & 0xFFFF;
                offset += 2;
                //have SNI
                if (type0 == 0x00 && type1 == 0x00 && length > 5) {
                    offset += 5;
                    length -= 5;
                    if (offset + length > limit) {
                        return null;
                    }
                    String serverName = new String(buffer, offset, length);
                    Log.i("HttpParser", String.format(Const.LOCALE, "HttpParser","SNI: %s\n",
                            serverName));

                    HttpHeader httpHeader = new HttpHeader();
                    httpHeader.isHttps = true;
                    httpHeader.host = serverName;
                    return httpHeader;
                } else {
                    offset += length;
                }
            }
            Log.e("HttpParser","TLS Client Hello packet doesn't contains Host field info.");
            return null;
        } else {
            Log.e("HttpParser","Bad TLS Client Hello packet.");
            return null;
        }
    }


}
