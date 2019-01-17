package com.leerybit.escpos;

import android.text.format.DateFormat;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Copyright 2015 LeeryBit
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Ticket {

  private List<Byte> data;
  private HashMap<String, String> fiscalData;
  private StringBuilder debugStrBuilder;
  private PosPrinter printer;

  int charsOnLine;
  final String charsetName;

  Ticket(PosPrinter printer) {
    this.printer = printer;

    data = new ArrayList<>();
    fiscalData = new HashMap<>();

    charsOnLine = printer.charsOnLine;
    charsetName = printer.charsetName;

    debugStrBuilder = new StringBuilder();
  }

  void putBytes(byte[] bytes) {
    for (byte b : bytes) {
      data.add(b);
    }
  }

  void setCyrillic() {
    putBytes(PrinterCommands.SELECT_CYRILLIC_CHARACTER_CODE_TABLE);
  }

  private String decode(String text) {
    try {
      return new String(text.getBytes(charsetName));
    } catch (UnsupportedEncodingException e) {
      return text;
    }
  }

  void text(String text, TicketBuilder.TextAlignment alignment) {
    debugStrBuilder.append(fixText(text, ' ', alignment, printer.charsOnLine));
    text = decode(text);
    putBytes(fixText(decode(text), ' ', alignment, charsOnLine).getBytes());
  }

  void menuItem(String key, String value, char space) {
    debugStrBuilder.append(fixMenu(key, value, space, printer.charsOnLine));
    putBytes(fixMenu(decode(key), decode(value), space, charsOnLine).getBytes());
  }

  void divider(char symbol) {
    debugStrBuilder.append(fixHR(symbol, printer.charsOnLine));
    putBytes(fixHR(symbol, charsOnLine).getBytes());
  }

  void accent(String text, char accent) {
    if (text.length() - 4 > charsOnLine) {
      accent = ' ';
    }

    debugStrBuilder.append(fixText(' ' + text + ' ', accent, TicketBuilder.TextAlignment.CENTER, printer.charsOnLine));
    putBytes(fixText(' ' + decode(text) + ' ', accent, TicketBuilder.TextAlignment.CENTER, charsOnLine).getBytes());
  }

  void feed(int count) {
    for (int i = 0; i < count; i++) {
      debugStrBuilder.append(fixHR(' ', printer.charsOnLine));
      putBytes("\n".getBytes());
    }
  }

  void saveFiscalData() {

  }

  void putFiscalData(String key, String value) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("Key is empty");
    }
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException("Value is empty");
    }
    if (key.equals("created_at")) {
      throw new IllegalArgumentException("Key \"created_at\" is predefined");
    }
    fiscalData.put(key, value);
  }

  List<Byte> getData() {
    return data;
  }

  private String fixHR(char symbol, int charsOnLine) {
    return StringUtils.repeat(symbol, charsOnLine) + "\n";
  }

  private String fixMenu(String key, String value, char space, int charsOnLine) {
    if (key.length() + value.length() + 2 > charsOnLine) {
      return fixText(key + ": " + value, ' ', TicketBuilder.TextAlignment.LEFT, charsOnLine);
    }

    return StringUtils.rightPad(key, charsOnLine - value.length(), space) + value + "\n";
  }

  private String fixText(String text, char fill, TicketBuilder.TextAlignment alignment, int charsOnLine) {
    if (text.length() > charsOnLine) {
      StringBuilder out = new StringBuilder();
      int len = text.length();
      for (int i = 0; i <= len / charsOnLine; i++) {
        String str = text.substring(i * charsOnLine, Math.min((i + 1) * charsOnLine, len));
        if (!str.trim().isEmpty()) out.append(fixText(str, fill, alignment, charsOnLine));
      }

      return out.toString();
    }

    switch (alignment) {
      case RIGHT:
        return StringUtils.leftPad(text, charsOnLine, fill) + "\n";
      case CENTER:
        return StringUtils.center(text, charsOnLine, fill) + "\n";
      default:
        return StringUtils.rightPad(text, charsOnLine, fill) + "\n";
    }
  }

  public String getTicketPreview() {
    return debugStrBuilder.toString();
  }

  private String getTimeStamp() {
    return DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()).toString();
  }

  public String getFiscalPreview() {
    if (fiscalData.size() == 0) {
      return "FISCAL DATA IS EMPTY";
    }

    StringBuilder builder = new StringBuilder();
    Set<String> keys = fiscalData.keySet();
    for (String key : keys) {
      builder.append(key).append(": ").append(fiscalData.get(key)).append("\n");
    }

    builder.append("created_at: ").append(getTimeStamp());
    return builder.toString();
  }

  @Override
  public String toString() {
    return "Ticket:\n" + getTicketPreview() + "Fiscal data:\n" + getFiscalPreview();
  }
}
