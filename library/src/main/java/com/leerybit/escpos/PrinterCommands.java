package com.leerybit.escpos;

/**
 * Copyright 2015 Samardak Yaroslav
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
@SuppressWarnings("UnusedDeclaration")
class PrinterCommands {
  public static final byte[] INIT = {27, 64};
  public static final byte[] FEED_LINE = {10};
  public static final byte[] NULL_BYTE = {0x00};
  public static final byte[] FEED_PAPER_AND_CUT = {0x1D, 0x56, 66, 0x00};
  public static final byte[] SELECT_CYRILLIC_CHARACTER_CODE_TABLE = {0x1B, 0x74, 0x11};
  public static final byte[] SELECT_FONT = {0x1b, 0x21, 0x8};
}
