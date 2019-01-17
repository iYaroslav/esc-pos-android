# ESC/POS Android
ESC/POS printer library supporting fiscal memory.

This library works on Android 4.0.3 and older.

#### Features
- you can forget about using bluetooth
- bluetooth devices are marked with an icon to improve UI
- small size of the library
- support for many Chinese printers
- ticket builder
- storage fiscal data in encrypted form
- ability to automatically send reports on time (will send decrypted fiscal data)

#### Shortcomings
- on some devices print gibberish

# How to include this library
Download this library, import to your IDE (eclipse...) as a library project.
##### Using Gradle
```groovy
compile 'com.leerybit.escpos:1.0'
```
##### Using Maven
```xml
<dependency>
    <groupId>com.leerybit</groupId>
    <artifactId>escpos</artifactId>
    <version>1.0</version>
    <type>apklib</type>
</dependency>
```

# How to use this library

### Printer connection

```java
//Initialization
printer = PosPrinter.getPrinter(activity);

//Subscribe to device events
printer.setDeviceCallbacks(new DeviceCallbacks() {
	@Override
	public void onConnected() {
		// ...
	}

	@Override
	public void onFailure() {
		// ...
	}

	@Override
	public void onDisconnected() {
		// ...
	}
});

//Connect to printer
printer.connect();
```
Library in the automatic mode will request switch bluetooth, if it is not turned on, and start the search for devices. When you restart the application, the application automatically connects to the printer, if it is available.
<p align="center"><img alt="Screenshot of choose device dialog" width="300px" src="https://github.com/iYaroslav/PosPrinter/blob/master/art/choose_device.png?sanitize=true&raw=true" /></p>

### Create ticket
To create a ticket you must use a generator **TicketBuilder** class:
```java
Ticket ticket = new TicketBuilder()
	.header("PosPrinter")
	.divider()
	.text("Date: " + DateFormat.format("dd.MM.yyyy", date).toString())
	.text("Time: " + DateFormat.format("HH:mm", date).toString())
	.text("Ticket No: " + (ticketNumber++))
	.divider()
	.subHeader("Hot dishes")
	//...
	.menuLine("— 2 Coffee", "3,00")
	.right("Total: 12,50")
	.dividerDouble()
	.menuLine("Total gift", "3,00")
	.menuLine("Total", "128,30")
	.dividerDouble()
	.stared("THANK YOU")
	.build();
```
Call the function **ticket.getPreview()** you get a string that can be use to debug without using the printer.

Example response **ticket.getPreview()**:
```
    ┌───────────────────────────────────┐
    │▓▓▓▓▓▓▓▓▓▓▓ PosPrinter ▓▓▓▓▓▓▓▓▓▓▓▓│
    ├───────────────────────────────────┤
    │Date: 08.07.2015                   │
    │Time: 19:46                        │
    │Ticket No: 1                       │
    ├───────────────────────────────────┤
    │          - Hot dishes -           │
    │— 3 Kazan kabob               60,00│
    │— 2 Full-Rack Ribs            32,00│
    │                       Total: 92,00│
    │                                   │
    │            - Salads -             │
    │— 1 Turkey & Swiss             4,50│
    │— 1 Classic Cheese             3,30│
    │— 1 Chicken Caesar Salad       7,00│
    │                       Total: 14,80│
    │                                   │
    │           - Desserts -            │
    │— 1 Blondie                    5,00│
    │— 2 Chocolate Cake             7,00│
    │                       Total: 12,00│
    │                                   │
    │          - Drinkables -           │
    │   50% sale for Coke on mondays!   │
    │— 3 Coca-Cola                  6,00│
    │— 7 Tea                        3,50│
    │— 2 Coffee                     3,00│
    │                       Total: 12,50│
    ╞═══════════════════════════════════╡
    │Total gift                     3,00│
    │Total                        128,30│
    ╞═══════════════════════════════════╡
    │        *** THANK YOU ***          │
    └───────────────────────────────────┘
```

> Cyrillic characters must be supported by the printer, otherwise will be printed gibberish.

### License
```
Copyright 2015 LeeryBit

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
