package cl.ione.simuladorapptoapp.components

import android.widget.EditText

fun EditText.setupMoneyFormat() {
    addTextChangedListener(MoneyTextWatcher(this))
}

fun EditText.getCleanMoneyValue(): Long {
    return MoneyTextWatcher.getCleanValue(text.toString())
}

fun EditText.setMoneyValue(amount: Long) {
    if (amount == 0L) {
        setText("")
    } else {
        setText(MoneyTextWatcher.formatMoney(amount))
    }
}