package sam.lab.posprinter;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class Ticket {

	private List<Byte> data;
	private HashMap<String, String> fiscalData;
	private StringBuilder debugStrBuilder;

	final int smallCharsOnLine;
	final int mediumCharsOnLine;
	final int largeCharsOnLine;
	final int hugeCharsOnLine;
	final boolean debug;
	final String charsetName;

	Ticket() {
		data = new ArrayList<>();
		fiscalData = new HashMap<>();

		PosPrinter printer = PosPrinter.getPrinter(null);
		smallCharsOnLine = printer.smallCharsOnLine;
		mediumCharsOnLine = printer.mediumCharsOnLine;
		largeCharsOnLine = printer.largeCharsOnLine;
		hugeCharsOnLine = printer.hugeCharsOnLine;
		debug = printer.debug;
		charsetName = printer.charsetName;

		if (debug) {
			debugStrBuilder = new StringBuilder();
			debugStrBuilder.append(debugHR('─', '┌', '┐'));
		}
	}

	private String decode(String text) {
		try {
			return new String(text.getBytes(charsetName));
		} catch (UnsupportedEncodingException e) {
			return text;
		}
	}
	void text(String text, TicketBuilder.TextAlignment alignment) {
		if (debug) debugStrBuilder.append(debugText(text, ' ', alignment));
		text = decode(text);
//		data.add();
	}

	void menuItem(String key, String value, char space) {
		if (debug) debugStrBuilder.append(debugMenu(key, value, space));

		key = decode(key);
		value = decode(value);

//		data.add();
	}

	void divider(char symbol, int borders) {
		if (debug) {
			final char l;
			final char r;
			switch (borders) {
				case 1:
					l = '├';
					r = '┤';
					break;
				case 2:
					l = '╞';
					r = '╡';
					break;
				default:
					l = '│';
					r = '│';
					break;
			}

			debugStrBuilder.append(debugHR(symbol, l, r));
		}

//		data.add();
	}

	void accent(String text, char accent) {
		if (text.length() - 4 > smallCharsOnLine) {
			accent = ' ';
		}

		if (debug) debugStrBuilder.append(debugText(' ' + text + ' ', accent, TicketBuilder.TextAlignment.CENTER));
	}

	void feed(int count) {
		for (int i = 0; i < count; i++) {
			if (debug) debugStrBuilder.append(debugHR(' ', '│'));

		}
	}

	void saveFiscalData() {

	}

	List<Byte> getData() {
		return data;
	}

	private String debugHR(char symbol, char lr) {
		return debugHR(symbol, lr, lr);
	}
	private String debugHR(char symbol, char l, char r) {
		return l + StringUtils.repeat(symbol, smallCharsOnLine) + r + "\n";
	}
	private String debugMenu(String key, String value, char space) {
		if (key.length() + value.length() + 2 > smallCharsOnLine) {
			return debugText(key + ": " + value, ' ', TicketBuilder.TextAlignment.LEFT);
		}

		return '│' + StringUtils.rightPad(key, smallCharsOnLine - value.length(), space) + value + "│\n";
	}
	private String debugText(String text, char fill, TicketBuilder.TextAlignment alignment) {
		if (text.length() > smallCharsOnLine) {
			String out = "";
			int len = text.length();
			for (int i = 0; i <= len / smallCharsOnLine; i++) {
				String str = text.substring(i * smallCharsOnLine, Math.min((i + 1) * smallCharsOnLine, len));
				if (!str.trim().isEmpty()) out += debugText(str, fill, alignment);
			}

			return out;
		}

		switch (alignment) {
			case RIGHT:
				return  "│" + StringUtils.leftPad(text, smallCharsOnLine, fill) + "│\n";
			case CENTER:
				return  "│" + StringUtils.center(text, smallCharsOnLine, fill) + "│\n";
			default:
				return  "│" + StringUtils.rightPad(text, smallCharsOnLine, fill) + "│\n";
		}
	}

	@Override
	public String toString() {
		if (!debug) return super.toString();
//		TODO add fiscal data!
		return debugStrBuilder.toString() + debugHR('─', '└', '┘');
	}
}
