package com.leerybit.escpos;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
public class TicketBuilder {

  public enum TextAlignment {
    LEFT,
    RIGHT,
    CENTER
  }

  private final Ticket ticket;
  private int charsOnLine;

  public TicketBuilder(PosPrinter printer) {
    ticket = new Ticket(printer);
    charsOnLine = printer.charsOnLine;
  }

  public TicketBuilder isCyrillic(boolean cyrillic) {
    if (cyrillic) ticket.setCyrillic();
    return this;
  }

  public TicketBuilder raw(Context context, int resId, String... values) throws IOException {
    InputStream inputStream = context.getResources().openRawResource(resId);
    InputStreamReader inputReader = new InputStreamReader(inputStream, "UTF-8");

    BufferedReader bufferedReader = new BufferedReader(inputReader);
    String line;

    int lineNumber = -3;
    int arg = 0;
    while ((line = bufferedReader.readLine()) != null) {
      if (line.startsWith("^")) {
        if (lineNumber == -3) {
          // TODO check version!
        } else if (lineNumber == -2) {
          PrinterFonts.FontGroup group;
          try {
            group = PrinterFonts.FontGroup.valueOf(line.substring(1));
          } catch (IllegalArgumentException | NullPointerException e) {
            group = PrinterFonts.FontGroup.BASIC;
          }

          PrinterFonts.setFontGroup(group);
        } else if (lineNumber == -1) {
          // TODO set character set
        } else {
          while (line.contains("~")) {
            line = line.replace("~", values[arg]);
            arg++;
          }

          String[] items = line.substring(1).split(":", 2);
          if (items.length > 2) continue;

          boolean small = false;
          boolean bold = false;
          boolean huge = false;
          boolean wide = false;
          boolean underline = false;

          String[] keys = items[0].toUpperCase().split("&");
          for (String key : keys) {
            if ("S".equals(key) || "SMALL".equals(key)) {
              small = true;
            } else if ("B".equals(key) || "BOLD".equals(key)) {
              bold = true;
            } else if ("H".equals(key) || "HUGE".equals(key)) {
              huge = true;
            } else if ("W".equals(key) || "WIDE".equals(key)) {
              wide = true;
            } else if ("U".equals(key) || "UNDERLINE".equals(key)) {
              underline = true;
            }
          }

          int charsOnLine = this.charsOnLine;
          if (small && wide) {
            charsOnLine = PrinterFonts.smallWideCharsOnLine(charsOnLine);
          } else if (wide) {
            charsOnLine = PrinterFonts.wideCharsOnLine(charsOnLine);
          } else if (small) {
            charsOnLine = PrinterFonts.smallCharsOnLine(charsOnLine);
          }
          ticket.charsOnLine = charsOnLine;

          ticket.putBytes(PrinterFonts.font(small, bold, huge, wide, underline));

          boolean find = false;
          for (String key : keys) {
            if ("HD".equals(key) || "HEADER".equals(key)) {
              header(items[1]);
              find = true;
              break;
            }
            if ("SHD".equals(key) || "SUB_HEADER".equals(key)) {
              subHeader(items[1]);
              find = true;
              break;
            }
            if ("C".equals(key) || "CENTER".equals(key)) {
              text(items[1], TextAlignment.CENTER);
              find = true;
              break;
            }
            if ("R".equals(key) || "RIGHT".equals(key)) {
              text(items[1], TextAlignment.RIGHT);
              find = true;
              break;
            }
            if ("A".equals(key) || "ACCENT".equals(key)) {
              find = true;
              String[] ss = items[1].split("|");
              if (ss.length != 2) break;
              accent(ss[0], ss[1].charAt(0));
              break;
            }
            if ("HR".equals(key)) {
              if (items.length == 1) {
                divider();
              } else {
                divider(items[1].charAt(0));
              }
              find = true;
              break;
            }
            if ("HRD".equals(key)) {
              dividerDouble();
              find = true;
              break;
            }
            if ("BR".equals(key)) {
              int count = 1;
              if (items.length == 2) {
                count = Integer.valueOf(items[1]);
              }

              feedLine(count);
              find = true;
              break;
            }
            if ("*".equals(key) || "STARED".equals(key)) {
              stared(items[1]);
              find = true;
              break;
            }
            if ("M".equals(key) || "MENU".equals(key)) {
              String[] ss = items[1].split("\\|", 3);
              if (ss.length == 2) {
                menuLine(ss[0], ss[1]);
              } else if (ss.length == 3) {
                menuLine(ss[0], ss[1], ss[2].charAt(0));
              }
              find = true;
              break;
            }
          }

          if (!find && items.length == 2) text(items[1]);
          // ticket.putBytes("\n".getBytes());
          ticket.putBytes(PrinterFonts.resetFont());
        }

        lineNumber++;
      }
    }

    ticket.charsOnLine = charsOnLine;
    return this;
  }

  /**
   * Will print text aligned by right like this:
   * ┌────────────────────┐
   * │          Right text│
   * │                    │
   * │Very long right stri│
   * │       ng text value│
   * └────────────────────┘
   *
   * @param text text value for print
   * @return TicketBuilder
   */
  @SuppressWarnings("SpellCheckingInspection")
  public TicketBuilder right(String text) {
    return text(text, TextAlignment.RIGHT);
  }

