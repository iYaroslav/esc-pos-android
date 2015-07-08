package sam.lab.posprinter;

/**
 * Copyright 2015 Samardak Yaroslav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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

	public static final byte[] SELECT_FONT_A = {27, 33, 0};

	public static final byte[] SET_BAR_CODE_HEIGHT = {29, 104, 100};
	public static final byte[] PRINT_BAR_CODE_1 = {29, 107, 2};
	public static final byte[] SEND_NULL_BYTE = {0x00};

	public static final byte[] SELECT_PRINT_SHEET = {0x1B, 0x63, 0x30, 0x02};
	public static final byte[] FEED_PAPER_AND_CUT = {0x1D, 0x56, 66, 0x00};

	public static final byte[] SELECT_CYRILLIC_CHARACTER_CODE_TABLE = {0x1B, 0x74, 0x11};

	//	public static byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33, (byte) 0xFE, 0x03};
	public static final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 0x64, 0x63, 48, (byte) 255};

	public static final byte[] SET_LINE_SPACING_24 = {0x1B, 0x33, 24};
	public static final byte[] SET_LINE_SPACING_30 = {0x1B, 0x33, 30};

	public static final byte[] TRANSMIT_DLE_PRINTER_STATUS = {0x10, 0x04, 0x01};
	public static final byte[] TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = {0x10, 0x04, 0x02};
	public static final byte[] TRANSMIT_DLE_ERROR_STATUS = {0x10, 0x04, 0x03};
	public static final byte[] TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = {0x10, 0x04, 0x04};


	public static final byte[] FONT_1 = {0x1b, 0x21, 0x8};          // DEFAULT
	public static final byte[] FONT_1_SMALL = {0x1b, 0x21, 0x4b};   // SMALL
	public static final byte[] FONT_2 = {0x1b, 0x21, 0x52};         //
	public static final byte[] FONT_2_SMALL = {0x1b, 0x21, 0x18};   //
	public static final byte[] FONT_3 = {0x1b, 0x21, 0x2c};         // MEDIUM
	public static final byte[] FONT_3_SMALL = {0x1b, 0x21, 0x67};   // BOLD
	public static final byte[] FONT_4 = {0x1b, 0x21, 0x7e};         // HUGE
	public static final byte[] FONT_4_SMALL = {0x1b, 0x21, 0x3d};   // LARGE

}
