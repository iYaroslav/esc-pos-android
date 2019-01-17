package io.github.iyaroslav.posprinter

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.google.android.gms.analytics.HitBuilders
import com.leerybit.escpos.*

import java.io.IOException
import java.util.Date

import com.leerybit.escpos.widgets.TicketPreview
import com.leerybit.escpos.bluetooth.BTService

/**
 * Created by Yaroslav on 04.07.15.
 * Copyright 2015 LeeryBit LLC.
 */
class MainActivity : AppCompatActivity() {

  private val printer by lazy { PosPrinter80mm(this) }
  private val preview by lazy { findViewById<TicketPreview>(R.id.ticket) }
  private val messageView by lazy { findViewById<TextView>(R.id.tv_message) }
  private val stateView by lazy { findViewById<TextView>(R.id.tv_state) }

  private var ticketNumber = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    (application as App).tracker.apply {
      setScreenName("MainActivity")
      send(HitBuilders.ScreenViewBuilder().build())
    }

    val btnSearch = findViewById<Button>(R.id.btn_search)

    printer.setCharsetName("UTF-8")
    printer.setDeviceCallbacks(object : DeviceCallbacks {
      override fun onConnected() {
        btnSearch.setText(R.string.action_disconnect)
      }

      override fun onFailure() {
        Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_SHORT).show()
      }

      override fun onDisconnected() {
        btnSearch.setText(R.string.action_connect)
      }
    })

    printer.setStateChangedListener { state, msg ->
      when (state) {
        BTService.STATE_NONE -> setState("NONE", R.color.text)
        BTService.STATE_CONNECTED -> setState("CONNECTED", R.color.green)
        BTService.STATE_CONNECTING -> setState("CONNECTING", R.color.blue)
        BTService.STATE_LISTENING -> setState("LISTENING", R.color.amber)
      }

      when (msg.arg2) {
        BTService.MESSAGE_STATE_CHANGE -> setMessage("STATE CHANGED", R.color.text)
        BTService.MESSAGE_READ -> setMessage("READ (${msg.what})", R.color.green)
        BTService.MESSAGE_WRITE -> setMessage("WRITE (${msg.what})", R.color.green)
        BTService.MESSAGE_CONNECTION_LOST -> setMessage("CONNECTION LOST", R.color.red)
        BTService.MESSAGE_UNABLE_TO_CONNECT -> setMessage("UNABLE TO CONNECT", R.color.red)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    printer.onActivityResult(requestCode, resultCode, data)
  }

  fun handleButtonClick(view: View) {
    when (view.id) {
      R.id.btn_search -> if (printer.isConnected) printer.disconnect() else printer.connect()
      R.id.btn_print_raw -> printRawTicket()
      R.id.btn_print_programmatically -> printTicket()
    }
  }

  private fun printRawTicket() {
    try {
      val date = Date()
      val ticket: Ticket

      ticket = TicketBuilder(printer)
          .isCyrillic(true)
          .raw(this, R.raw.ticket,
              DateFormat.format("dd.MM.yyyy", date).toString(),
              DateFormat.format("HH:mm", date).toString(),
              (++ticketNumber).toString() + ""
          )
          .fiscalInt("ticket_no", ticketNumber)
          .fiscalDouble("gift", 3.0, 2)
          .fiscalDouble("price", 131.30, 2)
          .fiscalDouble("out_price", 128.30, 2)
          .build()

      preview.setTicket(ticket)
      printer.send(ticket)
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun printTicket() {
    try {
      val date = Date()
      val ticket: Ticket

      ticket = TicketBuilder(printer)
          .isCyrillic(true)
          .header("PosPrinter")
          .divider()
          .text("Date: ${DateFormat.format("dd.MM.yyyy", date)}")
          .text("Time: ${DateFormat.format("HH:mm", date)}")
          .text("Ticket No: ${++ticketNumber}")
          .fiscalInt("ticket_no", ticketNumber)
          .divider()
          .subHeader("Hot dishes")
          .menuLine("- 3 Kazan kabob", "60,00")
          .menuLine("- 2 Full-Rack Ribs", "32,00")
          .right("Total: 92,00")
          .feedLine()
          .subHeader("Salads")
          .menuLine("- 1 Turkey & Swiss", "4,50")
          .menuLine("- 1 Classic Cheese", "3,30")
          .menuLine("- 1 Chicken Caesar Salad", "7,00")
          .right("Total: 14,80")
          .feedLine()
          .subHeader("Desserts")
          .menuLine("- 1 Blondie", "5,00")
          .menuLine("- 2 Chocolate Cake", "7,00")
          .right("Total: 12,00")
          .feedLine()
          .subHeader("Drinkables")
          .center("50% sale for Coke on mondays!")
          .menuLine("- 3 Coca-Cola", "6,00")
          .menuLine("- 7 Tea", "3,50")
          .menuLine("- 2 Coffee", "3,00")
          .right("Total: 12,50")
          .dividerDouble()
          .menuLine("Total gift", "3,00")
          .menuLine("Total", "128,30")
          .fiscalDouble("gift", 3.0, 2)
          .fiscalDouble("price", 131.30, 2)
          .fiscalDouble("out_price", 128.30, 2)
          .dividerDouble()
          .stared("THANK YOU")
          .feedLine(4)
          .build()

      preview.setTicket(ticket)
      printer.send(ticket)
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setState(value: String, color: Int) {
    stateView.text = "State: $value"
    stateView.setTextColor(ContextCompat.getColor(this, color))
  }

  @SuppressLint("SetTextI18n")
  private fun setMessage(value: String, color: Int) {
    messageView.text = "State: $value"
    messageView.setTextColor(ContextCompat.getColor(this, color))
  }
}