  /**
   * Will print text aligned by center like this:
   * ┌────────────────────┐
   * │    Center text     │
   * │                    │
   * │Very long center str│
   * │   ing text value   │
   * └────────────────────┘
   *
   * @param text text value for print
   * @return TicketBuilder
   */
  @SuppressWarnings("SpellCheckingInspection")
  public TicketBuilder center(String text) {
    return text(text, TextAlignment.CENTER);
  }

  /**
   * Will print line with spaces across the width of the paper like this:
   * ┌────────────────────┐
   * │Key            Value│
   * └────────────────────┘
   *
   * @param key   key
   * @param value value
   * @return TicketBuilder
   */
  public TicketBuilder menuLine(String key, String value) {
    return menuLine(key, value, ' ');
  }

  /**
   * Will print line with @space across the width of the paper like this:
   * ┌────────────────────┐
   * │Key............Value│
   * └────────────────────┘
   *
   * @param key   key
   * @param value value
   * @param space char for fill empty space
   * @return TicketBuilder
   */
  public TicketBuilder menuLine(String key, String value, char space) {
    ticket.menuItem(key, value, space);
    return this;
  }

  /**
   * Will print divider like this:
   * ┌────────────────────┐
   * │Text                │
   * ├────────────────────┤
   * │Another text        │
   * └────────────────────┘
   *
   * @return TicketBuilder
   */
  public TicketBuilder divider() {
    ticket.divider('-');
    return this;
  }

  /**
   * Will print divider like this:
   * ┌────────────────────┐
   * │Text                │
   * ╞════════════════════╡
   * │Another text        │
   * └────────────────────┘
   *
   * @return TicketBuilder
   */
  public TicketBuilder dividerDouble() {
    ticket.divider('=');
    return this;
  }

  /**
   * Will print divider with custom divider char like this:
   * ┌────────────────────┐
   * │Text                │
   * │••••••••••••••••••••│
   * │Another text        │
   * └────────────────────┘
   *
   * @return TicketBuilder
   */
  public TicketBuilder divider(char symbol) {
    ticket.divider(symbol);
    return this;
  }

  /**
   * Will print starred text like this:
   * ┌────────────────────┐
   * │    *** Star ***    │
   * └────────────────────┘
   *
   * @param text header text value
   * @return TicketBuilder
   */
  public TicketBuilder stared(String text) {
    ticket.text("*** " + text + " ***", TextAlignment.CENTER);
    return this;
  }

  /**
   * Will print header like text with custom accent char like this:
   * ┌────────────────────┐
   * │•••••• Accent ••••••│
   * └────────────────────┘
   *
   * @param text header text value
   * @return TicketBuilder
   */
  public TicketBuilder accent(String text, char accent) {
    ticket.accent(text, accent);
    return this;
  }

  /**
   * Will print header like this:
   * ┌────────────────────┐
   * │###### HEADER ######│
   * └────────────────────┘
   *
   * @param text header text value
   * @return TicketBuilder
   */
  public TicketBuilder header(String text) {
    return accent(text.toUpperCase(), '#');
  }

  /**
   * Will print sub header like this:
   * ┌────────────────────┐
   * │   - Sub header -   │
   * └────────────────────┘
   *
   * @param text sub header text value
   * @return TicketBuilder
   */
  public TicketBuilder subHeader(String text) {
    ticket.text("- " + text + " -", TextAlignment.CENTER);
    return this;
  }

  /**
   * Will print simple text value like this:
   * ┌────────────────────┐
   * │Text                │
   * └────────────────────┘
   *
   * @param text text value
   * @return TicketBuilder
   */
  public TicketBuilder text(String text) {
    return text(text, TextAlignment.LEFT);
  }

  /**
   * Will print simple text value like this:
   * ┌────────────────────┐
   * │Text                │
   * └────────────────────┘
   *
   * @param text      text value
   * @param alignment text alignment
   * @return TicketBuilder
   */
  public TicketBuilder text(String text, TextAlignment alignment) {
    ticket.text(text, alignment);
    return this;
  }

  /**
   * Feed line
   *
   * @return TicketBuilder
   */
  public TicketBuilder feedLine() {
    return feedLine(1);
  }

  /**
   * Feed some lines
   *
   * @return TicketBuilder
   */
  public TicketBuilder feedLine(int count) {
    ticket.feed(count);
    return this;
  }

  /**
   * Put a fiscal data
   *
   * @param key   key
   * @param value value
   * @return TicketBuilder
   */
  public TicketBuilder fiscal(String key, String value) {
    ticket.putFiscalData(key, value);
    return this;
  }

  public TicketBuilder fiscalInt(String key, int value) {
    return fiscal(key, String.valueOf(value));
  }

  public TicketBuilder fiscalDouble(String key, double value) {
    return fiscalDouble(key, value, 32);
  }

  public TicketBuilder fiscalDouble(String key, double value, int size) {
    // TODO format string!
    return fiscal(key, String.valueOf(value));
  }

  /**
   * Build a ticket
   *
   * @return Ticket
   */
  public Ticket build() {
    return ticket;
  }
}
