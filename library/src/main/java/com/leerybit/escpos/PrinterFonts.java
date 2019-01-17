package com.leerybit.escpos;

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
public class PrinterFonts {

  private static final byte SMALL = 0x1;
  private static final byte BOLD = 0x8;
  private static final byte HUGE = 0x10;
  private static final byte WIDE = 0x20;
  private static final byte UNDERLINE = (byte) 0x80;

  public enum FontGroup {
    BASIC, SECOND;

    public byte toByte() {
      if (this == BASIC) {
        return 0x0;
      } else {
        return 0x40;
      }
    }
  }

  private static FontGroup fontGroup;
  private static byte shift;

  public static void setShift(int shift) {
    shift = Math.max(0, Math.min(4, shift));

    switch (shift) {
      case 1:
        PrinterFonts.shift = 0x2;
      case 2:
        PrinterFonts.shift = 0x4;
      case 3:
        PrinterFonts.shift = 0x6;
      default:
        PrinterFonts.shift = 0x0;
    }
  }

  public static void setFontGroup(FontGroup fontGroup) {
    PrinterFonts.fontGroup = fontGroup;
  }

  public static int smallCharsOnLine(int normalCharsOnLine) {
    return normalCharsOnLine / 2 * 3;
  }

  public static int wideCharsOnLine(int normalCharsOnLine) {
    return normalCharsOnLine / 2;
  }

  public static int smallWideCharsOnLine(int normalCharsOnLine) {
    return normalCharsOnLine / 4 * 3;
  }

  private static byte[] doShift(byte[] cmd) {
    cmd[2] = (byte) (cmd[2] + fontGroup.toByte() + shift);
    return cmd;
  }

  public static byte[] resetFont() {
    return font(false, false, false, false, false);
  }

  public static byte[] font() {
    return font(false, false, false, false, false);
  }

  public static byte[] font(boolean small) {
    return font(small, false, false, false, false);
  }

  public static byte[] font(boolean small, boolean bold) {
    return font(small, bold, false, false, false);
  }

  public static byte[] font(boolean small, boolean bold, boolean huge) {
    return font(small, bold, huge, false, false);
  }

  public static byte[] font(boolean small, boolean bold, boolean huge, boolean wide) {
    return font(small, bold, huge, wide, false);
  }

  public static byte[] font(boolean small, boolean bold, boolean huge, boolean wide, boolean underline) {
    byte[] cmd = new byte[3];
    cmd[0] = 0x1b;
    cmd[1] = 0x21;
    cmd[2] = 0x0;

    byte font = 0x0;
    if (small) font += SMALL;
    if (bold) font += BOLD;
    if (huge) font += HUGE;
    if (wide) font += WIDE;
    if (underline) font += UNDERLINE;
    cmd[2] = font;

    return doShift(cmd);
  }

}
