package cl.ione.simuladorapptoapp.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import cl.ione.simuladorapptoapp.R
import cl.ione.simuladorapptoapp.databinding.ComponentFooterButtonsBinding

class FooterButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ComponentFooterButtonsBinding
    private var onPrimaryClickListener: (() -> Unit)? = null
    private var onSecondaryClickListener: (() -> Unit)? = null

    init {
        binding = ComponentFooterButtonsBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        setupAttributes(attrs)
        setupListeners()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.FooterButtons
            )

            val primaryText = typedArray.getString(R.styleable.FooterButtons_primaryText)
            val secondaryText = typedArray.getString(R.styleable.FooterButtons_secondaryText)

            setPrimaryButton(primaryText)
            setSecondaryButton(secondaryText)

            typedArray.recycle()
        }
    }

    private fun setupListeners() {
        binding.btnPrimary.setOnClickListener {
            hideKeyboard()
            onPrimaryClickListener?.invoke()
        }

        binding.btnSecondary.setOnClickListener {
            hideKeyboard()
            onSecondaryClickListener?.invoke()
        }
    }

    /**
     * Cierra el teclado virtual
     */
    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun setSingleButton(
        text: String,
        isPrimary: Boolean = true,
        onClickListener: () -> Unit = {}
    ) {
        if (isPrimary) {
            setPrimaryButton(text, onClickListener)
            binding.btnSecondary.visibility = GONE
        } else {
            setSecondaryButton(text, onClickListener)
            binding.btnPrimary.visibility = GONE
        }
    }

    fun setButtons(
        primaryText: String? = null,
        secondaryText: String? = null,
        onPrimaryClick: (() -> Unit)? = null,
        onSecondaryClick: (() -> Unit)? = null
    ) {
        if (primaryText != null) {
            setPrimaryButton(primaryText, onPrimaryClick)
        } else {
            binding.btnPrimary.visibility = GONE
        }

        if (secondaryText != null) {
            setSecondaryButton(secondaryText, onSecondaryClick)
        } else {
            binding.btnSecondary.visibility = GONE
        }

        adjustLayoutForSingleButton()
    }

    private fun setPrimaryButton(text: String?, onClickListener: (() -> Unit)? = null) {
        if (text.isNullOrEmpty()) {
            binding.btnPrimary.visibility = GONE
            return
        }

        binding.btnPrimary.apply {
            visibility = VISIBLE
            this.text = text
        }

        if (onClickListener != null) {
            onPrimaryClickListener = onClickListener
        }
    }

    private fun setSecondaryButton(text: String?, onClickListener: (() -> Unit)? = null) {
        if (text.isNullOrEmpty()) {
            binding.btnSecondary.visibility = GONE
            return
        }

        binding.btnSecondary.apply {
            visibility = VISIBLE
            this.text = text
        }

        if (onClickListener != null) {
            onSecondaryClickListener = onClickListener
        }
    }

    private fun adjustLayoutForSingleButton() {
        val primaryVisible = binding.btnPrimary.visibility == VISIBLE
        val secondaryVisible = binding.btnSecondary.visibility == VISIBLE

        if (primaryVisible && !secondaryVisible) {
            binding.btnPrimary.layoutParams = (binding.btnPrimary.layoutParams as LayoutParams).apply {
                weight = 0f
                width = LayoutParams.WRAP_CONTENT
                marginEnd = 0
            }
        } else if (!primaryVisible && secondaryVisible) {
            binding.btnSecondary.layoutParams = (binding.btnSecondary.layoutParams as LayoutParams).apply {
                weight = 0f
                width = LayoutParams.WRAP_CONTENT
            }
        }
    }

    fun getPrimaryButton(): androidx.appcompat.widget.AppCompatButton = binding.btnPrimary
    fun getSecondaryButton(): Button = binding.btnSecondary

    fun setPrimaryButtonStyle(
        textColor: Int = R.color.primary_red,
        strokeColor: Int = R.color.primary_red,
        backgroundColor: Int = android.R.color.transparent
    ) {
        binding.btnPrimary.apply {
            setTextColor(context.getColor(textColor))
            setBackgroundColor(context.getColor(backgroundColor))
        }
    }

    fun setSecondaryButtonStyle(
        textColor: Int = android.R.color.white,
        backgroundColor: Int = R.color.primary_red
    ) {
        binding.btnSecondary.apply {
            setTextColor(context.getColor(textColor))
            backgroundTintList = context.getColorStateList(backgroundColor)
        }
    }
}