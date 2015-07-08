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
@SuppressWarnings("unused")
public class TicketBuilder {

	public enum TextAlignment {
		LEFT,
		RIGHT,
		CENTER
	}

	private final Ticket ticket;

	public TicketBuilder() {
		ticket = new Ticket();
	}

	/**
	 * Will print text aligned by right like this:
	 *  ____________________
	 * |                    |
	 * |          Right text|
	 * |                    |
	 * |Very long right stri|
	 * |       ng text value|
	 * |                    |
	 *  ^^^^^^^^^^^^^^^^^^^^
	 * @param text text value for print
	 * @param style text style
	 * @return TicketBuilder
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public TicketBuilder right(String text) {
		return text(text, TextAlignment.RIGHT);
	}

	/**
	 * Will print text aligned by center like this:
	 *  ____________________
	 * |                    |
	 * |    Center text     |
	 * |                    |
	 * |Very long center str|
	 * |   ing text value   |
	 * |                    |
	 *  ^^^^^^^^^^^^^^^^^^^^
	 * @param text text value for print
	 * @param style text style
	 * @return TicketBuilder
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public TicketBuilder center(String text) {
		return text(text, TextAlignment.CENTER);
	}

	/**
	 * Will print line with dots across the width of the paper like this:
	 *  ____________________
	 * |                    |
	 * |Key............Value|
	 * |                    |
	 *  ^^^^^^^^^^^^^^^^^^^^
	 * @param key key
	 * @param value value
	 * @return TicketBuilder
	 */
	public TicketBuilder menuLine(String key, String value) {
		return menuLine(key, value, ' ');
	}
	public TicketBuilder menuLine(String key, String value, char space) {
		ticket.menuItem(key, value, space);
		return this;
	}

	public TicketBuilder accent(String text, char accent) {
		ticket.accent(text, accent);
		return this;
	}

	public TicketBuilder divider() {
		ticket.divider('─', 1);
		return this;
	}
	public TicketBuilder dividerDouble() {
		ticket.divider('═', 2);
		return this;
	}
	public TicketBuilder divider(char symbol) {
		ticket.divider(symbol, 0);
		return this;
	}
	public TicketBuilder stared(String text) {
		ticket.text("*** " + text + " ***", TextAlignment.CENTER);
		return this;
	}
	public TicketBuilder header(String text) {
		return accent(text, '▓');
	}
	public TicketBuilder subHeader(String text) {
		ticket.text("- " + text + " -", TextAlignment.CENTER);
		return this;
	}

	public TicketBuilder text(String text) {
		return text(text, TextAlignment.LEFT);
	}
	public TicketBuilder text(String text, TextAlignment alignment) {
		ticket.text(text, alignment);
		return this;
	}

	public TicketBuilder feedLine() {
		return feedLine(1);
	}
	public TicketBuilder feedLine(int count) {
		ticket.feed(count);
		return this;
	}

	public Ticket build() {
		return ticket;
	}
}
